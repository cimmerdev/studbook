package cimmerdev.studbook.data;

import cimmerdev.studbook.Studbook;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.*;

public class LineageRecord {

    /// Object which holds basic node data. This is what's passed via network packages when server and client exchange lineage data.
    public LineageData data;

    public LineageRecord(UUID entityID, ResourceLocation entityType, String name) {
        data = new LineageData(entityID, entityType, name, null, null, null, false, true, null);
    }

    ///Default Constructor - all constructors must call this one
    public LineageRecord(UUID entityID, ResourceLocation entityType, String name, UUID parentA, UUID parentB, CompoundTag entityTag, boolean isLoaded)
    {
        data = new LineageData(entityID, entityType, name, parentA, parentB, entityTag, isLoaded);
    }

    public LineageRecord(UUID uuid, ResourceLocation type, String nameStr, CompoundTag entityTag, UUID parentA, UUID parentB, UUID player, long birthTimeMillis, long birthTimeWorld) {
        data = new LineageData(uuid, birthTimeMillis, birthTimeWorld, type, nameStr, player, entityTag, true, true, null, -1, -1, parentA, parentB, new ArrayList<>());
    }

    ///Constructor from entity - called normally for newborn or first generation (naturally/command spawned) entities
    public static LineageRecord fromEntity(AgeableMob entity) {
        ResourceLocation type = EntityType.getKey(entity.getType());
        String nameStr = entity.getName().getString();

        CompoundTag entityTag = new CompoundTag();
        entity.saveWithoutId(entityTag);

        return new LineageRecord(entity.getUUID(), type, nameStr, null, null, entityTag, true);
    }

    /// Constructor for baby entities - called when a new entity is born by breeding
    public static LineageRecord fromEntityAndParentNodes(AgeableMob entity, LineageRecord parentANode, LineageRecord parentBNode, UUID player, long birthTimeMillis, long birthTimeWorld) {
        ResourceLocation type = EntityType.getKey(entity.getType());
        String nameStr = entity.getName().getString();
        //snapshot entity
        CompoundTag entityTag = new CompoundTag();
        entity.saveWithoutId(entityTag);
        return new LineageRecord(entity.getUUID(), type, nameStr, entityTag, parentANode.data.getEntityID(), parentBNode.data.getEntityID(), player, birthTimeMillis, birthTimeWorld);
    }

    /// Register new child UUID in our children list
    public void addChild(UUID childId) { if(!data.children.contains(childId)) data.children.add(childId); }

    /// Save NBT data method
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("EntityId", this.data.entityID);
        tag.putString("EntityType", this.data.entityType.toString());
        tag.putString("CustomName", this.data.name);
        //tag.putInt("Generation", this.data.generation);

        if (this.data.breederUUID != null) {
            tag.putUUID("Breeder",this.data.breederUUID);
            tag.putLong("BirthTime", this.data.birthTimeWorld);
        }

        tag.putBoolean("IsAlive", this.data.isAlive);
        if (!this.data.isAlive) {
            String jsonString = Component.Serializer.toJson(this.data.deathCause);
            tag.putString("DeathCause", jsonString);
            tag.putLong("DeathTime", this.data.deathTimeWorld);
        }

        if (this.data.parentA != null)
        {
            tag.putUUID("ParentA", this.data.parentA);
        }
        if (this.data.parentB != null)
        {
            tag.putUUID("ParentB", this.data.parentB);
        }

        ListTag childrenList = new ListTag();
        for (UUID childId : this.data.children) {
            childrenList.add(StringTag.valueOf(childId.toString()));
        }
        if(!childrenList.isEmpty()) tag.put("Children", childrenList);

        tag.putString("NBTCache", convertTagToString(data.entityTag));

        return tag;
    }

    /// Load NBT data method
    public static LineageRecord load(CompoundTag tag) {
        UUID entityId = tag.getUUID("EntityId");
        ResourceLocation entityType = ResourceLocation.tryParse(tag.getString("EntityType"));
        String customName = tag.getString("CustomName");
        int generation = tag.getInt("Generation");

        LineageRecord node = new LineageRecord(entityId, entityType, customName);

        if (tag.contains("ParentA"))
        {
            node.data.parentA = tag.getUUID("ParentA");
        }
        if (tag.contains("ParentB"))
        {
            node.data.parentB = tag.getUUID("ParentB");
        }

        node.data.isAlive = tag.getBoolean("IsAlive");
        if (!node.data.isAlive) {
            String jsonString = tag.getString("DeathCause");
            node.data.deathCause = Component.Serializer.fromJson(jsonString);
            node.data.deathTimeWorld = tag.getLong("DeathTime");
        }

        if (tag.contains("Breeder")) {
            node.data.breederUUID = tag.getUUID("Breeder");
            node.data.birthTimeWorld = tag.getLong("BirthTime");
        }

        if (tag.contains("Children", Tag.TAG_LIST)) {
            ListTag childrenList = tag.getList("Children", Tag.TAG_STRING);
            for (int i = 0; i < childrenList.size(); i++) {
                try {
                    UUID childId = UUID.fromString(childrenList.getString(i));
                    node.addChild(childId);
                } catch (IllegalArgumentException e) {
                    LogUtils.getLogger().info("[Studbook] Caught an exception: ", e);
                }
            }
        }

        if (tag.contains("NBTCache")) {
            node.data.entityTag = convertStringToTag(tag.getString("NBTCache"));
        }

        return node;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineageRecord javaNode = (LineageRecord) o;
        return Objects.equals(this.data.entityID, javaNode.data.entityID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.data.entityID);
    }

    public String convertTagToString(CompoundTag tag) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            NbtIo.writeCompressed(tag, outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (Exception e) {
            Studbook.LOGGER.info("[Studbook] Caught an exception during CompoundTag to String conversion: ", e);
            return "";
        }
    }

    public static CompoundTag convertStringToTag(String base64String) {
        if (base64String == null || base64String.isEmpty()) return new CompoundTag();
        try {
            byte[] bytes = Base64.getDecoder().decode(base64String);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            return NbtIo.readCompressed(inputStream);
        } catch (Exception e) {
            Studbook.LOGGER.info("[Studbook] Caught an exception during String to CompoundTag conversion: ", e);
            return new CompoundTag();
        }
    }

}
