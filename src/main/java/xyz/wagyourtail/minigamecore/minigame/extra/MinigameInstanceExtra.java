package xyz.wagyourtail.minigamecore.minigame.extra;

import xyz.wagyourtail.minigamecore.minigame.MinigameInstance;

public class MinigameExtra<T extends MinigameInstance<?>> {
    protected final T instance;

    public MinigameExtra(T instance) {
        this.instance = instance;
    }

    public void preStart() {

    }

    public void postStart() {

    }

    public void preStop(String stopReason) {

    }

    public void postStop(String stopReason) {

    }

}
