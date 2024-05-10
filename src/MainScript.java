import Paint.ScriptPaint;
import Task.subclasses.BankRestock;
import Task.subclasses.CombineItems;
import Task.subclasses.Idle;
import Task.CircularLLTask;
import Util.GUI;
import Util.GameTickUtil;
import Util.RngUtil;
import Util.StartUpUtil;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Task.CircularLLTask.clearSubclassInstances;

@ScriptManifest(author = "yfoo", name = "Bankstanding Skiller 0", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 1.0, logo = "https://i.imgur.com/un9b95T.png")
public class MainScript extends Script {
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
        new Idle(bot);
        new BankRestock(bot);
    }

    @Override
    public int onLoop() throws InterruptedException {
        if(CircularLLTask.isStopScriptNow()) {
            stop(false);
            return 5000;
        }

        CircularLLTask nextTask = CircularLLTask.nextTask();
        if(nextTask != null) {
            nextTask.runTask();
        }
        return RngUtil.gaussian(250, 50, 0, 350);
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        clearSubclassInstances();
        GameTickUtil.globalRef.removeListener();
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
