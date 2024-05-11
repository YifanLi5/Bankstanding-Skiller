package Task.subclasses;

import Paint.ScriptPaint;
import Task.LLCycleTask;
import Util.AnimationWatcher;
import Util.InventoryWatcher;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.concurrent.ScheduledExecutorService;

import static Util.ScriptConstants.*;

public class Idle extends LLCycleTask {

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
                    result = AnimationWatcher.hasPlayerBeenIdling();
            }
            return result;
        }
    };
    private ScheduledExecutorService scheduler;

    public Idle(Bot bot) {
        super(bot);
        InventoryWatcher.startWatcher(bot.getMethods());
        AnimationWatcher.startWatcher(bot.getMethods());
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
        AnimationWatcher.shutdownWatcher();
    }
}
