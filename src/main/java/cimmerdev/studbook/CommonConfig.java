package cimmerdev.studbook;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.*;

@Mod.EventBusSubscriber(modid = Studbook.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CommonConfig
{
    private static Set<String> entityTypeList;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Tracking TRACKING = new Tracking(BUILDER);

    public static class Tracking {
        public static ForgeConfigSpec.BooleanValue trackCustomNamedOnly;
        public static ForgeConfigSpec.BooleanValue whitelistEntities;
        public static ForgeConfigSpec.ConfigValue<ArrayList<String>> whitelistOfEntities;
        public static ForgeConfigSpec.ConfigValue<ArrayList<String>> blacklistOfEntities;

        Tracking(final ForgeConfigSpec.Builder builder) {
            ArrayList<String> DEFAULT_WHITELIST_ENTITIES = new ArrayList<>(Arrays.asList(
                    "minecraft:cat",
                    "minecraft:ocelot",
                    "minecraft:wolf",
                    "minecraft:parrot",
                    "minecraft:horse",
                    "minecraft:donkey",
                    "minecraft:mule",
                    "minecraft:llama",
                    "minecraft:panda",
                    "minecraft:rabbit",
                    "minecraft:axolotl",
                    "minecraft:goat",
                    "quark:shiba",
                    "quark:foxhound",
                    "dragnlivestock:o_horse",
                    "dragnlivestock:o_donkey",
                    "dragnlivestock:o_mule",
                    "dragnlivestock:o_cow",
                    "dragnlivestock:o_mooshroom",
                    "dragnlivestock:wheat_moobloom",
                    "dragnlivestock:melon_moobloom",
                    "dragnlivestock:pumpkin_moobloom",
                    "dragnlivestock:sweet_berry_moobloom",
                    "dragnlivestock:glow_berry_moobloom",
                    "dragnlivestock:flowering_moobloom",
                    "dragnlivestock:beetroot_moobloom",
                    "dragnlivestock:carrot_moobloom",
                    "dragnlivestock:azalea_moobloom",
                    "dragnlivestock:o_pig",
                    "dragnlivestock:o_sheep",
                    "dragnlivestock:o_goat",
                    "dragnlivestock:farm_goat",
                    "dragnlivestock:o_chicken",
                    "dragnlivestock:caribou",
                    "dragnlivestocK:o_camel",
                    "dragnlivestock:o_llama",
                    "dragnlivestock:unicorn",
                    "dragnpets:o_cat",
                    "dragnpets:o_dog",
                    "dragnpets:o_wolf",
                    "dragnpets:o_ocelot",
                    "dragnpets:o_fox",
                    "dragnpets:o_axolotl",
                    "dragnpets:macaw",
                    "dragnpets:cockatiel",
                    "dragnpets:ringneck",
                    "horse_colors:horse_felinoid",
                    "horse_colors:donkey",
                    "horse_colors:mule",
                    "eanimod:enhanced_cow",
                    "eanimod:enhanced_sheep",
                    "eanimod:enhanced_rabbit",
                    "eanimod:enhanced_chicken",
                    "eanimod:enhanced_pig",
                    "eanimod:enhanced_turtle",
                    "eanimod:enhanced_axolotl",
                    "eanimod:enhanced_mooshroom",
                    "eanimod:enhanced_llama"));

            ArrayList<String> DEFAULT_BLACKLIST_ENTITIES = new ArrayList<>(Arrays.asList(
                    "minecraft:villager",
                    "minecraft:wandering_trader"
                    ));

            builder.comment(" Rules for lineage tracking")
                    .push("TRACKING");
            trackCustomNamedOnly = builder
                    .comment(" If enabled, entities will be added to the lineage database only if at least one of the parents have custom name (named using Name Tag).")
                    .define("trackCustomNamedOnly", true);
            whitelistEntities = builder
                    .comment(" If enabled, only entity types listed in whitelist can be tracked in lineage database. If disabled mod will refer to blacklist instead.")
                    .define("whitelistEntities", false);
            whitelistOfEntities = builder
                    .comment(" List of entity types this mod can track if whitelistEntities is enabled. Entity types must be listed in namespace:id format, for example: \"minecraft:wolf\", \"anothermod:modded_animal\"...")
                    .define("whitelistOfEntities", DEFAULT_WHITELIST_ENTITIES);
            blacklistOfEntities = builder
                    .comment(" List of entity types this mod can track if whitelistEntities is *disabled*. Entity types must be listed in namespace:id format, for example: \"minecraft:wolf\", \"anothermod:modded_animal\"...")
                    .define("blacklistOfEntities", DEFAULT_BLACKLIST_ENTITIES);
            builder.pop();
        }
    }
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean shouldTrack(AgeableMob mobA, AgeableMob mobB) {
        if (Tracking.whitelistEntities.get())
        {
            if (entityTypeList == null) {
                entityTypeList = new HashSet<>(Tracking.whitelistOfEntities.get());
            }
            if (entityTypeList.contains(EntityType.getKey(mobA.getType()).toString()) &&  entityTypeList.contains(EntityType.getKey(mobB.getType()).toString())) {
                return passCustomNameRule(mobA, mobB);
            }
            return false;
        }
        else {
            if (entityTypeList == null) {
                entityTypeList = new HashSet<>(Tracking.blacklistOfEntities.get());
            }
            if (!entityTypeList.contains(EntityType.getKey(mobA.getType()).toString()) &&  !entityTypeList.contains(EntityType.getKey(mobB.getType()).toString())) {
                return passCustomNameRule(mobA, mobB);
            }
            return false;
        }
    }

    public static boolean shouldTrack(AgeableMob mob) {
        if (Tracking.whitelistEntities.get())
        {
            if (entityTypeList == null) {
                entityTypeList = new HashSet<>(Tracking.whitelistOfEntities.get());
            }
            return entityTypeList.contains(EntityType.getKey(mob.getType()).toString());
        }
        return true;
    }

    private static boolean passCustomNameRule(AgeableMob mobA, AgeableMob mobB)
    {
        if (Tracking.trackCustomNamedOnly.get()) {
            return (mobA.hasCustomName() && mobA.getCustomName() != null) || (mobB.hasCustomName() && mobB.getCustomName() != null);
        }
        else return true;
    }

    @SubscribeEvent
    public static void onConfigReload(final ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() == SPEC) {
            entityTypeList = null; // ensures list will be reloaded
        }
    }
}
