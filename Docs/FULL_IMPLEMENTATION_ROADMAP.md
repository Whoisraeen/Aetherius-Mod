# Aetherius MMORPG - Full PWI Parity Implementation Roadmap

## 📋 **Project Scope**
Implementing ALL features from "Aetherius MMO Overview.md" to achieve full Perfect World International (PWI) parity in Minecraft 1.20.1 Forge.

**Key Requirement**: Everything must be GUI/menu-based (no commands except debug). All skills need animation frameworks, all mobs need texture/model/sound frameworks.

---

## 🎯 **Implementation Status Overview**

### ✅ Already Complete (Foundation - 89%)
- Character Creation & Onboarding
- Core RPG Systems (Races, Classes, Attributes)
- Skill System Architecture (9/60 skills created)
- Quest System
- Party/Squad System (PWI-style with XP sharing)
- Instanced Dungeons (3/32 created)
- Friends System
- Basic NPC System
- Basic Mob System (2/140+ created)
- Networking Infrastructure

### 🔄 In Progress
- **Guild/Faction System** - Core classes created, needs packets + GUIs

### ⏳ To Be Implemented (Remaining ~65% of content + features)

---

## 📊 **Complete Feature Breakdown**

### **PHASE 1: Social Foundation** (Critical - ~2-3 sessions)

#### 1.1 Guild/Faction System
**Files to Create:**
- ✅ `Guild.java` - Core guild class
- ✅ `GuildMember.java` - Member data
- ✅ `GuildRank.java` - Rank & permissions
- ✅ `GuildPermission.java` - Permission enum
- ✅ `GuildManager.java` - Server-side management
- ⏳ `C2SGuildActionPacket.java` - Client actions
- ⏳ `S2CGuildDataSyncPacket.java` - Sync guild data
- ⏳ `S2CGuildInvitePacket.java` - Guild invitations
- ⏳ `ClientGuildData.java` - Client storage
- ⏳ `GuildScreen.java` - Main guild GUI
- ⏳ `GuildRosterScreen.java` - Member roster
- ⏳ `GuildBankScreen.java` - Guild bank GUI
- ⏳ `GuildSettingsScreen.java` - Guild settings
- ⏳ `GuildCreateScreen.java` - Guild creation GUI
- ⏳ Integration with keybind (G key)

**Features:**
- Guild creation with name, tag, crest
- 6 ranks: Leader, Marshal, Executioner, Commander, Officer, Member
- 20+ permissions for granular control
- Guild bank (gold + resources)
- Guild leveling (1-10) with experience
- Territory management integration
- Guild announcements
- Member contribution tracking
- Guild chat channel

#### 1.2 Enhanced Multi-Channel Chat
**Files to Create:**
- `ChatManager.java` - Server-side chat routing
- `ChatChannel.java` - Channel enum (Global, Local, Guild, Party, Trade, PM, System)
- `C2SChatMessagePacket.java` - Send chat
- `S2CChatMessagePacket.java` - Receive chat
- `ChatScreen.java` - Enhanced chat GUI with tabs
- `ChatSettings.java` - Per-channel settings
- Integration with existing party/guild systems

**Features:**
- 7 channels: Global, Local (range-based), Guild, Party, Trade, PM, System
- Chat tabs customization
- Chat history (scrollback)
- Mute/block functionality
- Profanity filter
- Chat bubbles above players (optional)
- Emote system

#### 1.3 Marriage System
**Files to Create:**
- `Marriage.java` - Marriage data
- `MarriageManager.java` - Server management
- `C2SMarriageActionPacket.java` - Marriage actions
- `S2CMarriageDataPacket.java` - Sync marriage data
- `MarriageProposalScreen.java` - Proposal GUI
- `MarriageCeremonyEvent.java` - Wedding event
- `MarriageQuestHandler.java` - Joint quests

**Features:**
- Proposal system via GUI
- Wedding ceremony events
- Joint quests for married couples
- Special couple abilities (teleport to spouse, shared XP bonus)
- Couple pets
- Marriage anniversary rewards

#### 1.4 Master/Apprentice System
**Files to Create:**
- `MentorshipManager.java` - Server management
- `Mentorship.java` - Mentor-apprentice relationship
- `C2SMentorActionPacket.java` - Actions
- `S2CMentorDataPacket.java` - Data sync
- `MentorScreen.java` - Mentor GUI
- `MentorQuestHandler.java` - Special quests

**Features:**
- Master can mentor up to 3 apprentices
- Apprentice must be 20+ levels lower
- Shared quests and rewards
- Graduation ceremony at level cap
- Master receives rewards when apprentice graduates
- Mentorship chat channel

