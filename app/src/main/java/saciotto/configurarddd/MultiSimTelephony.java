package saciotto.configurarddd;

import android.content.Context;
import android.telephony.TelephonyManager;
import java.lang.reflect.Method;

/**
 * Implementa funções de acesso a dados de telefonia multi SIM.
 * @author Matheus Rossi Saciotto.
 * @since versão 0.1
 */
class MultiSimTelephony {

    /**
     * Número máximo de cartões SIM permitidos.
     */
    public static final int MAX_SIM_CARDS_SUPPORTED = 3;

    /**
     * Instância única desta classe.
     */
    private static MultiSimTelephony telephonyInfo;

    /**
     * Variável para acesso às funções de telefonia.
     */
    private static TelephonyManager telephony;

    /**
     * Array com os nomes de todos as prestadoras reconhecidas.
     */
    private final String[] operatorName;

    /**
     * Array com o MCC + MNC de todas as prestadoras reconhecidas.
     */
    private final String[] operatorCountryCode;

    /**
     * Retorna o nome da prestadora solicitada.
     * @param index Slot do cartão SIM.
     * @return Nome da prestadora.
     */
    public String getOperatorName(int index) {
        if (index >= operatorName.length)
            return null;
        return operatorName[index];
    }

    /**
     * Retorna o MCC + MNC da prestadora solicitada.
     * @param index Slot do cartão SIM.
     * @return MCC + MNC da prestadora.
     */
    public String getOperator(int index) {
        if (index >= operatorCountryCode.length)
            return null;
        return operatorCountryCode[index];
    }

    /**
     * Construtor padrão da classe. Inicializa as variáveis.
     */
    private MultiSimTelephony() {
        int index;
        operatorName = new String[MAX_SIM_CARDS_SUPPORTED];
        for(index = 0; index < MAX_SIM_CARDS_SUPPORTED; index++) {
            operatorName[index] = null;
        }

        operatorCountryCode = new String[MAX_SIM_CARDS_SUPPORTED];
        for(index = 0; index < MAX_SIM_CARDS_SUPPORTED; index++) {
            operatorCountryCode[index] = null;
        }
    }

    /**
     * Construtor estático desta classe, retorna uma instância única.
     * @param context Contexto base da aplicação.
     * @return Instância da classe MultiSimTelephony.
     */
    public static MultiSimTelephony getInstance(Context context) {
        int index;

        if (telephonyInfo != null)
            return telephonyInfo;

        telephonyInfo = new MultiSimTelephony();
        telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        telephonyInfo.operatorName[0] = telephony.getSimOperatorName();

        // Getting operator name
        try {
            for (index = 0; index < MAX_SIM_CARDS_SUPPORTED; index++) {
                long[] subId = getSubId(index);
                if (subId == null)
                    telephonyInfo.operatorName[index] = "";
                else
                    telephonyInfo.operatorName[index] = getDeviceIdBySlot("getNetworkOperatorName", subId[0]);
                if ("".equals(telephonyInfo.operatorName[index]))
                    telephonyInfo.operatorName[index] = null;
            }
        } catch (GeminiMethodNotFoundException e) {
            telephonyInfo.operatorName[0] = telephony.getNetworkOperatorName();
            for (index = 1; index < MAX_SIM_CARDS_SUPPORTED; index++)
                telephonyInfo.operatorName[index] = null;
        }

        // Getting operator country code
        try {
            for (index = 0; index < MAX_SIM_CARDS_SUPPORTED; index++) {
                long[] subId = getSubId(index);
                if (subId == null)
                    telephonyInfo.operatorName[index] = "";
                else
                    telephonyInfo.operatorCountryCode[index] = getDeviceIdBySlot("getNetworkOperator", subId[0]);
                if ("".equals(telephonyInfo.operatorCountryCode[index]))
                    telephonyInfo.operatorCountryCode[index] = null;
            }
        } catch (GeminiMethodNotFoundException e) {
            telephonyInfo.operatorCountryCode[0] = telephony.getNetworkOperator();
            for (index = 1; index < MAX_SIM_CARDS_SUPPORTED; index++)
                telephonyInfo.operatorCountryCode[index] = null;
        }

        return telephonyInfo;
    }

    /**
     * Retorna o SubId do Slot de um cartão SIM.
     * @param slotId Slot solicitado.
     * @return SubId.
     * @throws GeminiMethodNotFoundException
     */
    private static long [] getSubId(int slotId) throws GeminiMethodNotFoundException {
        long [] subId;

        try {
            Class<?> subscriptionManager = Class.forName("android.telephony.SubscriptionManager");

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;

            Method getSimSubId = subscriptionManager.getMethod("getSubId", parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = slotId;
            Object obPhone = getSimSubId.invoke(telephony, obParameter);

            subId = (long []) obPhone;

        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(String.format("getSubId error slot [%d]", slotId));
        }

        return subId;
    }

    /**
     * Retorna a resposta para um método, acessado por reflexão, que possui como parâmetro de entrada o SubId.
     * @param predictedMethodName Nome do método.
     * @param subId SubId.
     * @return Retorno da função realizada por reflexão.
     * @throws GeminiMethodNotFoundException
     */
    private static String getDeviceIdBySlot( String predictedMethodName, long subId) throws GeminiMethodNotFoundException {

        String operator = null;

        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = long.class;

            Method getSimId = telephonyClass.getMethod(predictedMethodName, parameter);

            Object[] obParameter = new Object[1];
            obParameter[0] = subId;
            Object obPhone = getSimId.invoke(telephony, obParameter);

            if (obPhone != null) {
                operator = obPhone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeminiMethodNotFoundException(predictedMethodName);
        }

        return operator;
    }

    /**
     * Excessão gerada se alguma função acessada por reflexão não for encontrada.
     */
    private static class GeminiMethodNotFoundException extends Exception {

        /**
         * Função que trata a excessão gerada se alguma função acessada por reflexão não for encontrada.
         * @param info Informações da excessão.
         */
        public GeminiMethodNotFoundException(String info) {
            super(info);
        }
    }
}
