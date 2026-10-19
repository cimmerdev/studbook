package cimmerdev.studbook;

import net.minecraft.network.chat.Component;
import net.minecraftforge.common.ForgeConfigSpec;


public class ClientConfig {
    public enum ShowDateSettings  {
        NONE {
            @Override
            public Component getBirthTimeFormat(long birthTimeWorld, long gameTime, String baseKey) {
                return Component.empty();
            }
        },
        DAYS_SINCE {
            @Override
            public Component getBirthTimeFormat(long birthTimeWorld, long gameTime, String baseKey) {
                return Component.translatable(baseKey + ".days_since", (int) birthTimeWorld/24000L);
            }
        },
        DAYS_AGO {
            @Override
            public Component getBirthTimeFormat(long birthTimeWorld, long gameTime,String baseKey) {
                return Component.translatable(baseKey + ".days_ago", (int) (gameTime - birthTimeWorld)/24000L);
            }
        };
        public abstract Component getBirthTimeFormat(long birthTimeWorld, long gameTime, String baseKey);
    }

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ClientConfig.Gui GUI = new ClientConfig.Gui(BUILDER);

    public static class Gui {
        public static ForgeConfigSpec.EnumValue<ShowDateSettings> birthTimeDisplaySetting;
        public static ForgeConfigSpec.EnumValue<ShowDateSettings> deathTimeDisplaySetting;


        Gui(final ForgeConfigSpec.Builder builder) {
            builder.comment(" Display settings")
                    .push("GUI");
            birthTimeDisplaySetting = builder
                    .comment(" Display setting for birth time which is shown in a details panel on lineage screen.")
                    .comment(" NONE - do not display entity's birth time,")
                    .comment(" DAYS_AGO - show entity's birth time in Minecraft days relative to the current day, as 'born X days ago'.")
                    .comment(" DAYS_SINCE - show entity's birth time in Minecraft days relative to the this world creation time, as 'born at day X'.")
                    .defineEnum("birthTimeDisplaySetting", ShowDateSettings.DAYS_AGO);
            deathTimeDisplaySetting = builder
                    .comment(" Display setting for death time which is shown in tooltip over entity's status when it is tracked as Deceased.")
                    .comment(" NONE - do not display entity's death time,")
                    .comment(" DAYS_AGO - show entity's death time in Minecraft days relative to the current day, as 'X days ago'.")
                    .comment(" DAYS_SINCE - show entity's death time in Minecraft days relative to the this world creation time, as 'at day X'.")
                    .defineEnum("deathTimeDisplaySetting", ShowDateSettings.DAYS_AGO);
            builder.pop();
        }
    }
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static Component getBirthTimeFormat(long birthTimeWorld, long gameTime) {
        return Gui.birthTimeDisplaySetting.get().getBirthTimeFormat(birthTimeWorld, gameTime, "studbook.gui.sidepanel.birth");
    }

    public static boolean shouldDisplayBirthTime() {
        return Gui.birthTimeDisplaySetting.get() != ShowDateSettings.NONE;
    }

    public static Component getDeathTimeFormat(long deathTimeWorld, long gameTime) {
        return deathTimeWorld >= 0
                ? Gui.deathTimeDisplaySetting.get().getBirthTimeFormat(deathTimeWorld, gameTime, "studbook.gui.sidepanel.death")
                : Component.empty();
    }
}
