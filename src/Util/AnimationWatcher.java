package Util;

import org.osbot.rs07.script.MethodProvider;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnimationWatcher {

    private static AnimationWatcherTask task;
    private static ScheduledExecutorService scheduler;

    public static void startWatcher(MethodProvider methodProvider) {
        task = new AnimationWatcherTask(methodProvider);
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(task, 0, 500, TimeUnit.MILLISECONDS);
    }

    public static boolean hasPlayerBeenIdling() {
        return task.hasPlayerBeenIdling();
    }

    public static void shutdownWatcher() {
        scheduler.shutdown();
    }

    static class AnimationWatcherTask implements Runnable {
        private long lastAnimTime = 0;
        private final MethodProvider methodProvider;

        public AnimationWatcherTask(MethodProvider methodProvider) {
            this.methodProvider = methodProvider;
        }

        @Override
        public void run() {
            if (methodProvider.myPlayer().isAnimating() || lastAnimTime == 0)
                lastAnimTime = System.currentTimeMillis();
        }

        public boolean hasPlayerBeenIdling() {
            if (lastAnimTime != 0)
                return System.currentTimeMillis() - lastAnimTime > 3000;
            return false;
        }
    }
}
