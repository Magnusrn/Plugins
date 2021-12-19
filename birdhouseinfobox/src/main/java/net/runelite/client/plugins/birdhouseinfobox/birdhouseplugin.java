package net.runelite.client.plugins.birdhouseinfobox;

import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.ItemID;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Extension
@PluginDescriptor(
        name = "Birdhouse Infobox",
        description = "birdhouse infobox",
        tags = {"birdhouse","timer","infobox"}
)
public class birdhouseplugin extends Plugin {
    private static final Logger log = LoggerFactory.getLogger(birdhouseplugin.class);
    private birdhouseinfobox BHIB;
    public static int timeRemaining = 5200;
    @Inject
    private Client client;
    @Inject
    private InfoBoxManager infoBoxManager;

    @Inject
    private ItemManager itemManager;

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (client.getLocalPlayer()==null)
        {
            return;
        }
        if (this.client.getLocalPlayer().getAnimation() == 7057) {
            this.reset();
        }
    }

    protected void shutDown() throws Exception {
        this.reset();
    }

    @Subscribe
    public void onGameTick(GameTick tick) {
        if (this.BHIB != null) {
            this.infoBoxManager.removeInfoBox(this.BHIB);
            this.BHIB = null;
        }

        if (timeRemaining > 0) {
            --timeRemaining;
        }

        BHIB = new birdhouseinfobox(itemManager.getImage(ItemID.BIRD_HOUSE), this);
        this.infoBoxManager.addInfoBox(this.BHIB);
    }

    private void reset() {
        this.infoBoxManager.removeInfoBox(this.BHIB);
        this.BHIB = null;
        timeRemaining = 5200;
    }
}