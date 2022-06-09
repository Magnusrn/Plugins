package net.runelite.client.plugins.ktheatreofblood;

import net.runelite.client.config.*;

@ConfigGroup("ktheatreofblood")
public interface KTheatreOfBloodConfig extends Config {
    @ConfigSection(
            name = "Maiden",
            description = "Maiden options",
            position = 0,
            closedByDefault = true
    )
    String Maiden = "Maiden";
    @ConfigSection(
            name = "Bloat",
            description = "Bloat options",
            position = 1,
            closedByDefault = true
    )
    String Bloat = "Bloat";
    @ConfigSection(
            name = "Xarpus",
            description = "Xarpus options",
            position = 3,
            closedByDefault = true
    )
    String Xarpus = "Xarpus";

    @ConfigItem(
            keyName = "Maiden Freezer",
            name = "Maiden Freezer",
            description = "Automatically cast ice barrage on the initial optimal crab(S1>N1>N2>S2) if holding keybind and clicking maiden",
            section = Maiden,
            position = 0
    )
    default boolean maidenFreezer() {
        return false;
    }

    @ConfigItem(
            keyName = "keybind",
            name = "Keybind for Maiden Freeze",
            description = "",
            section = Maiden,
            position = 1
    )
    default Keybind maidenKeybind() {
        return Keybind.NOT_SET;
    }

    @ConfigItem(
            keyName = "Xarpus Wheelchair",
            name = "Xarpus Wheelchair",
            description = "Stops you from attacking xarpus if you try to while he's looking at you",
            section = Xarpus,
            position = 0
    )
    default boolean xarpusWheelchair() {
        return false;
    }


    @ConfigItem(
            keyName = "Phoenix Necklace Helper",
            name = "Phoenix Necklace Helper",
            description = "Equips Phoenix Necklace if one exists in your inventory while attacking bloat below certain threshold, while bloat is up",
            section = Bloat,
            position = 0
    )
    default boolean PneckHelper() {
        return false;
    }

    @Range(max = 100)
    @ConfigItem(
            keyName = "Phoenix Necklace Helper Threshold",
            name = "Phoenix Necklace Helper Threshold",
            description = "Hp to start equipping necks below",
            section = Bloat,
            position = 1
    )
    default int PneckHelperThreshold() {
        return 50;
    }
}