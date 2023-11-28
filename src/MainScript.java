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

@ScriptManifest(author = "yfoo", name = "(debug) Item Combiner v2", info = "Does 14-14 || 1-27 || 1-X-26 bankstanding tasks", version = 0.10, logo = "https://i.imgur.com/un9b95T.png")
public class MainScript extends Script {

    private static int noNextTaskCount = 0;
    private static final int FAILSAFE_LIMIT = 5;

    private static int nothingInterestingHappensCount = 0;
    private static final int N_I_H_LIMIT = 5;

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
            if(nextTask instanceof Idle && nothingInterestingHappensCount > 0) {
                log("resetting nothingInterestingHappensCount -> 0 due to going into item processing animation");
                nothingInterestingHappensCount = 0;
            }
            nextTask.runTask();
        } else {
            if(noNextTaskCount >= FAILSAFE_LIMIT) {
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
        itemA_id = -1;
        itemB_id = -1;
        itemC_id = -1;

        Item[] inventoryItems = inventory.getItems();
        HashMap<Integer, Item> intItemMapping = new HashMap<>();
        for(Item item: inventoryItems) {
            if(item == null || intItemMapping.containsKey(item.getId()))
                continue;
            intItemMapping.put(item.getId(), item);
        }
        int[] uniqueItemsIds = intItemMapping.keySet()
                .stream()
                .mapToInt(Integer::intValue)
                .toArray();

        if(uniqueItemsIds.length == 3) {
            combinationType = CombinationType._1_X_26;
            for(Item item: intItemMapping.values()) {
                if(item.getNotedId() == -1 && item.getAmount() > 1 && itemC_id == -1) {
                    itemC_id = item.getId();
                } else if (inventory.getAmount(item.getId()) == 1 && itemA_id == -1) {
                    itemA_id = item.getId();
                } else if(inventory.getAmount(item.getId()) > 1 && itemB_id == -1) {
                    itemB_id = item.getId();
                } else {
                    warn("Failsafe: Else condition hit when attempting to assign 3 items to A,B,C. This should not happen!");
                    stop(false);
                }
            }
            return;
        } else if(uniqueItemsIds.length <= 1) {
            warn("Detected only 1 unique item or an empty inventory. " +
                    "This script must be started with either a 14-14 || 1-27 || 1-X-26 inventory setup." +
                    "ex: 14 unf potions + 14 herbs OR 1 knife + 27 logs OR 1 needle, X thread, 26 leather");
            stop(false);
            return;
        } else if(uniqueItemsIds.length == 2) {
            long itemCount0 = inventory.getAmount(uniqueItemsIds[0]);
            long itemCount1 = inventory.getAmount(uniqueItemsIds[1]);


            combinationType = (Math.abs(itemCount0 - itemCount1) == 26) ? CombinationType._1_27 : CombinationType._14_14;
            itemA_id = (itemCount0 <= itemCount1) ? uniqueItemsIds[0] : uniqueItemsIds[1];
            itemB_id = (itemCount0 <= itemCount1) ? uniqueItemsIds[1] : uniqueItemsIds[0];


        } else {
            warn("Unable to determine what to do...");
            stop(false);
        }

        String itemCName = inventory.getItem(itemC_id) != null ? inventory.getItem(itemC_id).getName() : "null";
        log(String.format("Type: %s, ItemA: %s (id: %d), ItemB: %s (id: %d), ItemC: %s (id: %d)",
                combinationType,
                inventory.getItem(itemA_id).getName(),
                itemA_id,
                inventory.getItem(itemB_id).getName(),
                itemB_id,
                itemCName,
                itemC_id
        ));
    }

    @Override
    public void onMessage(Message msg) throws InterruptedException {
        super.onMessage(msg);
        if(msg.getType() == Message.MessageType.GAME && msg.getMessage().equals("Nothing interesting happens")) {
            nothingInterestingHappensCount += 1;
            warn(String.format("Received 'Nothing interesting happens' game message (%d/%d)", nothingInterestingHappensCount, N_I_H_LIMIT));
            if(nothingInterestingHappensCount >= N_I_H_LIMIT) {
                warn("Failsafe: Received multiple 'Nothing interesting happens' game messages.");
                stop(false);
            }

        }
    }
}
