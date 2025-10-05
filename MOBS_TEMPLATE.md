# Aetherius Custom Mobs Creation Guide

## Current Status

**Created:** 3 mob classes (Base + 2 examples)
**Remaining:** 140+ mobs to implement (100+ hostile, 4 passive, 32+ bosses)

## Mob System Overview

### Base Mob Class: `AetheriusMob`

All custom mobs extend `AetheriusMob` which provides:
- ✅ **Level System** - Mobs scale with level (health, damage, armor, XP)
- ✅ **Mob Types** - Common, Elite, Boss (with auto-scaling multipliers)
- ✅ **Special Abilities** - Override `useSpecialAbility()` for unique attacks
- ✅ **Custom Display Names** - Shows level and type (e.g., "[Elite] Shadow Wraith [Lv.15]")
- ✅ **Stat Scaling** - Automatic stat growth per level

## Creating a New Mob

### Step 1: Create Entity Class

```java
package com.aetheriusmmorpg.common.entity.hostile;

import com.aetheriusmmorpg.common.entity.AetheriusMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

public class YourMobEntity extends AetheriusMob {

    public YourMobEntity(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
        this.setMobLevel(10); // Set starting level
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AetheriusMob.createAttributes()
            .add(Attributes.MAX_HEALTH, 50.0D)
            .add(Attributes.MOVEMENT_SPEED, 0.25D)
            .add(Attributes.ATTACK_DAMAGE, 5.0D)
            .add(Attributes.ARMOR, 2.0D);
    }

    @Override
    protected void useSpecialAbility() {
        // Implement unique ability
        if (this.getTarget() != null) {
            // Your custom ability logic here
        }
    }

    @Override
    protected int getAbilityInterval() {
        return 100; // Ticks between ability uses
    }

    @Override
    protected int getAbilityCooldown() {
        return 200; // Cooldown after using ability
    }
}
```

### Step 2: Register Entity Type

Add to `ModEntities.java`:

```java
public static final RegistryObject<EntityType<YourMobEntity>> YOUR_MOB =
    ENTITIES.register("your_mob",
        () -> EntityType.Builder.of(YourMobEntity::new, MobCategory.MONSTER)
            .sized(0.6f, 1.8f)
            .build("your_mob"));
```

### Step 3: Register Attributes

In entity registration event:

```java
event.put(ModEntities.YOUR_MOB.get(), YourMobEntity.createAttributes().build());
```

### Step 4: Add Spawn Rules (Optional)

Configure natural spawning in biomes.

## Mob Examples from Documentation

### Hostile Mobs (100+)

#### Created:
- [x] **Shadow Wraith** (Level 10) - Phases through walls, life drain aura
- [x] **Frostbite Elemental** (Level 20) - Freezing aura, ice shards, blizzard

#### To Create:

**Low Level (1-20):**
- [ ] Gloom Spider (Lv.10) - Venomous spider, web attacks
- [ ] Cursed Soul (Lv.15) - Ghostly projectiles, possession
- [ ] Enchanted Pixie (Lv.25) - Illusion spells

**Mid Level (20-50):**
- [ ] Aqua Serpent (Lv.20) - Water spells, tidal waves
- [ ] Ember Imp (Lv.30) - Fireball thrower, teleportation
- [ ] Rock Golem (Lv.40) - Crushing blows, rock spikes
- [ ] Storm Elemental (Lv.50) - Lightning strikes, thunderstorms

**High Level (50-100):**
- [ ] Spectral Banshee (Lv.60) - Disorienting screams, debuffs
- [ ] Frost Wyvern (Lv.70) - Frost breath, aerial combat
- [ ] Clockwork Sentinel (Lv.80) - Precision strikes, heavy armor
- [ ] Mystic Treant (Lv.90) - Nature magic, self-healing
- [ ] Celestial Seraph (Lv.100) - Holy light attacks, blessings

### Passive Mobs

- [ ] **Harmony Sprite** - Glowing creature, health restoration aura
- [ ] **Wanderlust Deer** - Guides lost travelers, senses danger
- [ ] **Steamstrider** - Mechanical companion, fire/lava resistance
- [ ] **Luminescent Nymph** - Healing abilities, regeneration buffs

### Boss Mobs (32+)

#### Dungeon Bosses:
- [ ] **Shadowfang, the Nightstalker** - Dual blades, shadow abilities
- [ ] **Molten Core, the Fire Elemental** - Lava attacks, eruption
- [ ] **Siren, the Enchantress** - Mind control songs, water whip
- [ ] **Grimm, the Undying** - Necromancy, life drain, resurrection
- [ ] **Aetherius, the Stormbringer** - Lightning, wind attacks

