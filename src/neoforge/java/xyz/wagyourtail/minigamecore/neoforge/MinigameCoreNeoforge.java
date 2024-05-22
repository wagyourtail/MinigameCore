package xyz.wagyourtail.minigamecore.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import xyz.wagyourtail.minigamecore.MinigameCore;

@Mod("minigamecore")
public class MinigameCoreNeoforge extends MinigameCore {

    public MinigameCoreNeoforge(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::onInit);
        NeoForge.EVENT_BUS.register(this);
    }

    public void onInit(FMLCommonSetupEvent event) {
        super.onInitialize();
    }

    @SubscribeEvent
    public void onCommands(RegisterCommandsEvent event) {
        super.onRegisterCommands(event.getDispatcher(), event.getBuildContext());
    }

}