---

### **PHASE 2: Companion Systems** (High Priority - ~2-3 sessions)

#### 2.1 Pet System
**Files to Create:**
- `Pet.java` - Base pet entity
- `PetData.java` - Pet stats and skills
- `PetManager.java` - Pet ownership tracking
- `TameableMob.java` - Mobs that can be tamed
- `PetInventory.java` - Pet equipment
- `PetSkill.java` - Pet-specific abilities
- `C2SPetActionPacket.java` - Pet commands
- `S2CPetDataPacket.java` - Pet data sync
- `PetScreen.java` - Pet management GUI
- `PetBagScreen.java` - Pet inventory (10 slots)
- 20+ tameable mob types

**Features:**
- Taming system (requires specific items/quests)
- Pet leveling (1-100)
- Pet skills (3-5 per pet type)
- Pet evolution (3 stages)
- Pet equipment (collar, saddle, armor)
- Pet feeding (affects happiness/stats)
- Pet bag (store up to 10 pets, summon 1 at a time)
- Pet auto-loot
- Pet tanking/healing roles
- Rare/legendary pets

#### 2.2 Mount System
**Files to Create:**
- `Mount.java` - Base mount entity
- `FlyingMount.java` - Flying mount type
- `MountData.java` - Mount stats
- `MountManager.java` - Mount ownership
- `C2SMountActionPacket.java` - Mount controls
- `S2CMountDataPacket.java` - Mount sync
- `MountScreen.java` - Mount GUI
- `MountBagScreen.java` - Mount collection
- 30+ mount types

**Features:**
- Ground mounts (speed boost)
- Flying mounts (enables flight)
- Water mounts (underwater breathing)
- Mount leveling (affects speed)
- Mount equipment (saddles, armor)
- Mount bag (collection system)
- Mount rental NPCs
- Rare/event mounts
- Mount transformation items

#### 2.3 Genie System
**Files to Create:**
- `Genie.java` - Genie entity/companion
- `GenieData.java` - Stats and skills
- `GenieAffinityType.java` - Affinity types
- `GenieSkill.java` - Genie-specific skills
- `GenieManager.java` - Server management
- `C2SGenieActionPacket.java` - Genie commands
- `S2CGenieDataPacket.java` - Genie sync
- `GenieScreen.java` - Genie management GUI
- `GenieCraftingScreen.java` - Create genies

**Features:**
- Genie creation (crafting mini-game)
- 5 affinity types (Metal, Wood, Water, Fire, Earth)
- Genie leveling (1-100)
- 20+ genie skills
- Stat allocation system
- Genie equipment
- Genie fusion (combine for better stats)
- Genie stamina system

---

### **PHASE 3: Economy & Crafting** (High Priority - ~2-3 sessions)

#### 3.1 Crafting System
**Files to Create:**
- `CraftingRecipe.java` - Recipe data
- `CraftingStation.java` - Crafting stations
- `CraftingManager.java` - Recipe registry
- `CraftingCategory.java` - Categories (Weapon, Armor, Potion, etc.)
- `C2SCraftItemPacket.java` - Craft request
- `S2CCraftingDataPacket.java` - Recipe sync
- `CraftingScreen.java` - Main crafting GUI
- `RefinementScreen.java` - Equipment upgrading
- `SocketingScreen.java` - Gem socketing
- 200+ crafting recipes
- 50+ refinement materials

**Features:**
- 10 crafting professions (Blacksmith, Tailor, Alchemist, etc.)
- Profession leveling (1-10)
- Equipment crafting
- Potion/consumable crafting
- Material gathering
- Refinement system (+1 to +12)
- Socket system (add gems for stats)
- Crafting success chance
- Special/rare recipes
- Crafting stations in major cities

#### 3.2 Trading & Auction House
**Files to Create:**
- `Trade.java` - Trade session
- `TradeManager.java` - Server management
- `AuctionListing.java` - Auction item
- `AuctionHouse.java` - Auction data
- `AuctionHouseManager.java` - Server management
- `C2STradeActionPacket.java` - Trade actions
- `C2SAuctionActionPacket.java` - Auction actions
- `S2CTradeDataPacket.java` - Trade sync
- `S2CAuctionDataPacket.java` - Auction sync
- `TradeScreen.java` - Trade window
- `AuctionHouseScreen.java` - Auction GUI
- `PersonalShopScreen.java` - Player shop

**Features:**
- Direct player trading
- Trade windows with gold + item slots
- Auction house (search, filter, bid)
- Buyout prices
- Auction duration (1h, 12h, 24h, 48h)
- Auction house fee (5% of sale price)
- Personal player shops (stall mode)
- Shop search function
- Trade history
- Auction notifications

