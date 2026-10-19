package cimmerdev.studbook.data;

import cimmerdev.studbook.CommonConfig;
import cimmerdev.studbook.Studbook;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.UsernameCache;
import org.jetbrains.annotations.NotNull;
import java.lang.reflect.Field;
import java.util.*;

public class LineageManager extends SavedData {
    private static final String DATA_NAME = "studbook_database";

    /// Main Database: lineage nodes linked to entity UUIDs
    private final Map<UUID, LineageRecord> database = new HashMap<>();
    /// Pregnancies Database: tracks modded mobs pregnancies, linking dam to sire
    private final Map<UUID, PregnancyRecord> pregnancies = new HashMap<>();
    /// To handle multiple entities being born, instead of removing pregnancy records immediately we delay it to the end of a tick. We use this list as a reference.
    private final List<UUID> pregnanciesPendingRemoval = new ArrayList<>();

    public LineageRecord getRecord(UUID entityId) {
        return this.database.get(entityId);
    }

    public boolean isEntityTracked(UUID entityId) { return this.database.get(entityId) != null; }

    /// Method for registering new children (and parents) in lineage database. Called for vanilla mobs or mobs without pregnancy mechanics
    public void registerBirthInstant(AgeableMob child, AgeableMob parentA, AgeableMob parentB, UUID player, long timeMillis, long timeWorld) {
        if(CommonConfig.shouldTrack(parentA, parentB)) {
            UUID childId = child.getUUID();
            if (database.containsKey(childId)) return;
            UUID parentAId = parentA.getUUID();
            UUID parentBId = parentB.getUUID();
            LineageRecord pA = database.computeIfAbsent(parentAId, id -> LineageRecord.fromEntity(parentA));
            LineageRecord pB = database.computeIfAbsent(parentBId, id -> LineageRecord.fromEntity(parentB));
            createChildRecord(child, pA, pB, player, timeMillis, timeWorld);
        }
    }

    /// Method for registering children for modded mobs which have pregnancy mechanics
    public void registerBirthPregnancyEnd(AgeableMob child, long timeMillis, long timeWorld) {
        UUID childId = child.getUUID();
        if (database.containsKey(childId))
        {
            LogUtils.getLogger().info("[Studbook] Entity with UUID {} is already tracked in lineage database!", childId);
            return;
        }
        UUID motherId = null;
        UUID fatherId;

        if (EntityType.getKey(child.getType()).getNamespace().equals("eanimod")) {
            Field motherField = getFieldOfModdedEntityClass(child, "motherUUID", "mokiyoki.enhancedanimals.entity.EnhancedAnimalAbstract");
            try {
                assert motherField != null;
                String motherIdStr = (String) motherField.get(child);
                if (motherIdStr.isEmpty()) return;
                motherId = UUID.fromString(motherIdStr);
            }
            catch (IllegalAccessException e) { LogUtils.getLogger().info("[Studbook] Failed to access 'motherUUID' field in EnhancedAnimalAbstract class instance!");
            }
        }
        if (EntityType.getKey(child.getType()).getNamespace().equals("horse_colors")) {
            Field motherField = getFieldOfModdedEntityClass(child, "motherUUID", "sekelsta.horse_colors.entity.AbstractHorseGenetic");
            try {
                assert motherField != null;
                motherId = (UUID) motherField.get(child);
                if (motherId == null) return;
            }
            catch (IllegalAccessException e) { LogUtils.getLogger().info("[Studbook] Failed to access 'motherUUID' field in AbstractHorseGenetic class instance!");
            }
        }
        if(pregnancies.get(motherId) == null) {
            LogUtils.getLogger().info("[Studbook] Pregnancy record for entity with UUID {} wasn't found. Can't record baby entity({}) in database.", motherId, childId);
            return;
        }
        fatherId = pregnancies.get(motherId).getSire();

        LineageRecord pA = database.get(motherId);
        LineageRecord pB = database.get(fatherId);
        createChildRecord(child, pA, pB, pregnancies.get(motherId).getBreeder(), timeMillis, timeWorld);
    }

