package Task.subclasses;

import Paint.ScriptPaint;
import Task.CircularLLTask;
import Util.BankingUtil;
import Util.RetryUtil;
import org.osbot.rs07.Bot;

import static Util.ScriptConstants.*;

public class BankRestock extends CircularLLTask {

    public BankRestock(Bot bot) {
        super(bot);
    }

    @Override
    public boolean shouldRun() {
        boolean shouldRestock = false;
        switch (combinationType) {
            case _1_27:
                shouldRestock = !inventory.contains(itemB.getId());
                break;
            case _1_X_26:
                shouldRestock = inventory.getAmount(itemB.getId()) < 26;
                break;
            case _14_14:
                shouldRestock = !inventory.containsAll(itemA.getId(), itemB.getId());
                break;

        }

        return shouldRestock;
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        ScriptPaint.setStatus(String.format("Bank restock (%s)", combinationType));
        if (!RetryUtil.retry(() -> bank.isOpen() || bank.open(), 3, 1000)) {
            stopScriptNow("Unable to open the bank");
            return;
        }

        switch (combinationType) {
            case _1_X_26:
                if (!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.ALL)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.ALL);
                    return;
                }
                if (bank.getAmount(itemB.getId()) == 0) {
                    stopScriptNow("Shortage of " + itemB.getName());
                    return;
                }
                if (!inventory.contains(getItemC_Id()) || !inventory.contains(itemA.getId())) {
                    stopScriptNow(String.format("Shortage of %s or %s", itemA.getName(), itemC.getName()));
                    return;
                }
                if (!RetryUtil.retry(() -> bank.depositAllExcept(itemA.getId(), itemB.getId(), itemC.getId()) && bank.withdrawAll(itemB.getId()), 3, 1000)) {
                    stopScriptNow(String.format("Unable to deposit all processed items and fill inventory with %s", itemB.getName()));
                    return;
                }
                if (!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                    return;
                }
                break;
            case _1_27:
                if (!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.ALL)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.ALL);
                    return;
                }
                if (bank.getAmount(itemB.getId()) == 0) {
                    stopScriptNow("Shortage of " + itemB.getName());
                    return;
                }
                if (!RetryUtil.retry(() -> bank.depositAllExcept(itemA.getId()) && bank.withdrawAll(itemB.getId()), 3, 1000)) {
                    stopScriptNow(String.format("Unable to deposit all %s and fill inventory with %s", itemA.getName(), itemB.getName()));
                    return;
                }
                if (!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                    return;
                }
                break;
            case _14_14:
                if (!BankingUtil.setBankingQuantityOption(bot.getMethods(), BankingUtil.BankingQuantityWidgetOptions.X)) {
                    stopScriptNow("Unable to set banking quantity widget to " + BankingUtil.BankingQuantityWidgetOptions.X);
                    return;
                }
                if (!bank.depositAll()) {
                    stopScriptNow("Unable to deposit all items");
                    return;
                }
                if (bank.getAmount(itemA.getId()) < 14 || bank.getAmount(itemB.getId()) < 14) {
                    stopScriptNow(String.format("Shortage of either %s or %s", itemA.getName(), itemB.getName()));
                    return;
                }
                boolean itemA_withdrawn = RetryUtil.retry(() -> bank.withdraw(itemA.getId(), 14), 3, 1500);
                boolean itemB_withdrawn = RetryUtil.retry(() -> bank.withdraw(itemB.getId(), 14), 3, 1500);
                if (!itemA_withdrawn || !itemB_withdrawn) {
                    stopScriptNow(String.format("Unable to withdraw 14 of either %s or %s", itemA.getName(), itemB.getName()));
                }
                if (!bank.close()) {
                    stopScriptNow("Unable to close the bank.");
                }
                break;
        }
    }
}
