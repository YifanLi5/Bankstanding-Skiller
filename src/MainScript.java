import Paint.ScriptPaint;
import Task.LLCycleTask;
import Task.subclasses.BankRestock;
import Task.subclasses.CombineItems;
import Task.subclasses.Idle;
import Util.GUI;
import Util.StartUpUtil;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import static Task.LLCycleTask.clearSubclassInstances;

@ScriptManifest(author = "yfoo", name = "Bankstanding Skiller", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 1.0, logo = "https://github.com/YifanLi5/Bankstanding-Skiller/blob/master/bankstanding_skiller_logo.png?raw=true")
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
        if (LLCycleTask.isStopScriptNow()) {
            stop(false);
            return 5000;
        }

        LLCycleTask nextTask = LLCycleTask.nextTask();
        if (nextTask != null) {
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
