package saciotto.configurarddd;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity que permite a edição do código da prestadora para um determinado cartão SIM.
 * @author Matheus Rossi Saciotto
 * @since versão 0.1
 */
public class SimEditActivity extends Activity{

    /**
     * EditText utilizado pelo usuário para informar o código da prestadora que ele quer utilizar no momento da discagem.
     */
    private EditText operatorCodeEditText;

    /**
     * Slot do cartão SIM que está sendo editado.
     */
    private int simSlot;

    /**
     * Listener do clique do botão confirmar.
     */
    private final View.OnClickListener confirmClick = new View.OnClickListener() {

        /**
         * Aplica as alterações realizadas e encerra a Activity.
         * @param v Não utilizado.
         */
        @Override
        public void onClick(View v) {
            SharedPreferences simPreferences = getSharedPreferences(SimCards.PREFERENCE_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor =  simPreferences.edit();

            editor.putString(SimCards.PREFERENCE_SIM_CODE_KEY + String.format("%d", simSlot),
                    operatorCodeEditText.getText().toString());
            editor.apply();
            finish();
        }
    };

    /**
     * Listener do clique do botão confirmar.
     */
    private final View.OnClickListener cancelClick = new View.OnClickListener() {

        /**
         * Cancela as alterações realizadas e encerra a Activity.
         * @param v Não utilizado.
         */
        @Override
        public void onClick(View v) {
            finish();
        }
    };

    /**
     * Ponto de entrada desta Activity.
     * @param savedInstanceState Não utilizado.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_edit);

        Bundle extras = getIntent().getExtras();

        simSlot = extras.getInt(SimCards.SELECTED_POSITION);

        String operator = extras.getString(SimCards.SELECTED_OPERATOR_NAME);
        TextView operatorTextView = (TextView) findViewById(R.id.operatorTextView);
        operatorTextView.setText(operator);

        String previousSimCode = extras.getString(SimCards.SELECTED_OPERATOR_CODE);
        operatorCodeEditText = (EditText) findViewById(R.id.operatorCodeEditText);
        operatorCodeEditText.setText(previousSimCode);
        operatorCodeEditText.requestFocus();

        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(confirmClick);

        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(cancelClick);
    }

}
