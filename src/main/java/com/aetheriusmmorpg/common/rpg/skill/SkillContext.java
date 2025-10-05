package com.aetheriusmmorpg.common.rpg.skill;

import com.aetheriusmmorpg.common.capability.player.PlayerRpgData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Context object passed through skill execution pipeline.
 * Contains all information needed to execute a skill.
 */
public class SkillContext {
    private final LivingEntity caster;
    private final Skill skill;
    private final Level level;
    private LivingEntity target;
    private Vec3 targetPos;
    private final List<LivingEntity> affectedEntities = new ArrayList<>();

    public SkillContext(LivingEntity caster, Skill skill, Level level) {
        this.caster = caster;
        this.skill = skill;
        this.level = level;
    }

    // Getters
    public LivingEntity getCaster() { return caster; }
    public Skill getSkill() { return skill; }
    public Level getLevel() { return level; }
    public LivingEntity getTarget() { return target; }
    public Vec3 getTargetPos() { return targetPos; }
    public List<LivingEntity> getAffectedEntities() { return affectedEntities; }

    // Setters
    public void setTarget(LivingEntity target) { this.target = target; }
    public void setTargetPos(Vec3 pos) { this.targetPos = pos; }
    public void addAffectedEntity(LivingEntity entity) { affectedEntities.add(entity); }

    /**
     * Check if this skill should critically hit.
     * Uses caster's crit rate stat.
     */
    public boolean rollCriticalHit() {
        double critRate = getCasterAttribute("crit_rate");
        // Crit rate is percentage (5.0 = 5% chance)
        return level.random.nextDouble() * 100.0 < critRate;
    }

    /**
     * Get the critical strike multiplier.
     * Default is 2.0x damage.
     */
    public double getCriticalMultiplier() {
        return 2.0;
    }

    /**
     * Calculate adjusted cast time based on haste.
     * Haste reduces cast time (1.0 haste = 1% reduction).
     */
    public int getAdjustedCastTime() {
        int baseCastTime = skill.castTime();
        if (baseCastTime == 0) {
            return 0; // Instant cast spells aren't affected
        }

        double haste = getCasterAttribute("haste");
        // Each point of haste = 1% cast speed increase (reduction in cast time)
        double reduction = 1.0 - (haste / 100.0);
        reduction = Math.max(0.1, reduction); // Cap at 90% reduction

        return (int) (baseCastTime * reduction);
    }

    /**
     * Calculate adjusted cooldown based on haste.
     * Haste also reduces cooldowns (optional for game balance).
     */
    public int getAdjustedCooldown() {
        int baseCooldown = skill.cooldown();
        double haste = getCasterAttribute("haste");
        // Each point of haste = 0.5% cooldown reduction
        double reduction = 1.0 - (haste / 200.0);
        reduction = Math.max(0.5, reduction); // Cap at 50% reduction

        return (int) (baseCooldown * reduction);
    }

    /**
     * Get caster's attribute value for scaling calculations.
     * Pulls from PlayerRpgData capability for players, or returns base values for NPCs.
     */
    public double getCasterAttribute(String attributeName) {
        // For player casters, pull from capability
        if (caster instanceof Player player) {
            PlayerRpgData data = player.getCapability(PlayerRpgData.CAPABILITY).orElse(null);
            if (data != null) {
                return switch (attributeName.toLowerCase()) {
                    case "power" -> data.getPower();
                    case "spirit" -> data.getSpirit();
                    case "agility" -> data.getAgility();
                    case "defense" -> data.getDefense();
                    case "crit_rate", "critrate" -> data.getCritRate();
                    case "haste" -> data.getHaste();
                    default -> 10.0; // Default fallback
                };
            }
        }

        // For NPCs or if capability not found, use vanilla attributes or defaults
        return switch (attributeName.toLowerCase()) {
            case "power" -> caster.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            case "spirit", "agility", "defense", "crit_rate", "critrate", "haste" -> 10.0;
            default -> 1.0;
        };
    }
}
