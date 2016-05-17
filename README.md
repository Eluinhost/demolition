Demolition
==========

Spigot plugin with the following features (rates configurable):

- Enderman spawn holding TNT
- Endermen/players placing TNT 'autoprime' it
- TNT veins spawn naturally
- Creepers spawn powered
- Skeletons spawn with fire bows

## Configuration

    enderman have tnt chance: 100
    tnt auto prime on player place chance: 100
    tnt auto prime on enderman place chance: 100
    creepers are powered chance: 10
    skeletons have fire bows chance: 10
    skeletons have fire bows drop chance: 10
    vein rules:
      size: 16
      rounds: 20
      min height: 0
      max height: 128
      probability: 100
      biomes: []

`enderman have tnt chance` integer, percentage chance an enderman will
spawn holding a block of TNT

`tnt auto prime on player place chance` integer, percentage chance that
TNT will autoprime on being placed by a player

`tnt auto prime on enderman place chance` integer, percentage chance 
that TNT will autoprime on being placed by an enderman

`creepers are powered chance` integer, percentage chance that a creeper
will spawn powered

`skeletons have fire bows chance` integer, percentage chance that a 
skeleton will spawn with a bow with flame enchant

`skeletons have fire bows drop chance` integer, percentage chance that
a skeleton that was spawned with a fire bow will drop it on death

`vein rules` this controls the TNT vein spawning rate

Some values for vanilla ores (these may be very out of date, but close
enough):

| Ore | Size | Rounds | Height |
|-|-|-|-|
Coal | 16 | 20 | 0 - 128 |
Iron | 8 | 20 | 0 - 64 |
Gold | 8 | 2 | 0 - 32 |
Redstone | 7 | 8 | 0 - 32 |
Diamond | 7 | 1 | 0 - 16 |
Dirt | 32 | 20 | 0 - 128 |
Gravel | 32 | 10 | 0 - 128 |

`probability` is the chance of 'skipping' a round

`biomes` is an array of [biomes names](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/block/Biome.html)
to spawn in, if empty will spawn in all biomes