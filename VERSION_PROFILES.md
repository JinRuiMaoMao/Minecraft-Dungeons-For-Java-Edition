# Version Profiles

This project builds both Fabric and Forge from one codebase.

## Default profile

- Minecraft: `1.20.1`
- Fabric + Forge: enabled

Run:

- `.\gradlew.bat :fabric:build`
- `.\gradlew.bat :forge:build`

## Override profile from command line

You can override key versions without editing files:

```powershell
.\gradlew.bat :fabric:build `
  -Pminecraft_version=1.20.1 `
  -Pyarn_mappings=1.20.1+build.10 `
  -Pfabric_version=0.92.2+1.20.1 `
  -Pminecraft_version_range_fabric=">=1.20 <=1.20.1"
```

```powershell
.\gradlew.bat :forge:build `
  -Pminecraft_version=1.20.1 `
  -Pforge_version=1.20.1-47.2.0 `
  -Pminecraft_version_range_forge="[1.20,1.20.2)"
```

Notes:

- `ranged_weapon_api_version` and other dependency versions are MC-version specific.
- For a new Minecraft target, update those dependency properties together.
