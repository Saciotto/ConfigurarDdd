package saciotto.configurarddd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Realiza o redirecionamento das ligações para o número formatado com a adição do código da prestadora.
 * @author Matheus Rossi Saciotto
 * @since versão 0.1
 */
public class NumberFormat extends BroadcastReceiver {

    /**
     * Índice para função getNewNumber que representa o valor não formatado retornado.
     */
    private static final int NOT_FORMATTED_NUMBER_INDEX = 0;

    /**
     * Índice para função getNewNumber que representa o valor formatado retornado.
     */
    private static final int FORMATTED_NUMBER_INDEX = 1;

    /**
     * Chave utilizada para identificar o sharedPreference referente as opções do toast
     */
    private static final String PREFERENCE_TOAST_OPTION = "PREFERENCE_TOAST_OPTION";

    /**
     * Chave utilizada para identificar as opções do toast
     */
    private static final String PREFERENCE_TOAST_OPTION_KEY = "PREFERENCE_TOAST_OPTION_KEY";

    /**
     * Formata o número que será discado adicionando o código da prestadora quando necessário.
     * @param originalNumber Número original digitado pelo operador.
     * @param operatorCode Código da prestadora cadastrado.
     * @return Número formatado com o código da prestadora inserido se necessário.
     */
    private String [] getNewNumber (final String originalNumber, final String operatorCode) {
        String [] newNumberList, originalNumberList;
        String tempNumber, countryCodePrefix, codePrefix;

        newNumberList = new String[2];
        originalNumberList = new String[2];

        originalNumberList[NOT_FORMATTED_NUMBER_INDEX] = originalNumber;
        originalNumberList[FORMATTED_NUMBER_INDEX] = originalNumber;

        if (operatorCode.length() == 0)
            return originalNumberList;

        if (originalNumber.length() < 10)
            return originalNumberList;

        if (!originalNumber.matches(".*\\d.*") && (!originalNumber.substring(1).matches(".*\\d.*") || !originalNumber.matches("\\+.*")))
            return originalNumberList;

        if (originalNumber.matches("9090.*"))
            return originalNumberList;

        if (originalNumber.matches("0[3589]00.*"))
            return originalNumberList;

        countryCodePrefix = "";
        codePrefix = "";
        tempNumber = originalNumber;

        if (originalNumber.matches("\\+.*")) {
            tempNumber = tempNumber.substring(3);
            if (originalNumber.matches("\\+55.*")) {
                codePrefix += "0";
            } else {
                codePrefix += "00";
                countryCodePrefix = tempNumber.substring(1,3);
            }
        } else if (originalNumber.matches("90.*")) {
            tempNumber = tempNumber.substring(2);
            codePrefix += "90";
        } else if (originalNumber.matches("00.*")) {
            countryCodePrefix = tempNumber.substring(2,4);
            codePrefix += "00";
            tempNumber = tempNumber.substring(4);
        } else if (originalNumber.matches("0.*")) {
            tempNumber = tempNumber.substring(1);
            codePrefix += "0";
        } else {
            codePrefix += "0";
        }

        if (tempNumber.length() >  11)
            return originalNumberList;

        newNumberList[NOT_FORMATTED_NUMBER_INDEX] = codePrefix + operatorCode + countryCodePrefix + tempNumber;
        newNumberList[FORMATTED_NUMBER_INDEX] = codePrefix + " (" + operatorCode + ") " +  countryCodePrefix + tempNumber;
        return newNumberList;
    }

    /**
     * Realiza o redirecionamento da ligação se necessário.
     * <p> Permissão: android.intent.action.NEW_OUTGOING_CALL </>
     * @param context Contexto base.
     * @param intent Não utilizado.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        final String originalNumber = getResultData();
        SimCards.updateOperatorData(context);
        Bundle bundle = getResultExtras(true);
        String [] newNumberList;

        String message = "";
        Boolean skipEnter = true;
        for (int index = 0; index < SimCards.getNumberOfSims(); index++) {
            if (!skipEnter) {
                message += "\r\n";
            }
            skipEnter = false;
            String operator = SimCards.getOperatorName(index);
            if (operator == null) {
                skipEnter = true;
                continue;
            }

            newNumberList = getNewNumber(originalNumber, SimCards.getOperatorCode(index));
            if (!newNumberList[NOT_FORMATTED_NUMBER_INDEX].equals(originalNumber)) {
                message += context.getText(R.string.numberformat_operator_info).toString() + " " + operator + ":\r\n";
                message += newNumberList[FORMATTED_NUMBER_INDEX];
            } else
                skipEnter = true;

            bundle.putString("android.intent.extra.PHONE_NUMBER_SIM" + String.format("%d", index + 1), newNumberList[0]);
            if (index == 0) {
                setResultData(newNumberList[NOT_FORMATTED_NUMBER_INDEX]);
            }
        }

        setResultExtras(bundle);

        if (!"".equals(message) && isToastEnabled(context)) {
            Toast messageToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
            messageToast.setMargin(0, (float) 0.10);
            messageToast.show();
        }
    }

    /**
     * Verifica se o pop-up está abilitado.
     * @param context Contexto da aplicação.
     * @return true se o pop-up estiver abilitado.
     */
    public static boolean isToastEnabled(Context context) {
        SharedPreferences toastPreferences = context.getSharedPreferences(PREFERENCE_TOAST_OPTION, Context.MODE_PRIVATE);

        return toastPreferences.getBoolean(PREFERENCE_TOAST_OPTION_KEY, true);
    }

    /**
     * Abilita ou desabilita o pop-up.
     * @param context contexto da aplicação.
     * @param enable true para abilitar o pop-up.
     */
    public static void setToastEnable (Context context, boolean enable) {
        SharedPreferences toastPreferences = context.getSharedPreferences(PREFERENCE_TOAST_OPTION, Context.MODE_PRIVATE);
        SharedPreferences.Editor toastEditor =  toastPreferences.edit();

        toastEditor.clear();
        toastEditor.putBoolean(PREFERENCE_TOAST_OPTION_KEY, enable);
        toastEditor.apply();
    }
}
