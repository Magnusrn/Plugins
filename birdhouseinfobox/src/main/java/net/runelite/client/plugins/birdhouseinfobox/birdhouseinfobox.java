package net.runelite.client.plugins.birdhouseinfobox;

import java.awt.Color;
import java.awt.image.BufferedImage;

import lombok.Getter;
import net.runelite.client.plugins.cannon.CannonPlugin;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxPriority;

@Getter
class birdhouseinfobox extends InfoBox
{

    private final birdhouseplugin plugin;

    birdhouseinfobox(BufferedImage image, birdhouseplugin plugin)
    {
        super(image, plugin);
        this.plugin = plugin;
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