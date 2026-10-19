package cimmerdev.studbook.api;

import cimmerdev.studbook.data.Gender;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public record GenderValue(
        String raw_value,
        Component translated_value,
        Gender gender_enum,
        Optional<List<EntitySimpleTag>> required_tags
) {
    public static final Codec<GenderValue> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("raw_value").forGetter(GenderValue::raw_value),
                    ExtraCodecs.COMPONENT.fieldOf("translated_value").forGetter(GenderValue::translated_value),
                    Gender.CODEC.fieldOf("gender_enum").forGetter(GenderValue::gender_enum),
                    EntitySimpleTag.CODEC.listOf().optionalFieldOf("required_tags").forGetter(GenderValue::required_tags)
            ).apply(instance, GenderValue::new)
    );
}
