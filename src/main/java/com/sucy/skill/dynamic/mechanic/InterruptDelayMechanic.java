package com.sucy.skill.dynamic.mechanic;

import com.sucy.skill.api.util.DelayedTaskManager;
import org.bukkit.entity.LivingEntity;

import java.util.List;

/**
 * Clears all delayed tasks from the target
 */
public class InterruptDelayMechanic extends MechanicComponent {

    @Override
    public String getKey() {
        return "delay interrupt";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0) {
            return false;
        }

        for (LivingEntity target : targets) {
            if (target != null) {
                DelayedTaskManager.clearTasks(target.getUniqueId());
            }
        }

        return true;
    }
}
