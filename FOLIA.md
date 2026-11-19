# Folia support

RoseStacker declares `folia-supported: true`. This document describes how the threading model works and what is
still unverified.

## Status

Implemented and covered by unit/static tests. **Not yet verified on a live Folia server.** Treat as work in progress
until it has been run and signed off by a human maintainer.

## Ownership rules

| Work                                   | Runs on                                     |
|----------------------------------------|---------------------------------------------|
| Touching an entity (stack/merge/tags)  | The entity's owning region (entity scheduler) |
| Touching blocks/spawners/chunks        | The region that owns the location/chunk       |
| Timers that only dispatch work         | Global region (RoseGarden scheduler)          |
| Locale files, translation downloads    | Async scheduler                               |

`dev.rosewood.rosestacker.utils.Folia` is the single boundary. All Folia-only API lives in `Folia$Impl`, which is
never loaded on classic servers, so legacy Spigot/Paper (1.16+) linkage is unchanged. On classic servers every method
falls back to the previous "primary thread / async" behavior.

## Behavior differences on Folia

- **Stacking passes** are no longer one async pass over a whole world. Each stacked entity/item gets one task on its
  own region per stack interval (at most one queued per entity). Merging only considers entities in chunks the current
  region owns; across a region border, merging happens once both are in the same region.
- **EntityCacheManager** does not build a world snapshot on Folia. Lookups read live, owned state.
- **Nametags/holograms** are computed per player on the player's region and only for entities that region owns.
- **Loot calculation** (`entity-death-event-run-async`) always runs on the entity's region on Folia.
- **Temporary stacking disable** (used while spawning) is thread local instead of a global flag.
- **Persistence**: stack sizes are written through to the entity during the stacking pass, since a Folia shutdown can
  disable the plugin before region tasks can be queued. Spawner/block data is saved on chunk unload and autosave.

## Tests

`./gradlew :Plugin:test` runs `FoliaDetectionTest` (detection flips with `RegionizedServer`, outer class has no
Folia-only linkage) and `FoliaOwnershipAuditTest` (no Bukkit scheduler/raw threads, no owner-less `runSync`, no
world-wide entity scans on Folia, plugin.yml flag).

## Known limitations / follow-ups

- Dynamic nametags skip entities across a region border for that pass.
- No live-server or integration test harness yet (MockBukkit has no Folia region model).
- Autosave interval is the safety net for spawner/block data on abnormal shutdown.
