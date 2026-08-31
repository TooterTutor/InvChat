# InvChat

InvChat is a client-side Fabric mod that lets you chat, run commands, and use command completion without closing your inventory or another container screen.

Instead of leaving a chest, crafting table, furnace, or other inventory screen just to send a message, InvChat adds a small chat field directly to the screen you already have open.

## Features

- **Chat from inventory screens** — send normal chat messages without closing the current container.
- **Run commands in-place** — messages beginning with `/` are submitted through Minecraft's command system.
- **Familiar controls** — press `T` to focus InvChat, `Enter` to send, and `Escape` to return focus to the inventory.
- **Session history** — use `Up` and `Down` to browse messages and commands you have already sent during the current game session.
- **Command completion** — InvChat uses Minecraft's Brigadier command tree for server-aware Tab completion.
- **Configurable placement** — change the field width and position, or anchor it beneath the current inventory GUI.
- **Wide version support** — one Stonecutter codebase targets Minecraft **1.16.5 through 26.2**.
- **Client-side only** — servers do not need InvChat installed.

## How to use InvChat

1. Open your player inventory or another vanilla container screen.
2. Press `T`, or click the InvChat field.
3. Type a message or command.
4. Press `Enter` to send it.

InvChat keeps the current inventory open while you type. Press `Escape` once to leave the chat field and return focus to the inventory; press it again to close the inventory normally.

### Controls

| Input | Action |
| --- | --- |
| `T` | Focus the InvChat field |
| `Enter` / Numpad `Enter` | Send the current message or command |
| `Escape` | Unfocus InvChat without closing the inventory |
| `Up` | Move backward through InvChat history |
| `Down` | Move forward through InvChat history / restore your draft |
| `Tab` | Accept or cycle command completions |

Normal text-editing shortcuts supported by Minecraft's text field continue to work while InvChat is focused.

## Chat history

InvChat keeps up to **100 successfully submitted messages and commands** for the current Minecraft session.

For example, after sending:

```text
hello
/time set day
/gamemode creative
```

pressing `Up` repeatedly will walk backward through those entries. Pressing `Down` walks forward again.

If you start typing a new message and then press `Up`, InvChat saves that unfinished text as a draft. Navigating back past the newest history entry restores the draft instead of discarding it.

History is shared between container screens for the duration of the current client session.

## Command completion

When the field begins with `/`, InvChat asks Minecraft's Brigadier command dispatcher for completions.

For example:

```text
/gam
```

can offer `/gamemode`, and pressing `Tab` accepts the completion. Argument suggestions can also come from the command tree supplied by the server, allowing completion for vanilla, modded, and server-provided commands when the server exposes suggestions for them.

InvChat currently uses Minecraft's inline suggestion text rather than reproducing the full vanilla chat-screen suggestion dropdown.

## Configuration

InvChat creates the following file the first time the client starts:

```text
config/invchat.json
```

Default configuration:

```json
{
  "enabled": true,
  "anchorBelowInventory": false,
  "xOffset": 0,
  "yOffset": 0,
  "width": 200
}
```

| Option | Description |
| --- | --- |
| `enabled` | Enables or disables the InvChat field |
| `anchorBelowInventory` | Places InvChat relative to the bottom edge of the current container GUI instead of the bottom of the screen |
| `xOffset` | Horizontal pixel offset from the calculated position |
| `yOffset` | Vertical pixel offset from the calculated position |
| `width` | Width of the chat field in pixels; values are clamped between `40` and `1000` |

Configuration is currently loaded when Minecraft starts, so restart the client after manually changing `invchat.json`.

Per-screen overrides are intentionally not required for InvChat 3.0 and may be added later as an optional feature.

## Installation

InvChat is built for the **Fabric** mod loader.

1. Install Fabric Loader for your Minecraft version.
2. Download the InvChat JAR built specifically for that Minecraft version.
3. Place the JAR in your Minecraft `mods` directory.
4. Start Minecraft.

InvChat does not need to be installed on the server.

> [!IMPORTANT]
> InvChat produces separate JARs for individual Minecraft versions. Use the artifact that matches the Minecraft version you are launching.

## Supported Minecraft versions

The InvChat 3.0 Stonecutter project targets releases from **Minecraft 1.16.5 through Minecraft 26.2**.

The project intentionally handles several major compatibility eras, including Java 8-era Minecraft, the signed-chat transition in 1.19.x, the newer keyboard event system introduced in late 1.21.x, and the unobfuscated/Mojang-named 26.x development line.

## Compatibility notes

InvChat injects into Minecraft's vanilla container-screen base class. Vanilla inventory and container screens therefore share the same behavior without individual integrations.

Most modded screens that extend the same vanilla container base should also inherit InvChat automatically. A custom modded GUI that does not use Minecraft's normal container-screen hierarchy may require explicit support.

Command completion depends on the command tree and suggestion providers available to the client. A server can therefore affect which commands and arguments are suggested.

## Building from source

InvChat 3.0 uses [Stonecutter](https://stonecutter.kikugie.dev/) to maintain one source tree across many Minecraft versions, together with `loom-back-compat` for the transition between older obfuscated Minecraft releases and the 26.x unobfuscated toolchain.

Build every configured Minecraft target:

```bash
./gradlew build
```

Build one version:

```bash
./gradlew :1.16.5:build
./gradlew :1.20.1:build
./gradlew :1.21.11:build
./gradlew :26.2:build
```

Run a development client for one version:

```bash
./gradlew :1.21.11:runClient
```

Switch the Stonecutter source view used by your IDE:

```bash
./gradlew stonecutterSwitchTo1.20.1
./gradlew stonecutterSwitchTo26.2
```

The build selects the required Java toolchain for each Minecraft generation automatically:

| Minecraft | Java |
| --- | ---: |
| 1.16.5 | 8 |
| 1.17.x | 16 |
| 1.18.x – 1.20.4 | 17 |
| 1.20.5 – 1.21.11 | 21 |
| 26.1+ | 25 |

## Reporting issues

If you find a bug, include the Minecraft version you were using and, when relevant, the server/mod environment that reproduced it.

Issues can be reported at:

https://github.com/TooterTutor/InvChat/issues

## License

InvChat is licensed under the [MIT License](LICENSE).
