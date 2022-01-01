/*BSD 2-Clause License

        Copyright (c) 2016-2017, Adam <Adam@sigterm.info>
        All rights reserved.

        Redistribution and use in source and binary forms, with or without
        modification, are permitted provided that the following conditions are met:

        * Redistributions of source code must retain the above copyright notice, this
        list of conditions and the following disclaimer.

        * Redistributions in binary form must reproduce the above copyright notice,
        this list of conditions and the following disclaimer in the documentation
        and/or other materials provided with the distribution.

        THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
        AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
        IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
        DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
        FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
        DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
        SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
        CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
        OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
        OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/

package net.runelite.client.plugins.objecthider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;

@Extension
@PluginDescriptor(
        name = "Object Hider",
        description = "Hides objects from the game based on ID, modified from adams Fossil Island plugin. If you wish to unhide an object you must restart the plugin",
        enabledByDefault = false
)
@Slf4j
public class ObjectHiderPlugin extends Plugin {
    @Provides
    ObjectHiderConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(ObjectHiderConfig.class);
    }

    @Inject
    private ObjectHiderConfig config;

    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Subscribe
    public void onConfigChanged(ConfigChanged change) {
        hide();
    }

    @Override
    protected void startUp() {
        if (client.getGameState() == GameState.LOGGED_IN) {
            hide();
        }
    }

    @Override
    protected void shutDown() {
        clientThread.invoke(() ->
        {
            if (client.getGameState() == GameState.LOGGED_IN) {
                client.setGameState(GameState.LOADING);
            }
        });
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            hide();
        }
    }

    private void hide() {
        String OBJECT_ID_STRING = null;
        List<Integer> objectIdIntList = new ArrayList<Integer>();

        try {
            OBJECT_ID_STRING = config.objectIdsSet();
            String[] objectIds = OBJECT_ID_STRING.split("\\s*,\\s*");
            for (String i : objectIds) objectIdIntList.add(Integer.valueOf(i));
        } catch (Exception ignored) {
        }

        if (config.FossilIsland()) {
            objectIdIntList.addAll(Arrays.asList(30822, // Small white mushrooms A
                    30825, // Small white mushrooms B
                    30799, // Small yellow mushrooms A
                    30823, // smallRedMushroomsA
                    30824, // smallBlueMushroomsA
                    30828, // smallBlueMushroomsB
                    30829, // smallBlueMushroomsC
                    30830, // smallRedPlantsA
                    30831, // smallRedPlantsB
                    30826,  // smallFlowerA
                    30827, // smallFlowerB
                    30834, // mediumYellowMushroomA
                    30835, // mediumBlueMushroomA
                    30836, // mediumRedMushroomA
                    30832, // mediumRedPlantA
                    30840)); // mediumRedPlantB));
        }

        if (config.ZeahRunecrafting()) {
            objectIdIntList.addAll(Arrays.asList(34627, //various rocks/crystals throughout zeah
                    27881,
                    27884,
                    27885,
                    27901,
                    27885,
                    27888,
                    27899,
                    34266,
                    27879,
                    27883,
                    27886,
                    27882,
                    27878,
                    27878,
                    27910,
                    27898,
                    27902,
                    27908,
                    27887,
                    27904,
                    27906,
                    27912,
                    27902,
                    27904,
                    27900,
                    27906,
                    27903,
                    27907,
                    27905,
                    27911,
                    27913,
                    11428,
                    11431,
                    12593,
                    12594,
                    11435,
                    11434,
                    34626,
                    22495,
                    11921,
                    22495,
                    11427, //end of rocks/crystals
                    23100, //mystery fog under soul altar
                    27954, //hides huge fucking house
                    27955,
                    27956,
                    27957,
                    27958,
                    27959,
                    27960,
                    27961,
                    27962,
                    27963,
                    27964,
                    27965,
                    27966,
                    27967,
                    27968,
                    27969,
                    27970,
                    27971,
                    27972,
                    27973,
                    27974,
                    27975,
                    27976,
                    27977)); //end of house
        }

        if (config.AbyssalDemons()) {objectIdIntList.add(28871);}

        if (config.SotetsegWall()) {objectIdIntList.addAll(Arrays.asList(33040,33041,33042,33043,33044,33045,33046,33047,33048,33049,33050,33051,33052,33053,33054,33055,33056,33057,33058,33059));}

            Scene scene = client.getScene();
            Tile[][] tiles = scene.getTiles()[client.getPlane()];
            Tile[][] tiles1 = scene.getTiles()[1]; //get tiles on plane[1]
            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
                for (int y = 0; y < Constants.SCENE_SIZE; ++y) {

                    Tile tile = tiles[x][y];
                    Tile tile1 = tiles1[x][y];

                    if (tile == null || tile1 == null) {
                        continue;
                    }

                    for (GameObject gameObject : tile.getGameObjects()) {
                        if (gameObject != null && objectIdIntList.contains(gameObject.getId())) {
                            scene.removeGameObject(gameObject);
                            break;
                        }
                    }
                    for (GameObject gameObject : tile1.getGameObjects()) //iterate through tiles on plane[1] needed for hiding abyssal demon bridge
                    {
                        if (gameObject != null && objectIdIntList.contains(gameObject.getId())) {
                            scene.removeGameObject(gameObject);
                            break;
                        }
                    }
                }
            }


            for (int x = 0; x < Constants.SCENE_SIZE; ++x) {
                for (int y = 0; y < Constants.SCENE_SIZE; ++y) {
                    Tile tile = tiles[x][y];
                    if (tile == null) {
                        continue;
                    }

                    for (GameObject gameObject : tile.getGameObjects()) {
                        if (gameObject != null && objectIdIntList.contains(gameObject.getId())) {
                            scene.removeGameObject(gameObject);
                            break;
                        }
                    }
                }
            }

    }
}

//figure out a way to reset without having to restart
//figure a way to hide on entry rather than on moving in game
//clean up the repeating code for multiple planes