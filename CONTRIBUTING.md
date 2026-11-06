# Contributing

Thanks for taking a look at URC. Bug reports, rule ideas and pull requests are all welcome.

## Reporting a bug

Open an issue with:

- the Minecraft version and the exact jar you loaded (`URC-<ver>+<mc>.jar`);
- your Carpet / Fabric API / Fabric Loader versions;
- the output of `/carpet` for the rules you had enabled;
- `logs/latest.log` (or at least the lines mentioning `urc`, `Mixin` and the crash);
- what you expected vs. what happened.

## Building

Requires JDK 25.

```bash
./gradlew build              # default target, see gradle.properties (26.1.2)
./gradlew build -Pmc=26.3    # build a specific version
```

Every Minecraft version is built separately; the jar ends up in
`build/libs/URC-<mod version>+<mc>.jar` and its `fabric.mod.json` is pinned to that version.

## Rule design guidelines

- One feature per Carpet rule, declared in `URCSettings`, described in both lang files.
- Every injection must be a no-op when the rule is off - never change vanilla behaviour silently.
- Prefer a narrow injection point (one method, one call) over rewriting vanilla logic.
- If a target differs between 26.x releases, put the version-specific code in the matching
  source directory (`src/client26_1`, `src/client26_2`) or use an accessor mixin.
- Update `docs/rules.md` and `CHANGELOG.md` with the change.

## Pull requests

- Keep commits focused and describe *why* in the message.
- Run `./gradlew build` for at least the default version before opening the PR; CI builds all
  supported versions.