---

### **PHASE 4: Combat & Animation** (Critical - ~3-4 sessions)

#### 4.1 Flying System
**Files to Create:**
- `FlightManager.java` - Server flight control
- `FlightData.java` - Player flight state
- `FlyingMovement.java` - Custom movement handling
- `C2SFlightActionPacket.java` - Flight controls
- `S2CFlightDataPacket.java` - Flight sync
- `FlightSettings.java` - Flight config per mount
- Integration with mount system

**Features:**
- Toggle flight with flying mounts
- Altitude limits
- Flight speed based on mount
- Flight stamina/energy system
- Flight prohibited in dungeons/pvp zones
- Aerial dodge mechanics
- Vertical movement controls

#### 4.2 Aerial Combat
**Files to Create:**
- `AerialCombatHandler.java` - Air combat logic
- `AerialSkill.java` - Air-specific skills
- `AerialDamageCalculation.java` - Damage formulas
- Integration with skill system
- 20+ aerial combat skills

**Features:**
- Combat while flying
- Aerial-specific skills
- Dismount on damage threshold
- Air-to-ground attacks
- Air-to-air PvP
- Aerial boss fights

#### 4.3 Skill Animation Framework
**Files to Create:**
- `SkillAnimation.java` - Animation controller
- `AnimationData.java` - Animation definitions
- `ParticleEffect.java` - Particle systems
- `SoundEffect.java` - Sound management
- `AnimationRenderer.java` - Client rendering
- Animation JSON files for each skill

**Features:**
- JSON-based animation definitions
- Particle effect system (color, shape, motion)
- Sound effect system (skill cast, hit, complete)
- Player model animations (cast pose, swing, etc.)
- Projectile rendering
- AOE effect visualization
- Hit impact effects
- Placeholder system for adding custom animations later

---

### **PHASE 5: Progression Systems** (Medium Priority - ~2 sessions)

#### 5.1 Rebirth System
**Files to Create:**
- `RebirthManager.java` - Server management
- `RebirthData.java` - Rebirth stats
- `RebirthRewards.java` - Reward tiers
- `C2SRebirthActionPacket.java` - Rebirth request
- `S2CRebirthDataPacket.java` - Data sync
- `RebirthScreen.java` - Rebirth GUI
- `RebirthNPC.java` - Rebirth facilitator

**Features:**
- Rebirth at max level (100)
- 10 rebirth levels
- Reset to level 1 with stat bonuses
- New skill unlocks per rebirth
- Cosmetic rewards (titles, auras)
- Rebirth quests
- Increased stat growth

#### 5.2 Spiritual Cultivation System
**Files to Create:**
- `CultivationManager.java` - Server management
- `CultivationLevel.java` - Cultivation stages
- `SpiritEnergy.java` - Spirit resource
- `CultivationSkill.java` - Cultivation skills
- `C2SCultivationPacket.java` - Actions
- `S2CCultivationPacket.java` - Data sync
- `CultivationScreen.java` - Cultivation GUI
- 10 cultivation stages
- 30+ cultivation skills

**Features:**
- Spirit energy resource (like mana)
- 10 cultivation stages (Mortal → Immortal)
- Cultivation quests per stage
- Special cultivation skills
- Meditation system (regain spirit energy)
- Breakthrough challenges
- Cultivation-based stat bonuses

#### 5.3 Morai System (End-Game)
**Files to Create:**
- `MoraiManager.java` - Server management
- `MoraiQuest.java` - Morai quests
- `MoraiSkill.java` - Morai skills
- `MoraiOrder.java` - 3 factions
- `C2SMoraiActionPacket.java` - Actions
- `S2CMoraiDataPacket.java` - Data sync
- `MoraiScreen.java` - Morai GUI
- 50+ morai quests
- 20+ morai skills

**Features:**
- Unlocks at level 90+
- 3 Morai orders (Light, Dark, Neutral)
- Order-specific quests
- Order-specific skills
- Morai reputation system
- Special Morai equipment
- Morai PvP zone

---

### **PHASE 6: Territory & World** (High Priority - ~4-5 sessions)

#### 6.1 Territory Wars System
**Files to Create:**
- `Territory.java` - Territory data
- `TerritoryManager.java` - Server management
- `TerritoryWar.java` - War event
- `TerritoryBattle.java` - Battle instance
- `TowerDefense.java` - Defense structures
- `C2STerritoryActionPacket.java` - Actions
- `S2CTerritoryDataPacket.java` - Data sync
- `TerritoryMapScreen.java` - Territory map GUI
- `TerritoryWarScreen.java` - War status GUI
- 8 territories (as defined in overview)

