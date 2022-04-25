package net.runelite.client.plugins.ktheatreofblood;

import javax.inject.Inject;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.ktheatreofblood.rooms.Maiden.Maiden;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "K Theatre Of Blood",
        description = "Various OP features for Theatre of Blood",
        enabledByDefault = false
)
@Slf4j
public class KTheatreOfBloodPlugin extends Plugin {
    private Room[] rooms = null;

    @Inject
    private Maiden maiden;

    @Inject
    private KTheatreOfBloodConfig config;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private EventBus eventBus;

    @Provides
    KTheatreOfBloodConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(KTheatreOfBloodConfig.class);
    }

    protected void startUp() {
        if (rooms == null) {
            rooms = new Room[]{maiden};

            for (Room room : rooms) {
                room.init();
            }
        }
        for (Room room : rooms) {
            room.load();
            eventBus.register(room);
        }
    }

    protected void shutDown() {
        for (Room room : rooms) {
            eventBus.unregister(room);
            room.unload();
        }

    }
}