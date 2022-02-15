package net.runelite.client.plugins.oneclickblastfurnace;

import lombok.Getter;
import net.runelite.api.ItemID;
import net.runelite.api.Varbits;

@Getter
public enum OneClickBlastFurnaceTypes {
    IRON(0, ItemID.IRON_ORE,ItemID.IRON_BAR),
    STEEL(1,ItemID.IRON_ORE,ItemID.STEEL_BAR),
    SILVER(0,ItemID.SILVER_ORE,ItemID.SILVER_BAR),
    GOLD(0,ItemID.GOLD_ORE,ItemID.GOLD_BAR),
    MITHRIL(2,ItemID.MITHRIL_ORE,ItemID.MITHRIL_BAR),
    ADAMANTITE(3,ItemID.ADAMANTITE_ORE,ItemID.ADAMANTITE_BAR),
    RUNITE(4,ItemID.RUNITE_ORE,ItemID.RUNITE_BAR);


    private final int coal;
    private final int oreID;
    private final int barID;


    OneClickBlastFurnaceTypes(int coal,int oreID,int barID) {
        this.coal = coal;
        this.oreID = oreID;
        this.barID = barID;
    }
}
