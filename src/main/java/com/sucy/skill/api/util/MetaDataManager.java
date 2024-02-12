package com.sucy.skill.api.util;

import org.bukkit.entity.LivingEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MetaDataManager {

    private static final ConcurrentHashMap<UUID, MetaData> data = new ConcurrentHashMap<>();

    public static MetaData getMetaData(final LivingEntity entity) {
        return getMetaData(entity, true);
    }

    public static MetaData getMetaData(final LivingEntity entity, final boolean create) {
        if (entity == null) return null;

        if (!data.containsKey(entity.getUniqueId()) && create) {
            data.put(entity.getUniqueId(), new MetaData(entity));
        }
        return data.get(entity.getUniqueId());
    }

    public static void addMetaData(final LivingEntity entity, final String key, final Object value, final int expire) {
        final MetaData metaData = getMetaData(entity, true);
        if (metaData != null) {
            metaData.set(key, value, expire);
        }
    }

    public static void removeMetaData(final LivingEntity entity, final String key) {
        final MetaData metaData = getMetaData(entity, false);
        if (metaData != null) {
            metaData.remove(key);
        }
    }

    public static void clearData(LivingEntity entity) {
        if (entity == null) return;

        final MetaData result = data.remove(entity.getUniqueId());
        if (result != null) {
            result.clear();
        }
    }
}
