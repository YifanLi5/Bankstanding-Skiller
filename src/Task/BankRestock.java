package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.utility.ConditionalLoop;

import static Util.ScriptConstants.*;

public class BankRestock extends Task {
    class DoWhile_BankRestock extends ConditionalLoop {
        DoWhile_BankRestock(Bot bot, int maxLoopCycles) {
            super(bot, maxLoopCycles);
        }

        @Override
        public boolean condition() {
            try {
                boolean loopAgain = true;
                switch(combinationType) {
                    case _14_14:
                        loopAgain = !handle_14_14_Restock();
                        break;
                    case _1_27:
                        loopAgain = !handle_1_27_Restock();
                        break;
                    default:
                        log("bad enum");
                        bot.getScriptExecutor().stop(false);
                }
                return loopAgain;
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        private boolean handle_14_14_Restock() throws InterruptedException {
            if(bank.open() && bank.depositAll()) {
                if(!bank.containsAll(itemA_id, itemB_id)) {
                    log("Shortage of items.");
                    bot.getScriptExecutor().stop(false);
                } else {
                    return bank.withdraw(itemA_id, 14) && bank.withdraw(itemB_id, 14);
                }
            }
            return false;
        }

        private boolean handle_1_27_Restock() throws InterruptedException {
            if (bank.open()) {
                if(!bank.containsAll(itemB_id)) {
                    log("Shortage of items.");
                    bot.getScriptExecutor().stop(false);
                } else {
                    return bank.depositAllExcept(itemA_id) && bank.withdrawAll(itemB_id);
                }
            }
            return false;
        }
    }


    public BankRestock(Bot bot) {
        super(bot);
    }

    @Override
    boolean shouldRun() {
        return !inventory.containsAll(itemA_id, itemB_id);
    }

    @Override
    public void runTask() throws InterruptedException {
        log("Running: " + this.getClass().getSimpleName());
        DoWhile_BankRestock bankRestock = new DoWhile_BankRestock(bot, 3);
        bankRestock.start();
        if(!bankRestock.getResult()) {
            warn("Failsafe! Unable to use bank restock after 3 attempts.");
            bot.getScriptExecutor().stop(false);
        }

    }
}