**Features:**
- 8 capturable territories
- Weekly territory wars (scheduled)
- Capture mechanics (hold control points)
- Defense structures (towers, walls)
- War preparation phase
- Territory bonuses (resources, XP)
- Territory tax system
- War declaration GUI
- Territory alliance system

#### 6.2 Major Cities Implementation
**Files to Create:**
- Structure files for 14 cities:
  1. Verdantra (Elf city)
  2. Thornspire (Dark Elf)
  3. Solanium (Celestial)
  4. Abyssia (Merfolk/underwater)
  5. Grimforge (Dwarf/volcano)
  6. Skyreach (Avian/floating)
  7. Nomad's Plateau (Desert)
  8. Frosthold (Ice Giant/tundra)
  9. Beastden (Beastkin/jungle)
  10. Shadowfen (Reptilian/swamp)
  11. Crystalight (Crystal caverns)
  12. Ironhall (Human/industrial)
  13. Echowind (Plains/musical)
  14. Mysthaven (Wizard sanctuary)

- `City.java` - City data
- `CityManager.java` - City management
- `CityNPC.java` - City-specific NPCs
- `CityTeleporter.java` - City teleport system
- Spawn points for each race

**Features:**
- Unique architecture per city
- Race-specific starting cities
- City amenities (bank, auction, trainers, merchants)
- City teleport network
- City guards (faction NPCs)
- City quests
- City reputation system

#### 6.3 Custom Biomes
**Files to Create:**
- Biome definitions for:
  - Enchanted Grove
  - Mystic Ruins
  - Celestial Peaks
  - Aquraan Abyss
  - Terravore Enclave
  - Inferno Peaks
  - Frostfall Tundra
  - Whispering Meadows
  - Clockwork Caverns
  - Luminous Marshlands

- `CustomBiome.java` - Biome base class
- `BiomeFeatures.java` - Custom features
- `BiomeGeneration.java` - World gen integration

---

### **PHASE 7: Content Creation** (Ongoing - ~10+ sessions)

#### 7.1 Skills (51 remaining of 60 total)
**Template**: `SKILLS_TEMPLATE.md`

