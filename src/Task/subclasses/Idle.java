package Task.subclasses;

import Paint.ScriptPaint;
import Task.CircularLLTask;
import Util.InventoryWatcher;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static Util.ScriptConstants.*;

public class Idle extends CircularLLTask {
    private AnimationWatcher animationWatcher = null;
    private final ConditionalSleep sleepUntilInventoryProcessed = new ConditionalSleep(60000, 1000) {
        @Override
        public boolean condition() {
            if (dialogues.isPendingContinuation()) {
                return true;
            }

            boolean result = false;
            switch (combinationType) {
                case _1_27:
                case _14_14:
                    result = (!inventory.contains(itemB.getId()) || !inventory.contains(itemA.getId())) && !myPlayer().isAnimating();
                    break;
                case _1_X_26:
                    result = animationWatcher.hasPlayerBeenIdling();
            }
            return result;
        }
    };
    private ScheduledExecutorService scheduler;

    public Idle(Bot bot) {
        super(bot);
        InventoryWatcher.startWatcher(bot.getMethods());
        scheduler = Executors.newScheduledThreadPool(3);
        if (combinationType == CombinationType._1_X_26) {
            animationWatcher = new AnimationWatcher();
            scheduler.scheduleWithFixedDelay(animationWatcher, 0, 500, TimeUnit.MILLISECONDS);
        }

    }

    @Override
    public boolean shouldRun() {
        boolean shouldRun = false;
        switch (combinationType) {
            case _1_27:
            case _14_14:
                shouldRun = ConditionalSleep2.sleep(3000, () -> !inventory.onlyContains(itemA.getId(), itemB.getId()));
                break;
            case _1_X_26:
                shouldRun = ConditionalSleep2.sleep(3000, () -> !inventory.onlyContains(itemA.getId(), itemB.getId(), itemC.getId()));
        }
        return shouldRun;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ScriptPaint.setStatus("Combining Items... (Idle)");
        mouse.moveOutsideScreen();
        sleepUntilInventoryProcessed.sleep();

        //counter.resetCounter();
        if (!mouse.isOnScreen()) {
            ScriptPaint.setStatus("Simulating AFK");
            long idleTime = randomSessionGaussian();
            log(String.format("Simulating AFK for %dms", idleTime));
            sleep(idleTime);
        }
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        InventoryWatcher.shutdownWatcher();
        if (scheduler != null)
            scheduler.shutdown();
    }

    private final class AnimationWatcher implements Runnable {
        private long lastAnimTime = 0;

        @Override
        public void run() {
            if (myPlayer().isAnimating() || lastAnimTime == 0)
                lastAnimTime = System.currentTimeMillis();
        }

        public boolean hasPlayerBeenIdling() {
            if (lastAnimTime != 0)
                return System.currentTimeMillis() - lastAnimTime > 3000;
            return false;
        }
    }
}
