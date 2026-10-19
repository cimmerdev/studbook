package cimmerdev.studbook.api;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum TagVarType implements StringRepresentable {
    STRING("string") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) { return tag.getString(key); }
    },
    INT("int") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) { return tag.getInt(key); }
    },
    FLOAT("float") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) { return tag.getFloat(key); }
    },
    BOOLEAN("bool") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) { return tag.getBoolean(key); }
    },
    BYTE("byte") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) { return tag.getByte(key); }
    },
    ATTRIBUTE("attribute") {
        @Override
        public Object readFromNbt(CompoundTag tag, String key) {
            ListTag attributes_list = tag.getList("Attributes", CompoundTag.TAG_COMPOUND);
            for (int i = 0; i < attributes_list.size(); i++) {
                CompoundTag attributeTag = attributes_list.getCompound(i);
                if (attributeTag.getString("Name").equals(key)) {
                    return attributeTag.getDouble("Base");
                }
            }
            return null;
        }
    };

    private final String name;

    TagVarType(String name) { this.name = name; }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public abstract Object readFromNbt(CompoundTag tag, String key);

    public static final Codec<TagVarType> CODEC = StringRepresentable.fromEnum(TagVarType::values);
}
