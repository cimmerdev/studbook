package cimmerdev.studbook.data;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.api.EntityGenderTag;
import cimmerdev.studbook.api.EntityTypeInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import cimmerdev.studbook.api.EntityInfoTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityTypeInfoListener extends SimpleJsonResourceReloadListener {

    private static final Map<ResourceLocation, EntityTypeInfo> MAPPED_TAGS = new HashMap<>();

    public EntityTypeInfoListener(Gson gson) { super(gson, "entity_tags"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        MAPPED_TAGS.clear();
        Map<ResourceLocation, EntityTypeInfo> freshData = new HashMap<>();
        object.forEach((location, jsonElement) ->
        { try {
            EntityTypeInfo info = EntityTypeInfo.CODEC.parse(JsonOps.INSTANCE, jsonElement)
                    .getOrThrow(false, error -> Studbook.LOGGER.info("[Studbook] Failed to parse JSON: {}. {}", location, error));
            freshData.put(convert(location), info);
            Studbook.LOGGER.info("[Studbook] Successfully loaded JSON: {}", location);
        } catch (Exception e) {
            Studbook.LOGGER.info("[Studbook] Failed to load JSON: {}. {}", location, String.valueOf(e));
        }
        });
        MAPPED_TAGS.putAll(freshData);
        Studbook.LOGGER.info("[Studbook] JSON loading complete");
    }

    public static List<EntityInfoTag> getTagsInfo(ResourceLocation entityType) {
        return MAPPED_TAGS.get(entityType) != null ? MAPPED_TAGS.get(entityType).tags() : null;
    }

    public static EntityGenderTag getGenderTagInfo(ResourceLocation entityType) {
        if(MAPPED_TAGS.get(entityType) != null) return MAPPED_TAGS.get(entityType).gender_tag().isPresent()
                ? MAPPED_TAGS.get(entityType).gender_tag().get()
                : null;
        else return null;
    }

    public static ResourceLocation convert(ResourceLocation init_rl)
    {
        String path = init_rl.getPath();
        int slashIndex = path.indexOf('/');
        if (slashIndex != -1)
        {
            String targetNamespace = path.substring(0, slashIndex);
            String targetPath = path.substring(slashIndex + 1);
            return ResourceLocation.fromNamespaceAndPath(targetNamespace, targetPath);
        }
        return init_rl;
    }
}
