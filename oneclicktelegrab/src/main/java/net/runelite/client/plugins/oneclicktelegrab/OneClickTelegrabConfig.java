package net.runelite.client.plugins.oneclicktelegrab;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclicktelegrab")
public interface OneClickTelegrabConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "TrueOneClick",
            name = "Click anywhere on screen",
            description = "If this is active you can click anywhere to telegrab, else click on the wine"
    )
    default boolean TrueOneClick() {
        return false;
    }

    @ConfigItem(
            position = 1,
            keyName = "ConsumeClicks",
            name = "Consume clicks",
            description = "Consume clicks if Wine of zamorak not visible"
    )
    default boolean ConsumeClicks() {
        return false;
    }
}
