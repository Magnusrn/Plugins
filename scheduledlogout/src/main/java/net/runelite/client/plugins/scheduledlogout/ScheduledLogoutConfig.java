package net.runelite.client.plugins.scheduledlogout;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("scheduledlogout")
public interface ScheduledLogoutConfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "minutestologout",
            name = "Minutes Until Logout",
            description = "Automatically logout after desired minutes"
    )
    default int minutesToLogout()
    {
        return 60;
    }

    @ConfigItem(
            position = 1,
            keyName = "overlay",
            name = "Overlay",
            description = ""
    )
    default boolean overlay()
    {
        return true;
    }
}
