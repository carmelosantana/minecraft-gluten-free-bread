# Changelog

All notable changes to Gluten-Free Bread are documented here.

## 1.1.3 - 2026-07-20

### Fixed

- The plugin now actually compiles to Java 25, the ecosystem target. `maven-compiler-plugin`
  hardcoded `<source>21</source><target>21</target><release>21</release>`, which overrode the
  `maven.compiler.release=25` property, so every release through 1.1.2 shipped Java 21 bytecode
  (class file major 65) while the POM claimed 25. The plugin configuration is removed; the
  property is now the single source of truth. Verified against the built JAR: all classes are
  major 69.

### Removed

- `maven-shade-plugin`. Every dependency is `provided` or `test` scope, so it shaded nothing
  while still emitting an `original-*.jar` that the release workflow had to filter out. Removing
  it eliminates the artifact rather than working around it. Confirmed against
  `.github/workflows/build.yml` first: CI filters `original-*` but never requires shading.

## 1.1.2 - 2026-07-20

### Fixed

- `/gfbread give` and `/gfbread clear` now find Bedrock players who joined through
  Floodgate. Floodgate prefixes a Bedrock account's Java-side username with `.`, and
  `Bukkit.getPlayer` matches a prefix of the name, so a player known as `carm` — whose
  real username is `.acarm` — could never be targeted by name.
- A failed player lookup now lists who is actually online instead of dead-ending on
  "Player not found", which is the only way a Bedrock player discovers the prefixed
  form: Geyser sends no command-suggestion packets, so they get no tab completion.

### Added

- `PlayerLookup` utility, plus the repository's first unit tests covering its
  name-candidate and failure-message logic.

## 1.1.1 - 2026-07-19

### Fixed

- SHA256SUMS.txt now records bare JAR filenames instead of the build-time
  `target/` path, so `sha256sum --check` works against downloaded release assets.

## 1.1.0 - 2026-07-13

### Changed

- Updated the build baseline to Paper 26.1.2 and Java 25.
- Updated Maven compiler and shading plugins for Java 25 bytecode.
- Added GitHub Actions for tests, release JARs, SHA-256 checksums, and tagged releases.
- Verified plugin startup and command registration on the current server stack.

### Tested

- Paper 26.1.2 build 74
- Geyser 2.11.0
- Floodgate 2.2.5 build 138
- ViaVersion 5.11.0
