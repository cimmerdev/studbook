package cimmerdev.studbook.events;

import cimmerdev.studbook.CommonConfig;
import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.data.LineageManager;
import cimmerdev.studbook.data.LineageRecord;
import cimmerdev.studbook.network.StudbookNetwork;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Set;

@Mod.EventBusSubscriber(modid = Studbook.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InteractionEventHandler {
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getTarget() instanceof AgeableMob targetMob)) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        //Use paper as interact item
        ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (heldItem.is(Items.PAPER)) {
            tryOpenLineageGUI(event, serverLevel, targetMob, player);
        }
        else if(heldItem.is(Items.NAME_TAG)&&heldItem.hasCustomHoverName()) {
            // delay to next tick when name is actually changed
            MinecraftServer server = event.getLevel().getServer();
            if (server != null) {
                server.tell(new TickTask(server.getTickCount() + 1, () -> {
                    if (targetMob.isAlive() && targetMob.hasCustomName()) {
                        String updatedName = Objects.requireNonNull(targetMob.getCustomName()).getString();
                        LineageManager manager = LineageManager.get(serverLevel);
                        manager.updateEntityName(targetMob.getUUID(), updatedName);
                    }
                }));
            }
        }
    }

    private static void tryOpenLineageGUI(PlayerInteractEvent.EntityInteract event, ServerLevel serverLevel, AgeableMob targetMob, Player player) {
        LineageManager manager = LineageManager.get(serverLevel);
        LineageRecord node = manager.getRecord(targetMob.getUUID());
        if (node != null) {
            manager.updateEntityCache(targetMob.getUUID(), targetMob);
            Set<LineageRecord> tree = manager.getAllAncestorsAndDescendants(node);
            StudbookNetwork.OpenLineageGUI(manager.getNodesFromRecords(tree), player, targetMob.getUUID());
            event.setCanceled(true);
        }
        else {
            if(CommonConfig.shouldTrack(targetMob)) player.sendSystemMessage(Component.translatable("studbook.chat.info.no_lineage").withStyle(ChatFormatting.RED));
        }
    }
}
