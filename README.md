# Create-UfoPort

An unofficial port of [Create Fabric](https://modrinth.com/mod/create-fabric) for Minecraft 1.21 / 1.21.1.

Originally ported to 1.21 by Vlad2509; patches and bug fixes by BlazeBSC.

## Installation

Download the mod from [Releases](https://github.com/blazebsc/Create-UfoPort/releases/) and install the required dependencies:

- [Fabric Loader](https://fabricmc.net/use/installer/) (>= 0.16.0)
- [Fabric API](https://modrinth.com/mod/fabric-api) (>= 0.100.7)
- [Forge Config API Port](https://modrinth.com/mod/forge-config-api-port) (>= 21.0.5)

## Known Incompatibilities

- [Farmer's Delight Refabricated](https://modrinth.com/mod/farmers-delight-refabricated)
- Any outdated Create add-ons listed in the `breaks` section of `fabric.mod.json`.
  Always use the latest builds of Create addons when using UfoPort.

If you find any incompatibility with a mod, feel free to open an issue or send a pull request.

## Building from Source

```
git clone https://github.com/blazebsc/Create-UfoPort.git
cd Create-UfoPort
./gradlew build
```

The output jar is in `build/libs/`.

## Credits
- [1.21-1.21.1 Port Of Create Fabric (upstream)](https://github.com/vlad250906/Create-UfoPort)
- [Create Fabric](https://github.com/Fabricators-of-Create/Create)
- [Porting Lib](https://github.com/Fabricators-of-Create/Porting-Lib)
- [Flywheel](https://github.com/Engine-Room/Flywheel)
- [Milk Lib](https://github.com/TropheusJ/milk-lib)
- [Registrate Refabricated](https://github.com/Fabricators-of-Create/Registrate-Refabricated)
