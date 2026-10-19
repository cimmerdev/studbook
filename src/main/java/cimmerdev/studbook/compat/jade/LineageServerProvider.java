package cimmerdev.studbook.compat.jade;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.data.EntityTypeInfoListener;
import cimmerdev.studbook.data.Gender;
import cimmerdev.studbook.data.LineageManager;
import cimmerdev.studbook.data.LineageRecord;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

import java.util.UUID;

public enum LineageServerProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    @Override
    public ResourceLocation getUid() {
        return ResourceLocation.fromNamespaceAndPath(Studbook.MODID, "tooltips");
    }

    @Override
    public int getDefaultPriority() {
        return IServerDataProvider.super.getDefaultPriority();

    }

    @Override
    public void appendServerData(CompoundTag compoundTag, EntityAccessor entityAccessor) {
        if (entityAccessor.getEntity() instanceof AgeableMob mob) {
            LineageManager manager = LineageManager.get((ServerLevel)entityAccessor.getLevel());
            LineageRecord targetEntityRecord = manager.getRecord(mob.getUUID());
            if (targetEntityRecord != null) {
                    if (manager.isEntityTracked(targetEntityRecord.data.getParentA()))
                    {
                        if ((EntityTypeInfoListener.getGenderTagInfo(manager.getRecord(targetEntityRecord.data.getParentA()).data.getEntityType()) == null)) {
                            compoundTag.putString("ParentA", manager.getRecord(targetEntityRecord.data.getParentA()).data.getName());
                        } else {
                            compoundTag.putString("Dam", manager.getRecord(targetEntityRecord.data.getParentA()).data.getName());
                        }
                        }
                    if (manager.isEntityTracked(targetEntityRecord.data.getParentB()))
                    {
                        if ((EntityTypeInfoListener.getGenderTagInfo(manager.getRecord(targetEntityRecord.data.getParentB()).data.getEntityType()) == null)) {
                            compoundTag.putString("ParentB", manager.getRecord(targetEntityRecord.data.getParentB()).data.getName());
                        } else {
                            compoundTag.putString("Sire", manager.getRecord(targetEntityRecord.data.getParentB()).data.getName());
                        }
                    }
            }
        }
    }
}
