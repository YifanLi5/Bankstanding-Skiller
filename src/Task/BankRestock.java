package Task;

import Paint.ScriptPaint;
import Util.DoWhile_BankRestock;
import Util.GameTickUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

import static Util.ScriptConstants.*;

//Used for 1_27 or 14_14
public class BankRestock extends Task {

    private final Filter<Item> outputItemFilter = item -> item.getId() != itemA.getId() && item.getId() != itemB.getId() && item.getId() != getItemC_Id();

    public BankRestock(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        boolean inventoryMatchesRestockedState = false;
        switch (combinationType) {
            case _1_27:
                inventoryMatchesRestockedState = inventory.getAmount(itemA.getId()) == 1 && inventory.getAmount(itemB.getId()) == 27;
                break;
            case _14_14:
                inventoryMatchesRestockedState = inventory.getAmount(itemA.getId()) == 14 && inventory.getAmount(itemB.getId()) == 14;
                break;
            case _1_X_26:
                inventoryMatchesRestockedState = inventory.getAmount(itemA.getId()) == 1
                        && inventory.getAmount(itemB.getId()) == 26
                        && inventory.contains(itemC.getId());
        }

        return !GameTickUtil.globalRef.hasAnimatedRecently && !GameTickUtil.globalRef.inventoryHasChangedRecently.get() && !inventoryMatchesRestockedState;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ScriptPaint.incrementNumItemsProcessed((int) inventory.getAmount(outputItemFilter));

        DoWhile_BankRestock bankRestock = new DoWhile_BankRestock(bot, 3);
        bankRestock.start();
        if (!bankRestock.getResult()) {
            warn("Failsafe! Unable to use bank restock after 3 attempts.");
            bot.getScriptExecutor().stop(false);
        }
    }
}
