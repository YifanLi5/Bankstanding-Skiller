import Paint.ScriptPaint;
import Task.BankRestock;
import Task.CombineItems;
import Task.Idle;
import Task.Task;
import Util.GUI;
import Util.GameTickUtil;
import Util.StartUpUtil;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Task.Task.clearSubclassInstances;

@ScriptManifest(author = "yfoo", name = "(debug1) Item Combiner v2", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 1.0, logo = "https://i.imgur.com/un9b95T.png")
public class MainScript extends Script {
    // Todo: Pineapple pizzas don't have an animation...
    private static final int FAILSAFE_LIMIT = 5;
    //N_I_H == Nothing interesting happened
    private static final int N_I_H_LIMIT = 5;
    private static int noNextTaskCount = 0;
    private static int nothingInterestingHappensCount = 0;
    private ScriptPaint painter;

    @Override
    public void onStart() throws InterruptedException {
        StartUpUtil.handleRecipeConfiguration(this);
        GameTickUtil.createSingletonGlobalInstance(this.bot);
        GUI.startAndAwaitInput();
        if (GUI.userInput < 0) {
            warn("Stopping script, GUI was closed.");
            stop(false);

        }

        log("User Input: " + GUI.userInput);
        painter = new ScriptPaint(this);

        new CombineItems(bot);
        new BankRestock(bot);
        new Idle(bot);
    }

    @Override
    public int onLoop() throws InterruptedException {
        Task nextTask = Task.pollNextTask();
        if (nextTask != null) {
            noNextTaskCount = 0;
            if (nextTask instanceof Idle && nothingInterestingHappensCount > 0) {
                log("resetting nothingInterestingHappensCount -> 0 due to going into item processing animation");
                nothingInterestingHappensCount = 0;
            }
            nextTask.runTask();
        } else {
            if (noNextTaskCount >= FAILSAFE_LIMIT) {
                warn("Hit noNextTaskCount's FAILSAFE_LIMIT: " + FAILSAFE_LIMIT);
                stop(false);
            }
            noNextTaskCount += 1;
            warn(String.format("No next task... (%d/%d)", noNextTaskCount, FAILSAFE_LIMIT));
            return 2500;
        }
        return random(500, 1000);
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        clearSubclassInstances();
        painter.deconstructPainter();
    }


    @Override
    public void onMessage(Message msg) throws InterruptedException {
        super.onMessage(msg);
        if (msg.getType() == Message.MessageType.GAME && msg.getMessage().equals("Nothing interesting happens")) {
            nothingInterestingHappensCount += 1;
            warn(String.format("Received 'Nothing interesting happens' game message (%d/%d)", nothingInterestingHappensCount, N_I_H_LIMIT));
            if (nothingInterestingHappensCount >= N_I_H_LIMIT) {
                warn("Failsafe: Received multiple 'Nothing interesting happens' game messages.");
                stop(false);
            }

        }
    }
}