    private void createChildRecord(AgeableMob child, LineageRecord pA, LineageRecord pB, UUID player, long timeMillis, long timeWorld) {
        if(pA != null  && pB != null) {
            pregnanciesPendingRemoval.add(pA.data.getEntityID());
            UUID childId = child.getUUID();
            LineageRecord childNode = LineageRecord.fromEntityAndParentNodes(child, pA, pB, player, timeMillis, timeWorld);
            pA.addChild(childId);
            pB.addChild(childId);
            database.put(childId, childNode);
            LogUtils.getLogger().info("[Studbook] Successfully created a new record for newborn entity with UUID {}.", childId);
            this.setDirty();
        }
        else LogUtils.getLogger().info("[Studbook] Couldn't record a newborn entity with UUID {}. At least one of the supposed parent's aren't recorded.", child.getUUID());
    }

    public void registerPregnancy(AgeableMob parentA, AgeableMob parentB, UUID player) {
        if (CommonConfig.shouldTrack(parentA, parentB)) {
            pregnancies.put(parentA.getUUID(), new PregnancyRecord(parentA.getUUID(), parentB.getUUID(), player));
            LineageRecord pA = database.computeIfAbsent(parentA.getUUID(), id -> LineageRecord.fromEntity(parentA));
            LineageRecord pB = database.computeIfAbsent(parentB.getUUID(), id -> LineageRecord.fromEntity(parentB));
            this.setDirty();
        }
    }

    public void removePregnancyRecordsPendingRemoval()
    {
        for(UUID uuid : pregnanciesPendingRemoval) {
            pregnancies.remove(uuid);
        }
        pregnanciesPendingRemoval.clear();
        this.setDirty();
    }

