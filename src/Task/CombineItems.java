package Task;

import Paint.ScriptPaint;
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

    public CombineItems(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        boolean shouldRun = false;
        switch (combinationType) {
            case _14_14:
            case _1_27:
                shouldRun = inventory.containsAll(itemA.getId(), itemB.getId()) && (inventory.onlyContains(itemA.getId(), itemB.getId()) || dialogues.isPendingContinuation());
                break;
            case _1_X_26:
                shouldRun = inventory.containsAll(itemA.getId(), itemB.getId(), getItemC_Id()) && (inventory.onlyContains(itemA.getId(), itemB.getId(), getItemC_Id()) || dialogues.isPendingContinuation());
                break;
            default:
                warn("recognized Enum shouldRun :: CombineItems");
        }
        return shouldRun;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ConditionalSleep2.sleep(1000, () -> !bank.isOpen() || bank.close());
        ConditionalSleep2.sleep(1000, () -> !inventory.isItemSelected() || inventory.deselectItem());
        DoWhile_CombineItems combineItems = new DoWhile_CombineItems(bot, 3);
        combineItems.start();
        if (!combineItems.getResult()) {
            warn("Failsafe! Unable to combine items after 3 attempts.");
            bot.getScriptExecutor().stop(false);
        }
        sleep(1250);
    }

    private boolean combineComponents() throws InterruptedException {
        int[] slotPair = getInvSlotPair();
        Item item1 = inventory.getItemInSlot(slotPair[0]);
        Item item2 = inventory.getItemInSlot(slotPair[1]);

        boolean notNull = item1 != null && item2 != null;
        boolean notSameItem = notNull && item1.getId() != item2.getId();
        // A mostly reliable way to determine item1 and 2 are the configured itemA and itemB from before.
        boolean sumTo0 = notNull && (item1.getId() + item2.getId() - itemA.getId() - itemB.getId() == 0);

        boolean canUseSlotPair = notSameItem && sumTo0;

        if (canUseSlotPair && inventory.interact(slotPair[0], USE)) {
            ScriptPaint.setStatus("ItemA -> ItemB");
            sleep(randomGaussian(300, 100));
            return inventory.isItemSelected() && inventory.interact(slotPair[1], USE);
        } else {
            ScriptPaint.setStatus("ItemA -> ItemB w/ backup interaction");
            if (inventory.interact(USE, itemA.getId())) {
                sleep(randomGaussian(300, 100));
                return inventory.isItemSelected() && inventory.interact(USE, itemB.getId());
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
        if (!foundWidget) {
            warn("Unable to find the select item to create widget.");
            return false;
        }

        //Tap spacebar, stop when player starts animating
        sleep(randomGaussian(300, 100));
        keyboard.pressKey(VK_SPACE);
        ScriptPaint.setStatus("Spacebar-ing make widget");
        boolean result = ConditionalSleep2.sleep(1500, () -> myPlayer().isAnimating());
        sleep(randomGaussian(300, 100));
        keyboard.releaseKey(VK_SPACE);
        return result;
    }

    class DoWhile_CombineItems extends ConditionalLoop {
        DoWhile_CombineItems(Bot bot, int maxLoopCycles) {
            super(bot, maxLoopCycles);
        }

        @Override
        public boolean condition() {
            try {
                if (combineComponents()) {
                    return !spacebarMakeWidget();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            inventory.deselectItem();
            widgets.closeOpenInterface();
            return true;
        }
    }
}
