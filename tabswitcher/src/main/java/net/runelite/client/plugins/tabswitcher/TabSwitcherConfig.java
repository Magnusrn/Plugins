package net.runelite.client.plugins.tabswitcher;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Keybind;

@ConfigGroup("tabswitcher")
public interface TabSwitcherConfig extends Config {
    @ConfigItem(
            keyName = "keybind",
            name = "Keybind",
            description = "Press to cycle between tabs",
            position = 0
    )
    default Keybind keybind() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "hardcodedtabs",
            name = "Hardcoded Tabs",
            description = "Only cycle between inputted tabs",
            position = 1
    )
    default boolean hardCodeTabs() {
        return false;
    }

    @ConfigItem(
            keyName = "tab1",
            name = "Tab1",
            description = "First tab to cycle between",
            position = 2
    )
    default int tab1() {
        return 2;
    }

    @ConfigItem(
            keyName = "tab2",
            name = "Tab2",
            description = "Second tab to cycle between",
            position = 3
    )
    default int tab2() {
        return 3;
    }



}
