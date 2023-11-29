package Task;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.listener.GameTickListener;
import org.osbot.rs07.utility.ConditionalSleep;

import static Util.ScriptConstants.*;

public class Idle extends Task implements GameTickListener {
    private final ConditionalSleep sleepUntilInventoryProcessed = new ConditionalSleep(60000, 1000, 500) {

        @Override
        public boolean condition() {
            // Todo: Fix d'hide body crafting. There will be 2 leathers left, not enough to make 1 more.
            return !inventory.containsAll(itemA.getId(), itemB.getId()) || dialogues.isPendingContinuation();
        }
    };

    // Some animations such as stringing bows or making potions have downtime between cycles where the player's animation becomes -1
    // before preforming the next animation cycle. The below 2 variables help smooth this out, sorta like a capacitor.
    private boolean hasAnimatedRecently = false;
    private int animationCapacitorRunoff = 2;

    public Idle(Bot bot) {
        super(bot);
        bot.addGameTickListener(this);
    }

    @Override
    public boolean shouldRun() {
        return myPlayer().isAnimating() || hasAnimatedRecently;
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

    @Override
    public void onGameTick() {
        if(myPlayer().isAnimating()) {
            hasAnimatedRecently = true;
            animationCapacitorRunoff = 2;
        } else {
            animationCapacitorRunoff -= 1;
        }
        if(animationCapacitorRunoff <= 0) {
            hasAnimatedRecently = false;
        }
    }
}
