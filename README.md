|Plugin List(click for details) | Source |
|------------- |------------- |
| [Birdhouse Infobox](#birdhouse-infobox) | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/birdhouseinfobox/src/main/java/net/runelite/client/plugins/birdhouseinfobox) |
| [CoxRaidScouter](#coxraidscouter)  | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/coxraidscouter/src/main/java/net/runelite/client/plugins/coxraidscouter) |
| [Nex Red Click](#nex-red-click) |   [Click Here](https://github.com/Magnusrn/Plugins/tree/master/nexredclick/src/main/java/net/runelite/client/plugins/nexredclick) |
| [Object Hider](#object-hider)  | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/objecthider/src/main/java/net/runelite/client/plugins/objecthider) |
| [One Click Aerial Fishing](#one-click-aerial-fishing)| [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickaerialfishing/src/main/java/net/runelite/client/plugins/oneclickaerialfishing) |
| [One Click Amethyst](#one-click-amethyst)| [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickamethyst/src/main/java/net/runelite/client/plugins/oneclickamethyst) |
| [One Click Bloods](#one-click-bloods) | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickbloods/src/main/java/net/runelite/client/plugins/oneclickbloods) |
| [One Click Chins](#one-click-chins) | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickchins/src/main/java/net/runelite/client/plugins/oneclickchins) |
| [One Click Custom](#one-click-custom) | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickcustom/src/main/java/net/runelite/client/plugins/oneclickcustom) |
| [One Click Glassblowing](#one-click-glassblowing) |  [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickglassblowing/src/main/java/net/runelite/client/plugins/oneclickglassblowing) |
| [One Click Karambwans](#one-click-karambwans) | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickkarambwans/src/main/java/net/runelite/client/plugins/oneclickkarambwans) |
| [One Click Minnows](#one-click-minnows)  | [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickminnows/src/main/java/net/runelite/client/plugins/oneclickminnows) |
| [One Click Sandstone](#one-click-sandstone)|  [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclicksandstone/src/main/java/net/runelite/client/plugins/oneclicksandstone) |
| [One Click Swordfish](#one-click-swordfish)|   [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickswordfish/src/main/java/net/runelite/client/plugins/oneclickswordfish) |
| [One Click Telegrab](#one-click-telegrab) |  [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclicktelegrab/src/main/java/net/runelite/client/plugins/oneclicktelegrab) |
| [One Click ZMI](#one-click-zmi) |   [Click Here](https://github.com/Magnusrn/Plugins/tree/master/oneclickzmi/src/main/java/net/runelite/client/plugins/oneclickzmi) |
| [Scheduled Logout](#scheduled-logout) |   [Click Here](https://github.com/Magnusrn/Plugins/tree/master/scheduledlogout/src/main/java/net/runelite/client/plugins/scheduledlogout) |

### Birdhouse-Infobox
Starts a (fairly conservative) timer upon building a birdhouse, overlays an infobox

### CoxRaidScouter

Requires the "Chambers of Xeric" plugin "Raid Layout Message" option Toggled ON

SWIM ran this on 3 accounts for a little under 3 months(approx 2000 raids) two temps towards the end. However this is a commonly reported activity so would only recommend using on burner accounts. Works nicely if setup on a cloud VM and webhooked to discord. Only need to check once every 6 hours if using the auto leave/rejoin cc feature

Example Desired Rotations: [Tekton,Vasa,Guardians],[Muttadiles,Shamans,Mystics],[Guardians,Vasa,Tekton],[Vasa,Tekton,Vespula],[Vespula,Tekton,Vasa],[Mystics,Shamans,Muttadiles],[Tekton,Muttadiles,Guardians],[Guardians,Muttadiles,Tekton],[Muttadiles,Vespula,Guardians],[Vespula,Guardians,Muttadiles] 

Example Blacklisted Rooms: Unknown (puzzle),Unknown (combat),Ice Demon,Vanguards

<details>
  <summary>Change Log</summary>
  
v0.07 -
Added option to scout without Overload
  
V0.06 -
Removed dependancy on iutils but no longer sends clicks and randomness removed for now. Emphasis on only use burner accounts.

V0.05 -
Auto Leave/Rejoin CC no longer requires you to wait for the bot to rejoin

V0.04 -
Added Java webhook instead of external python file
Modified reset to be on logout instead of login due to sometimes leaving cc immediately upon login

V0.03 -
Added 5h login timer handling
Added reset on login(Prevents webhook posting raid taken on relog)
Added webhook message on logout

v0.02 -
Added Good Crabs detector
Added ability to input specific rotations rather than just blacklist
added os detection for python run command

v0.01 -
Moved webhook.py within plugins folder for ease of setup
Added layout to webhook (SCSPF e.g)
Added new embed for webhook
Added webhook message when raid is taken(user has started scouting again)
</details>

### Nex Red Click 
Remaps ctrl click on Nex to click door outside instance 

### Object Hider
Hides objects from the game based on ID, modified from adams Fossil Island plugin.

<details>
  <summary>Change Log</summary>
 
v0.04 - 
Added Sotetseg back wall and abyssal demon catacombs bridge to config  
  
v0.03 - 
remove obselete variable cnt 

v0.01 -
release 
</details>

### One Click Aerial Fishing
Molch Aerial fishing, will cut fish if you have a knife in your invent else drops fish.

### One Click Amethyst
Mines Amethyst and cuts into products. 

<details>
  <summary>Change Log</summary>
  
v0.03 - 
Updated to allow for banking if no chisel in inventory.

v0.01 -
release
</details>


### One Click Bloods
Initially requires the map to be loaded as a normal player would else it can get stuck. Currently uses a rather inelegant method of using a chisel on the cluster of rocks to get nearer to the altar, very open to suggestions on how to solve this as walking programatically seems very aids. SWIM has used for approx 100h without any detection.

<details>
  <summary>Change Log</summary>
  
v0.03 - 
Updated Inventory Full check to be on clienttick instead of menuoptionclicked. Added option to manual walk towards the altar. 

v0.01 -
release
</details>

### One Click Chins
Pretty simple plugin, requires initial setting of the traps and just resets them. Has the option to afk if another player is nearby as otherwise it will attempt to reset the other players traps if it's closest.

<details>
  <summary>Change Log</summary>
  
v0.03 - 
Updated to allow for grey/black chinchompas

v0.01 -
release
</details>

### One Click Custom
Input ID and choose your object type. This will find the nearest of that object as the crow flies and unfortunately not nearest by tiles.

<details>
  <summary>Change Log</summary>
  
v0.03 - 
Changed input to be list rather than single ID

v0.01 -
release
</details>

### One Click Glassblowing
Supports two banks, Clan hall and north of Fossil Island. Ensure you're in the main bank tab(Not sure why this is required)

### One Click Karambwans
Ensure last destination on fairy ring is DKP. Currently supports banking at zanaris only, works with or without Fish Barrel.

### One Click Minnows
Made this very quickly, haven't tested much, seems to do the job.

### One Click Sandstone
Ensure you have waterskins in your invent and humidify runes. If you run out of water it will cast humidify. Should add a check or toggle for this but if you run out and you don't have humidify you're dead soon anyway.

### One Click Swordfish
Use Pajeets One Click 2 tick instead. Does the same thing I think but his has Teaks also.

### One Click Telegrab
Supports telegrabbing Wine of Zamorak at the wildy spot or the safe chaos altar.

### One Click ZMI
This is state based so must be started near the banker at ZMI. Ensure you have 1 dose staminas in the bank and are in the main tab of the bank with the withdraw quantity set to 1. Ensure Last NPC contact was to dark mage as it will repair broken pouches. Only supports Medium/Medium + Large/Medium + Large + Giant pouches.

<details>
  <summary>Change Log</summary>
  
v0.04 - 
Updated getbanker opcode to break less

v0.01 -
release
</details>

### Scheduled Logout
Automatically logs out after inputted minutes