    /// Get lineage manager for current world
    public static LineageManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                LineageManager::load,
                LineageManager::new,
                DATA_NAME
        );
    }

    public Set<LineageRecord> getAllAncestorsAndDescendants (LineageRecord currentNode)
    {
        Set<LineageRecord> Descendants = new LinkedHashSet<>();
        getDescendantsTreeRecursive(currentNode, Descendants);
        Set<LineageRecord> Ancestors = new LinkedHashSet<>();
        for (LineageRecord node : Descendants) {
            getAncestorsTreeRecursive(node, Ancestors);
        }
        Ancestors.addAll(Descendants);
        return Ancestors;
    }

    /// Recursive function which build up tree from bottom to top
    private void getAncestorsTreeRecursive(LineageRecord currentNode, Set<LineageRecord> visited) {
        if (currentNode == null || visited.contains(currentNode)) {
            return;
        }
        visited.add(currentNode);

        if (database.containsKey(currentNode.data.getParentA())) {
            getAncestorsTreeRecursive(database.get(currentNode.data.getParentA()), visited);
        }
        if (database.containsKey(currentNode.data.getParentB())) {
            getAncestorsTreeRecursive(database.get(currentNode.data.getParentB()), visited);
        }
    }

    /// Recursive function which build up tree from top to bottom
    private void getDescendantsTreeRecursive(LineageRecord currentNode, Set<LineageRecord> visited) {
        if (currentNode == null || visited.contains(currentNode)) {
            return;
        }
        visited.add(currentNode);

        for(UUID uuid : currentNode.data.getChildren())
        {
            if (database.containsKey(uuid)) {
                getDescendantsTreeRecursive(database.get(uuid), visited);
            }
        }
    }

    public List<NodeWidgetData> getNodesFromRecords(Set<LineageRecord> set) {
        List<NodeWidgetData> output = new ArrayList<>();
        for (LineageRecord node : set) {
            LineageData d = node.data;
            output.add(new NodeWidgetData(
                    0,
                    0,
                    d.entityID,
                    d.entityType,
                    d.name,
                    d.entityTag,
                    EntityStatus.getStatus(d),
                    d.getBreederID() == null ? "?" : UsernameCache.getLastKnownUsername(d.getBreederID()),
                    d.birthTimeWorld,
                    d.getDeathCause(),
                    d.deathTimeWorld,
                    d.parentA,
                    d.parentB,
                    d.children,
                    1
            ));
        }
        return output;
    }

    public void updateEntityName(UUID uuid, String newName) {
        if (!database.containsKey(uuid)) return;
        database.get(uuid).data.name = newName;
        setDirty();
    }

    public void updateEntityCache(UUID uuid, AgeableMob entity) {
        CompoundTag entityTag = new CompoundTag();
        entity.saveWithoutId(entityTag);
        database.get(uuid).data.setEntityTag(entityTag);
        setDirty();
    }

    public void setEntityLoaded(UUID uuid, boolean newLoaded) {
        database.get(uuid).data.isLoaded = newLoaded;
        setDirty();
    }

    public void markEntityDeceased(UUID uuid, Component deathCause, long timeMillis, long timeWorld) {
        LineageData node = database.get(uuid).data;
        pregnancies.remove(uuid);
        node.isAlive = false;
        node.deathCause = deathCause;
        node.deathTimeMillis = timeMillis;
        node.deathTimeWorld = timeWorld;
        setDirty();
    }

    /// A black magic function to get things that aren't available with NBT tags
    private Field getFieldOfModdedEntityClass(AgeableMob entity, String fieldName, String fullClassName) {
        Class<?> entityClass = entity.getClass();
        Class<?> abstractTargetClass = null;
        while (entityClass != null && entityClass != Object.class) {
               if (entityClass.getName().equals(fullClassName)) {
                    abstractTargetClass = entityClass;
                    break;
               }
               entityClass = entityClass.getSuperclass();
        }
        if (abstractTargetClass != null) {
            try {
                Field field = abstractTargetClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {
                Studbook.LOGGER.info("[Studbook] Failed to get '{}' field in class '{}'. No such field exists.", fieldName, fullClassName);
            }
        }
        return null;
    }

    /// Load method - iterates though all registered lineage records
    public static LineageManager load(CompoundTag tag) {
        LineageManager manager = new LineageManager();
        if (tag.contains("Nodes", Tag.TAG_LIST)) {
            ListTag nodesList = tag.getList("Nodes", Tag.TAG_COMPOUND);
            for (int i = 0; i < nodesList.size(); i++) {
                CompoundTag nodeTag = nodesList.getCompound(i);
                LineageRecord node = LineageRecord.load(nodeTag);

                manager.database.put(node.data.getEntityID(), node);
            }
        }
        if (tag.contains("Pregnancies", Tag.TAG_LIST)) {
            ListTag pregnanciesList = tag.getList("Pregnancies", Tag.TAG_COMPOUND);
            for (int i = 0; i < pregnanciesList.size(); i++) {
                CompoundTag prTag = pregnanciesList.getCompound(i);
                PregnancyRecord record = PregnancyRecord.load(prTag);

                manager.pregnancies.put(record.getDam(), record);
            }
        }
        return manager;
    }

    /// Save method - iterates though all registered lineage nodes
    @Override public @NotNull CompoundTag save(@NotNull CompoundTag tag) {
        ListTag nodesList = new ListTag();
        for (Map.Entry<UUID, LineageRecord> entry : database.entrySet()) {
            nodesList.add(entry.getValue().save());
        }
        tag.put("Nodes", nodesList);

        ListTag pregnanciesList = new ListTag();
        for (Map.Entry<UUID, PregnancyRecord> entry : pregnancies.entrySet()) {
            pregnanciesList.add(entry.getValue().save());
        }
        tag.put("Pregnancies", pregnanciesList);

        return tag;
    }
}
