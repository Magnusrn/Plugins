package net.runelite.client.plugins.ktheatreofblood;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public abstract class Room {
    protected final KTheatreOfBloodPlugin plugin;
    protected final KTheatreOfBloodConfig config;

    @Inject
    protected Room(KTheatreOfBloodPlugin plugin, KTheatreOfBloodConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public void init() {
    }

    public void load() {
    }

    public void unload() {
    }

    protected void startUp() throws Exception {
    }
}