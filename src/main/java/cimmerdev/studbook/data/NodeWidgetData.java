package cimmerdev.studbook.data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class NodeWidgetData {
    int x;
    int y;
    final public UUID uuid;
    final public ResourceLocation entityType;
    final public String name;
    CompoundTag entityTag = new CompoundTag();
    final public EntityStatus status;
    final public Gender gender;
    final public String breeder;
    final long birthTimeWorld;
    final public Component deathCause;
    final long deathTimeWorld;
    final public UUID parentA;
    final public UUID parentB;
    final public List<UUID> children;
    final public int generation;
    private LivingEntity cachedEntity = null;
    private boolean isDirty = true;

    public NodeWidgetData(int x, int y, UUID uuid, ResourceLocation entityType, String name, CompoundTag entityTag, EntityStatus status, String breeder, long birthTimeWorld, Component deathCause, long deathTimeWorld, UUID parentA, UUID parentB, List<UUID> children, int generation) {
        this.x = x;
        this.y = y;
        this.uuid = uuid;
        this.entityType = entityType;
        this.name = name;
        this.entityTag = entityTag;
        this.breeder = breeder;
        this.status = status;
        this.gender = Gender.getGender(EntityTypeInfoListener.getGenderTagInfo(entityType), entityTag);
        this.birthTimeWorld = birthTimeWorld;
        this.deathTimeWorld = deathTimeWorld;
        this.deathCause = deathCause;
        this.parentA = parentA;
        this.parentB = parentB;
        this.children = children;
        this.generation = generation;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public CompoundTag getEntityTag() { return entityTag; }
    public long getBirthTimeWorld() { return birthTimeWorld; }
    public long getDeathTimeWorld() { return deathTimeWorld; }
    public UUID getParentA() { return parentA; }
    public UUID getParentB() { return parentB; }

    public LivingEntity getOrCreateClientEntity() {
        if (cachedEntity != null && !isDirty) {
            return cachedEntity;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || entityType == null || entityTag == null) {
            return null;
        }
        Optional<EntityType<?>> typeOpt = Optional.ofNullable(ForgeRegistries.ENTITY_TYPES.getValue(entityType));
        if (typeOpt.isPresent()) {
            Entity entity = typeOpt.get().create(mc.level);

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.load(entityTag.copy());
                cachedEntity = livingEntity;
                isDirty = false;;
                LocalPlayer player = mc.player;
                if (player != null) {
                    cachedEntity.tickCount = mc.player.tickCount;
                    cachedEntity.setYRot(0.0F);
                    cachedEntity.setXRot(0.0F);
                    cachedEntity.yRotO = 0.0F;
                    cachedEntity.xRotO = 0.0F;
                    cachedEntity.setYHeadRot(0.0F);
                    cachedEntity.yHeadRotO = 0.0F;
                    cachedEntity.yBodyRot = 0.0F;
                    cachedEntity.yBodyRotO = 0.0F;
                }
                return cachedEntity;
            }
        }
        return null;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(x);
        buf.writeInt(y);
        buf.writeUUID(uuid);
        buf.writeResourceLocation(entityType);
        buf.writeUtf(name);
        buf.writeUtf(breeder);
        buf.writeLong(birthTimeWorld);
        buf.writeEnum(status);
        if (status == EntityStatus.DECEASED) buf.writeComponent(deathCause);
        buf.writeLong(deathTimeWorld);
        boolean haveParentA = parentA != null;
        buf.writeBoolean(haveParentA);
        if (haveParentA) buf.writeUUID(parentA);
        boolean haveParentB = parentB != null;
        buf.writeBoolean(haveParentB);
        if (haveParentB) buf.writeUUID(parentB);
        int childrenCount = children.size();
        buf.writeInt(childrenCount);
        for (int i = 0; i < childrenCount; i++) buf.writeUUID(children.get(i));
        buf.writeInt(generation);
        buf.writeNbt(entityTag);
    }

    // Client buf reading
    public static NodeWidgetData fromBytes(FriendlyByteBuf buf) {
        int x = buf.readInt();
        int y = buf.readInt();
        UUID uuid = buf.readUUID();
        ResourceLocation entityType = buf.readResourceLocation();
        String name = buf.readUtf();
        String breeder = buf.readUtf();
        long birthTimeWorld = buf.readLong();
        EntityStatus status = buf.readEnum(EntityStatus.class);

        Component deathCause = null;
        if (status == EntityStatus.DECEASED) deathCause = buf.readComponent();
        long deathTimeWorld = buf.readLong();

        UUID parentA = null;
        UUID parentB = null;
        boolean haveParentA = buf.readBoolean();
        if (haveParentA) parentA = buf.readUUID();
        boolean haveParentB = buf.readBoolean();
        if (haveParentB) parentB = buf.readUUID();
        List<UUID> children = new ArrayList<>();
        int childrenCount = buf.readInt();
        for (int i = 0; i < childrenCount; i++) children.add(buf.readUUID());

        int generation = buf.readInt();
        CompoundTag entityTag = buf.readNbt();
        return new NodeWidgetData(x, y, uuid, entityType, name, entityTag, status, breeder, birthTimeWorld, deathCause, deathTimeWorld, parentA, parentB, children, generation);
    }

}
