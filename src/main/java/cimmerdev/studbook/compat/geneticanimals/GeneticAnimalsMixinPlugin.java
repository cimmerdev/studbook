package cimmerdev.studbook.compat.geneticanimals;

import cimmerdev.studbook.Studbook;
import net.minecraftforge.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class GeneticAnimalsMixinPlugin implements IMixinConfigPlugin {

    private static final String TARGET_MOD_ID = "eanimod";
    private boolean isModLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        this.isModLoaded = LoadingModList.get().getModFileById(TARGET_MOD_ID) != null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if(mixinClassName.contains("EAnimal")) {
            return isModLoaded;
        };
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return "";
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return List.of();
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        Studbook.LOGGER.info("[Studbook] Detected Genetic Animals mod, applying {}.", mixinClassName);
    }
}
