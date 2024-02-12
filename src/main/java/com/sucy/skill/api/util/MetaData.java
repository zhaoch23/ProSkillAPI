package com.sucy.skill.api.util;

import com.google.common.collect.ImmutableList;
import com.rit.sucy.config.parse.NumberParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetaData {

    private final ConcurrentHashMap<String, ObjectWrapper> data = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final LivingEntity entity;

    public MetaData(LivingEntity entity) {
        this.plugin = Bukkit.getPluginManager().getPlugin("SkillAPI");
        this.entity = entity;
    }

    public void set(String key, Object value, int expire) {
        ObjectWrapper wrapper = data.get(key);
        BukkitTask task;
        if (wrapper != null) {
            task = wrapper.task;
            if (task != null) {
                task.cancel();
            }
        }
        if (expire < 0) { // Permanent
            data.put(key, new ObjectWrapper(value, Long.MAX_VALUE, null));
        } else {
            task = new MetaDataTask(key).runTaskLater(plugin, expire);
            data.put(key, new ObjectWrapper(value, expire * 50L + System.currentTimeMillis(), task));
        }
    }

    public Object get(String key) {
        ObjectWrapper wrapper = data.get(key);
        if (wrapper == null) {
            return null;
        }
        return wrapper.value;
    }

    public int getInt(String key, int defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberParser.parseInt(value.toString());
    }

    public double getDouble(String key, double defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return NumberParser.parseDouble(value.toString());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value.toString());
    }

    public String getString(String key, String defaultValue) {
        Object value = get(key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    @SuppressWarnings("unchecked")
    public List<String> getStringList(String key) {
        Object value = get(key);
        if (value instanceof List<?>) {
            return (List<String>) value;
        } else {
            return ImmutableList.of(value.toString());
        }
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        ObjectWrapper wrapper = data.remove(key);
        if (wrapper != null) {
            BukkitTask task = wrapper.task;
            if (task != null) {
                task.cancel();
            }
        }
    }

    public void clear() {
        for (ObjectWrapper wrapper : data.values()) {
            BukkitTask task = wrapper.task;
            if (task != null) {
                task.cancel();
            }
        }
        data.clear();
        MetaDataManager.clearData(entity);
    }

    private static class ObjectWrapper {
        Object value;
        long expire;
        BukkitTask task;

        public ObjectWrapper(Object value, long expire, BukkitTask task) {
            this.value = value;
            this.expire = expire;
            this.task = task;
        }
    }

    private class MetaDataTask extends BukkitRunnable {
        private final String key;

        public MetaDataTask(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            if (!entity.isValid() || entity.isDead()) {
                // Clear all metadata
                clear();
                return;
            }
            remove(key);
        }
    }

}
