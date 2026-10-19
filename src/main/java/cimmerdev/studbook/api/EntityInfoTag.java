package cimmerdev.studbook.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import java.util.List;
import java.util.Map;

public record EntityInfoTag(
        String tag_name,
        Component translated_name,
        TagVarType variable_type,
        Map<String, Component> mapped_values
) {
    public static final Codec<EntityInfoTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("tag_name").forGetter(EntityInfoTag::tag_name),
            ExtraCodecs.COMPONENT.fieldOf("translated_name").forGetter(EntityInfoTag::translated_name),
            TagVarType.CODEC.fieldOf("variable_type").forGetter(EntityInfoTag::variable_type),
            Codec.unboundedMap(Codec.STRING, ExtraCodecs.COMPONENT).fieldOf("mapped_values").forGetter(EntityInfoTag::mapped_values)
    ).apply(instance, EntityInfoTag::new)
    );

    public static final Codec<List<EntityInfoTag>> LIST_CODEC = CODEC.listOf();
}
