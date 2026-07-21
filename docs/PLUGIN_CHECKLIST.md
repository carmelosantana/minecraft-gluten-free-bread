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
- ~~**The releasable JAR is compiled for Java 21, not Java 25.**~~ **RESOLVED in 1.1.3.** The
  `maven-compiler-plugin` `<configuration>` block hardcoding `<source>/<target>/<release>` to `21`
  is removed, so the pom's `maven.compiler.release=25` property is now the single source of truth.
  Verified against the built `gluten-free-bread-1.1.3.jar`: **all nine class entries read bytecode
  major 69 (Java 25)**, checked exhaustively rather than sampled. `maven-shade-plugin` was removed
  in the same change — it shaded nothing (every dependency is `provided`/`test` scope) and existed
  only to emit the `original-*.jar` the release workflow then filtered; `target/` now contains
  exactly one JAR. Confirmed safe against `.github/workflows/build.yml` first: CI filters
  `original-*` but never requires shading.

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
      clean verify` → `BUILD SUCCESS`, producing `gluten-free-bread-1.1.3.jar`, with all nine class
      entries at bytecode major 69 (Java 25).

      **Correction:** this box was ticked for `1.1.2` on the strength of `BUILD SUCCESS` alone,
      which was misleading — that build emitted Java 21 bytecode (major 65) because the compiler
      plugin overrode the release property. A green build is not evidence of the compile target;
      only reading the class files is. Verified that way from `1.1.3` onward.
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
- [x] The releasable JAR and embedded `plugin.yml` were inspected; `original-*` JARs are excluded.
      Verified for `1.1.3` by unzipping the built JAR. Embedded `plugin.yml` reads
      `version: '1.1.3'`, `api-version: '1.21'`,
      `main: com.carmelosantana.glutenfreebread.GlutenFreeBreadPlugin`.

      **Java 25 compile target confirmed — the 1.1.2 violation is resolved.** All nine `.class`
      entries in `gluten-free-bread-1.1.3.jar` read bytecode major **69 (Java 25)**, checked
      exhaustively rather than sampled. Through 1.1.2 the JAR read major 65 (Java 21) because
      `maven-compiler-plugin` hardcoded `<source>/<target>/<release>` to `21`, overriding the pom's
      `maven.compiler.release=25`. That `<configuration>` block is removed in 1.1.3; the property
      is now the single source of truth, matching `agua-de-florida`.

      **`original-*` can no longer be produced at all.** `maven-shade-plugin` was removed in
      `1.1.3` — every dependency is `provided`/`test` scope, so it shaded nothing and existed only
      to rename the untouched jar, which is what created the `original-*` file. `target/` now
      contains exactly one JAR (`gluten-free-bread-1.1.3.jar`). Removal was confirmed safe against
      `.github/workflows/build.yml` first: CI *filters* `original-*` on the SHA256SUMS step, the
      `gh release upload` step, and the uploaded build artifact, but never *requires* shading. The
      release contract is now satisfied at build time rather than by a CI filter.

## 7. Matrix

### 7a — single-plugin runtime verification (`1.1.2`) — PARTIAL

Evidence below comes from a **single disposable Legendary stack run on 2026-07-20**
(image `05jchambers/legendary-minecraft-geyser-floodgate:latest`) with **all six fixed plugin
JARs mounted together**. The same run backs the gate 7a note in all six repositories.

- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together. **Verified.** Paper
      reached `Done (18.178s)! For help, type "help"`. The Java port answered a real Minecraft
      protocol handshake — not merely a TCP connect — reporting `Paper 26.1.2 | protocol 775` and
      `PLAYERS: 0 / 20`. `/plugins` reported 9 plugins, all green/enabled: AguaDeFlorida, floodgate,
      Geyser-Spigot, GlutenFreeBread, StarterPack, TheCurse, ViaVersion, WildWeatherUpdate,
      WorldCRUD. Companion versions observed: floodgate v2.2.5-SNAPSHOT (b138-fc99cfc),
      Geyser-Spigot v2.11.0-SNAPSHOT (Geyser 2.11.0-b1200), ViaVersion present; Geyser started on
      UDP port 19200. Each plugin enabled at its new version with **zero exceptions, errors, or
      SEVERE lines attributable to any of the six** — including `Enabling GlutenFreeBread v1.1.2`.
- [ ] Java and Bedrock smoke tests cover joins plus affected commands, events, permissions,
      persistence, and reloads. **PARTIAL — the Java side was exercised, the Bedrock side was not.
      Left unchecked deliberately.**

      *What was exercised.* The **Floodgate prefix assumption was confirmed empirically, not merely
      from documentation**: reading `/minecraft/plugins/floodgate/config.yml` inside the running
      container on the Floodgate 2.2.5 build showed `username-prefix: "."` and
      `replace-spaces: true`, alongside the shipped comment "Floodgate prepends a prefix to bedrock
      usernames to avoid conflicts". The `.` prefix this fix depends on is now **observed on the
      actual runtime, not assumed** — the single most important upgrade to the evidence.

      The **new failure path was then exercised end-to-end over RCON on the live server** for every
      fixed command across all six plugins — `/aguadeflorida give carm`, `/curse start carm`,
      `/curse book carm`, `/worldcrud listpermissions carm`, `/starterpack give carm`,
      `/gfbread clear carm`, and `/weather trigger rain carm` — and each returned the new
      message with no exception: exactly `No player matches 'carm'; no players are online.` This proves that
      `PlayerLookup.resolve` / `resolveAllowingPartial` / `onlineNames` / `noSuchPlayerMessage`
      actually execute correctly against real Bukkit APIs, that command dispatch reaches them, and
      that the message renders — none of which the unit tests could show.

      *What remains unverified.* **The positive match is still unproven.** No real Bedrock client
      was available, so no player with a `.`-prefixed Java-side username ever joined. What is
      verified is that the resolution path runs without error and that the not-found branch is
      correct; that `/gfbread clear carm` actually **finds** a Bedrock player named `.acarm` has
      **not** been observed. Only the empty-online-list branch of `noSuchPlayerMessage` was
      exercised; the branch that lists online player names was not. The operator will verify live on
      the dev server with helpers. `resolve` / `resolveAllowingPartial` still have **no unit-test
      coverage** (Bukkit statics, no MockBukkit).
- [ ] Public deployment smoke tests verify `play.xpfarm.org` reaches the intended Java and Bedrock entry points. Belongs to gate 11, not this gate.

### 7b — ten-plugin ecosystem matrix — NOT RUN

- [ ] Out-of-band and not a prerequisite for this release. No updater manifest entry and no
      dependency changes.

### 7b — ecosystem matrix (12 plugins) — PASSED 2026-07-21

Trigger: the updater manifest changed — Timber Blast `v1.0.0` was enrolled
(`carmelosantana/minecraft-plugin-updater` commit `6065b03`), taking the roster from 11 to 12.

- [x] Fresh-volume Legendary stack test covers all updater-managed plugins. **12/12 PRESENT.**
      Run via the shared rig (`xpfarm-test-stack matrix up --from-releases`) on a fresh volume,
      roster read from the live `plugins.json` rather than a hardcoded list. The rig cross-checks
      the plugin count the server announces against what it parsed, and asserts each plugin is
      **enabled**, not merely listed.
- [x] Each updater-managed plugin's manifest `enabled` value, default state, and expected
      fresh-volume behavior are recorded separately. All 12 entries have `enabled` absent
      (equivalent to `true`) and no `pin`; every one was therefore expected to install and enable,
      and every one did. No entry was disabled, so there is no intentional-absence row this run.
- [x] Paper, Geyser, Floodgate, and ViaVersion start successfully together.
      Paper reached `Done (15.543s)! For help, type "help"`; the Java port answered a real
      protocol handshake reporting `Paper 26.1.2 | protocol 775`, `PLAYERS: 0 / 20`. Companions:
      Geyser-Spigot 2.11.0-SNAPSHOT, floodgate 2.2.5-SNAPSHOT, ViaVersion 5.11.0.
- [ ] Java and Bedrock smoke tests cover joins. **Not performed — no client attaches to this
      stack by design.** Per `PLUGIN_LIFECYCLE.md` §7 this is not a blocker; client behavior is a
      tracked gate-12 play-test obligation, not a matrix result.
- [x] `play.xpfarm.org` reaches the intended Java and Bedrock entry points.
      Read-only production check, separate from the disposable stack: DNS `168.231.74.113`;
      Java `25565` answered a real handshake (`Paper 26.1.2 | protocol 775`, 1 player online);
      Bedrock UDP `19132` reachable.
- [x] Ollama and Umami unavailable-endpoint tests keep the server and plugins available.
      Neither service exists in this stack, so this is the negative path by construction. Both
      self-disabled cleanly: `Ollama integration is disabled; no API client or listeners were
      started.` and `Umami analytics is disabled; no tracking listeners or network clients were
      started.` Server stayed healthy (`list` responded) with all 12 enabled.

This plugin's row: the updater reported `GlutenFreeBread: installed v1.1.3` from the published release
asset and Paper enabled it alongside the other 11. `--from-releases` was used deliberately — it
installs the real published assets through the real updater, so this is what production installs.

Co-resident: AguaDeFlorida 2.0.0, CopperKingdom 0.2.1, TheCurse 0.2.2, DeathDepot 1.1.1, ElectricFurnace 0.2.1, Ollama 0.2.1, StarterPack 1.1.2, TimberBlast 1.0.0, Umami 1.1.1, WildWeatherUpdate 1.0.2, WorldCRUD 1.1.2.

Zero exceptions, SEVERE lines, or enable failures attributable to any plugin. No secrets in any
log line. Stack torn down with `matrix down`; lease released, no orphaned containers.

## 8. CI/CD

- [x] Standard plugin Actions workflow is installed at `.github/workflows/build.yml`. Present from
      `1.1.0`.
- [x] Successful main Actions run recorded before tagging. For `1.1.3`:
      `fix/java-25-compile-target` was merged fast-forward to `main` and pushed on 2026-07-20; the
      `main`-branch run for commit `767c8d2` completed with conclusion `success` **before** tag
      `v1.1.3` was created. Previously for `1.1.2`: `fix/floodgate-name-resolution` merged
      fast-forward, `main` run for commit `1447dc2` `success` before `v1.1.2`. No tag has been
      pushed against a red or in-flight run.

## 9. Release — `v1.1.3` COMPLETE

- [x] Semantic version matches the POM, plugin metadata, and `v<version>` tag. Verified: `pom.xml`
      `<version>` `1.1.3` equals tag `v1.1.3` equals the `plugin.yml` version read out of the built
      JAR.
- [x] Annotated tag `v1.1.3` created on verified commit `767c8d2` and pushed; the tag Actions run
      completed with conclusion `success`.
- [x] GitHub release `v1.1.3` published 2026-07-20 with `draft=false`, `prerelease=false`, and it
      is now the repository's Latest release.
- [x] Release contains exactly one updater-matching JAR plus `SHA256SUMS.txt` and no `original-*`
      JAR. Verified by downloading the published release assets. From `1.1.3` this is guaranteed at
      build time rather than by CI filter, since `maven-shade-plugin` — the only thing that ever
      produced an `original-*` JAR — is removed.
- [x] Downloaded release assets pass `sha256sum --check SHA256SUMS.txt`. Reported `OK` for the JAR.
- [x] **The published artifact was confirmed to be Java 25 bytecode.** The JAR downloaded from the
      `v1.1.3` release — built by CI, not locally — reads bytecode major **69**. This closes the
      Java 21 drift that shipped silently in every release through `1.1.2`.

      Prior release for history: `v1.1.2`, commit `1447dc2`, published 2026-07-20 14:47:58 UTC,
      checksum `OK`. That release is functional but ships Java 21 bytecode; prefer `1.1.3`.

## 10. Updater

- [ ] Not revisited. Already enrolled; `1.1.2` changes no manifest entry. Updater enrollment was
      not performed in this pass.

## 11. Deployment

- [ ] Not performed. The operator will deploy and verify live on `play.xpfarm.org` via the dev
      server with helpers.

## 12. Handoff

- [ ] `CURRENT_STATE.md` not updated. Pending local work: this branch is unmerged and unpushed, and
      the Bedrock runtime verification in §7a is outstanding.
