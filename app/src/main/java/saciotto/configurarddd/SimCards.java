package saciotto.configurarddd;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;

/**
 * Acesso as principais características dos cartões SIM presentes no celular.
 * @author Matheus Rossi Saciotto
 * @since versão 0.1
 */
class SimCards {

    /**
     * Lista que contém os nomes das operadoras presentes em cada cartão, ordentados pelo Slot.
     */
    private static final String[] operatorNameList = new String[MultiSimTelephony.MAX_SIM_CARDS_SUPPORTED];

    /**
     * Lista que contém os códigos das operadoras utilizados em cada cartão, ordentados pelo Slot.
     */
    private static final String[] operatorCodeList = new String[MultiSimTelephony.MAX_SIM_CARDS_SUPPORTED];

    /**
     * Lista que contém MCC + MNC presentes em cada cartão, ordentados pelo Slot.
     */
    private static final String[] operatorCountryCodeList = new String[MultiSimTelephony.MAX_SIM_CARDS_SUPPORTED];

    /**
     * Número de cartões SIM encontrados no telefone.
     */
    private static int numberOfSims = 0;

    /**
     * Chave utilizada para salvar o MCC + MNC dos cartões configurados. Utilizado para identificar substituição
     * do cartão.
     */
    private static final String PREFERENCE_SIM_CARD_KEY = "PREFERENCE_SIM_CARD_KEY_";

    /**
     * Chave utilizada para salvar o nome da operadora que não foi identificado corretamente. Pode ocorrer se no
     * momento da identificação o SIM estiver sem cobertura ou se o SIM não estiver presente.
     */
    private static final String PREFERENCE_SIM_CARD_NAME = "PREFERENCE_SIM_CARD_NAME_";

    /**
     * Texto utilizado para identificar o cartão SIM quando o nome da operadora não foi identificado corretamente.
     */
    private static final String NO_NAME_SIM_CARD = "SIM ";

    /**
     * Chave utilzado para identificar a o Slot do SIM selecionado.
     */
    public static final String SELECTED_POSITION = "SELECTED_POSITION";

    /**
     * Chave utilizada para identificar o nome da prestadora selecionada.
     */
    public static final String SELECTED_OPERATOR_NAME = "SELECTED_OPERATOR_NAME";

    /**
     * Chave utilizada para identificar o código do operador atual.
     */
    public static final String SELECTED_OPERATOR_CODE = "SELECTED_OPERATOR_CODE";

    /**
     * Chave para identificar o arquivo privado que contém os dados dos cartões salvos.
     */
    public static final String PREFERENCE_KEY = "SIM_OPERATORS";

    /**
     * Chave utilizada para salvar os códigos das operadoras utilizados no momento da discagem.
     */
    public static final String PREFERENCE_SIM_CODE_KEY = "PREFERENCE_SIM_CODE_KEY_";

    /**
     * Obtém o código da prestadora normalmente utilizado para um lista de MCC + MNC conhecidos.
     * @param key MCC + MNC.
     * @return Código da prestadora padrão.
     */
    private static String getOperatorCodeDefault (String key) {
        String code;
        HashMap<String, String> mobileCodeHashMap = new HashMap<>();

        // MCC + MNC most important operators list
        mobileCodeHashMap.put("72403", "41");
        mobileCodeHashMap.put("72404", "41");
        mobileCodeHashMap.put("72405", "21");
        mobileCodeHashMap.put("72438", "21");
        mobileCodeHashMap.put("72406", "15");
        mobileCodeHashMap.put("72410", "15");
        mobileCodeHashMap.put("72411", "15");
        mobileCodeHashMap.put("72423", "15");
        mobileCodeHashMap.put("72430", "31");
        mobileCodeHashMap.put("72431", "31");

        code = mobileCodeHashMap.get(key);
        if (code == null)
            code = "";
        return code;
    }

    /**
     * Retorna código da prestadora salvo, ou padrão se constar nenhum registro para o operador.
     * @param context Contexto base da aplicação.
     * @param operator MCC + MNC.
     * @param simIndex Slot do cartão desejado.
     * @return Código da prestadora.
     */
    private static String getOperatorCode(Context context, String operator, int simIndex) {
        String simCard, simCode;
        SharedPreferences simPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);

