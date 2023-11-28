package Task;

import Paint.ScriptPaint;
import Util.DoWhile_BankRestock;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

import static Util.ScriptConstants.*;

//Used for 1_27 or 14_14
public class BankRestock extends Task {

    private final Filter<Item> outputItemFilter = item -> item.getId() != itemA_id && item.getId() != itemB_id && item.getId() != itemC_id;

    public BankRestock(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        return !inventory.containsAll(itemA_id, itemB_id);
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
