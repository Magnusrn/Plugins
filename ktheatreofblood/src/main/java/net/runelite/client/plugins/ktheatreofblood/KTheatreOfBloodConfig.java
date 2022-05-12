package net.runelite.client.plugins.ktheatreofblood;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("ktheatreofblood")
public interface KTheatreOfBloodConfig extends Config
{
    @ConfigItem(
            keyName = "keybind",
            name = "Keybind for Maiden Freeze",
            description = "",
            position = 0
    )
    default Keybind maidenKeybind() {
        return Keybind.NOT_SET;
    }
}