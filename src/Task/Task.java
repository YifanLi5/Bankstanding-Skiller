package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.script.MethodProvider;

import java.util.ArrayList;

public abstract class Task extends MethodProvider {


    private static final ArrayList<Task> subclassInstances = new ArrayList<>();

    public Task(Bot bot) {
        exchangeContext(bot);
        subclassInstances.add(this);

        log("Initialized task instance of type: " + this.getClass().getCanonicalName());
    }

    public static Task pollNextTask() {
        int weightingSum = 0;
        ArrayList<Task> runnableTasks = new ArrayList<>();
        for (Task task : Task.subclassInstances) {
            if (task.shouldRun()) {
                runnableTasks.add(task);
                weightingSum += task.probabilityWeight();
            }
        }
        if (runnableTasks.isEmpty()) {
            return null;
        } else if (runnableTasks.size() == 1) {
            return runnableTasks.get(0);
        }

        int roll = random(weightingSum);
        int idx = 0;
        for (; idx < runnableTasks.size(); idx++) {
            roll -= runnableTasks.get(idx).probabilityWeight();
            if (roll < 0) {
                break;
            }
        }

        return runnableTasks.get(idx);
    }

    public static void clearSubclassInstances() {
        for (Task task: subclassInstances) {
            task.cleanUp();
        }
        subclassInstances.clear();
    }

    public abstract boolean shouldRun();

    public abstract void runTask() throws InterruptedException;

    public int probabilityWeight() {
        return 1;
    }

    void cleanUp() {
        log("Cleaning up " + this.getClass().getSimpleName());
    }
}
