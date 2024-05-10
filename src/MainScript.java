import Paint.ScriptPaint;
import Task.subclasses.BankRestock;
import Task.subclasses.CombineItems;
import Task.subclasses.Idle;
import Task.CircularLLTask;
import Util.GUI;
import Util.StartUpUtil;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Task.CircularLLTask.clearSubclassInstances;

@ScriptManifest(author = "yfoo", name = "Bankstanding Skiller 1", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 1.0, logo = "https://i.imgur.com/un9b95T.png")
public class MainScript extends Script {

    private ScriptPaint painter;

    @Override
    public void onStart() throws InterruptedException {
        StartUpUtil.handleRecipeConfiguration(this);
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
        return 0;
    }

    @Override
    public void onStop() throws InterruptedException {
        super.onStop();
        clearSubclassInstances();
        painter.deconstructPainter();
    }
}
