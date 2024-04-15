/**
 * SkillAPI
 * com.sucy.skill.api.projectile.ParticleProjectile
 * <p>
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2014 Steven Sucy
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software") to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.sucy.skill.api.projectile;

import com.sucy.skill.api.Settings;
import com.sucy.skill.api.event.*;
import com.sucy.skill.api.util.ParticleHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.UUID;

/**
 * A fake projectile that plays particles along its path
 */
public class ParticleProjectile extends CustomProjectile {

    final public Settings settings;
    private final int steps;
    private final int freq;
    private final Vector gravity;
    private final boolean pierce;
    private final double collisionRadius;
    public UUID id;
    private Location loc;
    private Vector vel;
    private int count;
    private int life;

    /**
     * Constructor
     *
     * @param shooter         entity that shot the projectile
     * @param loc             initial location of the projectile
     * @param speed           speed of the projectile
     * @param frequency       frequency of the particles
     * @param lifespan        lifespan of the projectile
     * @param gravity         gravity of the projectile
     * @param pierce          whether or not the projectile pierces through entities
     * @param collisionRadius radius for colliding
     * @param settings        settings for the particles
     */
    public ParticleProjectile(LivingEntity shooter,
                              Location loc,
                              double speed,
                              double frequency,
                              int lifespan,
                              double gravity,
                              boolean pierce,
                              double collisionRadius,
                              Settings settings) {
        super(shooter);

        this.loc = loc;
        this.id = UUID.randomUUID();
        this.vel = loc.getDirection().multiply(speed);
        this.freq = (int) (20 * frequency);
        this.life = lifespan * 20;
        this.gravity = new Vector(0, gravity, 0);
        this.pierce = pierce;
        this.collisionRadius = collisionRadius;
        this.settings = settings;
        steps = (int) Math.ceil(vel.length() * 2);
        vel.multiply(1.0 / steps);
        this.gravity.multiply(1.0 / steps);
        Bukkit.getPluginManager().callEvent(new ParticleProjectileLaunchEvent(this));
    }

    /**
     * Fires a spread of projectiles from the location.
     *
     * @param shooter         entity shooting the projectiles
     * @param center          the center direction of the spread
     * @param loc             location to shoot from
     * @param angle           angle of the spread
     * @param amount          number of projectiles to fire
     * @param speed           speed of the projectiles
     * @param frequency       frequency of the particles
     * @param lifespan        lifespan of the projectiles
     * @param gravity         gravity of the projectiles
     * @param pierce          whether or not the projectiles pierce through entities
     * @param collisionRadius radius for colliding
     * @param settings        settings for the particles
     * @param callback        optional callback for when projectiles hit
     * @return list of fired projectiles
     */
    public static ArrayList<ParticleProjectile> spread(LivingEntity shooter,
                                                       Vector center,
                                                       Location loc,
                                                       double angle,
                                                       int amount,
                                                       double speed,
                                                       double frequency,
                                                       int lifespan,
                                                       double gravity,
                                                       boolean pierce,
                                                       double collisionRadius,
                                                       Settings settings,
                                                       ProjectileCallback callback) {
        ArrayList<Vector> dirs = calcSpread(center, angle, amount);
        ArrayList<ParticleProjectile> list = new ArrayList<>();
        for (Vector dir : dirs) {
            Location l = loc.clone();
            l.setDirection(dir);
            ParticleProjectile p = new ParticleProjectile(shooter, l, speed, frequency,
                    lifespan, gravity, pierce, collisionRadius, settings);
            p.setCallback(callback);
            list.add(p);
        }
        return list;
    }

    /**
     * Fires a spread of projectiles from the location.
     *
     * @param shooter         entity shooting the projectiles
     * @param center          the center location to rain on
     * @param radius          radius of the circle
     * @param height          height above the center location
     * @param amount          number of projectiles to fire
     * @param speed           speed of the projectiles
     * @param frequency       frequency of the particles
     * @param lifespan        lifespan of the projectiles
     * @param gravity         gravity of the projectiles
     * @param pierce          whether or not the projectiles pierce through entities
     * @param collisionRadius radius for colliding
     * @param settings        settings for the particles
     * @param callback        optional callback for when projectiles hit
     * @return list of fired projectiles
     */
    public static ArrayList<ParticleProjectile> rain(
            LivingEntity shooter,
            Location center,
            double radius,
            double height,
            int amount,
            double speed,
            double frequency,
            int lifespan,
            double gravity,
            boolean pierce,
            double collisionRadius,
            Settings settings,
            ProjectileCallback callback) {
        Vector vel = new Vector(0, 1, 0);
        ArrayList<Location> locs = calcRain(center, radius, height, amount);
        ArrayList<ParticleProjectile> list = new ArrayList<>();
        for (Location l : locs) {
            l.setDirection(vel);
            ParticleProjectile p = new ParticleProjectile(shooter, l, speed, frequency,
                    lifespan, gravity, pierce, collisionRadius, settings);
            p.setCallback(callback);
            list.add(p);
        }
        return list;
    }

    /**
     * Retrieves the location of the projectile
     *
     * @return location of the projectile
     */
    @Override
    public Location getLocation() {
        return loc;
    }

    /**
     * Handles expiring due to range or leaving loaded chunks
     */
    @Override
    protected Event expire() {
        return new ParticleProjectileExpireEvent(this);
    }

    /**
     * Handles landing on terrain
     */
    @Override
    protected Event land() {
        return new ParticleProjectileLandEvent(this);
    }

    /**
     * Handles hitting an entity
     *
     * @param entity entity the projectile hit
     */
    @Override
    protected Event hit(LivingEntity entity) {
        return new ParticleProjectileHitEvent(this, entity);
    }

    /**
     * @return true if passing through a solid block, false otherwise
     */
    @Override
    protected boolean landed() {
        return getLocation().getBlock().getType().isSolid();
    }

    /**
     * @return squared radius for colliding
     */
    @Override
    protected double getCollisionRadius() {
        return collisionRadius;
    }

    /**
     * @return velocity of the projectile
     */
    @Override
    public Vector getVelocity() {
        return vel;
    }

    /**
     * Sets the velocity of the projectile
     *
     * @param vel new velocity
     */
    @Override
    public void setVelocity(Vector vel) {
        this.vel = vel;
    }

    /**
     * Teleports the projectile to a location
     *
     * @param loc location to teleport to
     */
    public void teleport(Location loc) {
        this.loc = loc;
    }

    /**
     * Updates the projectiles position and checks for collisions
     */
    @Override
    public void run() {
        // Go through multiple steps to avoid tunneling
        for (int i = 0; i < steps; i++) {
            loc.add(vel);
            vel.add(gravity);

            if (!isTraveling())
                return;

            if (!checkCollision(pierce)) break;
        }

        // Particle along path
        count++;
        if (count >= freq) {
            count = 0;
            ParticleHelper.play(loc, settings);
            Bukkit.getPluginManager().callEvent(new ParticleProjectileRunningEvent(this, loc));
        }

        // Lifespan
        life--;
        if (life <= 0) {
            cancel();
            Bukkit.getPluginManager().callEvent(new ParticleProjectileExpireEvent(this));
        }
    }
}
