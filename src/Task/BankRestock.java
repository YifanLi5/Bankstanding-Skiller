package Task;

import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.utility.ConditionalLoop;
import org.osbot.rs07.utility.ConditionalSleep2;

import static Util.ScriptConstants.*;

public class BankRestock extends Task {
    class DoWhile_BankRestock extends ConditionalLoop {
        final int BANK_ROOT_ID = 12;
        final int ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED = 1666;
        final int _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED = 0b1100;
        final int ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT = 304;
        final int _304_WITHDRAW_X_AMOUNT_OF_14 = 0b11100;
        final int _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED = 0b10000;


        DoWhile_BankRestock(Bot bot, int maxLoopCycles) {
            super(bot, maxLoopCycles);
        }

        @Override
        public boolean condition() {
            try {
                boolean loopAgain = true;
                switch(combinationType) {
                    case _14_14:
                        boolean fixWithdrawXAmount = !configs.isSet(
                                ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                                _304_WITHDRAW_X_AMOUNT_OF_14
                        );
                        boolean toggleWithdrawXWidget = !configs.isSet(
                                ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                                _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
                        );

                        loopAgain = fixWithdrawXAmount || toggleWithdrawXWidget ? !setWithdrawXFor_14_14_Restock() : !handle_14_14_Restock();
                        break;
                    case _1_27:
                        boolean toggleWithdrawAll = !configs.isSet(
                                ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                                _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
                        );

                        loopAgain = toggleWithdrawAll ? !setWithdrawAllFor_1_27_Restock() : !handle_1_27_Restock();
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

        private boolean setWithdrawXFor_14_14_Restock() throws InterruptedException {
            boolean withdrawXSetTo14 = configs.isSet(
                    ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                    _304_WITHDRAW_X_AMOUNT_OF_14
            );

            boolean withdrawXWidgetActive = configs.isSet(
                    ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                    _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
            );
            if(!bank.open()) {
                log("Unable to open bank. May not be close enough.");
                return false;
            }

            if(!withdrawXSetTo14) {
                log("setting withdraw X amount to 14");
                if(inventory.isEmpty() || bank.depositAll()) {
                    if(!bank.containsAll(itemA_id, itemB_id)) {
                        log("Shortage of items.");
                        bot.getScriptExecutor().stop(false);
                    } else {
                        boolean itemA_withdrawn = bank.withdraw(itemA_id, 14);
                        boolean itemB_withdrawn = bank.withdraw(itemB_id, 14);
                        if(!(itemA_withdrawn && itemB_withdrawn)) {
                            warn("Unable with withdraw 14 of itemA or itemB when trying to set withdraw X amount to 14.");
                            return false;
                        }
                    }
                }
            }

            if(!withdrawXWidgetActive) {
                log("toggle ON withdraw X widget");
                RS2Widget withdrawX = widgets.getWidgetContainingText(BANK_ROOT_ID, "X");
                if(withdrawX != null) {
                    WidgetDestination widgetDestination = new WidgetDestination(bot, withdrawX);
                    mouse.click(widgetDestination);
                }
            }

            return ConditionalSleep2.sleep(1000, () -> configs.isSet(
                    ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                    _304_WITHDRAW_X_AMOUNT_OF_14
                ) && configs.isSet(
                        ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                        _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
                )
            );
        }

        private boolean setWithdrawAllFor_1_27_Restock() throws InterruptedException {
            log("toggling withdraw all");
            boolean withdrawAllWidgetActive = configs.isSet(
                    ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                    _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
            );

            if(!bank.open()) {
                log("Unable to open bank. May not be close enough.");
                return false;
            }

            if(!withdrawAllWidgetActive) {
                RS2Widget withdrawAll = widgets.getWidgetContainingText(BANK_ROOT_ID, "ALL");
                if(withdrawAll != null) {
                    WidgetDestination widgetDestination = new WidgetDestination(bot, withdrawAll);
                    mouse.click(widgetDestination);
                }
            }

            return ConditionalSleep2.sleep(1000, () -> configs.isSet(
                    ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                    _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
                )
            );
        }

        private boolean handle_14_14_Restock() throws InterruptedException {
            if(bank.open() && bank.depositAll()) {
                if(!bank.containsAll(itemA_id, itemB_id)) {
                    log("Shortage of items.");
                    bot.getScriptExecutor().stop(true);
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
    public boolean shouldRun() {
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
