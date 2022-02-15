package net.runelite.client.plugins.oneclickblastfurnace;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("oneclickblastfurnace")
public interface OneClickBlastFurnaceConfig extends Config {

    @ConfigItem(
            keyName = "bartype",
            name = "Bar Type",
            description = "Choose which bar to smelt",
            position = 0
    )
    default OneClickBlastFurnaceTypes barType() {
        return OneClickBlastFurnaceTypes.STEEL;
    }



}