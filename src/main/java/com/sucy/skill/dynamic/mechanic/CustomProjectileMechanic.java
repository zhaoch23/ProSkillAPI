package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.particle.EffectPlayer;
import com.sucy.skill.api.particle.target.FollowTarget;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.api.projectile.ParticleProjectile;
import com.sucy.skill.api.projectile.ProjectileCallback;
import com.sucy.skill.cast.*;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

abstract public class CustomProjectileMechanic extends MechanicComponent implements ProjectileCallback {

    protected static final Vector UP = new Vector(0, 1, 0);
    protected static final String ANGLE = "angle";
    protected static final String AMOUNT = "amount";
    protected static final String LEVEL = "skill_level";
    protected static final String HEIGHT = "height";
    protected static final String RAIN_RADIUS = "rain-radius";
    protected static final String SPREAD = "spread";
    protected static final String ALLY = "group";
    protected static final String RIGHT = "right";
    protected static final String UPWARD = "upward";
    protected static final String FORWARD = "forward";
    protected static final String USE_EFFECT = "use-effect";
    protected static final String EFFECT_KEY = "effect-key";

    /**
     * Settings key for the projectile speed
     */
    protected static final String VELOCITY = "velocity";
    /**
     * Settings key for the projectile lifespan
     */
    protected static final String LIFESPAN = "lifespan";

    /**
     * Settings key for the projectile's frequency of playing particles
     */
    protected static final String FREQUENCY = "frequency";

    /**
     * Settings key for the projectile's effective gravity
     */
    protected static final String GRAVITY = "gravity";

    protected static final String PIERCE = "pierce";
    protected static final String COLLISION_RADIUS = "radius";

    /**
     * Creates the list of indicators for the skill
     *
     * @param list    list to store indicators in
     * @param caster  caster reference
     * @param targets location to base location on
     * @param level   the level of the skill to create for
     */
    @Override
    public void makeIndicators(
            List<IIndicator> list,
            Player caster,
            List<LivingEntity> targets,
            int level) {
        targets.forEach(target -> {
            // Get common values
            int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
            double speed = parseValues(caster, "velocity", level, 1);
            String spread = settings.getString(SPREAD, "cone").toLowerCase();

            // Apply the spread type
            if (spread.equals("rain")) {
                double radius = parseValues(caster, RAIN_RADIUS, level, 2.0);

                if (indicatorType == IndicatorType.DIM_2) {
                    IIndicator indicator = new CircleIndicator(radius);
                    indicator.moveTo(target.getLocation().add(0, 0.1, 0));
                    list.add(indicator);
                } else {
                    double height = parseValues(caster, HEIGHT, level, 8.0);
                    IIndicator indicator = new CylinderIndicator(radius, height);
                    indicator.moveTo(target.getLocation());
                    list.add(indicator);
                }
            } else {
                Vector dir = target.getLocation().getDirection();
                if (spread.equals("horizontal cone")) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                ArrayList<Vector> dirs = CustomProjectile.calcSpread(dir, angle, amount);
                Location loc = caster.getLocation().add(0, caster.getEyeHeight(), 0);
                for (Vector d : dirs) {
                    ProjectileIndicator indicator = new ProjectileIndicator(speed, 0);
                    indicator.setDirection(d);
                    indicator.moveTo(loc);
                    list.add(indicator);
                }
            }
        });
    }

    /**
     * Fires a projectile for each target
     *
     * @param caster the caster of the skill
     * @param targets the targets to fire at
     * @param settings the settings for the skill
     * @param spread the spread type to use
     * @param level the level of the skill
     * @return true if fired at least one projectile, false otherwise
     */
    protected boolean fireForEachTargets(LivingEntity caster,
                                      List<LivingEntity> targets,
                                      Settings settings,
                                      SpreadType spread,
                                      int level) {
        // Get common values
        int amount = (int) parseValues(caster, AMOUNT, level, 1.0);
        double speed = parseValues(caster, VELOCITY, level, 1.0);
        boolean ally = settings.getString(ALLY, "enemy").equalsIgnoreCase("ally");
        int frequency = (int) (int) (20 * settings.getDouble(FREQUENCY, 0.5));
        int lifeSpan = (int) (settings.getDouble(LIFESPAN, 2) * 20);
        double gravity = settings.getDouble(GRAVITY, 0);
        boolean pierce = settings.getBool(PIERCE, false);
        double collisionRadius = settings.getDouble(COLLISION_RADIUS, 1.5);

        // Fire from each target
        for (LivingEntity target : targets) {
            Location loc = target.getLocation();

            // Apply the spread type
            ArrayList<ParticleProjectile> list;
            if (spread == SpreadType.RAIN) {
                double radius = parseValues(caster, RAIN_RADIUS, level, 2.0);
                double height = parseValues(caster, HEIGHT, level, 8.0);
                list = ParticleProjectile.rain(caster, level, loc, radius, height, amount, speed,
                        frequency, lifeSpan, gravity, pierce, collisionRadius, settings, this);
            } else {
                Vector dir = target.getLocation().getDirection();

                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = dir.clone().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right));

                if (spread == SpreadType.HORIZONTAL_CONE) {
                    dir.setY(0);
                    dir.normalize();
                }
                double angle = parseValues(caster, ANGLE, level, 30.0);
                list = ParticleProjectile.spread(
                        caster,
                        level,
                        dir,
                        loc.add(looking).add(0, upward + 0.5, 0),
                        angle,
                        amount,
                        speed,
                        frequency,
                        lifeSpan,
                        gravity,
                        pierce,
                        collisionRadius,
                        settings,
                        this
                );
            }

            // Set metadata for when the callback happens
            for (ParticleProjectile p : list) {
                SkillAPI.setMeta(p, LEVEL, level);
                p.setAllyEnemy(ally, !ally);
            }

            if (settings.getBool(USE_EFFECT, false)) {
                EffectPlayer player = new EffectPlayer(settings);
                for (CustomProjectile p : list) {
                    player.start(
                            new FollowTarget(p),
                            settings.getString(EFFECT_KEY, skill.getName()),
                            9999,
                            level,
                            true);
                }
            }
        }
        return targets.size() > 0;
    }

    public static enum SpreadType {
        CONE("cone"),
        HORIZONTAL_CONE("horizontal cone"),
        RAIN("rain");

        final String key;
        SpreadType(String key) {
            this.key = key;
        }

        public static SpreadType fromKey(String key) {
            for (SpreadType type : values()) {
                if (type.key.equals(key)) {
                    return type;
                }
            }
            return null;
        }
    }
}
