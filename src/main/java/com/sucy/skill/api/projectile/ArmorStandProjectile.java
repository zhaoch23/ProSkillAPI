package com.sucy.skill.api.projectile;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

public class ArmorStandProjectile extends CustomProjectile {

    private final Vector gravity;
    private final boolean pierce;
    private final double collisionRadius;
    private final ArmorStand instance;
    private final Location loc;
    private Vector vel;
    private int life;

    /**
     * Constructor
     *
     * @param armorStand      armor stand to use as the projectile
     * @param thrower         entity that shot the projectile
     * @param loc             location of the projectile with direction
     * @param velocity        velocity of the projectile in blocks per second
     * @param lifespan        lifespan of the projectile in seconds
     * @param gravity         gravity of the projectile
     * @param pierce          whether or not the projectile pierces through entities
     * @param collisionRadius radius for colliding
     */
    public ArmorStandProjectile(ArmorStand armorStand,
                                LivingEntity thrower,
                                Location loc,
                                double velocity,
                                int lifespan,
                                double gravity,
                                boolean pierce,
                                double collisionRadius) {
        super(thrower);
        this.loc = loc;
        this.vel = loc.getDirection().multiply(velocity / 20.0);
        this.instance = armorStand;
        this.life = lifespan * 20;
        this.gravity = new Vector(0, gravity, 0);
        this.pierce = pierce;
        this.collisionRadius = collisionRadius;
    }

    @Override
    public Location getLocation() {
        return instance.getLocation();
    }

    @Override
    protected Event expire() {
        return null;
    }

    @Override
    protected Event land() {
        return null;
    }

    @Override
    protected Event hit(LivingEntity entity) {
        return null;
    }

    @Override
    protected boolean landed() {
        return getLocation().getBlock().getType().isSolid();
    }

    @Override
    protected double getCollisionRadius() {
        return collisionRadius;
    }

    @Override
    protected Vector getVelocity() {
        return vel;
    }

    @Override
    protected void setVelocity(Vector vel) {
        this.vel = vel;
    }

    @Override
    public void run() {
        loc.add(vel);
        instance.teleport(loc);
        vel.add(gravity);

        if (!isTraveling())
            return;

        checkCollision(pierce);

        // Lifespan
        life--;
        if (life <= 0) {
            cancel();
        }
    }

    @Override
    public void cancel() {
        super.cancel();
        instance.remove();
    }


}
