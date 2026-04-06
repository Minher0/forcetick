# ForceTick Server

**A server-side Fabric mod that forces chunks to tick without players nearby. Perfect for automatic farms!**

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.15%2B-orange)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## ✨ Features

- **100% Server-side** - No client mod required!
- **Forced Chunk Loading** - Keeps chunks loaded even without players
- **Automatic Random Ticks** - Crops grow automatically (sugar cane, cactus, etc.)
- **Full Farm Support** - Works with observers, pistons, hoppers, items on water, chests
- **Persistent Storage** - Chunks stay forced after server restart
- **Simple Commands** - Easy to use in-game commands

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/forcetick add` | Force-tick the chunk you're standing in |
| `/forcetick add <x> <z>` | Force-tick a specific chunk |
| `/forcetick remove` | Remove force-tick from current chunk |
| `/forcetick remove <x> <z>` | Remove force-tick from specific chunk |
| `/forcetick list` | List all force-ticked chunks in current dimension |
| `/forcetick removeall` | Remove all force-ticked chunks in current dimension |

**Permission Level:** 2 (OP)

## 📦 Requirements

- Minecraft 1.20.1
- Fabric Loader 0.15.0+
- Fabric API

## 🔧 Installation

1. Install Fabric Loader on your server
2. Install Fabric API
3. Drop `forcetick-server-2.0.0.jar` into the `mods` folder
4. Start the server!

## 🌾 How It Works

Based on the proven concept from [FXNT Chunks](https://github.com/foxynotail/fxnt-chunks-mod-fabric) by foxynotail.

**The Secret:**
1. `setChunkForced()` - Keeps the chunk loaded (game handles entities, block entities, scheduled ticks automatically)
2. Manual Random Ticks - Applied to blocks for crop growth

**What Works Automatically:**
| Component | How It Works |
|-----------|--------------|
| 🌱 Sugar Cane/Cactus | Random ticks for growth |
| 👁️ Observers | Scheduled ticks (chunk forced) |
| 🔧 Pistons | Scheduled ticks (chunk forced) |
| 💧 Items on Water | Entity ticks (chunk forced) |
| 🪣 Hoppers | Block entity ticks (chunk forced) |
| 📦 Chests | Block entity ticks (chunk forced) |

## 💾 Data Storage

Force-ticked chunks are saved in `world/forcetick-server-chunks.txt` and persist across server restarts.

## 🔗 Links

- [GitHub Repository](https://github.com/Minher0/forcetick)
- [Source Inspiration](https://github.com/foxynotail/fxnt-chunks-mod-fabric) (FXNT Chunks by foxynotail)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Credits

- Concept based on [FXNT Chunks](https://github.com/foxynotail/fxnt-chunks-mod-fabric) by [foxynotail](https://github.com/foxynotail)
- Built with [Fabric](https://fabricmc.net/)
