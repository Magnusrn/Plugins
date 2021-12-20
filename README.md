|Plugin List(click for details) |
|------------- |
| [Birdhouse Infobox](#birdhouse-infobox) | 
| [CoxRaidScouter](#coxraidscouter)  | 
| [One Click Aerial Fishing](#one-click-aerial-fishing)| 
| [One Click Bloods](#one-click-bloods) | 
| [One Click Chins](#one-click-chins) | 
| [One Click Custom](#one-click-custom) | 
| [One Click Glassblowing](#one-click-glassblowing) |  
| [One Click Karambwans](#one-click-karambwans) | 
| [One Click Minnows](#one-click-minnows)  | 
| [One Click Sandstone](#one-click-sandstone)|  
| [One Click Swordfish](#one-click-swordfish)|   
| [One Click Telegrab](#one-click-telegrab) |  
| [One Click ZMI](#one-click-zmi) |   

### Birdhouse-Infobox
Nothing fancy, starts a (fairly conservative) timer upon building a birdhouse, overlays an infobox

### CoxRaidScouter

Requires the "Chambers of Xeric" plugin "Raid Layout Message" option Toggled ON

SWIM ran this on 3 accounts for a little under 3 months(approx 2000 raids) two temps towards the end. However this is a commonly reported activity so would only recommend using on burner accounts. Works nicely if setup on a cloud VM and webhooked to discord. Only need to check once every 6 hours if using the auto leave/rejoin cc feature

V1.6
Removed dependancy on iutils but no longer sends clicks and randomness removed for now. Emphasis on only use burner accounts.

V1.5
Auto Leave/Rejoin CC no longer requires you to wait for the bot to rejoin

V1.4
Added Java webhook instead of external python file
Modified reset to be on logout instead of login due to sometimes leaving cc immediately upon login

V1.3
Added 5h login timer handling
Added reset on login(Prevents webhook posting raid taken on relog)
Added webhook message on logout

v1.2 -
Added Good Crabs detector
Added ability to input specific rotations rather than just blacklist
added os detection for python run command

v1.1 -
Moved webhook.py within plugins folder for ease of setup
Added layout to webhook (SCSPF e.g)
Added new embed for webhook
Added webhook message when raid is taken(user has started scouting again)

### One Click Aerial Fishing
Molch Aerial fishing, will cut fish if you have a knife in your invent else drops fish.

### One Click Bloods
Initially requires the map to be loaded as a normal player would else it can get stuck. Currently uses a rather inelegant method of using a chisel on the cluster of rocks to get nearer to the altar. SWIM has used for approx 100h without any detection.

### One Click Chins
Pretty simple plugin, requires initial setting of the traps and just resets them. Has the option to afk if another player is nearby as otherwise it will attempt to reset the other players traps if it's closest.

### One Click Custom
Input ID and choose your object type. This will find the nearest of that object as the crow flies and unfortunately not nearest by tiles.

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
This is state based so must be started near the banker at ZMI. Ensure you have 1 dose staminas in the bank and are in the main tab of the bank. Ensure Last NPC contact was to dark mage as it will repair broken pouches.|Plugin List(click for details) |
