package cimmerdev.studbook.events;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.data.LineageManager;
import cimmerdev.studbook.util.DeathMessages;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.BabyEntitySpawnEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Studbook.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SpawnDespawnEventHandler {

    @SubscribeEvent
    public static void onAnimalBorn(BabyEntitySpawnEvent event) {
        // just in case
        if (!(event.getParentA() instanceof AgeableMob) || !(event.getParentB() instanceof AgeableMob) || event.getChild() == null) {
            return;
        }
        AgeableMob child = event.getChild();
        Level level = child.level();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            AgeableMob parentA = (AgeableMob) event.getParentA();
            AgeableMob parentB = (AgeableMob) event.getParentB();
            LineageManager manager = LineageManager.get(serverLevel);
            UUID playerId = (event.getCausedByPlayer() == null) ? null : event.getCausedByPlayer().getUUID();
            // RHG mod use pregnancy mechanics and don't use UUIDs generated during construction so registerBirthInstant fails
            if (EntityType.getKey(parentA.getType()).getNamespace().equals("horse_colors")) {
                manager.registerPregnancy(parentA, parentB, playerId);
            } else manager.registerBirthInstant(child, parentA, parentB, playerId, System.currentTimeMillis(), serverLevel.getGameTime());
        }
    }

    @SubscribeEvent
    public static void onEntityLoaded(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            Entity entity = event.getEntity();
            if (entity instanceof AgeableMob ageablemob) {
                if (event.loadedFromDisk()) {
                    LineageManager manager = LineageManager.get(serverLevel);
                    if (manager.isEntityTracked(ageablemob.getUUID())) {
                        // update cache
                        manager.updateEntityCache(ageablemob.getUUID(), ageablemob);
                        manager.setEntityLoaded(ageablemob.getUUID(), true);
                    }
                }
                else {
                    if (ageablemob.isBaby()) {
                        // end pregnancy
                        LineageManager manager = LineageManager.get(serverLevel);
                        manager.registerBirthPregnancyEnd(ageablemob, System.currentTimeMillis(), serverLevel.getGameTime());
                        // delay removing pregnancy record to the next tick to handle multiple children being born from same pair.
                        MinecraftServer server = event.getLevel().getServer();
                        if (server != null) {
                            server.tell(new TickTask(server.getTickCount() + 1, () -> {
                                ;
                                manager.removePregnancyRecordsPendingRemoval();
                            }));
                        }
                    }
                }
            }
        }
    }
    @SubscribeEvent
    public static void onEntityUnloaded(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getLevel() instanceof ServerLevel serverLevel) {
            Entity entity = event.getEntity();
            if (entity instanceof AgeableMob ageablemob && !(entity.getRemovalReason() == Entity.RemovalReason.KILLED) && !(entity.getRemovalReason() == Entity.RemovalReason.DISCARDED)) {
                LineageManager manager = LineageManager.get(serverLevel);
                if (manager.isEntityTracked(ageablemob.getUUID())) {
                    manager.updateEntityCache(ageablemob.getUUID(), ageablemob);
                    manager.setEntityLoaded(ageablemob.getUUID(), false);
                }
            }
        }
    }
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().level() instanceof ServerLevel serverLevel) {
            Entity entity = event.getEntity();
            if (entity instanceof AgeableMob ageablemob) {
                LineageManager manager = LineageManager.get(serverLevel);
                if (manager.isEntityTracked(ageablemob.getUUID())) {
                    //manager.updateEntityCache(ageablemob.getUUID(), ageablemob);  commented out cause corpse state is scary
                    manager.setEntityLoaded(ageablemob.getUUID(), false);
                    manager.markEntityDeceased(ageablemob.getUUID(), DeathMessages.getCleanDeathCause(event.getSource()), System.currentTimeMillis(), serverLevel.getGameTime());
                }
            }
        }
    }
}
