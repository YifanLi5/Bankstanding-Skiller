import Paint.ScriptPaint;
import Task.BankRestock;
import Task.CombineItems;
import Task.Idle;
import Task.Task;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.Message;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.HashMap;

import static Task.Task.clearSubclassInstances;
import static Util.ScriptConstants.*;

@ScriptManifest(author = "yfoo", name = "(debug5) Item Combiner v2", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 0.9, logo = "https://i.imgur.com/un9b95T.png")
public class MainScript extends Script {
    // Todo: Add CLI support
    private static final int FAILSAFE_LIMIT = 5;
    private static final int N_I_H_LIMIT = 5;
    private static int noNextTaskCount = 0;
    private static int nothingInterestingHappensCount = 0;
    private ScriptPaint painter;

    @Override
    public void onStart() {
        handleRecipeConfiguration();
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

    private void handleRecipeConfiguration() {
        Item[] inventoryItems = inventory.getItems();
        HashMap<Integer, Item> intItemMapping = new HashMap<>();
        for (Item item : inventoryItems) {
            if (item == null || intItemMapping.containsKey(item.getId()))
                continue;
            intItemMapping.put(item.getId(), item);
        }

        if (intItemMapping.size() == 3) {
            combinationType = CombinationType._1_X_26;
            for (Item item : intItemMapping.values()) {
                if (itemA == null && inventory.getAmount(item.getId()) == 1 && item.getAmount() == 1) {
                    itemA = item.getDefinition();
                } else if (itemB == null && inventory.getAmount(item.getId()) > 1) {
                    itemB = item.getDefinition();
                } else if (itemC == null && item.getNotedId() == -1 && item.getAmount() > 1) {
                    itemC = item.getDefinition();
                } else {
                    warn("Failsafe: Else condition hit when attempting to assign 3 items to A,B,C.");
                    stop(false);
                    return;
                }
            }
        } else if (intItemMapping.size() == 2) {
            for (Item item : intItemMapping.values()) {
                if (inventory.getAmount(item.getId()) == 1 && itemA == null) {
                    itemA = item.getDefinition();
                } else if (inventory.getAmount(item.getId()) == 27 && itemB == null) {
                    itemB = item.getDefinition();
                } else if (inventory.getAmount(item.getId()) == 14) {
                    if (itemA == null)
                        itemA = item.getDefinition();
                    else if (itemB == null)
                        itemB = item.getDefinition();
                }
            }

            if (inventory.getAmount(itemA.getId()) == 1 && inventory.getAmount(itemB.getId()) == 27) {
                combinationType = CombinationType._1_27;
            } else if (inventory.getAmount(itemA.getId()) == inventory.getAmount(itemB.getId())) {
                combinationType = CombinationType._14_14;
            } else {
                warn("Something went wrong. Unable to discern between 1_27 or 14_14.");
                stop(false);
                return;
            }
        } else if (intItemMapping.size() <= 1) {
            warn("Detected only 1 unique item or an empty inventory. " +
                    "This script must be started with either a 14-14 || 1-27 || 1-X-26 inventory setup." +
                    "ex: 14 unf potions + 14 herbs OR 1 knife + 27 logs OR 1 needle, X thread, 26 leather");
            stop(false);
            return;
        } else {
            warn("Inventory is not properly setup.");
            stop(false);
            return;
        }

        if (itemA == null || itemB == null) {
            warn("Failed assert, itemA and itemB cannot be null!");
            stop(false);
        } else if (combinationType == CombinationType._1_X_26 && itemC == null) {
            warn("Failed assert, itemC cannot be null if combination type is _1_X_26.");
            stop(false);
        }

        log(String.format("Type: %s, ItemA: %s, ItemB: %s, ItemC: %s",
                combinationType,
                itemA.getName(),
                itemB.getName(),
                itemC != null ? itemC.getName() : "N/A"
        ));
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
