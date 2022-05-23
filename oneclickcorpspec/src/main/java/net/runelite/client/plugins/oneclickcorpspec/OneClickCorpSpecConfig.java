package net.runelite.client.plugins.oneclickcorpspec;

import net.runelite.client.config.*;

@ConfigGroup("oneclickcorpspec")
public interface OneClickCorpSpecConfig extends Config {

    @ConfigItem(
            position = 0,
            keyName = "emergencyTele",
            name = "Emergency Tele",
            description = "Hitpoints to tele at even while speccing"
    )
    @Range(max = 100)
    default int emergencyTele(){return 0;}

    @ConfigItem(
            position = 1,
            keyName = "hammerHits",
            name = "DWH",
            description = "How many hammers"
    )
    default int hammerHits(){return 3;}

    @ConfigItem(
            position = 2,
            keyName = "arclightHits",
            name = "Arclight",
            description = "How many arclights"
    )
    default int arclightHits(){return 7;}

    @ConfigItem(
            position = 3,
            keyName = "godswordDamage",
            name = "BGS",
            description = "How much BGS damage"
    )
    default int godswordDamage(){return 400;}
}