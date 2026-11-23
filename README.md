# G.O.A.T. Industries

The ultimate Slimefun addon.

## Current test build

This build includes the EMC Diamond achievement reward, G.O.A.T. Trophy and pyramid guide, and the Dynamic Crystal Former with six Dynamic Crystal outputs.

At startup, G.O.A.T. Industries verifies Slimefun and all 14 required addon plugins. It also checks every required Slimefun item ID in the enabled-item registry, including the Slimefun-core Plutonium ingredient. An addon is confirmed only when its plugin is installed and enabled and all catalogued item IDs are present. The checker continues through all dependencies even after finding a missing plugin or item.

If a required plugin or item is missing, or Slimefun's item registry cannot be read, G.O.A.T. Industries disables itself and reports the missing requirements.

## Required addons

1. InfinityExpansion2
2. TranscEndence
3. Supreme
4. MomoTech
5. LiteXpansion
6. Galactifun
7. Tsingshan Technology
8. Ultimate Generators 2
9. FNAmplifications
10. DankTech2
11. Networks
12. Crystamae Historia
13. Laboratory
14. EMCTech

The Dynamic Crystal Former specifically requires these registered items:

- Infinity Expansion 2: Void Smeltery (`VOID_SMELTERY`), Void Quarry (`QUARRY_3`), and Void Capacitor (`VOID_CAPACITOR`).
- Supreme: Electric Core Fabricator III (`SUPREME_ELECTRIC_CORE_III`).
- Crystamae Historia: Exalted Beacon (`CRY_EXALTED_BEACON`) and all 54 rarity/type crystals.
- Laboratory: Laser Drill (`LAB_LASER_DRILL`).
- Slimefun: Plutonium (`PLUTONIUM`).

## Dynamic Crystal Former

The machine is crafted in the Enhanced Crafting Table using this 3×3 arrangement, left-to-right and top-to-bottom:

1. Void Smeltery
2. Void Quarry
3. Void Smeltery
4. Supreme Electric Core Fabricator III
5. Void Capacitor
6. Supreme Electric Core Fabricator III
7. Exalted Beacon
8. Laboratory Laser Drill
9. Plutonium

The machine uses Deepslate Tiles as its item texture and connects to Slimefun's energy network. It has a 16,000,000 J energy buffer and consumes 500,000 J per Slimefun tick. Slimefun runs two machine ticks per second, so this is equivalent to 1,000,000 J/s. Each recipe takes 600 Slimefun ticks (30 seconds).

Each recipe requires one of each of the nine Crystamae Historia crystal types in alphabetical order:

1. Alchemical
2. Animal
3. Celestial
4. Elemental
5. Historical
6. Human
7. Mechanical
8. Philosophical
9. Void

There is one recipe for each rarity: Common, Uncommon, Rare, Epic, Mythical, and Unique. Each produces one matching Dynamic Crystal. All six output items use the approved Griffin Upgrade Stone head textures and implement Slimefun's non-placeable item attribute.

Each Dynamic Crystal's Slimefun Guide entry displays its complete 3×3 recipe with the Dynamic Crystal Former as the crafting station. The machine itself displays its Enhanced Crafting Table recipe.

## EMC Diamond reward

Players receive one EMC Diamond when their EMC balance first reaches 1,000,000,000. The achievement is saved in `emcachieved.yml` and persists across restarts. To permit a second reward, remove that player's UUID entry under `claimed:` in that file while the server is stopped.

## Build

Requirements: Java 21 and Maven.

On a machine that has the exact Slimefun dependency installed in its local Maven repository:

```bash
mvn -Dslimefun.local=true clean verify
```

The local profile uses these dependency coordinates:

```xml
<groupId>com.github.slimefun</groupId>
<artifactId>Slimefun</artifactId>
<version>2026.7</version>
<scope>provided</scope>
```

The GitHub Actions build compiles against Paper API 1.21.1 and uses reflection for Slimefun API access, so the CI runner does not need a local Slimefun Maven dependency. Byte Buddy is bundled in the plugin JAR for the runtime-generated Slimefun machine subclasses. The produced plugin JAR is uploaded as the `goat-industries-test-jar` workflow artifact.

## Server test

1. Download the latest test JAR from the GitHub Actions artifact.
2. Place it in the server's `plugins` folder.
3. Restart the server.
4. Confirm the startup dependency checks pass.
5. Craft the Dynamic Crystal Former in the Enhanced Crafting Table.
6. Connect it to an energy network, open its 3×3 input GUI, and test each rarity recipe.

The JAR is a test build. Successful compilation does not replace the in-game checks of energy-network behavior, machine inventory handling, and texture rendering.
