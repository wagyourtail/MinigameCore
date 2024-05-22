package xyz.wagyourtail.minigamecore;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import xyz.wagyourtail.minigamecore.minigame.Minigame;
import xyz.wagyourtail.minigamecore.utils.TeamUtils;
import xyz.wagyourtail.minigamecore.utils.TranslationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

public class CoreCommands {
    private static final SimpleCommandExceptionType NOT_ENOUGH_TEAMS = new SimpleCommandExceptionType(TranslationUtils.translatable("minigamecore.not_enough_teams"));

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context) {
        dispatcher.register(Commands.literal("minigame")
            .then(Commands.literal("list").executes(CoreCommands::listMinigames))
            .then(Commands.literal("autoteam").executes(ctx -> autoTeam(ctx, null, 0))
                .then(Commands.argument("teams", IntegerArgumentType.integer(0))
                    .executes(ctx -> autoTeam(ctx, null, IntegerArgumentType.getInteger(ctx, "teams")))
                    .then(Commands.argument("players", EntityArgument.players())
                        .executes(ctx -> autoTeam(ctx, EntityArgument.getPlayers(ctx, "players"), IntegerArgumentType.getInteger(ctx, "teams")))
                    )
                )
            )
        );
    }

    private static int listMinigames(CommandContext<CommandSourceStack> context) {
        for (Minigame<?, ?, ?> value : MinigameCore.minigames.values()) {
            context.getSource().sendSuccess(() -> Component.literal(value.name), false);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int autoTeam(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, int count) throws CommandSyntaxException {
        var server = context.getSource().getServer();
        if (players == null) {
            players = server.getPlayerList().getPlayers();
        }

        var teams = new HashSet<>(server.getScoreboard().getPlayerTeams());

        // assign players to unused teams
        if (teams.size() < count) {
            throw NOT_ENOUGH_TEAMS.create();
        }

        var teamList = new ArrayList<>(teams);

        // randomize players
        var playerList = new ArrayList<>(players);
        Collections.shuffle(playerList);

        TeamUtils.autoTeam(playerList, teamList.subList(0, count));

        return Command.SINGLE_SUCCESS;
    }

}
