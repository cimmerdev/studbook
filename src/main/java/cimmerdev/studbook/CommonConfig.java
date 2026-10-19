package cimmerdev.studbook;

import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.*;

public class CommonConfig
{
    private static Set<String> whitelist;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final Tracking TRACKING = new Tracking(BUILDER);

    public static class Tracking {
        public static ForgeConfigSpec.BooleanValue trackCustomNamedOnly;
        public static ForgeConfigSpec.BooleanValue whitelistEntities;
        public static ForgeConfigSpec.ConfigValue<ArrayList<String>> whitelistOfEntities;

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

            builder.comment(" Rules for lineage tracking")
                    .push("TRACKING");
            trackCustomNamedOnly = builder
                    .comment(" If enabled, entities will be added to the lineage database only if at least one of the parents have custom name (named using Name Tag).")
                    .define("trackCustomNamedOnly", true);
            whitelistEntities = builder
                    .comment(" If enabled, only entity types listed in whitelist can be tracked in lineage database. If disabled mod will be able to track any entity inherited from AgeableMob")
                    .define("whitelistEntities", true);
            whitelistOfEntities = builder
                    .comment(" List of entity types this mod can track if whitelistEntities is enabled. Entity types must be listed in namespace:id format, for example: \"minecraft:wolf\", \"anothermod:modded_animal\"...")
                    .define("whitelistOfEntities", DEFAULT_WHITELIST_ENTITIES);
            builder.pop();
        }
    }
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean shouldTrack(AgeableMob mobA, AgeableMob mobB) {
        if (Tracking.whitelistEntities.get())
        {
            if (whitelist == null) {
                whitelist = new HashSet<>(Tracking.whitelistOfEntities.get());
            }
            if (whitelist.contains(EntityType.getKey(mobA.getType()).toString()) &&  whitelist.contains(EntityType.getKey(mobB.getType()).toString())) {
                return passCustomNameRule(mobA, mobB);
            }
            return false;
        }
        return passCustomNameRule(mobA, mobB);
    }

    public static boolean shouldTrack(AgeableMob mob) {
        if (Tracking.whitelistEntities.get())
        {
            if (whitelist == null) {
                whitelist = new HashSet<>(Tracking.whitelistOfEntities.get());
            }
            return whitelist.contains(EntityType.getKey(mob.getType()).toString());
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
}
