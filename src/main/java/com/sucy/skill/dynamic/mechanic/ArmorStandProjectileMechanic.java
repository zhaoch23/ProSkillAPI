package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.projectile.ArmorStandProjectile;
import com.sucy.skill.api.projectile.CustomProjectile;
import com.sucy.skill.dynamic.TempEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

import static com.sucy.skill.dynamic.mechanic.ArmorStandMechanic.DURATION;
import static com.sucy.skill.dynamic.mechanic.ArmorStandMechanic.generateArmorStands;

public class ArmorStandProjectileMechanic extends CustomProjectileMechanic {

    @Override
    public String getKey() {
        return "armor stand projectile";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        // Get common values
        int duration = (int) parseValues(caster, DURATION, level, 5);
        double gravity = settings.getDouble(GRAVITY, 0);
        boolean pierce = settings.getBool(PIERCE, false);
        double radius = settings.getDouble(COLLISION_RADIUS, 1.5);
        boolean ally = settings.getString(ALLY, "enemy").equalsIgnoreCase("ally");
        double velocity = parseValues(caster, VELOCITY, level, 1);

        List<LivingEntity> armorStands = generateArmorStands(
            this, caster, level, targets, settings,
            (armorStand, target) -> {
                Vector dir = target.getLocation().getDirection();

                double right = parseValues(caster, RIGHT, level, 0);
                double upward = parseValues(caster, UPWARD, level, 0);
                double forward = parseValues(caster, FORWARD, level, 0);

                Vector looking = dir.clone().setY(0).normalize();
                Vector normal = looking.clone().crossProduct(UP);
                looking.multiply(forward).add(normal.multiply(right)).setY(upward);

                ArmorStandProjectile projectile = new ArmorStandProjectile(
                    armorStand,
                    caster,
                    target.getLocation().clone().add(looking),
                    velocity,
                    duration,
                    gravity,
                    pierce,
                    radius
                );
                projectile.setCallback(this);
                SkillAPI.setMeta(projectile, LEVEL, level);
                projectile.setAllyEnemy(ally, !ally);
            });

        return armorStands.size() > 0;
    }

    @Override
    protected void doCleanUp(LivingEntity caster) {
        super.doCleanUp(caster);
    }

    @Override
    public void callback(CustomProjectile projectile, LivingEntity hit) {
        if (hit == null) {
            hit = new TempEntity(projectile.getLocation());
        }
        ArrayList<LivingEntity> targets = new ArrayList<>();
        targets.add(hit);
        executeChildren(projectile.getShooter(), SkillAPI.getMetaInt(projectile, LEVEL), targets);
    }

}
