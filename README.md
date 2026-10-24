[![Modrinth Downloads](https://img.shields.io/modrinth/dt/1Ls2pii3?logo=modrinth&style=for-the-badge)](https://modrinth.com/mod/dap-ur-homies-unofficially-neoforged) [![CurseForge Downloads](https://img.shields.io/curseforge/dt/1597240?logo=curseforge&style=for-the-badge)](https://www.curseforge.com/minecraft/mc-mods/dap-ur-homies-unofficially-neoforged)

# Dap ur HOMIES

Cooperative Multiplayer Moves for Minecraft
This is a Neoforge mod that adds cooperative player-to-player interaction mechanics. Dap your friends, high-five, hug, grab & throw, and pull off combo chains with perfectly timed QTEs.

This branch targets Minecraft Java **26.2** with **NeoForge 26.2.0.93 or later in the 26.2 series**.
The Minecraft 1.21.1 version remains on [`main`](https://github.com/MXUMX/Dap-ur-Homies-neoforged/tree/main).

# Includes all features of the original Dap ur Homies mod: 

- Charged dap with `G`  
- Dap timing tiers  
- Dap combo chain  
- Dap hold/group dap  
- Face-to-face / normal dap  
- Fire dap combo using `J`  
- High five with `H`  
- High five combo/hug/QTE hug  
- Huddle  
- Grab players with `R`  
- Throw grabbed players with `T`  
- Human shield mode with `V`  
- Spin/helicopter-style grab  
- Ground pound  
- Push interaction  
- Fall catch  
- Fall dap  
- Squash animation  
- Mario jump off another player  
- Kick/drop kick  
- Clap  
- Hug with `F`  
- Sit mechanic with `/sit`  
- Mahito (potion effect)  
- First-person animation support  
- Custom sounds  
- Custom player animations through Player Animation Library  
- Config file: `config/coopmoves.json`

### Requirements for 26.2:

- Minecraft Java 26.2 and Java 25
- NeoForge 26.2.0.93 or newer for Minecraft 26.2
- [Player Animation Library](https://modrinth.com/mod/player-animation-library), NeoForge build `1.2.8+mc.26.2` or newer compatible 26.2 build

Install the mod and PAL on both the server and each client. Fabric API, Forgified Fabric API, and Sinytra Connector are not required.

### Building

With JDK 25 selected in `JAVA_HOME`, run `./gradlew build`.
The mod JAR is written to `build/libs/coopmod-1.0.0+mc26.2.jar`.
Development clients and servers use separate directories under `build/run-26.2/`.

Run `./gradlew runGameTestServer` for the grab/dismount regression test. It checks forced
player mounting and verifies that both players' grab state and poses reset after dismounting.
The development test mod is excluded from the release JAR.
