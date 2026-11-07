package cimmerdev.studbook.compat.jade;

import cimmerdev.studbook.Studbook;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.EntityAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;

public enum LineageClientProvider implements IEntityComponentProvider {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        if (accessor.getEntity() instanceof AgeableMob) {
            var serverData = accessor.getServerData();

            // Read data
            if (serverData.contains("ParentA")) {
                String str = serverData.getString("ParentA");
                tooltip.add(Component.translatable("studbook.compat.jade.tooltip.parenta", str));
            }

            if (serverData.contains("ParentB")) {
                String str = serverData.getString("ParentB");
                tooltip.add(Component.translatable("studbook.compat.jade.tooltip.parentb", str));
            }

            if (serverData.contains("Sire")) {
                String str = serverData.getString("Sire");
                tooltip.add(Component.translatable("studbook.compat.jade.tooltip.sire", str));
            }

            if (serverData.contains("Dam")) {
                String str = serverData.getString("Dam");
                tooltip.add(Component.translatable("studbook.compat.jade.tooltip.dam", str));
            }
        }
    }

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(Studbook.MODID, "tooltips");
    }
}
