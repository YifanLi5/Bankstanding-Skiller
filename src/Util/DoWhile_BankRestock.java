package Util;

import Paint.ScriptPaint;
import org.osbot.rs07.Bot;
import org.osbot.rs07.api.ui.RS2Widget;
import org.osbot.rs07.input.mouse.WidgetDestination;
import org.osbot.rs07.script.MethodProvider;
import org.osbot.rs07.utility.ConditionalLoop;
import org.osbot.rs07.utility.ConditionalSleep2;

import static Util.ScriptConstants.*;


public class DoWhile_BankRestock extends ConditionalLoop {
    final int BANK_ROOT_ID = 12;
    final int ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED = 1666;
    final int _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED = 0b1100;
    final int ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT = 304;
    final int _304_WITHDRAW_X_AMOUNT_OF_14 = 0b11100;
    final int _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED = 0b10000;

    MethodProvider methods;

    public DoWhile_BankRestock(Bot bot, int maxLoopCycles) {
        super(bot, maxLoopCycles);
        this.methods = bot.getMethods();
    }

    @Override
    public boolean condition() {
        try {
            boolean loopAgain = true;
            switch (combinationType) {
                case _14_14:
                    boolean fixWithdrawXAmount = !methods.configs.isSet(
                            ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                            _304_WITHDRAW_X_AMOUNT_OF_14
                    );
                    boolean toggleWithdrawXWidget = !methods.configs.isSet(
                            ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                            _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
                    );

                    loopAgain = fixWithdrawXAmount || toggleWithdrawXWidget ? !turnOnWithdrawXFor_14_14_Restock() : !handle_14_14_Restock();
                    break;
                case _1_X_26:
                    if (methods.inventory.getAmount(itemC_id) <= 0) {
                        methods.warn(String.format("Ran out of consumable itemC (id: %d)", itemC_id));
                        methods.bot.getScriptExecutor().stop(false);
                    }
                    boolean toggleWithdrawAll1 = !methods.configs.isSet(
                            ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                            _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
                    );
                    loopAgain = toggleWithdrawAll1 ? !turnOnWithdrawAll() : !handle_1_X_26_Restock();
                    break;
                case _1_27:
                    boolean toggleWithdrawAll2 = !methods.configs.isSet(
                            ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                            _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
                    );

                    loopAgain = toggleWithdrawAll2 ? !turnOnWithdrawAll() : !handle_1_27_Restock();
                    break;
                default:
                    methods.log("bad or unimplemented enum");
                    methods.bot.getScriptExecutor().stop(false);
            }
            return loopAgain;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean turnOnWithdrawXFor_14_14_Restock() throws InterruptedException {
        boolean withdrawXSetTo14 = methods.configs.isSet(
                ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                _304_WITHDRAW_X_AMOUNT_OF_14
        );

        boolean withdrawXWidgetActive = methods.configs.isSet(
                ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
        );
        if (!methods.bank.open()) {
            methods.log("Unable to open bank. May not be close enough.");
            return false;
        }

        if (!withdrawXSetTo14) {
            ScriptPaint.setStatus("setting withdraw X -> 14");
            if (methods.inventory.isEmpty() || methods.bank.depositAll()) {
                if (!methods.bank.containsAll(itemA_id, itemB_id)) {
                    methods.warn("Stopping script due to shortage of items.");
                    methods.bot.getScriptExecutor().stop(false);
                } else {
                    boolean itemA_withdrawn = methods.bank.withdraw(itemA_id, 14);
                    boolean itemB_withdrawn = methods.bank.withdraw(itemB_id, 14);
                    if (!(itemA_withdrawn && itemB_withdrawn)) {
                        methods.warn("Unable with withdraw 14 of itemA or itemB when trying to set withdraw X amount to 14.");
                        return false;
                    }
                }
            }
        }

        if (!withdrawXWidgetActive) {
            ScriptPaint.setStatus("toggling withdrawX -> Active");
            RS2Widget withdrawX = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "X");
            if (withdrawX != null) {
                WidgetDestination widgetDestination = new WidgetDestination(methods.bot, withdrawX);
                methods.mouse.click(widgetDestination);
            }
        }

        return ConditionalSleep2.sleep(1000, () -> methods.configs.isSet(
                        ID_OF_VARBIT_HANDLING_WITHDRAW_X_AMOUNT,
                        _304_WITHDRAW_X_AMOUNT_OF_14
                ) && methods.configs.isSet(
                        ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                        _1666_VARBIT_VALUE_IF_WITHDRAW_X_SELECTED
                )
        );
    }

    private boolean turnOnWithdrawAll() throws InterruptedException {
        boolean withdrawAllWidgetActive = methods.configs.isSet(
                ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
        );

        if (!methods.bank.open()) {
            methods.log("Unable to open bank. May not be close enough.");
            return false;
        }

        if (!withdrawAllWidgetActive) {
            ScriptPaint.setStatus("toggling withdraw All -> Active");
            RS2Widget withdrawAll = methods.widgets.getWidgetContainingText(BANK_ROOT_ID, "ALL");
            if (withdrawAll != null) {
                WidgetDestination widgetDestination = new WidgetDestination(methods.bot, withdrawAll);
                methods.mouse.click(widgetDestination);
            }
        }

        return ConditionalSleep2.sleep(1000, () -> methods.configs.isSet(
                        ID_OF_VARBIT_HANDLING_WITHDRAW_QUANTITY_SELECTED,
                        _1666_VARBIT_VALUE_IF_WITHDRAW_ALL_SELECTED
                )
        );
    }

    private boolean handle_14_14_Restock() throws InterruptedException {
        if (methods.bank.open() && methods.bank.depositAll()) {
            if (!methods.bank.containsAll(itemA_id, itemB_id)) {
                methods.log("Shortage of items.");
                methods.bot.getScriptExecutor().stop(true);
            } else {
                return methods.bank.withdraw(itemA_id, 14) && methods.bank.withdraw(itemB_id, 14);
            }
        }
        return false;
    }

    private boolean handle_1_27_Restock() throws InterruptedException {
        if (methods.bank.open()) {
            if (!methods.bank.contains(itemB_id)) {
                methods.log("Shortage of items.");
                methods.bot.getScriptExecutor().stop(false);
            } else {
                return methods.bank.depositAllExcept(itemA_id) && methods.bank.withdrawAll(itemB_id);
            }
        }
        return false;
    }

    private boolean handle_1_X_26_Restock() throws InterruptedException {
        if (methods.bank.open()) {
            if (!methods.bank.contains(itemB_id)) {
                methods.log("Shortage of items.");
                methods.bot.getScriptExecutor().stop(false);
            } else {
                return methods.bank.depositAllExcept(itemA_id, itemC_id) && methods.bank.withdrawAll(itemB_id);
            }
        }
        return false;
    }
}