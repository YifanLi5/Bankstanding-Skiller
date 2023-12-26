package Util;

import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.script.Script;

import java.util.HashMap;

import static Util.ScriptConstants.*;
import static Util.ScriptConstants.itemC;

public class StartUpUtil {

    public static void handleRecipeConfiguration(Script script) throws InterruptedException {
        Item[] inventoryItems = script.inventory.getItems();
        HashMap<Integer, Item> intItemMapping = new HashMap<>();
        for (Item item : inventoryItems) {
            if (item == null || intItemMapping.containsKey(item.getId()))
                continue;
            intItemMapping.put(item.getId(), item);
        }

        if (intItemMapping.size() == 3) {
            combinationType = ScriptConstants.CombinationType._1_X_26;
            for (Item item : intItemMapping.values()) {
                if (itemA == null && script.inventory.getAmount(item.getId()) == 1 && item.getAmount() == 1) {
                    itemA = item.getDefinition();
                } else if (itemB == null && script.inventory.getAmount(item.getId()) > 1) {
                    itemB = item.getDefinition();
                } else if (itemC == null && item.getNotedId() == -1 && item.getAmount() > 1) {
                    itemC = item.getDefinition();
                } else {
                    script.warn("Failsafe: Else condition hit when attempting to assign 3 items to A,B,C.");
                    script.stop(false);
                    return;
                }
            }
        } else if (intItemMapping.size() == 2) {
            for (Item item : intItemMapping.values()) {
                if (script.inventory.getAmount(item.getId()) == 1 && itemA == null) {
                    itemA = item.getDefinition();
                } else if (script.inventory.getAmount(item.getId()) == 27 && itemB == null) {
                    itemB = item.getDefinition();
                } else if (script.inventory.getAmount(item.getId()) == 14) {
                    if (itemA == null)
                        itemA = item.getDefinition();
                    else if (itemB == null)
                        itemB = item.getDefinition();
                }
            }

            if (script.inventory.getAmount(itemA.getId()) == 1 && script.inventory.getAmount(itemB.getId()) == 27) {
                combinationType = CombinationType._1_27;
            } else if (script.inventory.getAmount(itemA.getId()) == script.inventory.getAmount(itemB.getId())) {
                combinationType = CombinationType._14_14;
            } else {
                script.warn("Something went wrong. Unable to discern between 1_27 or 14_14.");
                script.stop(false);
                return;
            }
        } else if (intItemMapping.size() <= 1) {
            script.warn("Detected only 1 unique item or an empty inventory. " +
                    "This script must be started with either a 14-14 || 1-27 || 1-X-26 inventory setup." +
                    "ex: 14 unf potions + 14 herbs OR 1 knife + 27 logs OR 1 needle, X thread, 26 leather");
            script.bot.getScriptExecutor().stop(false);
            return;
        } else {
            script.warn("Inventory is not properly setup.");
            script.stop(false);
            return;
        }

        if (itemA == null || itemB == null) {
            script.warn("Failed assert, itemA and itemB cannot be null!");
            script.stop(false);
        } else if (combinationType == CombinationType._1_X_26 && itemC == null) {
            script.warn("Failed assert, itemC cannot be null if combination type is _1_X_26.");
            script.stop(false);
        }

        script.log(String.format("Type: %s, ItemA: %s, ItemB: %s, ItemC: %s",
                combinationType,
                itemA.getName(),
                itemB.getName(),
                itemC != null ? itemC.getName() : "N/A"
        ));
    }
}
