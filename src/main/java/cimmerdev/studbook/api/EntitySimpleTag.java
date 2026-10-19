package cimmerdev.studbook.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

public record EntitySimpleTag(
        String tag_name,
        TagVarType variable_type,
        List<String> values
) {
    public static final Codec<EntitySimpleTag> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("tag_name").forGetter(EntitySimpleTag::tag_name),
                    TagVarType.CODEC.fieldOf("variable_type").forGetter(EntitySimpleTag::variable_type),
                    Codec.STRING.listOf().fieldOf("values").forGetter(EntitySimpleTag::values)
            ).apply(instance, EntitySimpleTag::new)
    );
}
