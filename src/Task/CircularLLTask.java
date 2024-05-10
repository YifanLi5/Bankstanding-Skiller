package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.ArrayList;

public abstract class CircularLLTask extends MethodProvider {
    public final static boolean LOGOUT_ON_SCRIPT_STOP = false;
    private static final ArrayList<CircularLLTask> subclassInstances = new ArrayList<>();
    private static boolean stopScriptNow = false;
    private static int iterCount = 0;
    protected final Script script;

    public CircularLLTask(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);
        this.script = bot.getScriptExecutor().getCurrent();
        log("Initialized task instance of type: " + this.getClass().getCanonicalName());
    }

    public static CircularLLTask nextTask() throws InterruptedException {
        int startIdx = iterCount % subclassInstances.size();
        CircularLLTask task = null;

        while (task == null || !task.shouldRun()) {
            if (task != null) {
                task.script.log(String.format("%s shouldRun() -> false", task.getClass().getSimpleName()));
                if (startIdx == iterCount % subclassInstances.size()) {
                    task.stopScriptNow("Made a full loop and did not find a suitable task to run");
                    task = null;
                    break;
                }
            }
            task = subclassInstances.get(iterCount % subclassInstances.size());
            iterCount++;
        }

        return task;
    }

    public static void clearSubclassInstances() {
        for (CircularLLTask task : subclassInstances) {
            task.cleanup();
        }
        subclassInstances.clear();
    }

    public static boolean isStopScriptNow() {
        return stopScriptNow;
    }

    public void stopScriptNow(String errorMsg) {
        warn("Error: " + errorMsg);
        script.stop(LOGOUT_ON_SCRIPT_STOP);
        stopScriptNow = true;
    }

    public abstract boolean shouldRun() throws InterruptedException;

    public abstract void runTask() throws InterruptedException;

    protected void cleanup() {
    }
}
