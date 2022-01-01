/*
 * Copyright (c) 2018, Tomas Slusny <slusnucky@gmail.com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.objecthider;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("objecthider")
public interface ObjectHiderConfig extends Config
{
    @ConfigItem(
            keyName = "ObjectConfigData",
            name = "Object ID's",
            description = "List Desired object ID'S to be hidden, separated by a comma. **If you wish to unhide an object you must restart the plugin**",
            position = 1
    )
    default String objectIdsSet()
    {
        return "";
    }

    @ConfigItem(
            position = 2,
            keyName = "FossilIsland",
            name = "Fossil Island",
            description = "Hides various game objects whilst on Fossil Island"
    )
    default boolean FossilIsland()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName ="ZeahRunecrafting" ,
            name = "Zeah Runecrafting",
            description = "Hides various game objects whilst Zeah Runecrafting"
    )
    default boolean ZeahRunecrafting()
    {
        return false;
    }

    @ConfigItem(
            position = 3,
            keyName ="AbyssalDemons" ,
            name = "Abyssal Demons",
            description = "Hides the annoying bridge at Abyssal Demons in the Catacombs of Kourend"
    )
    default boolean AbyssalDemons()
    {
        return false;
    }

    @ConfigItem(
            position = 4,
            keyName ="SotetsegWall" ,
            name = "Sotetseg Wall",
            description = "Hides the wall behind Sotetseg"
    )
    default boolean SotetsegWall()
    {
        return false;
    }

}