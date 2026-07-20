# New or Edited Plugin Checklist

Leave an unchecked box with a short explanation when a gate is not complete; do not silently remove inapplicable checks.

- Plugin name: `Gluten-Free Bread`
- Slug: `gluten-free-bread`
- Repository: `carmelosantana/minecraft-gluten-free-bread`
- Owner: `Carmelo Santana`
- Target version: `1.1.2` (bug-fix-only patch over `1.1.1`)
- Paper version: `26.1.2 build 74`
- Java version: `25`
- Updater destination: `gluten-free-bread.jar`
- External services: `none`
- Status: `active`
- Autonomy: `autonomous`

Maven `artifactId`: `gluten-free-bread`. `plugin.yml` name: `GlutenFreeBread`. Releasable JAR:
`gluten-free-bread-<version>.jar`.

This file was created retroactively for a shipped, released plugin. It records the plugin's real
current state and the evidence for the `1.1.2` bugfix only. It does not reconstruct evidence for
gates that were satisfied before it existed, and it does not claim evidence that was never
gathered.

## 1. Scope

- [x] Status is explicitly recorded as active, experimental, or excluded.
- [x] Known limitations and any intentionally withheld gates are recorded.
- [ ] Purpose, commands, events, permissions, configuration, persistence, and acceptance checks are
      fully defined here. Not re-derived — this plugin shipped before this checklist existed. The
      `1.1.2` change is scoped below.

### What changes in 1.1.2

Floodgate joins a Bedrock account under a prefixed Java-side username (`.acarm` for a player who
calls themselves `carm`), using Floodgate's default `username-prefix: "."`. Three call sites in
`GFBreadCommand` used `Bukkit.getPlayer(String)`, which prefix-matches the *name* — so
`getPlayer("carm")` never matches `.acarm`, whose name starts with a dot. An operator or player
naming the unprefixed form got "Player not found" for someone standing in front of them.

The three sites now go through `PlayerLookup.resolveAllowingPartial`, which tries the typed name and
the `.`-prefixed form exactly, then case-insensitively, then falls back to Bukkit's own partial
matching so the previous partial-name behaviour is not regressed. The failure message now names who
is online — the only channel through which a Bedrock player can discover the prefixed form, since
Geyser sends no command-suggestion packets and Bedrock clients get no tab completion.

### Acceptance checks for 1.1.2

1. `/gfbread give <type> <bedrock-name>` reaches a Floodgate player named without the `.` prefix.
2. `/gfbread give <type> <bedrock-name> <amount>` does the same in the 4-argument form.
3. `/gfbread clear <bedrock-name>` does the same.
4. `/gfbread clear` with no argument still targets the sender.
5. Partial Java names still resolve, as they did with `Bukkit.getPlayer`.
6. A failed lookup lists the online players rather than dead-ending.
7. `mvn clean verify` passes with tests present.

Checks 1-6 are **not verified** — see §7a.

### Known limitations

- The Floodgate prefix is hardcoded to Floodgate's default `.` rather than read from config. A
  plugin-side key would be a second, unvalidatable source of truth for a value owned by another
  plugin's config. A server that reconfigured the prefix still resolves via the case-insensitive
  sweep and the partial-match tier.
- `PlayerLookup.resolve` and `resolveAllowingPartial` are not unit-tested: both call `Bukkit`
  statics that cannot be constructed headlessly. Only the pure functions are covered.

## 2. Repository

- [x] Repository is `carmelosantana/minecraft-gluten-free-bread` on `main`. Working tree was clean
      before this branch was cut.
- [x] Existing user-owned worktree changes were identified and preserved. None existed.

## 3. Metadata

- [x] AGPL-3.0-or-later `LICENSE` and Maven license metadata are present.
- [x] `https://xpfarm.org` and Carmelo Santana author metadata are present.
- [x] `org.xpfarm` Maven group is in use.
- [x] Repository slug, artifact, releasable JAR, and `plugin.yml` names are consistent.
- [ ] No secrets committed in source, defaults, tests, logs, history, or documentation. Not audited
      as part of this bugfix; no secrets were added by it.

## 4. Compatibility

- [x] Java 25 / Paper 26.1.2 build 74 compile succeeds. `mvn --batch-mode --no-transfer-progress
      clean verify` → `BUILD SUCCESS`, producing `gluten-free-bread-1.1.2.jar`.
- [x] Geyser/Floodgate review covers Bedrock-safe identity behavior. This release *is* that fix —
      see §1. No new player-facing interaction is introduced.
- [ ] Hard/soft dependencies and load ordering re-reviewed. Unchanged by this release; `softdepend:
      [floodgate]` is deliberately **not** added, as the fix is a pure string/name concern and never
      touches `FloodgateApi`.

## 5. External services

- [x] No external integrations. Not applicable throughout.

## 6. Tests and build

- [x] Unit tests cover separable logic and failure paths. **6 tests added — the repository had no
      `src/test/java` directory at all**, though `junit-jupiter` 5.10.2 and surefire were already
      declared in `pom.xml`. `PlayerLookupTest` covers `targetNameCandidates` and
      `noSuchPlayerMessage`.
- [x] Tests were confirmed to fail before the fix. Against a stubbed pre-fix `PlayerLookup`:
      `Tests run: 6, Failures: 5, Errors: 0, Skipped: 0` / `BUILD FAILURE`.
- [x] `mvn --batch-mode --no-transfer-progress clean verify` succeeds.
      `Tests run: 6, Failures: 0, Errors: 0, Skipped: 0` / `BUILD SUCCESS`. Confirmed executed via
      `target/surefire-reports/TEST-…PlayerLookupTest$TargetResolution.xml` (`tests="6"`).
- [ ] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded.
      Not inspected this release. Note `maven-shade-plugin` is still configured despite every
      dependency being `provided`/`test` scope, so it shades nothing and can produce `original-*`.

## 7. Matrix

### 7a — single-plugin runtime verification — NOT RUN

- [ ] Paper, Geyser, Floodgate, and ViaVersion start successfully together. Not run for `1.1.2`.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands. **NOT DONE — no real Bedrock
      client available.** This is the central gap: the fix is *about* Bedrock name resolution, and
      the only evidence behind it is 6 unit tests over the candidate-name and message logic plus a
      green compile. Acceptance checks 1-6 remain unverified at this point.
- [ ] Public deployment smoke tests. Belongs to gate 11.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Out-of-band and not a prerequisite for this release. No updater manifest entry and no
      dependency changes.

## 8. CI/CD

- [x] Standard plugin Actions workflow is installed at `.github/workflows/build.yml`. Present from
      `1.1.0`.
- [ ] Successful main Actions run recorded before tagging. Not applicable yet — this work sits on
      `fix/floodgate-name-resolution` and has not been pushed.

## 9. Release

- [ ] Not performed. Version is bumped to `1.1.2` in `pom.xml` and `CHANGELOG.md`, but nothing is
      tagged, pushed, or published.

## 10. Updater

- [ ] Not revisited. Already enrolled; `1.1.2` changes no manifest entry.

## 11. Deployment

- [ ] Not performed.

## 12. Handoff

- [ ] `CURRENT_STATE.md` not updated. Pending local work: this branch is unmerged and unpushed, and
      the Bedrock runtime verification in §7a is outstanding.
