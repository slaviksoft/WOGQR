package ua.wog.wogqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.EditText;

import ua.wog.shared.Utils;


public class TestActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_test);
        setContentView(R.layout.begin_order_wallets_activity);

        Utils.setActionBarUp(getSupportActionBar(), false);

        //initDecimalEnter();

    }

    private void initDecimalEnter() {

        final EditText edit = (EditText) findViewById(R.id.editTextDecimal);

//        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                //if (actionId == EditorInfo.IME_ACTION_){
//                if (!checkDecimal(edit.getText().toString())) edit.setError("Ошибка ввода числа"); else edit.setError(null);
//                //}
//                return false;
//            }
//
//            private boolean checkDecimal(String s) {
//                float res = Float.valueOf(s);
//                if (res > 100) return false; else return true;
//            }
//
//        });

    }


    public void onDecimalClick(View view) {

        startActivity(new Intent(getApplicationContext(), EnterQRActivity.class));

    }


}
