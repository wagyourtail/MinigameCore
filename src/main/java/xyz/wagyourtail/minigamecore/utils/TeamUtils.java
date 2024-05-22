package xyz.wagyourtail.minigamecore.utils;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.List;

public class TeamUtils {

    private TeamUtils() {
    }

    /**
     * Assign players to teams, round-robin style.
     * if you want random teams, shuffle the player list first.
     */
    public static void autoTeam(List<ServerPlayer> players, Collection<PlayerTeam> teams) {
        var playerList = new ArrayDeque<>(players);

        while (!playerList.isEmpty()) {
            for (PlayerTeam team : teams) {
                if (playerList.isEmpty()) {
                    return;
                }
                var player = playerList.poll();
                player.server.getScoreboard().addPlayerToTeam(player.getGameProfile().getName(), team);
            }
        }
    }

}
