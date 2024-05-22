package xyz.wagyourtail.minigamecore.minigame;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import xyz.wagyourtail.minigamecore.minigame.extra.MinigameExtra;
import xyz.wagyourtail.minigamecore.minigame.extra.MinigameInstanceExtra;
import xyz.wagyourtail.minigamecore.utils.TranslationUtils;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierConnector;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierSettingConnector;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

public abstract class Minigame<T extends Minigame<T, U, V>, U extends MinigameExtra<U, Minigame<T, U, V>, MinigameInstanceExtra<U, V>, MinigameInstance<T, MinigameInstanceExtra<U, V>>>, V extends MinigameInstance<Minigame<T, U, V>, ?>> extends UniConfig {
    private static final DynamicCommandExceptionType PLAYER_ALREADY_IN_GAME = new DynamicCommandExceptionType(a -> TranslationUtils.translatable("minigamecore.player_already_in_game", a));
    private static final DynamicCommandExceptionType PLAYER_NOT_IN_GAME = new DynamicCommandExceptionType(a -> TranslationUtils.translatable("minigamecore.player_not_in_game", a));
    private static final SimpleCommandExceptionType NOT_ALLOWED_FROM_CONSOLE = new SimpleCommandExceptionType(TranslationUtils.translatable("minigamecore.not_allowed_from_console"));

    public final ClassToInstanceMap<U> extraData = MutableClassToInstanceMap.create();
    public final List<V> instances = new ArrayList<>();

    public Minigame(String name, Path path) {
        super(name, path, true);
        requireConnector(BrigadierSettingConnector.class);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void registerExtra(Function<T, U> extraFactory) {
        var extra = extraFactory.apply((T) this);
        this.group(extra);
        ((ClassToInstanceMap) extraData).put(extra.type(), extra);
    }

    public void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        var builder = Commands.literal(name);
        setupCommand(dispatcher, builder, context);
        addPlayerCommand(dispatcher, builder, context);
        customSubCommands(dispatcher, builder, context);
        extraSubCommands(dispatcher, builder, context);
        globalConfigCommand(dispatcher, builder, context);
        dispatcher.register(builder);
    }

    public void customSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {
    }

    public void extraSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {
        for (var extra : extraData.values()) {
            var extraCommand = Commands.literal(extra.name);
            builder.then(extraCommand);
            extra.registerSubCommands(dispatcher, extraCommand, context);
        }
    }

    public abstract V createInstance();

    public void assertNotInGame(ServerPlayer player, @Nullable MinigameInstance<?, ?> minigameInstance) throws CommandSyntaxException {
        for (var instance : instances) {
            if (instance == minigameInstance) {
                continue;
            }
            if (instance.players.contains(player.getGameProfile().getName())) {
                throw PLAYER_ALREADY_IN_GAME.create(player.getGameProfile().getName());
            }
        }
    }

    @Nullable
    public V findGameFor(ServerPlayer player) {
        for (var instance : instances) {
            if (instance.players.contains(player.getGameProfile().getName())) {
                return instance;
            }
        }
        return null;
    }

    public void setupCommand(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {
        builder.then(
            Commands.literal("setup")
                .requires(e -> e.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(e -> {
                    var player = e.getSource().getPlayer();
                    if (player == null) {
                        throw NOT_ALLOWED_FROM_CONSOLE.create();
                    }
                    assertNotInGame(player, null);

                    var instance = createInstance();
                    for (U value : extraData.values()) {
                        value.createInstance((MinigameInstance<T, MinigameInstanceExtra<U, V>>) instance);
                    }
                    instance.addPlayer(player);
                    instances.add(instance);

                    return Command.SINGLE_SUCCESS;
                })
                .then(Commands.argument("player", EntityArgument.player())
                    .requires(e -> e.hasPermission(Commands.LEVEL_ADMINS))
                    .executes(e -> {
                        var player = EntityArgument.getPlayer(e, "player");
                        assertNotInGame(player, null);

                        var instance = createInstance();
                        for (U value : extraData.values()) {
                            value.createInstance((MinigameInstance<T, MinigameInstanceExtra<U, V>>) instance);
                        }
                        instance.addPlayer(player);
                        instances.add(instance);

                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
    }

    public void addPlayerCommand(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {
        builder.then(
            Commands.literal("add")
                .requires(e -> e.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.argument("players", EntityArgument.players())
                    .executes(e -> {
                        var target = e.getSource().getPlayer();
                        if (target == null) {
                            throw NOT_ALLOWED_FROM_CONSOLE.create();
                        }
                        var players = EntityArgument.getPlayers(e, "players");
                        var instance = findGameFor(target);
                        for (var p : players) {
                            assertNotInGame(p, instance);
                        }
                        if (instance == null) {
                            throw PLAYER_NOT_IN_GAME.create(target.getGameProfile().getName());
                        }
                        for (var p : players) {
                            instance.addPlayer(p);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                ).then(Commands.argument("target", EntityArgument.player())
                    .requires(e -> e.hasPermission(Commands.LEVEL_ADMINS))
                    .executes(e -> {
                        var target = EntityArgument.getPlayer(e, "target");
                        var instance = findGameFor(target);
                        if (instance == null) {
                            throw PLAYER_NOT_IN_GAME.create(target.getGameProfile().getName());
                        }
                        var players = EntityArgument.getPlayers(e, "players");
                        for (var p : players) {
                            assertNotInGame(p, instance);
                        }
                        for (var p : players) {
                            instance.addPlayer(p);
                        }
                        return Command.SINGLE_SUCCESS;
                    })
                )
        );
    }

    public void globalConfigCommand(CommandDispatcher<CommandSourceStack> dispatcher, ArgumentBuilder<CommandSourceStack, ?> builder, CommandBuildContext context) {
        var config = Commands.literal("global").requires(e -> e.hasPermission(Commands.LEVEL_GAMEMASTERS));
        BrigadierConnector.register(this, config, a -> {}, a -> {});
        builder.then(config);
    }

}
