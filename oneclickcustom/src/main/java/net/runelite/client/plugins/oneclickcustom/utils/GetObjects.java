package net.runelite.client.plugins.oneclickcustom.utils;

import net.runelite.api.*;
import net.runelite.api.queries.GameObjectQuery;
import net.runelite.api.queries.NPCQuery;
import net.runelite.client.plugins.oneclickcustom.oneClickCustomConfig;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class GetObjects {

    @Inject
    private Client client;

    @Inject
    private oneClickCustomConfig config;

    public <T extends Locatable> Object getNearestObject(Stream<T> stream) {
        return stream.filter(x -> x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())<config.withinTiles())
                .min(Comparator.comparing(x -> x.getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())))
                .orElse(null);
    }

    public GameObject getGameObject(List<Integer> ids) {
        return (GameObject) getNearestObject(new GameObjectQuery()
                .idEquals(ids)
                .result(client)
                .stream());
    }
    public GameObject getGameObject(int id) {
        return (GameObject) getNearestObject(new GameObjectQuery()
                .idEquals(id)
                .result(client)
                .stream());
    }

    public NPC getNpc(List<Integer> ids) {
        return (NPC) getNearestObject(new NPCQuery()
                .idEquals(ids)
                .result(client)
                .stream());
    }

    public NPC getNpc(int ids) {
        return (NPC) getNearestObject(new NPCQuery()
                .idEquals(ids)
                .result(client)
                .stream());
    }

    public NPC getNearestAliveNPC(List<Integer> ids) {
        ArrayList<NPC> npcs = new NPCQuery()
                .idEquals(ids)
                .result(client)
                .list;
        return (NPC) getNearestObject(npcs.stream());
    }

    public TileItem getNearestTileItem(List<TileItem> tileItems) {
        if (tileItems.size()==0 || tileItems.get(0) == null) return null;
        return tileItems.stream()
                .filter(tileItem -> tileItem.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())<config.withinTiles())
                .min(Comparator.comparing(tileItem -> tileItem.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation())))
                .orElse(null);
    }
}