**Per Class Skills Needed:**
- Warblade: 6 skills (0/6)
- Spellweaver: 5 skills (2/5 - Pyroblast, Arcane Bolt)
- Beastlord: 5 skills (2/5 - Thunderclap, Lion's Roar)
- Shapeshifter: 5 skills (0/5)
- Windrunner: 5 skills (0/5)
- Seraph: 5 skills (1/5 - Healing Touch)
- Shadowblade: 5 skills (0/5)
- Mindbender: 5 skills (0/5)
- Sentinel: 5 skills (0/5)
- Nature's Oracle: 5 skills (0/5)
- Nightblade: 5 skills (0/5)
- Tempestcaller: 5 skills (0/5)

**Each skill needs:**
- JSON data file
- Animation framework entry
- Particle effects definition
- Sound effects definition
- Icon (placeholder or actual)

#### 7.2 Mobs (138+ remaining of 140+ total)
**Template**: `MOBS_TEMPLATE.md`

**Mob Categories:**
- **Common Mobs** (100+):
  - Gloom Spider, Aqua Serpent, Ember Imp, Rock Golem, Storm Elemental, Spectral Banshee, Frost Wyvern, Clockwork Sentinel, Mystic Treant, Celestial Seraph, Cursed Soul, Enchanted Pixie, etc.
- **Elite Mobs** (30+):
  - Powered-up versions of common mobs
- **Dungeon Bosses** (32+):
  - Multi-phase bosses for each dungeon
- **World Bosses** (10+):
  - Large-scale open-world bosses
- **Passive Mobs** (4):
  - Harmony Sprite, Wanderlust Deer, Steamstrider, Luminescent Nymph

**Each mob needs:**
- Java class extending AetheriusMob
- Special abilities
- Texture framework (placeholder ResourceLocation)
- Model framework (placeholder)
- Sound framework (placeholder)
- Loot tables
- Spawn rules

#### 7.3 Dungeons (29+ remaining of 32+ total)
**Dungeons to Create:**
- 5 Easy dungeons (Lv 10-20)
- 8 Normal dungeons (Lv 20-40)
- 8 Hard dungeons (Lv 40-60)
- 6 Elite dungeons (Lv 60-80)
- 5 Nightmare dungeons (Lv 80-100)

**Each dungeon needs:**
- JSON definition
- 1-3 bosses
- Instance structure
- Loot tables
- Quest integration

---

### **PHASE 8: Quality of Life** (Medium Priority - ~2-3 sessions)

#### 8.1 Player Housing
**Files to Create:**
- `PlayerHouse.java` - House data
- `HouseManager.java` - Server management
- `HouseFurniture.java` - Furniture items
- `HouseUpgrade.java` - House upgrades
- `C2SHouseActionPacket.java` - Actions
- `S2CHouseDataPacket.java` - Data sync
- `HouseScreen.java` - House GUI
- `FurniturePlacementScreen.java` - Decoration

**Features:**
- Personal house instance
- 5 house sizes (upgradeable)
- 100+ furniture items
- Furniture placement system
- House teleport
- House storage (chest)
- House customization (wallpaper, flooring)
- House visitors system

#### 8.2 Mini-map & Waypoints
**Files to Create:**
- `Minimap.java` - Map renderer
- `MinimapData.java` - Map data
- `Waypoint.java` - Waypoint data
- `WaypointManager.java` - Server management
- `MinimapHUD.java` - HUD overlay
- `MapScreen.java` - Full map GUI
- `WaypointScreen.java` - Waypoint management

**Features:**
- Circular mini-map HUD
- Player position indicator
- Nearby entities (players, mobs, NPCs)
- Waypoint markers
- Quest objective markers
- Full-screen map
- Zoom levels
- Map sharing

#### 8.3 World Events & Bounty Quests
**Files to Create:**
- `WorldEvent.java` - Event data
- `WorldEventManager.java` - Server management
- `BountyQuest.java` - Bounty data
- `BountyBoard.java` - Bounty system
- `EventScheduler.java` - Timed events
- `S2CEventNotificationPacket.java` - Event alerts

**Features:**
- Scheduled world events (invasions, disasters)
- Dynamic events (meteor showers, etc.)
- Bounty board in cities
- Daily/weekly bounty quests
- Event participation rewards
- Server-wide event announcements

#### 8.4 Weather & Day/Night Effects
**Files to Create:**
- `WeatherEffectHandler.java` - Weather logic
- `DayNightCycle.java` - Cycle effects
- `WeatherBuff.java` - Weather buffs/debuffs

**Features:**
- Rain affects visibility
- Snow slows movement
- Fog reduces range
- Night spawns more dangerous mobs
- Day/night cycle affects quests
- Weather-specific events

---

## 📈 **Estimated Implementation Timeline**

- **Phase 1 (Social)**: 3 sessions
- **Phase 2 (Companions)**: 3 sessions
- **Phase 3 (Economy)**: 3 sessions
- **Phase 4 (Combat/Animations)**: 4 sessions
- **Phase 5 (Progression)**: 2 sessions
- **Phase 6 (Territory/World)**: 5 sessions
- **Phase 7 (Content)**: 10+ sessions (ongoing)
- **Phase 8 (QoL)**: 3 sessions

**Total Estimated**: 33+ sessions

---

## 📁 **File Count Estimate**

- **Java Classes**: 500+ files
- **JSON Data Files**: 300+ files
- **GUI Screens**: 80+ files
- **Packet Classes**: 100+ files
- **Animation Definitions**: 60+ files
- **Total**: 1000+ files

---

## 🎮 **PWI Parity Features Checklist**

### Core Systems
- [x] Diverse Class System
- [ ] Flying System
- [ ] Pet & Mount System
- [ ] Crafting & Refinement
- [x] Dungeons & World Bosses (partial)
- [ ] Territory Wars
- [ ] Cultivation System
- [ ] Marriage System
- [x] Quest System
- [ ] Player Housing
- [ ] Seasonal Events
- [ ] Genie System
- [ ] Bounty Quests
- [ ] Aerial Combat
- [ ] Rebirth System
- [ ] Master/Apprentice
- [ ] Morai System

### Technical Features
- [ ] All skills have animations
- [ ] All mobs have textures/models/sounds
- [ ] All systems use GUIs (no commands)
- [ ] Full multiplayer support
- [ ] Data-driven content (JSON)
- [ ] Server-authoritative logic
- [ ] Proper network sync

---

## 🚀 **Next Steps**

1. **Immediate**: Complete Guild System (packets + GUIs)
2. **Short-term**: Enhanced Chat System
3. **Follow priority order**: Phases 1-8

**Each phase will be broken into multiple development sessions with iterative testing and refinement.**

---

*This roadmap represents the full scope of achieving PWI parity. Implementation will be done systematically, with each phase building on previous foundations.*
