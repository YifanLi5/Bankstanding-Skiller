package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.utility.ConditionalLoop;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.util.ArrayList;
import java.util.List;

import static Util.ScriptConstants.*;
import static java.awt.event.KeyEvent.VK_SPACE;

public class CombineItems extends Task {

    class DoWhile_CombineItems extends ConditionalLoop {
        DoWhile_CombineItems(Bot bot, int maxLoopCycles) {
            super(bot, maxLoopCycles);
        }

        @Override
        public boolean condition() {
            try {
                if(combineComponents()) {
                    if(spacebarMakeWidget()) {
                        boolean result = ConditionalSleep2.sleep(1500, () -> myPlayer().isAnimating());
                        if(keyboard.isKeyDown(VK_SPACE)) {
                            keyboard.releaseKey(VK_SPACE);
                        }
                        return !result;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            inventory.deselectItem();
            widgets.closeOpenInterface();
            return true;
        }
    }
    public CombineItems(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return inventory.containsAll(itemA_id, itemB_id) && (inventory.onlyContains(itemA_id, itemB_id) || dialogues.isPendingContinuation());
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ConditionalSleep2.sleep(1000, () -> !bank.isOpen() || bank.close());
        ConditionalSleep2.sleep(1000, () -> !inventory.isItemSelected() || inventory.deselectItem());
        DoWhile_CombineItems combineItems = new DoWhile_CombineItems(bot, 3);
        combineItems.start();
        if(!combineItems.getResult()) {
            warn("Failsafe! Unable to combine items after 3 attempts.");
            bot.getScriptExecutor().stop(false);
        }
        sleep(1250);
    }

    private boolean combineComponents() throws InterruptedException {
        int[] slotPair = getInvSlotPair();
        Item item1 = inventory.getItemInSlot(slotPair[0]);
        Item item2 = inventory.getItemInSlot(slotPair[1]);

        boolean canUseSlotPair = item1 != null && item2 != null &&
                (item1.getId() == itemA_id || item1.getId() == itemB_id) && (item2.getId() == itemA_id || item2.getId() == itemB_id);
        if(canUseSlotPair && inventory.interact(slotPair[0], USE)){
            sleep(randomGaussian(300,100));
            return inventory.isItemSelected() && inventory.interact(slotPair[1], USE);
        } else {
            if(inventory.interact(USE, itemA_id)) {
                sleep(randomGaussian(300, 100));
                return inventory.isItemSelected() && inventory.interact(USE, itemB_id);
            }
        }
        return false;
    }

    private boolean spacebarMakeWidget() throws InterruptedException {
        boolean foundWidget = ConditionalSleep2.sleep(1500, () -> {
            // Actions may not be inclusive of every "Create" style verb. So put more here as needed.
            List<RS2Widget> widgets = new ArrayList<>(getWidgets().containingActions(270, "Make", "String"));
            return !widgets.isEmpty() && widgets.get(0) != null;
        });
        if(!foundWidget) {
            warn("Unable to find the select item to create widget.");
            return false;
        }
        sleep(randomGaussian(300, 100));
        keyboard.pressKey(VK_SPACE);
        return true;
    }
}
