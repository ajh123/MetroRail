package me.ajh123.metro_rail.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TaskScheduler {
    private static final List<ScheduledTask> tasks = new LinkedList<>();

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register(TaskScheduler::tick);
    }

    public static void runTaskLater(Runnable task, int delayTicks) {
        tasks.add(new ScheduledTask(task, delayTicks));
    }

    private static void tick(MinecraftServer server) {
        Iterator<ScheduledTask> iterator = tasks.iterator();
        while (iterator.hasNext()) {
            ScheduledTask scheduled = iterator.next();
            scheduled.ticks--;
            if (scheduled.ticks <= 0) {
                scheduled.task.run();
                iterator.remove(); // Remove after running
            }
        }
    }

    private static class ScheduledTask {
        Runnable task;
        int ticks;

        ScheduledTask(Runnable task, int ticks) {
            this.task = task;
            this.ticks = ticks;
        }
    }
}
