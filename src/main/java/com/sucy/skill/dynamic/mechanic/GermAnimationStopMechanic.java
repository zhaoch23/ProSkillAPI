package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

/**
 * Send model animation clear packet to all players around the caster
 *
 * @author zhaoch23
 */
public class GermAnimationStopMechanic extends MechanicComponent{

    private static final String NAME = "name";
    private static final String TARGET = "target";

    @Override
    public String getKey() {
        return "germ animation stop";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(NAME)) {
            return false;
        }

        String animName = settings.getString(NAME);

        final List<LivingEntity> animationTargets =
                settings.getString(TARGET, "caster").equalsIgnoreCase("caster") ?
                        Collections.singletonList(caster) : targets;

        for (Player player : caster.getWorld().getPlayers()) {
            for (LivingEntity target : animationTargets) {
                // Only send the packet to players within 64 blocks
                if (target.getLocation().distance(player.getLocation()) < 64d) {
                    GermPacketAPI.stopModelAnimation(player, target.getEntityId(), animName);
                    GermPacketAPI.sendBendClear(player, target.getEntityId());
                }
            }
        }

        return true;
    }
}
