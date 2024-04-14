package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.bean.AnimDataDTO;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Send model animation packet to all players around the caster
 *
 * @author zhaoch23
 */
public class GermAnimationStartMechanic extends MechanicComponent {

    private static final String NAME = "name";
    private static final String TARGET = "target";
    private static final String SPEED = "speed";
    private static final String REVERSED = "reversed";


    @Override
    public String getKey() {
        return "germ animation start";
    }


    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(NAME)) {
            return false;
        }

        final List<LivingEntity> animationTargets =
                settings.getString(TARGET, "caster").equalsIgnoreCase("caster") ?
                        Collections.singletonList(caster) : targets;

        String animName = settings.getString(NAME);
        float speed = (float) settings.getDouble(SPEED, 1.0d);
        boolean reversed = Boolean.parseBoolean(settings.getString(REVERSED, "False"));
        AnimDataDTO animDataDTO = new AnimDataDTO(animName, speed, reversed);

        List<Player> players = caster.getWorld().getPlayers();
        for (Player player : players) {
            for (LivingEntity target : animationTargets) {
                // Only send the packet to players within 64 blocks
                if (target.getLocation().distance(player.getLocation()) < 64d) {
                    GermPacketAPI.sendBendAction(player, target.getEntityId(), animDataDTO);
                    GermPacketAPI.sendModelAnimation(player, target.getEntityId(), animDataDTO);
                }
            }
        }

        return true;
    }
}
