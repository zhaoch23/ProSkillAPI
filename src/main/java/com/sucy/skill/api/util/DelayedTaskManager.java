package com.sucy.skill.api.util;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DelayedTaskManager {

    private static final ConcurrentHashMap<String, Map<Integer, BukkitTask>> tasks = new ConcurrentHashMap<>();

    /**
     * This method is used to run a task after a specified delay. The task is associated with a id.
     * If tasks already exist for the given UUID, the new task is added to the existing list.
     * If no tasks exist for the given id, a new list is created with the task.
     *
     * @param id  The id associated with the task.
     * @param task  The task to be run after the specified delay.
     * @param delay The delay (in ticks) after which the task should be run.
     */
    public static void runTaskLater(String id, Runnable task, long delay) {

        // Create a new DelayedTask object and run it
        new DelayedTask(id, task, delay);
    }

    /**
     * This method is used to clear all tasks associated with a given UUID.
     * It retrieves the list of tasks associated with the UUID, cancels each task in the list,
     * and then removes the list of tasks from the map.
     *
     * @param id The UUID whose associated tasks are to be cleared.
     */
    public static void clearTasks(String id) {
        // Retrieve the list of tasks associated with the UUID
        Map<Integer, BukkitTask> map = tasks.get(id);
        if (map != null) {
            // Cancel each task in the list
            for (BukkitTask task : map.values()) {
                task.cancel();
            }
            // Remove the list of tasks from the map
            tasks.remove(id);
        }
    }

    private static class DelayedTask extends BukkitRunnable {

        private final String id;
        private final Runnable task;
        private final int taskId;

        private DelayedTask(String id, Runnable task, long delay) {
            this.id = id;
            this.task = task;
            delay = delay < 0 ? 0 : delay;
            BukkitTask t = runTaskLater(Objects.requireNonNull(Bukkit.getPluginManager().getPlugin("SkillAPI")), delay);
            taskId = t.getTaskId();
            if (tasks.containsKey(id)) {
                tasks.get(id).put(taskId, t);
            } else {
                tasks.put(id, new ConcurrentHashMap<Integer, BukkitTask>() {{
                    put(taskId, t);
                }});
            }
        }

        @Override
        public void run() {
            task.run();
            // remove the task from the list
            Map<Integer, BukkitTask> list = tasks.get(id);
            if (list != null) {
                list.remove(taskId);
            }
        }
    }
}
