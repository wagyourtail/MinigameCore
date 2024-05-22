package xyz.wagyourtail.minigamecore.minigame.extra;

import com.mojang.brigadier.arguments.BoolArgumentType;
import net.minecraft.server.level.ServerPlayer;
import xyz.wagyourtail.minigamecore.minigame.MinigameInstance;
import xyz.wagyourtail.uniconfig.Group;
import xyz.wagyourtail.uniconfig.Setting;
import xyz.wagyourtail.uniconfig.connector.brigadier.BrigadierConnector;

public abstract class MinigameInstanceExtra<T extends MinigameExtra<?, ?, ?, ?>, U extends MinigameInstance<?, ?>> extends Group {
    protected final U instance;

    public final Setting<Boolean> enabled = setting("enabled", false, s -> {
        BrigadierConnector.connect(s, BrigadierConnector.value(BoolArgumentType.bool()));
    });

    public MinigameInstanceExtra(T extra, U instance) {
        super(extra.name, instance);
        this.instance = instance;
    }

    public void preAddPlayer(ServerPlayer player) {}

    public void postAddPlayer(ServerPlayer player) {}

    public void preRemovePlayer(ServerPlayer player) {}

    public void postRemovePlayer(ServerPlayer player) {}

    public void preSetup() {}

    public void postSetup() {}

    public void preStart() {}

    public void postStart() {}

    public void preStop(String stopReason) {}

    public void postStop(String stopReason) {}

}
