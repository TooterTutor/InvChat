# InvChat 3.0

InvChat is a client-side Fabric mod that allows players to chat from inventory and container screens.

## 3.0 development line

InvChat 3.0 uses Stonecutter to maintain one source tree across Minecraft 1.16.5 through 26.2.
The project uses Mojang mappings for obfuscated Minecraft releases and Loom's no-remap path for
Minecraft 26.1 and newer via `loom-back-compat`.

Java is selected automatically by Minecraft version:

- 1.16.5: Java 8
- 1.17.x: Java 16
- 1.18.x through 1.20.4: Java 17
- 1.20.5 through 1.21.11: Java 21
- 26.1 and newer: Java 25

This foundation commit intentionally contains only the client entry point. The inventory chat widget,
container-screen mixin, focus/input compatibility layer, configuration, Mod Menu integration, command
history, and tab completion are introduced in later commits after the build matrix is stable.

## Building

Stonecutter creates one Gradle subproject per Minecraft release. Examples:

```bash
gradle :1.16.5:build
gradle :1.20.1:build
gradle :1.21.11:build
gradle :26.2:build
```

To switch the source view used by your IDE:

```bash
gradle stonecutterSwitchTo1.20.1
gradle stonecutterSwitchTo26.2
```

A standard Gradle wrapper should be generated and committed after applying the foundation patch:

```bash
gradle wrapper --gradle-version 9.7.1
```

Then use `./gradlew`/`gradlew.bat` normally.
