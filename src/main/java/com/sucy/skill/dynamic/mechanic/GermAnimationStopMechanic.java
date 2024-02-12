package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Send model animation clear packet to all players around the caster
 *
 * @author zhaoch23
 */
public class GermAnimationStopMechanic extends MechanicComponent{

    private static final String NAME = "name";

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

        for (Player player : caster.getWorld().getPlayers()) {
            // Only send the packet to players within 64 blocks
            if (caster.getLocation().distance(player.getLocation()) < 64d) {
                GermPacketAPI.stopModelAnimation(player, caster.getEntityId(), animName);
                GermPacketAPI.sendBendClear(player, caster.getEntityId());
            }
        }

        return true;
    }
}
