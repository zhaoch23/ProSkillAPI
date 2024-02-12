package com.sucy.skill.api.util;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DelayedTaskManager {

    private static final ConcurrentHashMap<UUID, List<BukkitTask>> tasks = new ConcurrentHashMap<>();

    /**
     * This method is used to run a task after a specified delay. The task is associated with a UUID.
     * If tasks already exist for the given UUID, the new task is added to the existing list.
     * If no tasks exist for the given UUID, a new list is created with the task.
     *
     * @param uuid  The UUID associated with the task.
     * @param task  The task to be run after the specified delay.
     * @param delay The delay (in ticks) after which the task should be run.
     */
    public static void runTaskLater(UUID uuid, Runnable task, long delay) {

        final BukkitTask[] bukkitTask = new BukkitTask[1];
        bukkitTask[0] = new BukkitRunnable() {
            @Override
            public void run() {
                task.run();
                // remove the task from the list
                List<BukkitTask> list = tasks.get(uuid);
                if (list != null) {
                    list.remove(bukkitTask[0]);
                }
            }
        }.runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SkillAPI")), delay);

        if (tasks.containsKey(uuid)) {
            tasks.get(uuid).add(bukkitTask[0]);
        } else {
            tasks.put(uuid, Collections.singletonList(bukkitTask[0]));
        }
    }

    /**
     * This method is used to clear all tasks associated with a given UUID.
     * It retrieves the list of tasks associated with the UUID, cancels each task in the list,
     * and then removes the list of tasks from the map.
     *
     * @param uuid The UUID whose associated tasks are to be cleared.
     */
    public static void clearTasks(UUID uuid) {
        // Retrieve the list of tasks associated with the UUID
        List<BukkitTask> list = tasks.get(uuid);
        if (list != null) {
            // Cancel each task in the list
            for (BukkitTask task : list) {
                task.cancel();
            }
            // Remove the list of tasks from the map
            tasks.remove(uuid);
        }
    }
}
