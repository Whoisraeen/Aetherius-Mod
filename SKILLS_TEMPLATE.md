# Aetherius Skills Creation Guide

## Current Status

**Created:** 9 skills
**Remaining:** 51 skills to implement

## Skill Template

Use this template to create new skills:

```json
{
  "id": "aetherius:skill_name",
  "name": "Skill Display Name",
  "description": "What the skill does",
  "icon": "minecraft:item_id",
  "cooldown_ticks": 100,
  "mana_cost": 25,
  "cast_time_ticks": 20,
  "max_range": 15,
  "required_level": 5,
  "effects": [
    {
      "type": "damage|heal|dot|hot|buff|debuff|stun|knockback",
      "amount": 10.0,
      "duration_ticks": 100,
      "particles": "minecraft:particle_type"
    }
  ],
  "sound": "minecraft:sound_event"
}
```

## Skills by Class

### Aelorians - Warblade
- [x] Swordsmanship - Attack damage buff
- [ ] Fist of Fury - Multi-hit melee combo
- [ ] Polearm Dance - Spinning AOE attack
- [ ] Hammerfall Mastery - Heavy single-target damage
- [x] Lion's Roar - AOE stun
- [x] Wind Slash - Single target stun

### Aelorians - Spellweaver
- [x] Pyroblast - Fire damage + DOT
- [ ] Frostbite - Water damage + slow debuff
- [ ] Earthshatter - Earth AOE damage
- [ ] Ice Dragon Fury - Powerful AOE water spell
- [ ] Arcane Mastery - Magic damage buff

### Lycans - Beastlord
- [ ] Alpha's Call - Party buff
- [ ] Beastly Speed - Movement speed buff
- [ ] Tidal Strike - Water melee attack
- [x] Thunderclap - Lightning AOE (already exists)
- [ ] Spirit Guard - Damage reduction

### Lycans - Shapeshifter
- [ ] Poison Dart - Poison DOT
- [ ] Ironbark Curse - Defense debuff
- [ ] Savage Assault - Damage amplification debuff
- [ ] Sprint of the Wild - Speed buff
- [ ] Parasite Storm - Multi-target DOT

### Celestials - Windrunner
- [ ] Swift Arrow - Rapid ranged attack
- [ ] Gale Shot - Wind-powered arrow
- [ ] Thunderbolt - Lightning strike
- [ ] Sharpened Talon - Critical strike buff
- [ ] Arrow Rain - AOE ranged attack

### Celestials - Seraph
- [ ] Pureheart Blessing - Party buff
- [ ] Divine Heal - Strong single-target heal
- [ ] Radiant Beam - Light damage
- [ ] Flowing Renewal - HOT effect
- [ ] Divine Tempest - AOE holy damage

### Aquafae - Shadowblade
- [ ] Dual Strike - Double attack
- [ ] Stinger - Poison melee
- [ ] Frenzied Slash - Attack speed buff
- [ ] Abyssal Slash - Dark damage
- [ ] Phantom Escape - Invisibility

### Aquafae - Mindbender
- [ ] Aqua Burst - Water projectile
- [ ] Spirit Wave - AOE knockback
- [ ] Healing Bubble - AOE heal
- [ ] Soulfire - Damage reflection
- [ ] Oceanic Trance - Mana regeneration

### Terrans - Sentinel
- [ ] Heartpiercer - Stun attack
- [ ] Samurai's Edge - Critical strike
- [ ] Spirit Break - Defense debuff
- [ ] Northern Waltz - Dodge buff
- [ ] Blade Harmony - Attack combo

### Terrans - Nature's Oracle
- [ ] Cloudburst - Rain healing
- [ ] Petal Rain - HOT effect
- [ ] Nature's Shield - Defense buff
- [ ] Verdant Snare - AOE stun + damage
- [ ] Guardian's Grace - Party shield

### Umbral - Nightblade
- [ ] Dusk Strike - Basic shadow attack
- [ ] Dark Veil - Stealth
- [ ] Night Hunter - Crit rate buff
- [ ] Crescent Strike - Curved AOE
- [ ] Shadow Step - Teleport + stun

