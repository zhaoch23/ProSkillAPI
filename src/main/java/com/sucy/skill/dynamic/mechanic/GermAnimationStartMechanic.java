package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.bean.AnimDataDTO;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Send model animation packet to all players around the caster
 *
 * @author zhaoch23
 */
public class GermAnimationStartMechanic extends MechanicComponent {

    private static final  String NAME = "name";
    private static final  String SPEED = "speed";
    private static final String REVERSED = "reversed";


    @Override
    public String getKey() {
        return "germ animation start";
    }


    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (targets.size() == 0 || !settings.has(NAME)) {
            return false;
        }

        String animName = settings.getString(NAME);
        float speed = (float) parseValues(caster, SPEED, level, 1);
        boolean reversed = Boolean.parseBoolean(settings.getString(REVERSED, "False"));

        AnimDataDTO animDataDTO = new AnimDataDTO(animName, speed, reversed);
        for (Player player : caster.getWorld().getPlayers()) {
            // Only send the packet to players within 64 blocks
            if (caster.getLocation().distance(player.getLocation()) < 64d) {
                GermPacketAPI.sendBendAction(player, caster.getEntityId(), animDataDTO);
                GermPacketAPI.sendModelAnimation(player, caster.getEntityId(), animDataDTO);
            }
        }

        return true;
    }
}
