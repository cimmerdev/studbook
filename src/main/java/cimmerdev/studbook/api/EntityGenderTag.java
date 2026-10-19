package cimmerdev.studbook.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record EntityGenderTag(
        String tag_name,
        TagVarType variable_type,
        List<GenderValue> values
) {
    public static final Codec<EntityGenderTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("tag_name").forGetter(EntityGenderTag::tag_name),
                    TagVarType.CODEC.fieldOf("variable_type").forGetter(EntityGenderTag::variable_type),
                    GenderValue.CODEC.listOf().fieldOf("values").forGetter(EntityGenderTag::values)
            ).apply(instance, EntityGenderTag::new)
    );
}
