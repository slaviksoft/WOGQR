package ua.wog.wogqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ua.wog.shared.OrderData;
import ua.wog.shared.Utils;


public class OrderBonusesActivity extends ActionBarActivity {

    private OrderData orderData;
    private EditText edSum;
    private EditText edCount;

    private float moneyOrdered;
    private float countOrdered;

    void initViews(){
        edSum = (EditText) findViewById(R.id.edSum);
        edCount = (EditText) findViewById(R.id.edCount);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_bonuses);

        Utils.setActionBarUp(getSupportActionBar());

        initViews();

        edSum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                float newMoney = Float.valueOf(v.getText().toString());
                if (!checkNewMoney(newMoney)){
                    v.setError(getString(R.string.msg_count_is_wrong));
                    return true;
                }else{
                    v.setError(null);
                    setOrderedMoney(newMoney);
                    return false;
                }
            }
        });

        edCount.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                float newCount = Float.valueOf(v.getText().toString());
                if (!checkNewCount(newCount)){
                    v.setError(getString(R.string.msg_count_is_wrong));
                    return true;
                }else{
                    v.setError(null);
                    setOrderedCount(newCount);
                    return false;
                }
            }
        });

        orderData = new OrderData(getApplicationContext());

    }


    @Override
    protected void onResume() {
        super.onResume();

        updateOrderDataText();
    }

    private void updateOrderDataText(){

        TextView tvGoodName = (TextView) findViewById(R.id.tvGoodName);
        TextView tvPrice = (TextView) findViewById(R.id.tvPrice);
        TextView tvBonusesBalance = (TextView) findViewById(R.id.tvBonusesBalance);
        TextView tvPossibleCount = (TextView) findViewById(R.id.tvPossibleCount);

        float bonuses = (float)Math.floor(orderData.bonuses/100);
        float possibleCount = (float)Math.floor(bonuses / orderData.price);

        tvGoodName.setText(orderData.name);
        tvPrice.setText( Utils.getStringFloat(orderData.price) );
        tvBonusesBalance.setText( Utils.getStringFloat(bonuses) );
        tvPossibleCount.setText( Utils.getStringFloat(possibleCount) );

    }

    private boolean checkNewCount(float newCount){

        float newMoney = (float) (newCount * orderData.price * 100);
        if (newMoney > orderData.bonuses) return false; else return true;

    }

    private boolean checkNewMoney(float newMoney){

        if (newMoney * 100 > orderData.bonuses) return false; else return true;

    }

    private void setOrderedMoney(float newMoney) {
        moneyOrdered = newMoney;
        countOrdered = (float) Utils.round( moneyOrdered/orderData.price, 2);
        updateOrdered();
    }

    private void setOrderedCount(float newCount) {
        countOrdered = newCount;
        moneyOrdered = (float) Utils.round(countOrdered * orderData.price, 2);
        updateOrdered();
    }

    private void updateOrdered(){
        edCount.setText(Utils.getStringFloat(countOrdered));
        edSum.setText(Utils.getStringFloat(moneyOrdered));
    }

    public void onSendClick(View view) {

        orderData.orderedCount = countOrdered;
        orderData.orderedMoney = moneyOrdered;
        orderData.SaveAsPreferences(getApplicationContext());

        Intent i = new Intent(this, EnterPinActivity.class);
        startActivityForResult(i, EnterPinActivity.request_code);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EnterPinActivity.request_code){
            if (resultCode == RESULT_OK){

                orderData.md5 = data.getStringExtra(EnterPinActivity.extra_MD5);
                orderData.SaveAsPreferences(getApplicationContext());

                Intent i = new Intent(this, OrderExecuteActivity.class);
                startActivity(i);

                finish();

            }
        }
    }

}
