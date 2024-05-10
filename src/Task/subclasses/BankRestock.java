package Task.subclasses;

import Paint.ScriptPaint;
import Task.CircularLLTask;
import Util.BankingUtil;
import Util.RetryUtil;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.filter.Filter;
import org.osbot.rs07.api.model.Item;

import static Util.ScriptConstants.*;

public class BankRestock extends CircularLLTask {

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

        return !inventoryMatchesRestockedState;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ScriptPaint.setStatus(String.format("Bank restock (%s)", combinationType));
        ScriptPaint.incrementNumItemsProcessed((int) inventory.getAmount(outputItemFilter));
        if(!RetryUtil.retry(() -> bank.isOpen() || bank.open(), 3, 1000)) {
            stopScriptNow("Unable to open the bank");
            return;
        }

        switch (combinationType) {
            case _1_X_26:
                if(!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.ALL)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.ALL);
                    return;
                }
                if(bank.getAmount(itemB.getId()) == 0) {
                    stopScriptNow("Shortage of " + itemB.getName());
                    return;
                }
                if(!RetryUtil.retry(() -> bank.depositAllExcept(itemA.getId(), itemC.getId()) && bank.withdrawAll(itemB.getId()), 3, 1000)) {
                    stopScriptNow(String.format("Unable to deposit all %s and fill inventory with %s", itemA.getName(), itemB.getName()));
                }
                if(!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                }
                break;
            case _1_27:
                if(!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.ALL)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.ALL);
                    return;
                }
                if(bank.getAmount(itemB.getId()) == 0) {
                    stopScriptNow("Shortage of " + itemB.getName());
                    return;
                }
                if(!RetryUtil.retry(() -> bank.depositAllExcept(itemA.getId()) && bank.withdrawAll(itemB.getId()), 3, 1000)) {
                    stopScriptNow(String.format("Unable to deposit all %s and fill inventory with %s", itemA.getName(), itemB.getName()));
                    return;
                }
                if(!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                    return;
                }
                break;
            case _14_14:
                if(!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.X)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.X);
                    return;
                }
                if(bank.getAmount(itemA.getId()) < 14 || bank.getAmount(itemB.getId()) < 14) {
                    stopScriptNow(String.format("Shortage of either %s or %s", itemA.getName(), itemB.getName()));
                    return;
                }
                boolean itemA_withdrawn = RetryUtil.retry(() -> bank.withdraw(itemA.getId(), 14), 3, 1500);
                boolean itemB_withdrawn = RetryUtil.retry(() -> bank.withdraw(itemB.getId(), 14), 3, 1500);
                if(!itemA_withdrawn || !itemB_withdrawn) {
                    stopScriptNow(String.format("Unable to withdraw 14 of either %s or %s", itemA.getName(), itemB.getName()));
                    return;
                }
                if(!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                }
                break;
        }
    }
}
