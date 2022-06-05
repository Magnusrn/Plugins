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
            name = "Xarpus",
            description = "Xarpus options",
            position = 0,
            closedByDefault = true
    )
    String Xarpus = "Xarpus";

    @ConfigItem(
            keyName = "Maiden Freezer",
            name = "Maiden Freezer",
            description = "Automatically cast ice barrage on the optimal crab if holding keybind and clicking maiden",
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
            description = "Stops you from attacking xarpus if try to while he's looking",
            section = Xarpus,
            position = 0
    )
    default boolean xarpusWheelchair() {
        return false;
    }

    @ConfigItem(
            keyName = "Xarpus Wheelchair Weapon Cooldown",
            name = "Xarpus Wheelchair Weapon Cooldown",
            description = "Only prevent attacking if Xarpus is looking at you while weapon is off cooldown(Scythe supported)",
            section = Xarpus,
            position = 1
    )
    default boolean xarpusWheelchairWeaponCooldown() {
        return false;
    }

    @ConfigItem(
            keyName = "Xarpus Wheelchair Click Floor",
            name = "Xarpus Wheelchair Click Floor",
            description = "If enabled will click the floor to stop any future attacks while using xarpus wheelchair, else click is consumed.",
            section = Xarpus,
            position = 2
    )
    default boolean xarpusWheelchairClickFloor() {
        return false;
    }


}