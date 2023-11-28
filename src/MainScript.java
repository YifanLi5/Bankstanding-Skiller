import Paint.ScriptPaint;
import Task.BankRestock;
import Task.CombineItems;
import Task.Idle;
import Task.Task;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.Script;
import org.osbot.rs07.script.ScriptManifest;

import java.util.Arrays;
import java.util.Objects;

import static Util.ScriptConstants.*;

@ScriptManifest(author = "yfoo", name = "Item Combiner 2", info = "Does 14-14 or 1-27 bankstanding tasks", version = 0.1, logo = "https://imgur.com/a/7rxlGZw")
public class MainScript extends Script {

    @Override
    public void onStart() {
        handleRecipeConfiguration();
        new ScriptPaint(this);

        new CombineItems(bot);
        new BankRestock(bot);
        new Idle(bot);
    }

    @Override
    public int onLoop() throws InterruptedException {
        Task currentTask = Task.pollNextTask();
        if (currentTask != null) {
            currentTask.runTask();
        }
        return random(500, 1000);
    }

    private void handleRecipeConfiguration() {
        Item[] inventoryItems = inventory.getItems();
        int[] uniqueItemsIds = Arrays.stream(inventoryItems)
                .filter(Objects::nonNull)
                .mapToInt(Item::getId)
                .distinct()
                .toArray();

        if(uniqueItemsIds.length > 2) {
            // Todo: handle needle/thread/hides
            stop(false);
            return;
        } else if(uniqueItemsIds.length < 2) {
            warn("Detected only 1 item type or an empty inventory. " +
                    "This script must be started with either a 14-14 or 1-27 inventory setup." +
                    "ex: 14 unf potions + 14 herbs OR 1 knife + 27 logs");
            stop(false);
            return;
        }

        long itemCount0 = inventory.getAmount(uniqueItemsIds[0]);
        long itemCount1 = inventory.getAmount(uniqueItemsIds[1]);





        combinationType = (Math.abs(itemCount0 - itemCount1) == 26) ? CombinationType._1_27 : CombinationType._14_14;
        itemA_id = (itemCount0 <= itemCount1) ? uniqueItemsIds[0] : uniqueItemsIds[1];
        itemB_id = (itemCount0 <= itemCount1) ? uniqueItemsIds[1] : uniqueItemsIds[0];

        log(String.format("Type: %s, ItemA: %s (id: %d), ItemB: %s (id: %d)",
            combinationType,
            inventory.getItem(itemA_id).getName(),
            itemA_id,
            inventory.getItem(itemB_id).getName(),
            itemB_id
        ));
    }
}
