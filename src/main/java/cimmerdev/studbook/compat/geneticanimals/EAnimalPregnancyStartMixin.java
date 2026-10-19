package cimmerdev.studbook.compat.geneticanimals;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.data.LineageManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "mokiyoki.enhancedanimals.entity.EnhancedAnimalAbstract", remap = true)
    public class EAnimalPregnancyStartMixin {
        @Inject(method = "Lmokiyoki/enhancedanimals/entity/EnhancedAnimalAbstract;getBreedOffspring(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/AgeableMob;)Lnet/minecraft/world/entity/AgeableMob;", at = @At("HEAD"))
        public void onGetBreedOffspring(ServerLevel serverWorld, AgeableMob ageable, CallbackInfoReturnable<AgeableMob> cir) {
            LineageManager manager = LineageManager.get(serverWorld);
            Animal thisAnimal = (Animal) (Object) this;
            AgeableMob thisMob = (AgeableMob) (Object) this;
            ServerPlayer player = thisAnimal.getLoveCause();
            manager.registerPregnancy(thisMob, ageable, player == null ? null : player.getUUID());
        }
    }