### Umbral - Tempestcaller
- [ ] Frost Thunder - Ice + lightning hybrid
- [ ] Lunar Wave - Moon magic damage
- [ ] Arcane Tempest - Magic storm AOE
- [ ] Wind Whisper - Evasion buff
- [ ] Elemental Aegis - Elemental resistance buff

## Effect Types Reference

### Damage
```json
{
  "type": "damage",
  "amount": 15.0,
  "damage_type": "physical|magic|fire|ice|lightning|holy|dark"
}
```

### Heal
```json
{
  "type": "heal",
  "amount": 20.0,
  "particles": "minecraft:heart"
}
```

### DOT (Damage Over Time)
```json
{
  "type": "dot",
  "amount": 3.0,
  "duration_ticks": 100,
  "tick_interval": 20,
  "particles": "minecraft:flame"
}
```

### HOT (Heal Over Time)
```json
{
  "type": "hot",
  "amount": 2.0,
  "duration_ticks": 100,
  "tick_interval": 20,
  "particles": "minecraft:heart"
}
```

### Buff
```json
{
  "type": "buff",
  "attribute": "attack_damage|movement_speed|armor|attack_speed",
  "value": 5.0,
  "duration_ticks": 200,
  "particles": "minecraft:enchant"
}
```

### Debuff
```json
{
  "type": "debuff",
  "attribute": "movement_speed|armor|attack_damage",
  "value": -3.0,
  "duration_ticks": 100,
  "particles": "minecraft:smoke"
}
```

### Stun
```json
{
  "type": "stun",
  "duration_ticks": 60,
  "particles": "minecraft:flash"
}
```

### Knockback
```json
{
  "type": "knockback",
  "strength": 2.0,
  "vertical": 0.5,
  "particles": "minecraft:explosion"
}
```

## Particle Types

Common Minecraft particles:
- `minecraft:flame` - Fire
- `minecraft:soul` - Soul fire/magic
- `minecraft:heart` - Healing
- `minecraft:enchant` - Magic/buffs
- `minecraft:crit` - Critical hits
- `minecraft:sweep_attack` - Melee swipes
- `minecraft:sonic_boom` - Powerful effects
- `minecraft:flash` - Stuns
- `minecraft:explosion` - AOE
- `minecraft:smoke` - Debuffs
- `minecraft:end_rod` - Light magic
- `minecraft:dragon_breath` - Poison/dark
- `minecraft:cloud` - Wind/air

## Sound Events

Common sounds:
- `minecraft:entity.player.attack.strong` - Melee
- `minecraft:entity.player.attack.sweep` - AOE melee
- `minecraft:entity.blaze.shoot` - Fire magic
- `minecraft:entity.lightning_bolt.impact` - Lightning
- `minecraft:entity.warden.roar` - Powerful skills
- `minecraft:block.bell.use` - Buffs
- `minecraft:entity.evoker.cast_spell` - Magic
- `minecraft:entity.elder_guardian.curse` - Debuffs
- `minecraft:entity.arrow.shoot` - Ranged
- `minecraft:entity.player.levelup` - Heals

## Quick Creation Script

To quickly create multiple skills, you can use this pattern:

1. Copy the template
2. Replace placeholders:
   - `skill_name` → lowercase_skill_name
   - `Skill Display Name` → Actual name
   - Adjust values for balance
3. Save to `src/main/resources/data/aetherius/skills/skill_name.json`

## Balance Guidelines

- **Cooldown**: 20 ticks = 1 second
- **Mana Cost**: 10-50 for regular skills, 75-100 for ultimates
- **Damage**: 5-20 for regular, 30-50 for ultimates
- **Duration**: 100 ticks (5s) typical for buffs/debuffs
- **Range**: 10-15 for melee, 20-30 for ranged

## Next Steps

1. Create remaining 51 skills following the template
2. Balance test each skill in-game
3. Add skill icons (custom textures)
4. Create skill trees linking skills by level
5. Add skill unlocks per class progression
