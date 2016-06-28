package ua.wog.wogqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import ua.wog.shared.OrderData;
import ua.wog.shared.PreOrderData;
import ua.wog.shared.Utils;


public class PaySelectionActivity extends ActionBarActivity {

    private Utils.CodeQR data;
    private OrderData orderData;

//    private int    goodCode;
//    private String goodName;
//    private double goodRemain;
//    private double goodPrice;
//    private double goodBonuses;

    private TextView tvBalanceBonuses;
    private TextView tvBalanceWallets;
    private TextView tvErrorInfo;
    private RelativeLayout llWarning;
    private LinearLayout llAccepted;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_selection);

        Utils.setActionBarUp(getSupportActionBar());

        initWidgets();

        data = (Utils.CodeQR) getIntent().getParcelableExtra(Utils.KEY_QR_DATA);

        HTTPSTask request = getHTTPSTask();
        request.backendPreOrder(data.Code, data.Dispenser);

    }

    private void setWarningState(String message){
        llWarning.setVisibility(View.VISIBLE);
        llAccepted.setVisibility(View.GONE);
        tvErrorInfo.setText(message);
    }

    private void setSelectState(){
        llWarning.setVisibility(View.GONE);
        llAccepted.setVisibility(View.VISIBLE);
    }

    private void initWidgets(){
        tvBalanceBonuses = (TextView) findViewById(R.id.tvBalanceBonuses);
        tvBalanceWallets = (TextView) findViewById(R.id.tvBalanceWallets);

        tvErrorInfo = (TextView) findViewById(R.id.tvErrorInfo);
        llWarning = (RelativeLayout) findViewById(R.id.rlWarning);
        llAccepted = (LinearLayout) findViewById(R.id.llAccepted);

    }

    private HTTPSTask getHTTPSTask(){

        HTTPSTask request = new HTTPSTask(this);
        request.setResultListener(new HTTPSTask.ResultListener() {
            @Override
            public void onResult(int operation_ID, String result, String param) {
                if (operation_ID == HTTPSTask.OPER_BEGIN_ORDER) {
                    doOperationAccepted(result);
                }
            }

            @Override
            public void onCancel(int operation_ID) {
                if (operation_ID == HTTPSTask.OPER_BEGIN_ORDER) {
                    doOperationCanceled();
                }
            }

            @Override
            public void onError(int operation_ID, Exception e, int code) {
                if (operation_ID == HTTPSTask.OPER_BEGIN_ORDER) {
                    doOperationError(HTTPSTask.getErrorMessageId(code));
                }
            }
        });

        return request;
    }


    private void doOperationError(int resId){
        setWarningState(getString(resId));
    }

    private void doOperationCanceled(){
        setWarningState(getString(R.string.msg_operation_canceled));
    }


    // 0 - ОК
    // 1 - ошибка опредиления пистолета (1-hardware error, 2-no dispenser, 3-have active order, 4-no good, 5-no active nozzle)
        //1 - помилка заліза
        //2 - немає ТРК з номером
        //3 - обслуговується інше замовлення
        //4 - товар відсутній на азс
        //5 - не піднято пістолет
        //6 - помилка отримання даних з азс
        //100 - товар відсутній на сервері застосунку
    // 2 - ошибка получения баланса
        //1 - відсутня карта
        //2 - не правильний пароль
        //3 - помилка сервера

    private void doOperationAccepted(String result) {

        try{
            JSONObject json = new JSONObject(result);
            int status = json.getInt(PreOrderData.JSON_STATUS);
            if (status != 0){

                int errorNumber = json.getInt("Error");
                String message = "";

                if (status == 1){
                    switch(errorNumber){
                        case 1: {message = getString(R.string.msg_order_dispenser_malfunction); break;}
                        case 2: {message = getString(R.string.msg_order_dispenser_malfunction); break;}
                        case 3: {message = getString(R.string.msg_order_dispenser_busy); break;}
                        case 4: {message = getString(R.string.msg_order_dispenser_malfunction); break;}
                        case 5: {message = getString(R.string.msg_order_raise_nozzle); break;}
                        case 6: {message = getString(R.string.msg_order_error_azs_data); break;}
                        default: {message = getString(R.string.msg_something_wrong); break;}
                    }

                }else if(status == 2){
                    message = getString(R.string.msg_order_get_balance_error) + ":\n";
                    switch(errorNumber){
                        case 1: {message = getString(R.string.msg_no_card); break;}
                        case 2: {message = getString(R.string.msg_wrong_pass); break;}
                        case 3: {message = getString(R.string.msg_loyalty_server_error); break;}
                        default: {message = getString(R.string.msg_something_wrong); break;}
                    }
                }
                setWarningState(message);
            }
            else{

                orderData = new OrderData();
                orderData.generateNewUUID();
                orderData.name   = json.getString(PreOrderData.JSON_GOOD_NAME);
                orderData.code   = json.getInt(PreOrderData.JSON_GOOD_CODE);
                orderData.remain = (float)json.getDouble(PreOrderData.JSON_GOOD_REMAIN);
                orderData.price  = (float)json.getDouble(PreOrderData.JSON_PRICE);
                orderData.bonuses= (float)json.getDouble(PreOrderData.JSON_BONUSES);
                orderData.dispenser   = data.Dispenser;
                orderData.stationCode = data.Code;

                orderData.SaveAsPreferences(getApplicationContext());

                double count = Math.floor( orderData.bonuses/100/orderData.price );
                tvBalanceBonuses.setText( orderData.name+": "+String.valueOf(count)+" л.");
                tvBalanceWallets.setText( orderData.name+": "+String.valueOf(orderData.remain) +" л.");

                setSelectState();
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
            setWarningState(getString(R.string.msg_wrong_answer));
        }


    }

    public void onCancelClick(View view) {
        finish();
    }

    public void onRepeatClick(View view) {
        HTTPSTask request = getHTTPSTask();
        request.backendPreOrder(data.Code, data.Dispenser);
    }


    // Pay method click

    public void onBonusesPayClick(View view) {

        orderData.payMethod = OrderData.PM_BONUSES;
        orderData.SaveAsPreferences(getApplicationContext());

        Intent i = new Intent(getApplicationContext(), OrderBonusesActivity.class);
        startActivity(i);

        finish();

    }

    public void onWalletsPayClick(View view) {

        orderData.payMethod = OrderData.PM_WALLETS;
        orderData.SaveAsPreferences(getApplicationContext());

        Intent i = new Intent(getApplicationContext(), OrderWalletsActivity.class);
        //i.putExtra(OrderData.EXTRA_NAME, orderData);

        startActivity(i);

        finish();

    }

    public void onOnlinePayClick(View view) {
    }


}
