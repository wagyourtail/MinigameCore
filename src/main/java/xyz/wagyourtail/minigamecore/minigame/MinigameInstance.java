package xyz.wagyourtail.minigamecore.minigame;

import com.google.common.collect.ClassToInstanceMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import xyz.wagyourtail.minigamecore.minigame.extra.MinigameInstanceExtra;
import xyz.wagyourtail.uniconfig.UniConfig;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierSettingConnector;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public abstract class MinigameInstance<M extends Minigame<?, ?, ?>, T extends MinigameInstanceExtra<?, ?>> extends UniConfig {
    private final MinecraftServer server;
    final Set<String> players = new HashSet<>();
    private boolean started = false;
    protected final M minigame;

    private ClassToInstanceMap<T> extraData;


    public MinigameInstance(M minigame, MinecraftServer server, ClassToInstanceMap<T> extraData) {
        super(minigame.name);
        requireConnector(BrigadierSettingConnector.class);
        this.minigame = minigame;
        this.server = server;
        this.extraData = extraData;
    }

    public MinecraftServer getServer() {
        return server;
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * add player to game, possibly late.
     */
    public final void addPlayer(ServerPlayer player) {
        if (players.contains(player.getGameProfile().getName())) {
            return;
        }
        for (T value : extraData.values()) {
            value.preAddPlayer(player);
        }
        players.add(player.getGameProfile().getName());
        addPlayerIntl(player);
        for (T value : extraData.values()) {
            value.postAddPlayer(player);
        }
    }

    public final void removePlayer(ServerPlayer player) {
        for (T value : extraData.values()) {
            value.preRemovePlayer(player);
        }
        players.remove(player.getGameProfile().getName());
        removePlayerIntl(player);
        for (T value : extraData.values()) {
            value.postRemovePlayer(player);
        }
    }

    protected abstract void addPlayerIntl(ServerPlayer player);

    protected abstract void removePlayerIntl(ServerPlayer player);


    @SuppressWarnings({"unchecked", "rawtypes"})
    public Optional<T> getExtra(Class<?> clazz) {
        return Optional.ofNullable((T) ((ClassToInstanceMap) extraData).getInstance(clazz));
    }

    public final void setup() {
        if (started) {
            throw new IllegalStateException("Already started");
        }
        for (T value : extraData.values()) {
            value.preSetup();
        }
        setupIntl();
        for (T value : extraData.values()) {
            value.postSetup();
        }
    }

    protected abstract void setupIntl();

    public final void start(ClassToInstanceMap<T> extraData) {
        if (started) {
            throw new IllegalStateException("Already started");
        }
        this.extraData = extraData;
        for (T value : extraData.values()) {
            value.preStart();
        }
        started = true;
        startIntl();
        for (T value : extraData.values()) {
            value.postStart();
        }
    }

    protected abstract void startIntl();

    public final void stop(String stopReason) {
        for (T value : extraData.values()) {
            value.preStop(stopReason);
        }
        stopIntl(stopReason);
        minigame.instances.remove(this);
        for (T value : extraData.values()) {
            value.postStop(stopReason);
        }
    }

    protected abstract void stopIntl(String stopReason);


}
