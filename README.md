# VillagerKeeper

A NeoForge 1.21.1 mod: **delays the profession reset after curing zombie villagers**.

## Features

- After curing a zombie villager, the profession is kept for a **watch window** (default 30 seconds, configurable)
- During the window the villager keeps its profession (appearance, profession name and trades) and can trade immediately, including the vanilla cure discount
- After the window expires, vanilla behavior resumes: villagers who never traded and have no workstation reset to none

## The vanilla problem

At the moment of curing, `ZombieVillager.finishConversion` copies profession data to the new villager,
but the new villager has a brand-new brain (**no JOB_SITE workstation memory**), so 1 tick later the
`ResetProfession` behavior resets the profession to none.

`ResetProfession` triggers only when **all** of the following are true:

1. The profession is not `none` / `nitwit`
2. The `JOB_SITE` memory is missing
3. `villagerXp == 0`
4. The level is ≤ 1

A cured villager hits every single one.

## How it works

**At the moment of curing** (`LivingConversionEvent.Post`): the watch-window expiry time is written to the
villager's persistent data (`villagerkeeper_window_end`, saved with the world).

**During the window**: a Mixin adds a condition to `ResetProfession`'s decision method (`lambda$create$0`)
— while the current game time has not reached the window expiry time, the whole behavior is skipped and the
profession is kept; after expiry the marker naturally lapses and the vanilla conditions apply again.

No entity data is modified and no timers are needed — expiry is handled by the condition itself.

Two exceptions match vanilla semantics (naturally guaranteed by the vanilla conditions after expiry):
- Villagers who **traded** during the window (xp > 0): vanilla never resets them
- Villagers who **claimed a workstation** during the window: `JOB_SITE` exists, so `ResetProfession` does not trigger

## Configuration

`run/config/villagerkeeper-common.toml`:

```toml
[general]
# Profession watch-window duration after curing (seconds, default 30)
# After expiry, revert to vanilla behavior: villagers who never traded and have no workstation reset to none
# Set to 0 to restore vanilla behavior immediately at the moment of curing
resetDelaySeconds = 30
```

## Building

```bash
./gradlew build
```

The artifact is `build/libs/villagerkeeper-0.0.1.jar`; drop it into the `mods` folder.

## In-game verification

1. Summon a zombie villager with a profession (fletcher as an example):
   ```
   /summon zombie_villager ~ ~ ~ {VillagerData:{profession:"minecraft:fletcher"}}
   ```
2. Apply weakness and cure it with a golden apple:
   ```
   /effect give @e[type=zombie_villager] weakness 100 1
   ```
   (then use a golden apple on the zombie villager)
3. During the watch window (default 30 seconds): the villager keeps the fletcher profession and can trade (with the cure discount)
4. After the window expires: villagers that never traded become unemployed; those who traded or claimed a workstation keep their profession
5. Control: without this mod, the same procedure leaves the villager unemployed after 1 tick

## Notes

- Only handles the zombie villager → villager curing direction; the reverse (villager → zombie) already copies profession data in vanilla
- One event listener + one Mixin (`ResetProfessionMixin`); the window expiry time is stored in persistent data,
  so the window remains valid across server restarts and chunk unloads

## License

[GNU Lesser General Public License v3.0](LICENSE) (LGPL-3.0).
Copyright (c) 2026 pasze888
