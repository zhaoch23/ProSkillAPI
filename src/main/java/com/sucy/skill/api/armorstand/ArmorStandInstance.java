package com.sucy.skill.api.armorstand;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class ArmorStandInstance {
    private static final Vector UP = new Vector(0, 1, 0);
    private final ArmorStand armorStand;
    private final LivingEntity target;
    private final boolean follow;
    private final BukkitRunnable tickTask;
    private double forward;
    private double upward;
    private double right;

    public ArmorStandInstance(ArmorStand armorStand, LivingEntity target, BukkitRunnable tickTask) {
        this.armorStand = armorStand;
        this.target = target;
        this.follow = false;
        this.tickTask = tickTask;
    }

    public ArmorStandInstance(ArmorStand armorStand,
                              LivingEntity target,
                              double forward,
                              double upward,
                              double right,
                              BukkitRunnable tickTask) {
        this.armorStand = armorStand;
        this.target = target;
        this.forward = forward;
        this.upward = upward;
        this.right = right;
        this.follow = true;
        this.tickTask = tickTask;
    }

    /**
     * @return true if the instance is still valid
     */
    public boolean isValid() {
        return target.isValid() && armorStand.isValid();
    }

    /**
     * Removes the armor stand
     */
    public void remove() {
        armorStand.remove();
    }

    /**
     * Ticks the armor stand
     */
    public void tick() {
        if (follow) {
            Location loc = target.getLocation().clone();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));
            armorStand.teleport(loc);
        }
        if (tickTask != null) {
            tickTask.run();
        }
    }
}
