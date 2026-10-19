package cimmerdev.studbook.data;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LineageData {
    final UUID entityID;
    ResourceLocation entityType;
    String name;
    CompoundTag entityTag;
    boolean isAlive;
    boolean isLoaded;
    UUID breederUUID;
    final long birthTimeMillis;
    long birthTimeWorld;
    Component deathCause;
    long deathTimeMillis;
    long deathTimeWorld;
    UUID parentA;
    UUID parentB;
    List<UUID> children;

    public LineageData(UUID entityID, long birthTimeMillis, long birthTimeWorld, ResourceLocation entityType, String name, UUID breederUUID, CompoundTag entityTag, boolean isAlive, boolean isLoaded, Component deathCause, long deathTimeMillis, long deathTimeWorld, UUID parentA, UUID parentB, List<UUID> children) {
        this.entityID = entityID;
        this.birthTimeMillis = birthTimeMillis;
        this.birthTimeWorld = birthTimeWorld;
        this.entityType = entityType;
        this.name = name;
        this.breederUUID = breederUUID;
        this.entityTag = entityTag;
        this.isAlive = isAlive;
        this.isLoaded = isLoaded;
        this.deathCause = deathCause;
        this.deathTimeMillis = deathTimeMillis;
        this.deathTimeWorld = deathTimeWorld;
        this.parentA = parentA;
        this.parentB = parentB;
        this.children = children;
    }

    public LineageData(UUID uuid, ResourceLocation entityType, String name, UUID uuidA, UUID uuidB, CompoundTag entityTag, boolean isLoaded, boolean isAlive, Component deathCause) {
        this(uuid, -1, -1, entityType, name, null, entityTag, isAlive, isLoaded, deathCause, -1, -1, uuidA, uuidB, new ArrayList<>());
    }

    public LineageData(UUID entityID, ResourceLocation entityType, String name, UUID parentA, UUID parentB, CompoundTag entityTag, boolean isLoaded) {
        this(entityID, -1, -1, entityType, name, null, entityTag, true, isLoaded, null, -1, -1, parentA, parentB, new ArrayList<>());
    }

    public void setEntityTag(CompoundTag newTag) {
        entityTag = newTag;
    }

    public UUID getEntityID() {
        return entityID;
    }

    public ResourceLocation getEntityType() {
        return entityType;
    }

    public UUID getParentA() {
        return parentA;
    }

    public String getName() {
        return name;
    }

    public UUID getBreederID() {
        return breederUUID;
    }

    public UUID getParentB() {
        return parentB;
    }

//    public int getGeneration() {
//        return generation;
//    }

    public List<UUID> getChildren() {
        return children;
    }

    public CompoundTag getEntityTag() {
        return entityTag;
    }

    public boolean getIsAlive() {
        return isAlive;
    }

    public boolean getIsLoaded() {
        return isLoaded;
    }

    public long getBirthTimeWorld() {
        return birthTimeWorld;
    }

    public long getDeathTimeWorld() {
        return deathTimeWorld;
    }

    public Component getDeathCause() {
        return deathCause == null ? null : deathCause.copy().withStyle(style -> style.applyFormat(ChatFormatting.DARK_RED));
    }
}
