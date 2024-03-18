package com.sucy.skill.dynamic.mechanic;

import com.germ.germplugin.api.GermPacketAPI;
import com.germ.germplugin.api.SoundType;
import com.sun.media.jfxmedia.logging.Logger;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

import java.util.List;

public class GermPlaySoundMechanic extends MechanicComponent {

    private static final String SOUND_TYPE = "soundType";

    private static final String SOUND_NAME = "soundName";

    private static final String LOCATION = "location";

    private static final String VOLUME = "volume";

    private static final String PITCH = "pitch";

    private static final String DELAY_TICK = "delayTick";

    @Override
    public String getKey() {
        return "germ play sound";
    }

    @Override
    public boolean execute(LivingEntity caster, int level, List<LivingEntity> targets) {
        if (!settings.has(SOUND_NAME)) {
            Logger.logMsg(Logger.DEBUG, "No sound name found for sound effect");
            return false;
        }

        String soundName = settings.getString(SOUND_NAME);
        String soundType = settings.getString(SOUND_TYPE, "AMBIENT");
        String locationType = settings.getString(LOCATION, "caster");
        float volume = (float) settings.getDouble(VOLUME, 1d);
        float pitch = (float) settings.getDouble(PITCH, 1d);
        int delayTick = settings.getInt(DELAY_TICK, 0);

        if (locationType.equalsIgnoreCase("caster")) {
            Location location = caster.getLocation();
            GermPacketAPI.playSound(location, soundName, SoundType.valueOf(soundType), delayTick, pitch, volume);
        } else { // targets
            for (LivingEntity target : targets) {
                Location location = target.getLocation();
                GermPacketAPI.playSound(location, soundName, SoundType.valueOf(soundType), delayTick, pitch, volume);
            }
        }
        return true;
    }
}
