package cimmerdev.studbook.util;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class DeathMessages {

    public static Component getCleanDeathCause(DamageSource source) {
        String baseId = source.getMsgId();
        Entity attacker = source.getEntity();

        if (attacker instanceof LivingEntity livingAttacker) {
            ItemStack weapon = livingAttacker.getMainHandItem();
            if (!weapon.isEmpty() && weapon.hasCustomHoverName()) {
                // key format "death.attack.mob.item"
                return Component.translatable(
                        "death.attack." + baseId + ".item",
                        Component.empty(),
                        attacker.getDisplayName(),
                        weapon.getDisplayName()
                );
            }
        }
        if (attacker != null) {
            // key format "death.attack.mob.player"
            String key = "base".equals(baseId) || "player".equals(baseId) ? "death.attack." + baseId : "death.attack.mob";
            return Component.translatable(
                    key,
                    Component.empty(),
                    attacker.getDisplayName()
            );
        }
        return Component.translatable("death.attack." + baseId, Component.empty());
    }
}

