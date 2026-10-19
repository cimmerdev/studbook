package cimmerdev.studbook;

import cimmerdev.studbook.events.ReloadEventHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.logging.LogUtils;
import cimmerdev.studbook.network.StudbookNetwork;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Studbook.MODID)
@Mod.EventBusSubscriber(modid = Studbook.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Studbook {
    public static final String MODID = "studbook";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public Studbook(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ReloadEventHandler::registerConfigDatapack);
        context.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        context.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        StudbookNetwork.register();
    }

}