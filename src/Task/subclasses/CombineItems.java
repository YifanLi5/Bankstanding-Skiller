package Task.subclasses;

import Paint.ScriptPaint;
import Task.LLCycleTask;
import Util.GUI;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.model.Item;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.utility.Condition;
import org.osbot.rs07.utility.ConditionalLoop;
import org.osbot.rs07.utility.ConditionalSleep2;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import static Util.ScriptConstants.*;

public class CombineItems extends LLCycleTask {

    private static final String[] CREATE_VERBS = {"Make", "String", "Cut", "Make sets:"};

    public CombineItems(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        boolean shouldRun = false;
        switch (combinationType) {
            case _14_14:
            case _1_27:
                shouldRun = inventory.containsAll(itemA.getId(), itemB.getId());
                break;
            case _1_X_26:
                shouldRun = inventory.containsAll(itemA.getId(), itemB.getId(), getItemC_Id());
                break;
        }
        return shouldRun;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
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
        // Assert Item1 and 2 are the equivalent items determined at script start (ItemA and B).
        // Not foolproof, but a simple method
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
            List<RS2Widget> widgets = new ArrayList<>(getWidgets().containingActions(270, CREATE_VERBS));
            return !widgets.isEmpty();
        });
        if (!foundWidget) {
            warn("Unable to find the select item to create widget.");
            return false;
        }

        // set the correct spacebar make option based on script startup param.
        // config 2673 determines which make option is currently bound to spacebar.
        boolean result;

        if (GUI.userInput > 0 && !configs.isSet(2673, GUI.userInput - 1)) {
            char keyToType = (char) ('0' + GUI.userInput);
            String status = "Setting ingame make option -> " + keyToType;
            ScriptPaint.setStatus(status);
            log(status);

            result = keyboard.typeContinualKey(keyToType, new Condition() {
                @Override
                public boolean evaluate() {
                    return ConditionalSleep2.sleep(2000, () -> {
                        List<RS2Widget> widgets = new ArrayList<>(getWidgets().containingActions(270, CREATE_VERBS));
                        return widgets.isEmpty();
                    });
                }
            });
        } else {
            sleep(randomGaussian(300, 100));
            keyboard.pressKey(KeyEvent.VK_SPACE);
            ScriptPaint.setStatus("Spacebar-ing make widget");
            result = ConditionalSleep2.sleep(2000, () -> {
                List<RS2Widget> widgets = new ArrayList<>(getWidgets().containingActions(270, CREATE_VERBS));
                return widgets.isEmpty();
            });
            sleep(randomGaussian(300, 100));
            keyboard.releaseKey(KeyEvent.VK_SPACE);
        }
        return result;
    }

    class DoWhile_CombineItems extends ConditionalLoop {
        DoWhile_CombineItems(Bot bot, int maxLoopCycles) {
            super(bot, maxLoopCycles);
        }

        @Override
        public boolean condition() {
            boolean loopAgain = true;
            try {
                if (combineComponents()) {
                    loopAgain = !spacebarMakeWidget();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (loopAgain) {
                inventory.deselectItem();
                widgets.closeOpenInterface();
            }
            return loopAgain;
        }
    }
}
