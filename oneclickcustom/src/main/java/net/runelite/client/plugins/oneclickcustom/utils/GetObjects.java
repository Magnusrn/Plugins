package net.runelite.client.plugins.oneclickcustom.utils;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.api.TileItem;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GetObjects {

    @Inject
    private Client client;

    public GameObject getGameObject(List<Integer> ids)
    {
        return new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }
    public GameObject getGameObject(int id)
    {
        return new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public NPC getNpc(List<Integer> ids)
    {
        return new NPCQuery()
                .idEquals(ids)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public NPC getNpc(int... id)
    {
        return new NPCQuery()
                .idEquals(id)
                .result(client)
                .nearestTo(client.getLocalPlayer());
    }

    public NPC getNearestAliveNPC(List<Integer> ids) {
        ArrayList<NPC> npcs = new NPCQuery()
                .idEquals(ids)
                .result(client)
                .list;
        return npcs.stream()
                .filter(npc -> npc.getHealthRatio()>0)
                .min(Comparator.comparing(npc -> npc.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation())))
                .orElse(null);
    }

    public TileItem getNearestTileItem(List<TileItem> tileItems) {
        if (tileItems.size()==0 || tileItems.get(0) == null) return null;

        return tileItems.stream().min(Comparator.comparing(tileItem -> tileItem.getTile().getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation())))
                .orElse(null);
    }
}
