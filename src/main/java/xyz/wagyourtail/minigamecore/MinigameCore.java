package xyz.wagyourtail.minigamecore;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.wagyourtail.minigamecore.minigame.Minigame;

public class MinigameCore {
    protected static final ClassToInstanceMap<Minigame<?, ?, ?>> minigames = MutableClassToInstanceMap.create();
    public static final String MOD_ID = "minigamecore";
    public static final Logger LOGGER = LoggerFactory.getLogger(MinigameCore.class);


    public void onInitialize() {
        LOGGER.info("MinigameCore initialized");
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerMinigame(Minigame<?, ?, ?> minigame) {
        ((ClassToInstanceMap) minigames).putInstance(minigame.getClass(), minigame);
    }

    public void onRegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        for (Minigame<?, ?, ?> value : minigames.values()) {
            value.registerCommands(dispatcher, context);
        }
        CoreCommands.registerCommands(dispatcher, context);
    }

}
