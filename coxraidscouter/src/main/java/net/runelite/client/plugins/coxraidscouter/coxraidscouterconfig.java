package net.runelite.client.plugins.coxraidscouter;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("coxraidscouter")
public interface coxraidscouterconfig extends Config {
    @ConfigItem(
            position = 0,
            keyName = "requireRope",
            name = "Require rope",
            description = "RequireRope"
    )
    default boolean requireRope() {
        return true;
    }

    @ConfigItem(
            position = 1,
            keyName = "requireoverload",
            name = "Require Overload",
            description = "Disable if you want a raid without overloads"
    )
    default boolean requireOverload() {
        return true;
    }

    @ConfigItem(
            position = 2,
            keyName = "requireGoodCrabs",
            name = "Good Crabs",
            description = "Scouts for good crabs only(If crabs in raid)"
    )
    default boolean requireGoodCrabs() {
        return true;
    }

    @ConfigItem(
            position = 3,
            keyName = "autoLeaveCC",
            name = "Auto Leave/Rejoin CC",
            description = "Automatically Leaves and rejoins cc if someone enters raid(Make sure to not start raid until bot has rejoined)"
    )
    default boolean autoLeaveCC() {
        return true;
    }

    @ConfigItem(
            position = 4,
            keyName = "desiredRotationsToggle",
            name = "Enable Desired Rotations",
            description = "Enable Desired Rotations"
    )
    default boolean desiredRotationsToggle() {
        return true;
    }

    @ConfigItem(
            position = 5,
            keyName = "desiredRotations",
            name = "Desired Rotations",
            description = "Input Desired rotations, formatted e.g [Tekton,Vasa,Guardians],[Vasa,Tekton,Vespula]"
    )
    default String desiredRotations() {
        return "";
    }

    @ConfigItem(
            position = 6,
            keyName = "blacklistedRooms",
            name = "Blacklisted Rooms",
            description = "Blacklisted rooms, separate with comma"
    )
    default String blacklistedRooms() {
        return "Unknown (puzzle),Unknown (combat),Ice Demon,Vanguards";
    }

    @ConfigItem(
            position = 7,
            keyName = "webhook",
            name = "Webhook",
            description = "Add Webhook URL"
    )
    default String webhook() {
        return "";
    }

    @ConfigItem(
            position = 8,
            keyName = "Notify",
            name = "Notify on raid found",
            description = "Sends system notification when a raid is found if enabled."
    )
    default boolean Notify() {
        return true;
    }

    @ConfigItem(
            position = 9,
            keyName = "debugScouting",
            name = "Debug Scouting",
            description = "Displays reason for leaving raid in chat if unnacceptable"
    )
    default boolean debugScouting() {
        return true;
    }

    @ConfigItem(
            position = 10,
            keyName = "5hHandler",
            name = "5h Handler",
            description = "Clicks through the 5h runescape login timer warning"
    )
    default boolean fivehHandler() {
        return true;
    }
}