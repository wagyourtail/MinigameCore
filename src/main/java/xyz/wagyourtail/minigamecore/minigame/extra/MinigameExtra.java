package xyz.wagyourtail.minigamecore.minigame.extra;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import xyz.wagyourtail.minigamecore.minigame.Minigame;
import xyz.wagyourtail.minigamecore.minigame.MinigameInstance;
import xyz.wagyourtail.uniconfig.Group;

public abstract class MinigameExtra<T extends MinigameExtra<?, ?, ?, ?>, U extends Minigame<?, ?, ?>, V extends MinigameInstanceExtra<?, ?>, W extends MinigameInstance<?, ?>> extends Group {
    protected final U minigame;
    public final String name;

    protected MinigameExtra(U minigame, String name) {
        super(name, minigame);
        this.minigame = minigame;
        this.name = name;
    }

    public abstract Class<V> type();

    public abstract V createInstance(W instance);

    public void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {}

}
