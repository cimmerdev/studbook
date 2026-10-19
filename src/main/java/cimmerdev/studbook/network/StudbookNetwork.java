package cimmerdev.studbook.network;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.api.EntityInfoTag;
import cimmerdev.studbook.gui.LineageScreen;
import cimmerdev.studbook.data.EntityTypeInfoListener;
import cimmerdev.studbook.data.NodeWidgetData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.*;
import java.util.function.Supplier;

public class StudbookNetwork
{
    private static final String PROTOCOL_VERSION = "1";

    static SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath (Studbook.MODID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static class ClientboundOpenLineagePacket {

        private final List<NodeWidgetData> data;
        private final UUID targetEntity;

        public ClientboundOpenLineagePacket(List<NodeWidgetData> data, UUID targetEntity) {
            this.data = data;
            this.targetEntity = targetEntity;
        }

        public static void encode(ClientboundOpenLineagePacket msg, FriendlyByteBuf buf) {
            buf.writeCollection(msg.data, (buffer, data) -> data.toBytes(buffer));
            buf.writeUUID(msg.targetEntity);
        }

        public static ClientboundOpenLineagePacket decode(FriendlyByteBuf buf) {
            List<NodeWidgetData> receivedData = buf.readCollection(
                    ArrayList::new,
                    NodeWidgetData::fromBytes
            );
            UUID recievedUUID = buf.readUUID();

            return new ClientboundOpenLineagePacket(receivedData, recievedUUID);
        }

        public static void handle(ClientboundOpenLineagePacket msg, Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {

                Minecraft.getInstance().setScreen(new LineageScreen(msg.data, msg.targetEntity));

            });
            ctx.get().setPacketHandled(true);
        }
    }

    public static void register() {
        CHANNEL.messageBuilder(ClientboundOpenLineagePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientboundOpenLineagePacket::encode)
                .decoder(ClientboundOpenLineagePacket::decode)
                .consumerNetworkThread(ClientboundOpenLineagePacket::handle)
                .add();
    }

    public static void OpenLineageGUI(List<NodeWidgetData> nodesData, Player player, UUID targetEntity) {
        CHANNEL.sendTo(
                new ClientboundOpenLineagePacket(nodesData, targetEntity),
                ((ServerPlayer) player).connection.connection,
                NetworkDirection.PLAY_TO_CLIENT);
    }
}


