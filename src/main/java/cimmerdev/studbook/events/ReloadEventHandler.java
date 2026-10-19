package cimmerdev.studbook.events;

import cimmerdev.studbook.Studbook;
import cimmerdev.studbook.data.EntityTypeInfoListener;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = Studbook.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ReloadEventHandler {

    private static final Path DATAPACK_DIR = FMLPaths.CONFIGDIR.get().resolve(Studbook.MODID).resolve("datapack");

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        EntityTypeInfoListener manager = new EntityTypeInfoListener(Studbook.GSON);
        event.addListener(manager);
    }

    @SubscribeEvent
    public static void registerConfigDatapack(AddPackFindersEvent event) {
        if (event.getPackType() == PackType.SERVER_DATA) {
            try {
                generateDefaultStructure();

                String packId = "studbook_default_pack";

                Pack.ResourcesSupplier resourcesSupplier = (id) -> new PathPackResources(id, DATAPACK_DIR, false);
                Pack configPack = Pack.readMetaAndCreate(
                        packId,
                        Component.literal("Studbook default datapack"),
                        false,
                        resourcesSupplier,
                        PackType.SERVER_DATA,
                        Pack.Position.TOP,
                        PackSource.BUILT_IN
                );
                if (configPack != null) {
                    event.addRepositorySource(packConsumer -> packConsumer.accept(configPack));
                    Studbook.LOGGER.info("[Studbook]: Successfully registered Studbook default datapack.");
                }

            } catch (IOException e) {
                Studbook.LOGGER.error("[Studbook] Couldn't create default datapack in /config/studbook folder. ", e);
            }
        }
    }

    private static void generateDefaultStructure() throws IOException {
        Path tagsDir = DATAPACK_DIR.resolve("data").resolve(Studbook.MODID).resolve("entity_tags");

        if (!Files.exists(tagsDir)) {
            Files.createDirectories(tagsDir);

            Path mcmetaPath = DATAPACK_DIR.resolve("pack.mcmeta");
            String mcmetaContent = "{\n" +
                    "  \"pack\": {\n" +
                    "    \"description\": \"Dynamically generated config datapack for Studbook mod. It contains data that should be displayed in Lineage Screen for each entity type. Source files from which this pack generates from can be edited in /config/studbook/datapack folder.\",\n" +
                    "    \"pack_format\": 15\n" +
                    "  }\n" +
                    "}";
            Files.writeString(mcmetaPath, mcmetaContent);

            IModFile modFile = ModList.get().getModFileById(Studbook.MODID).getFile();
            if (modFile != null) {
                Path jarRootPath = modFile.findResource("data", Studbook.MODID, "entity_tags");

                if (Files.exists(jarRootPath)) {
                    try (Stream<Path> stream = Files.walk(jarRootPath)) {
                        stream.forEach(sourcePath -> {
                            try {
                                Path relativePath = jarRootPath.relativize(sourcePath);
                                Path destinationPath = tagsDir.resolve(relativePath.toString());

                                if (Files.isDirectory(sourcePath)) {
                                    Files.createDirectories(destinationPath);
                                } else if (sourcePath.toString().endsWith(".json")) {
                                    Files.copy(sourcePath, destinationPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                                    Studbook.LOGGER.info("[Studbook] Copied config file: {}", relativePath);
                                }
                            } catch (IOException e) {
                                Studbook.LOGGER.error("[Studbook] Error copying file: " + sourcePath, e);
                            }
                        });
                    }
                } else {
                    Studbook.LOGGER.error("[Studbook] Path '{}' doesn't exist in mod's resources.", jarRootPath);
                }
            }
            else Studbook.LOGGER.warn("[Studbook] Can't find myself... (Couldn't get mod file by this mod's ID).");
        }
        else Studbook.LOGGER.info("[Studbook] Config datapack already exists, proceed.");
    }
}
