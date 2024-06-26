package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.Settings;
import com.sucy.skill.api.armorstand.ArmorStandInstance;
import com.sucy.skill.api.armorstand.ArmorStandManager;
import com.sucy.skill.api.attribute.AttributeAPI;
import com.sucy.skill.api.skills.PassiveSkill;
import com.sucy.skill.api.skills.Skill;
import com.sucy.skill.api.skills.SkillCastAPI;
import com.sucy.skill.dynamic.DynamicSkill;
import com.sucy.skill.dynamic.EffectComponent;
import com.sucy.skill.listener.MechanicListener;
import com.sucy.skill.task.RemoveTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import static com.sucy.skill.dynamic.mechanic.WolfMechanic.LEVEL;
import static com.sucy.skill.dynamic.mechanic.WolfMechanic.SKILL_META;

/**
 * Summons an armor stand that can be used as a marker or for item display. Applies child components on the armor stand
 */
public class ArmorStandMechanic extends MechanicComponent {
    private static final Vector UP = new Vector(0, 1, 0);

    private static final String KEY = "key";
    public static final String DURATION = "duration";
    private static final String NAME = "name";
    private static final String NAME_VISIBLE = "name-visible";
    private static final String FOLLOW = "follow";
    private static final String GRAVITY = "gravity";
    private static final String SMALL = "tiny";
    private static final String ARMS = "arms";
    private static final String BASE = "base";
    private static final String VISIBLE = "visible";
    private static final String MARKER = "marker";
    private static final String FORWARD = "forward";
    private static final String UPWARD = "upward";
    private static final String RIGHT = "right";

    private static final String SKILLS = "skills";

    private static final String TICK_PERIOD = "period";
    private static final String REMEMBER = "remember";

    @Override
    public String getKey() {
        return "armor stand";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        String key = settings.getString(KEY, skill.getName());
        int duration = (int) (20 * parseValues(caster, DURATION, level, 5));
        int tickPeriod = settings.getInt(TICK_PERIOD, -1);

        List<LivingEntity> armorStands = generateArmorStands( this,
            caster, level, targets, settings,
            (armorStand, target) -> {
                // If follow is enabled, create an instance to update the armor stand
                ArmorStandInstance instance;
                BukkitRunnable tickTask = tickPeriod > 0 ? new OnTickTask(caster, level, targets, tickPeriod) : null;
                if (settings.getBool(FOLLOW, false)) {
                    instance = new ArmorStandInstance(
                        armorStand,
                        target,
                        parseValues(caster, FORWARD, level, 0),
                        parseValues(caster, UPWARD, level, 0),
                        parseValues(caster, RIGHT, level, 0),
                        tickTask
                    );
                } else { // Otherwise, just register the armor stand
                    instance = new ArmorStandInstance(armorStand, target, tickTask);
                }
                // Register the armor stand
                ArmorStandManager.register(instance, target, key);
        });

        // If tickPeriod is 0, execute children immediately
        if (tickPeriod == 0)
            executeChildren(caster, level, armorStands);

        new RemoveTask(armorStands, duration);
        return targets.size() > 0;
    }

    public static List<LivingEntity> generateArmorStands(
            EffectComponent component,
            LivingEntity caster,
            int level,
            List<LivingEntity> targets,
            Settings settings,
            BiConsumer<ArmorStand, LivingEntity> apply) {
        String name = settings.getString(NAME, "Armor Stand");
        boolean nameVisible = settings.getBool(NAME_VISIBLE, false);
        boolean gravity = settings.getBool(GRAVITY, false);
        boolean small = settings.getBool(SMALL, false);
        boolean arms = settings.getBool(ARMS, false);
        boolean base = settings.getBool(BASE, false);
        boolean visible = settings.getBool(VISIBLE, true);
        boolean marker = settings.getBool(MARKER, false);
        double forward = component.parseValues(caster, FORWARD, level, 0);
        double upward = component.parseValues(caster, UPWARD, level, 0);
        double right =component.parseValues(caster, RIGHT, level, 0);

        List<String> skills = settings.getStringList(SKILLS);

        String remember = settings.getString(REMEMBER, null);

        List<LivingEntity> armorStands = new ArrayList<>();
        for (LivingEntity target : targets) {
            Location loc = target.getLocation().clone();
            Vector dir = loc.getDirection().setY(0).normalize();
            Vector side = dir.clone().crossProduct(UP);
            loc.add(dir.multiply(forward)).add(0, upward, 0).add(side.multiply(right));

            ArmorStand armorStand = target.getWorld().spawn(loc, ArmorStand.class, as -> {
                try {
                    as.setMarker(marker);
                    as.setInvulnerable(true);
                } catch (NoSuchMethodError ignored) {
                }
                try {
                    as.setSilent(true);
                } catch (NoSuchMethodError ignored) {
                }
                as.setGravity(gravity);
                as.setCustomName(name.replace("{player}", caster.getName()));
                as.setCustomNameVisible(nameVisible);
                as.setSmall(small);
                as.setArms(arms);
                as.setBasePlate(base);
                as.setVisible(visible);
            });
            SkillAPI.setMeta(armorStand, MechanicListener.ARMOR_STAND, true);
            //设置一下主人
            SkillAPI.setMeta(armorStand, AttributeAPI.FX_SKILL_API_MASTER, caster.getUniqueId());

            for (String skillName : skills) {
                Skill skill = SkillAPI.getSkill(skillName);
                if (skill != null) {
                    SkillCastAPI.cast(armorStand, skill, level);
                }
            }

            armorStands.add(armorStand);

            if (remember != null && !remember.isEmpty()) {
                DynamicSkill.getCastData(caster).put(remember, armorStands);
            }

            if (apply != null) {
                apply.accept(armorStand, target);
            }
        }
        return armorStands;
    }

     class OnTickTask extends BukkitRunnable {
        private final List<LivingEntity> targets;
        private final LivingEntity caster;
        private final int level;
        private final int tickPeriod;
        private int tickCount = 0;

        OnTickTask(LivingEntity caster, int level, List<LivingEntity> targets, int tickPeriod) {
            this.targets = new ArrayList<>(targets);
            this.caster = caster;
            this.level = level;
            this.tickPeriod = tickPeriod;
        }

        @Override
        public void run() {
            // Check if this is on the main thread
            if (!Bukkit.isPrimaryThread()) {
                Bukkit.getScheduler().runTask(SkillAPI.singleton, this::tick);
                return;
            }
            tick();
        }

        private void tick() {
            tickCount++;
            if (tickCount % tickPeriod != 0) {
                return;
            }
            if (caster.isDead() || !caster.isValid()) {
                cancel();
                return;
            }

            targets.removeIf(target -> target.isDead() || !target.isValid());
            executeChildren(caster, level, targets);

            if (skill.checkCancelled()) {
                cancel();
            }
        }
    }
}
