package cimmerdev.studbook.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;
import java.util.Optional;

public record EntityTypeInfo(
        List<EntityInfoTag> tags,
        Optional<EntityGenderTag> gender_tag
) {
    public static final Codec<EntityTypeInfo> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EntityInfoTag.LIST_CODEC.fieldOf("tags").forGetter(EntityTypeInfo::tags),
            EntityGenderTag.CODEC.optionalFieldOf("gender_tag").forGetter(EntityTypeInfo::gender_tag)
            ).apply(instance, EntityTypeInfo::new)
    );
}
