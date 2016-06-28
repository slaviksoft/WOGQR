package ua.wog.wogqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ua.wog.shared.OrderData;
import ua.wog.shared.Utils;


public class OrderWalletsActivity extends ActionBarActivity {

    private float countOrdered;
    private float goodRemain;

    private RelativeLayout llWarning;
    private RelativeLayout llPreOrder;
    private TextView tvPreOrderGoodName;
    private TextView tvPreOrderAvailable;
    private EditText edPreOrderOrdered;
    private Button btnPreOrderSend;
    private Button btnPreOrderFullSum;

    private Map<Button, Float> mapCalculator;

    ////////////////////////////////////////////////////////
    private OrderData orderData;


    private void initViews(){

        llWarning = (RelativeLayout)findViewById(R.id.llWarning);
        llWarning.setVisibility(View.GONE);

        llPreOrder = (RelativeLayout)findViewById(R.id.llPreOrder);
        llPreOrder.setVisibility(View.GONE);

        tvPreOrderGoodName = (TextView)findViewById(R.id.tvPreOrderGoodName);
        tvPreOrderAvailable = (TextView)findViewById(R.id.tvPreOrderAvailable);

        mapCalculator = new HashMap<Button, Float>();
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc01), 5f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc02), 10f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc03), 15f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc04), 20f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc05), 25f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc06), 30f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc07), 40f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc08), 50f);
        mapCalculator.put((Button)findViewById(R.id.btnPreOrderCalc09), 0f);

        setCalcButtonsEnabled(false);

        edPreOrderOrdered = (EditText)findViewById(R.id.edPreOrderOrdered);

        btnPreOrderSend = (Button)findViewById(R.id.btnPreOrderSend);
        btnPreOrderSend.setEnabled(false);

        btnPreOrderFullSum = (Button)findViewById(R.id.btnPreOrderCalc09);
        setCountOrdered(0f);

        llWarning.setVisibility(View.GONE);
        llPreOrder.setVisibility(View.VISIBLE);



    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_wallets);

        Utils.setActionBarUp(getSupportActionBar());

        initViews();

        orderData = new OrderData(getApplicationContext());
        goodRemain = orderData.remain;

        setCalcButtonsEnabledBySum();
        setCalcButtonsFullSum();

        tvPreOrderGoodName.setText(orderData.name);
        tvPreOrderAvailable.setText( Utils.getStringFloat(goodRemain) + " " + getString(R.string.text_fuel_count_units));

        edPreOrderOrdered.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return !trySetOrderedCount();
            }

            private boolean trySetOrderedCount(){

                float count = Float.valueOf(edPreOrderOrdered.getText().toString());
                if (count > goodRemain) {
                    edPreOrderOrdered.setError(getString(R.string.msg_count_is_wrong));
                    return false;
                }
                else {
                    edPreOrderOrdered.setError(null);
                    setCountOrdered(count);
                    return true;
                }

            }

        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onBackClick(View v){
        finish();
    }

    public void onCalcClick(View v) {
        setCountOrdered( mapCalculator.get(v) );
    }

    public void onSendClick(View v) {

        orderData.orderedCount = countOrdered;
        orderData.SaveAsPreferences(getApplicationContext());

        Intent i = new Intent(this, EnterPinActivity.class);
        startActivityForResult(i, EnterPinActivity.request_code);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EnterPinActivity.request_code){
            if (resultCode == RESULT_OK){


                //orderData.accepted = 0;
                orderData.md5 = data.getStringExtra(EnterPinActivity.extra_MD5);
                orderData.SaveAsPreferences(getApplicationContext());

                Intent i = new Intent(this, OrderExecuteActivity.class);
                startActivity(i);

                finish();

            }
        }
    }

    public void setCountOrdered(float countOrdered) {

        this.countOrdered = countOrdered;

        int intCountOrdered = (int)countOrdered;
        if(intCountOrdered == countOrdered){
            edPreOrderOrdered.setText(String.valueOf(intCountOrdered));
        }else{
            edPreOrderOrdered.setText(String.valueOf(countOrdered));
        }

        setSendButtonEnabledByCount();
    }

    public void setCalcButtonsFullSum(){

        float maxCount = (float)Math.floor(goodRemain);

        mapCalculator.put(btnPreOrderFullSum, maxCount);
        btnPreOrderFullSum.setText(""+(int)maxCount);

    }

    private void setCalcButtonsEnabled(boolean enabled){
        Button btn;
        for (Iterator<Button> i = mapCalculator.keySet().iterator(); i.hasNext(); ) {
            btn = i.next();
            btn.setEnabled(enabled);
        }
    }

    private void setCalcButtonsEnabledBySum(){
        Button btn;
        Float  val;

        Iterator it = mapCalculator.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            btn = (Button)pairs.getKey();
            val = (Float)pairs.getValue();

            btn.setEnabled( val <= goodRemain );

        }
    }

    private void setSendButtonEnabledByCount(){

        if ((goodRemain >0)&&(countOrdered>0)){
            btnPreOrderSend.setEnabled(true);
        }else{
            btnPreOrderSend.setEnabled(false);
        }
    }

}
