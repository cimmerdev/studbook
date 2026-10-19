package cimmerdev.studbook.data;

import cimmerdev.studbook.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public enum EntityStatus {
    ALIVE {
        @Override
        public Component getStatusText(Component deathCause, long deathTime, long currentTime) {
            return Component.translatable("studbook.gui.sidepanel.status.alive").withStyle(ChatFormatting.DARK_GREEN);
        }

        @Override
        public Component getStatusIcon() {
            return Component.literal("●").withStyle(ChatFormatting.DARK_GREEN);
        }
    },
    UNKNOWN {
        @Override
        public Component getStatusText(Component deathCause, long deathTime, long currentTime) {
            return Component.translatable("studbook.gui.sidepanel.status.unknown").withStyle(ChatFormatting.DARK_GRAY);
        }

        @Override
        public Component getStatusIcon() {
            return Component.literal("●").withStyle(ChatFormatting.DARK_GRAY);
        }
    },
    DECEASED {
        @Override
        public Component getStatusText(Component deathCause, long deathTime, long currentTime) {
            Component tooltip = Component.empty()
                    .append(deathCause == null ? Component.empty() : deathCause)
                    .append(ClientConfig.getDeathTimeFormat(deathTime, currentTime))
                    .withStyle(ChatFormatting.DARK_RED);
            return Component.translatable("studbook.gui.sidepanel.status.deceased")
                    .withStyle(style -> style
                            .applyFormat(ChatFormatting.DARK_RED)
                            .withUnderlined(true)
                            .withHoverEvent(new HoverEvent(
                                    HoverEvent.Action.SHOW_TEXT,
                                    tooltip
                            )));
        }

        @Override
        public Component getStatusIcon() {
            return Component.literal("●").withStyle(ChatFormatting.DARK_RED);
        }
    };

    public static EntityStatus getStatus(LineageData data) {
        if (!data.getIsAlive()) return EntityStatus.DECEASED;
        if (data.getIsLoaded()) return EntityStatus.ALIVE;
        else return EntityStatus.UNKNOWN;
    }
    public abstract Component getStatusText(Component deathCause, long deathTime, long currentTime);
    public abstract Component getStatusIcon();
}