# G.O.A.T. Industries

The ultimate Slimefun addon.

## Current test build

This first test build is a dependency preflight only. It does not add recipes, machines, or the G.O.A.T. trophy yet.

At startup it checks:

1. Slimefun is available.
2. The plugin named `EMCTech` is installed and enabled.
3. Every Slimefun item ID currently recorded in `DependencyCatalog.java` is present in Slimefun's enabled-item registry.

If any requirement is missing, G.O.A.T. Industries logs the exact missing plugin or addon/item ID and disables itself.

## Current item checks

The current catalog contains the item requirements collected so far for InfinityExpansion2, TranscEndence, Supreme, MomoTech, LiteXpansion, Galactifun, Tsingshan Technology, FNAmplifications, DankTech2, Networks, and Crystamae Historia.

- Crystamae Historia: all 54 crystals (six rarities × nine crystal types), using IDs verified in the SlimefunGuguProject source.
- MomoTech: all 50 compressed cobblestone levels.
- EMCTech: plugin presence/enabled state only; no EMCTech item has been specified yet.
- Ultimate Generators 2 Lambda Battery is not included yet because its exact registered Slimefun ID has not been verified.

Some other collected addon IDs still need a version-by-version comparison against the SlimefunGuguProject builds. The live server registry check will expose mismatches; a missing ID is reported verbatim rather than silently guessed or substituted.

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

1. Download the test JAR from the GitHub Actions artifact.
2. Place it in the server's `plugins` folder.
3. Restart the server.
4. Review the console lines beginning with `[G.O.A.T. Industries]`.

This build is only a startup dependency test. No gameplay content is implemented yet.
