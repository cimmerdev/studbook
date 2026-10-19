package cimmerdev.studbook.data;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.api.EntityGenderTag;
import cimmerdev.studbook.api.EntitySimpleTag;
import cimmerdev.studbook.api.GenderValue;
import cimmerdev.studbook.api.TagVarType;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import javax.swing.text.StyledEditorKit;
import java.util.List;

public enum Gender implements StringRepresentable {
    NONE("none") {
        @Override
        public Component getSymbol() {
            return Component.empty();
        }

        @Override
        public Component getDisplayName() {
            return Component.literal("None");
        }

    },
    MALE("male") {
        @Override
        public Component getSymbol() {
            return Component.literal("♂").withStyle(style -> style.withColor(0x0380fc));
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("studbook.gui.sidepanel.gender.male").withStyle(style -> style.withColor(0x0380fc));
        }
    },
    FEMALE("female") {
        @Override
        public Component getSymbol() {
            return Component.literal("♀").withStyle(style -> style.withColor(0xfc037e));
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("studbook.gui.sidepanel.gender.female").withStyle(style -> style.withColor(0xfc037e));
        }
    },
    OMNIGENDER("omnigender") {
        @Override
        public Component getSymbol() {
            return Component.literal("⚥").withStyle(style -> style.withColor(0x7e03fc));
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("studbook.gui.sidepanel.gender.omnigender").withStyle(style -> style.withColor(0x7e03fc));
        }
    };

    private final String name;

    Gender(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static final Codec<Gender> CODEC = StringRepresentable.fromEnum(Gender::values);

    public static Gender getGender(EntityGenderTag genderTag, CompoundTag entityTag) {
        if (genderTag != null) {
            CompoundTag tag = entityTag.copy();
            Object rawResult;
            if(tag.contains(genderTag.tag_name())) {
                rawResult = genderTag.variable_type().readFromNbt(tag, genderTag.tag_name());
                String stringResult = rawResult.toString();
                for(GenderValue val : genderTag.values()) {
                    if(stringResult.equals(val.raw_value()))
                    {
                        return val.gender_enum();
                    }
                }
            }
        }
        return Gender.NONE;
    }

    public static Component getTranslatableGender(EntityGenderTag genderTag, CompoundTag entityTag) {
        if (genderTag != null) {
            CompoundTag tag = entityTag.copy();
            Object rawResult;
            if(tag.contains(genderTag.tag_name())) {
                rawResult = genderTag.variable_type().readFromNbt(tag, genderTag.tag_name());
                String stringResult = rawResult.toString();
                for(GenderValue val : genderTag.values()) {
                    if(stringResult.equals(val.raw_value()))
                    {
                        if(val.required_tags().isPresent()) {
                            Object reqRawResult;
                            for(EntitySimpleTag reqTag : val.required_tags().get()) {
                                reqRawResult = reqTag.variable_type().readFromNbt(tag, reqTag.tag_name());
                                String stringReqResult = reqRawResult.toString();
                                for(String reqValue : reqTag.values()) {
                                    if(reqValue.equals(stringReqResult)) return val.translated_value();
                                }
                            }
                        }
                        else return val.translated_value();
                    }
                }
            }
        }
        return Gender.NONE.getDisplayName();
    }

    public abstract Component getSymbol();
    public abstract Component getDisplayName();
}