        simCard = simPreferences.getString(PREFERENCE_SIM_CARD_KEY + String.format("%d", simIndex), "");
        simCode = simPreferences.getString(PREFERENCE_SIM_CODE_KEY + String.format("%d", simIndex), null);

        if (("".equals(simCard) || "".equals(operator) || operator.equals(simCard)) && simCode != null)
            return simCode;

        return getOperatorCodeDefault(operator);
    }

    /**
     * Retorna o nome da prestadora anteriormente salva para determinado Slot.
     * @param context Contexto base da aplicação.
     * @param simIndex Slot do cartão desejado.
     * @return Nome da prestadora.
     */
    private static String getSavedInfoOperatorName(Context context, int simIndex) {
        SharedPreferences simPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        return simPreferences.getString(PREFERENCE_SIM_CARD_NAME + String.format("%d", simIndex), null);
    }

    /**
     * Retorna o MCC + MNC anteriormente salvo para determinado Slot.
     * @param context Contexto base da aplicação.
     * @param simIndex Slot do cartão desejado.
     * @return MCC + MNC.
     */
    private static String getSavedInfoOperator(Context context, int simIndex) {
        SharedPreferences simPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        return simPreferences.getString(PREFERENCE_SIM_CARD_KEY + String.format("%d", simIndex), null);
    }

    /**
     * Salva configurações dos cartões.
     * @param context Contexto base da aplicação.
     */
    private static void saveConfiguration(Context context) {
        SharedPreferences simPreferences = context.getSharedPreferences(PREFERENCE_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor configEditor =  simPreferences.edit();

        configEditor.clear();
        for (int index = 0; index < numberOfSims; index++) {
            configEditor.putString(PREFERENCE_SIM_CARD_KEY + String.format("%d", index), operatorCountryCodeList[index]);
            configEditor.putString(PREFERENCE_SIM_CODE_KEY + String.format("%d", index), operatorCodeList[index]);
            configEditor.putString(PREFERENCE_SIM_CARD_NAME + String.format("%d", index), operatorNameList[index]);
        }
        configEditor.apply();
    }

    /**
     * Atualiza os dados das prestadoras.
     * @param context Contexto base da aplicação.
     */
    public static void updateOperatorData(Context context) {
        MultiSimTelephony telephony = MultiSimTelephony.getInstance(context);

        numberOfSims = 0;
        for (int index = 0; index < MultiSimTelephony.MAX_SIM_CARDS_SUPPORTED; index++) {
            String operatorName = telephony.getOperatorName(index);
            String operator = telephony.getOperator(index);
            if (operatorName == null || operator == null || "".equals(operatorName)) {
                operatorName = getSavedInfoOperatorName(context, index);
                operator = "";

                if (operatorName == null)
                    continue;
            }
            operatorNameList[index] = operatorName;
            if ("".equals(operator))
                operatorCountryCodeList[index] = getSavedInfoOperator(context, index);
            else
                operatorCountryCodeList[index] = operator;
            operatorCodeList[index] = getOperatorCode(context, operator, index);

            numberOfSims = index + 1;
        }

        for (int index = 0; index < numberOfSims; index++) {
            if (operatorNameList[index] == null) {
                operatorNameList[index] = NO_NAME_SIM_CARD + String.format("%d", index + 1);
                operatorCodeList[index] = "";
                operatorCountryCodeList[index] = "";
            }
        }

        saveConfiguration(context);
    }

    /**
     * Retorna o nome da prestadora selecionada.
     * @param index Slot do cartão desejado.
     * @return Nome da prestadora.
     */
    public static String getOperatorName(int index) {
        if (index >= numberOfSims)
            return null;
        return operatorNameList[index];
    }

    /**
     * Retorna o código da prestadora selecionada, utilizado no momento da discagem.
     * @param index Slot do cartão desejado.
     * @return Código da prestadora.
     */
    public static String getOperatorCode(int index) {
        if (index >= numberOfSims)
            return null;
        return operatorCodeList[index];
    }

    /**
     * Retorna a quantidade de cartões SIM identificados.
     * @return Número de cartões SIM identificados.
     */
    public static int getNumberOfSims() {
        return numberOfSims;
    }
}
