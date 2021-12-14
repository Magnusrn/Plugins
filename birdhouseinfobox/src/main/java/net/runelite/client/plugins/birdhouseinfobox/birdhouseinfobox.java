package net.runelite.client.plugins.birdhouseinfobox;

import java.awt.Color;
import net.runelite.api.Client;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.util.ImageUtil;

public class birdhouseinfobox extends InfoBox {
    private birdhouseplugin plugin;
    private Client client;

    public birdhouseinfobox(Client client, birdhouseplugin plugin) {
        super(ImageUtil.getResourceStreamFromClass(birdhouseplugin.class, "birdhouse.png"), plugin);
        this.plugin = plugin;
        this.client = client;
    }

    public String getText() {
        return String.valueOf(birdhouseplugin.timeRemaining / 100);
    }

    public Color getTextColor() {
        return Color.WHITE;
    }

    public String getTooltip() {
        return "Time remaining: " + birdhouseplugin.timeRemaining / 100 + " minutes.";
    }
}