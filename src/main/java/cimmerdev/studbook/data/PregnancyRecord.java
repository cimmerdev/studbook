package cimmerdev.studbook.data;

import net.minecraft.nbt.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public class PregnancyRecord {
    UUID dam;
    UUID sire;
    UUID breeder;

    public PregnancyRecord(UUID dam, UUID sire, UUID breeder) {
        this.dam = dam;
        this.sire = sire;
        this.breeder = breeder;
    }

    public UUID getSire() { return sire;}
    public UUID getBreeder() { return breeder; }
    public UUID getDam() { return dam; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PregnancyRecord r = (PregnancyRecord) o;
        return Objects.equals(this.dam, r.dam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dam);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("Dam", this.dam);
        tag.putUUID("Sire", this.sire);
        tag.putUUID("Breeder", this.breeder);
        return tag;
    }

    public static PregnancyRecord load(CompoundTag tag) {
        UUID dam = tag.getUUID("Dam");
        UUID sire = tag.getUUID("Sire");
        UUID breeder = tag.getUUID("Breeder");
        return new PregnancyRecord(dam, sire, breeder);
    }
}
