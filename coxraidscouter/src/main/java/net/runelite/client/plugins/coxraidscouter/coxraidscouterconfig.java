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
            position = 8,
            keyName = "SendLayoutToCC",
            name = "Send layout to cc",
            description = "Sends the layout to cc when a raid is found."
    )
    default boolean SendLayoutToCC()
    {
        return false;
    }

    @ConfigItem(
        position = 8,
        keyName = "RespondToLayoutRequest",
        name = "Respond to layout request",
        description = "Sends the layout to cc when someone types ?l in cc."
    )
    default boolean RespondToLayoutRequest()
    {
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

    @ConfigItem(
            position = 11,
            keyName = "timeout",
            name = "Start raid/Read board Timeout",
            description = "Increase this if the timeout is causing problems. Works fine for me on 2t but in case of lag I left the option in. Defaults to 3t"
    )
    default int timeout() {
        return 3;
    }

    @ConfigItem(
            position = 12,
            keyName = "leavecctimeout",
            name = "Leave CC Timeout",
            description = "Timeout after leaving/rejoining CC on successful raid found. I Think this prevents a rare bug when you rejoin cc with 'raid started' message not showing while joining cc"
    )
    default int leaveCCTimeout() {
        return 10;
    }
}