# G.O.A.T. Industries

The ultimate Slimefun addon.

## Current test build

This first test build is a dependency preflight only. It does not add recipes, machines, or the G.O.A.T. trophy yet.

At startup it checks all 13 required addon plugins and the required Slimefun item IDs:

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
13. EMCTech

Each addon is checked completely before its success line is printed. An addon is confirmed only when its plugin is installed and enabled and every catalogued item for that addon is present in Slimefun's enabled-item registry. The checker continues through all 13 addons even after finding a missing plugin or item.

After all addons have been checked, missing addons and missing items are listed in the console. G.O.A.T. Industries then disables itself if any requirement is missing or if Slimefun's item registry cannot be read.

## Current item checks

- InfinityExpansion2: 2 items.
- TranscEndence: 5 Daxi items.
- Supreme: 12 specified items, including Supreme Attribute Bomb.
- MomoTech: all 50 compressed cobblestone levels.
- LiteXpansion: UU Matter.
- Galactifun: 9 specified items.
- Tsingshan Technology: PRO Generator.
- Ultimate Generators 2: LAMBDA-λ Electricity Storage, catalogued as `LAMBDA_ELECTRICITY_STORAGE`.
- FNAmplifications: FN Generator R12.
- DankTech2: Dank Pack Tier 9.
- Networks: Quantum Storage 140.7T.
- Crystamae Historia: all 54 crystals (six rarities × nine crystal types).
- EMCTech: plugin presence/enabled state only; no EMCTech item has been specified.

Some collected addon IDs still need a version-by-version comparison against the SlimefunGuguProject builds. The live server registry check compares the catalogued IDs exactly and reports mismatches verbatim.

## Build

Requirements: Java 21 and Maven.

On a machine that has the exact Slimefun dependency installed in its local Maven repository:

```bash
mvn -Dslimefun.local=true clean verify
```

The local profile uses the required dependency coordinates:

```xml
<groupId>com.github.slimefun</groupId>
<artifactId>Slimefun</artifactId>
<version>2026.7</version>
<scope>provided</scope>
```

The GitHub Actions build compiles against Paper API 1.21.1 and uses reflection for Slimefun registry access, so the CI runner does not need access to a developer's local Maven repository. The produced plugin JAR is available as the `goat-industries-test-jar` workflow artifact.

## Server test

1. Download the latest test JAR from the GitHub Actions artifact.
2. Place it in the server's `plugins` folder.
3. Restart the server.
4. Review the console lines beginning with `[G.O.A.T. Industries]`.

This build is only a startup dependency test. No gameplay content is implemented yet.
