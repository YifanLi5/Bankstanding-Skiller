package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalSleep;

import static Util.ScriptConstants.*;

public class Idle extends Task {
    private final ConditionalSleep sleepUntilInventoryProcessed = new ConditionalSleep(60000, 1000, 500) {

        @Override
        public boolean condition() {
            return !inventory.containsAll(itemA.getId(), itemB.getId()) || dialogues.isPendingContinuation();
        }
    };

    public Idle(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().isAnimating();
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ScriptPaint.setStatus("Combining Items... (Idle)");
        mouse.moveOutsideScreen();
        sleepUntilInventoryProcessed.sleep();
        if (!mouse.isOnScreen()) {
            ScriptPaint.setStatus("Simulating AFK");
            long idleTime = randomSessionGaussian();
            log(String.format("Simulating AFK for %dms", idleTime));
            sleep(idleTime);
        }
    }
}
