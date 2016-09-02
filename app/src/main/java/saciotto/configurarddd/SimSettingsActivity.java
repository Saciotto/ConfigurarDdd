package saciotto.configurarddd;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contém a principal janela do aplicativo.
 * Lista os nomes das operadores dos cartões SIM presentes no aplicativo, bem como lista o código da prestadora
 * escolhido pelo usuário.
 * Também permite acesso à area de edição do código da prestadora.
 * @author Matheus Rossi Saciotto
 * @since versão 0.1
 */
public class SimSettingsActivity extends ActionBarActivity {

    /**
     * Chave utilizada para identificar um campo referente ao nome da prestadora na ListView.
     */
    private static final String KEY_OPERATOR_NAME = "KEY_OPERATOR_NAME";

    /**
     * Chave utilizada para identificar um campo referente ao código da prestadora (utilizado no momento da discagem)
     * na ListView.
     */
    private static final String KEY_OPERATOR_CODE = "KEY_OPERATOR_CODE";

    /**
     * Mapa da de chaves passado para a ListView.
     */
    private static final String [] KEY_MAP_OPERATOR = {KEY_OPERATOR_NAME, KEY_OPERATOR_CODE};

    /**
     * ListView contendo todas as operadoras encontradas.
     */
    private ListView operatorListView;


    private MenuItem toastMenuItem;

    /**
     * Array com o Slot que cada cartão SIM oculpa em relação a sua posição na lista.
     */
    private final int [] simIndex = new int[MultiSimTelephony.MAX_SIM_CARDS_SUPPORTED];

    /**
     * Listener dos eventos de clique da ListView que contém a lista das prestadoras e seus códigos.
     */
    private final AdapterView.OnItemClickListener CallPreferenceLayout = new AdapterView.OnItemClickListener() {

        /**
         * Acesso a Activity usada na configuração do código da prestadora do cartão escolhido.
         * @param parent Não utilizado.
         * @param view Não utilizado.
         * @param position posição do da operadora selecionada.
         * @param id Não utilizado.
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent = new Intent(getBaseContext(), SimEditActivity.class);
            intent.putExtra(SimCards.SELECTED_POSITION, simIndex[position]);
            intent.putExtra(SimCards.SELECTED_OPERATOR_NAME, SimCards.getOperatorName(simIndex[position]));
            intent.putExtra(SimCards.SELECTED_OPERATOR_CODE, SimCards.getOperatorCode(simIndex[position]));
            startActivity(intent);
        }
    };

    /**
     * Atualiza a lista de cartões mostrada na tela principal.
     */
    private void updateSimList() {
        ArrayList <HashMap<String, String>> simArrayList = new ArrayList<>();
        for (int index = 0; index < SimCards.getNumberOfSims(); index++) {
            HashMap<String, String> simHashMap = new HashMap<>();
            String operatorName = SimCards.getOperatorName(index);
            if (operatorName == null)
                continue;
            simHashMap.put(KEY_OPERATOR_NAME, operatorName);
            String operatorCode = SimCards.getOperatorCode(index);
            if (operatorCode == null || operatorCode.length() == 0) {
                operatorCode = getText(R.string.simsettings_notused).toString();
            }
            simHashMap.put(KEY_OPERATOR_CODE, operatorCode);
            simArrayList.add(simHashMap);
            simIndex[simArrayList.size() - 1] = index;
        }

        int[] to = new int[] {android.R.id.text1, android.R.id.text2};

        operatorListView.setAdapter(new SimpleAdapter(SimSettingsActivity.this, simArrayList,
                android.R.layout.two_line_list_item, KEY_MAP_OPERATOR, to));
    }

    private void updateToastOption () {
        if (NumberFormat.isToastEnabled(this)) {
            toastMenuItem.setTitle(R.string.action_change_toast_option_turn_off);
        } else {
            toastMenuItem.setTitle(R.string.action_change_toast_option_turn_on);
        }
    }

    /**
     * Ponto de entrada principal do aplicativo.
     * @param savedInstanceState Não utilizado.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_settings);

        // Get Views
        operatorListView = (ListView) findViewById(R.id.operatorListView);
        operatorListView.setOnItemClickListener(CallPreferenceLayout);
    }

    /**
     * Atualiza a lista de operadoras no momento em que a aplicação ganha o foco.
     */
    @Override
    protected void onResume(){
        super.onResume();
        SimCards.updateOperatorData(this);
        updateSimList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sim_settings, menu);

        toastMenuItem = menu.findItem(R.id.action_change_toast_option);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        updateToastOption();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_change_toast_option) {
            NumberFormat.setToastEnable(this, !NumberFormat.isToastEnabled(this));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