## Ability Examples

### Phasing (Shadow Wraith)
```java
private void activatePhase() {
    this.noPhysics = true; // Pass through blocks
    this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 60, 0));
    // Spawn particles, play sounds
}
```

### Elemental Attack (Frostbite Elemental)
```java
private void shootIceShard() {
    Snowball projectile = new Snowball(this.level(), this);
    projectile.shoot(targetX, targetY, targetZ, 1.5F, 1.0F);
    this.level().addFreshEntity(projectile);
}
```

### Area Effect (Frostbite Elemental)
```java
private void summonBlizzard() {
    // Damage all nearby entities
    this.level().getEntitiesOfClass(LivingEntity.class,
        this.getBoundingBox().inflate(4.0D))
        .forEach(entity -> {
            entity.hurt(source, 3.0F);
            entity.setTicksFrozen(entity.getTicksFrozen() + 40);
        });
}
```

### Life Drain (Shadow Wraith)
```java
if (distance < 3.0D && this.tickCount % 20 == 0) {
    this.getTarget().hurt(this.damageSources().magic(), 2.0F);
    this.heal(1.0F);
}
```

## Mob Type Modifiers

Set mob type to apply automatic scaling:

```java
mob.setMobType("elite");  // 2x health/damage, 3x XP
mob.setMobType("boss");   // 5x health, 3x damage, 10x XP
mob.setMobType("common"); // Normal stats
```

## Common Particle Effects

```java
// Fire
ParticleTypes.FLAME
ParticleTypes.SOUL_FIRE_FLAME

// Ice/Frost
ParticleTypes.SNOWFLAKE
ParticleTypes.ITEM_SNOWBALL

// Magic
ParticleTypes.ENCHANT
ParticleTypes.PORTAL
ParticleTypes.END_ROD

// Dark/Shadow
ParticleTypes.SMOKE
ParticleTypes.LARGE_SMOKE
ParticleTypes.SOUL

// Electric
ParticleTypes.ELECTRIC_SPARK
ParticleTypes.FIREWORK

// Nature
ParticleTypes.FALLING_SPORE_BLOSSOM
ParticleTypes.SPORE_BLOSSOM_AIR
```

## Common Sound Events

```java
// Combat
SoundEvents.PLAYER_ATTACK_STRONG
SoundEvents.PLAYER_ATTACK_SWEEP
SoundEvents.PLAYER_ATTACK_CRIT

// Magic
SoundEvents.EVOKER_CAST_SPELL
SoundEvents.ILLUSIONER_CAST_SPELL
SoundEvents.PORTAL_TRAVEL

// Elemental
SoundEvents.BLAZE_SHOOT          // Fire
SoundEvents.SNOW_GOLEM_SHOOT     // Ice
SoundEvents.LIGHTNING_BOLT_IMPACT // Electric

// Teleport/Phase
SoundEvents.ENDERMAN_TELEPORT

// Roars/Screams
SoundEvents.WARDEN_ROAR
SoundEvents.WOLF_HOWL
```

## Spawn Configuration

Add natural spawning (later integration):

```json
{
  "mob": "aetherius:shadow_wraith",
  "biomes": ["dark_forest", "deep_dark"],
  "min_group": 1,
  "max_group": 3,
  "weight": 10,
  "min_level": 10,
  "max_level": 15
}
```

## Drop Configuration

Add custom drops:

```java
@Override
protected void dropCustomDeathLoot(DamageSource source, int looting, boolean wasRecentlyHit) {
    super.dropCustomDeathLoot(source, looting, wasRecentlyHit);

    // Drop custom items
    this.spawnAtLocation(ModItems.SPECTRAL_ESSENCE.get());

    // Chance-based drops
    if (this.random.nextFloat() < 0.25F) {
        this.spawnAtLocation(ModItems.RARE_DROP.get());
    }
}
```

## Next Steps

1. ✅ Base mob system created
2. ✅ Example mobs created (Shadow Wraith, Frostbite Elemental)
3. ⏳ Create remaining 140+ mobs using template
4. ⏳ Configure spawn rules
5. ⏳ Add custom drops and loot tables
6. ⏳ Create mob models and textures
7. ⏳ Balance testing

## Quick Reference

**Scaling Formula:**
- Health: base + (level × 5)
- Damage: base + (level × 0.5)
- Armor: level × 0.2
- XP: 10 + (level × 5)

**Tick Conversion:**
- 20 ticks = 1 second
- 100 ticks = 5 seconds
- 200 ticks = 10 seconds

**Mob Categories:**
- Common: 1x multiplier
- Elite: 2x health/damage, 3x XP
- Boss: 5x health, 3x damage, 10x XP
