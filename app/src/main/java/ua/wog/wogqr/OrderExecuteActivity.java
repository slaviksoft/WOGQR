package ua.wog.wogqr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ua.wog.shared.AppOptions;
import ua.wog.shared.OrderData;
import ua.wog.shared.Utils;


public class OrderExecuteActivity extends ActionBarActivity {

    final private String JSON_STATUS = "Status";
    final private String JSON_ERROR  = "Error";
    final private String JSON_INFO   = "Info";

    final private int stateRefresh = 1;
    final private int stateError   = 2;
    final private int stateDone    = 3;
    final private int stateWaiting = 4;

    private int mState;
    private AppOptions mOptions;

    private TextView tvOrderGoodName;
    private TextView tvOrderOrdered;
    private TextView tvOrderStatus;
    private Button   btnOrderRefresh;
    private Button   btnOrderBack;
    private Button   btnOrderCancel;

    private String goodCode;
    private String goodName = "";
    private double  goodCount = 0;
    private boolean firstTime;
    private String uuid;
    private String station;
    private String dispenser;
    private String pinMD5;

    private OrderData orderData;

    private void initViews(){

        tvOrderGoodName = (TextView)findViewById(R.id.tvOrderGoodName);
        tvOrderOrdered = (TextView)findViewById(R.id.tvOrderOrdered);
        tvOrderStatus = (TextView)findViewById(R.id.tvOrderStatus);
        btnOrderRefresh = (Button)findViewById(R.id.btnOrderRefresh);
        btnOrderBack = (Button)findViewById(R.id.btnOrderBack);
        btnOrderCancel = (Button)findViewById(R.id.btnOrderCancel);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_execute);

        Utils.setActionBarUp(getSupportActionBar());

        initViews();

        //Intent i = getIntent();
        //orderData = i.getParcelableExtra(OrderData.EXTRA_NAME);

//        orderData = new OrderData();
//        orderData.LoadFromPreferences(getApplicationContext());

    }

    @Override
    protected void onResume(){
        super.onResume();

        orderData = new OrderData(getApplicationContext());
        //orderData.LoadFromPreferences(getApplicationContext());

        if (orderData.state == OrderData.STATE_NEW){
            sendOrder();
        }else{
            getOrderStatus();
        }

        setOrderState(stateWaiting);

        setGoodText();

    }

    public void sendOrder(){

        orderData.uuid = UUID.randomUUID().toString();
        orderData.SaveAsPreferences(getApplicationContext());

        HTTPSTask request = new HTTPSTask(this);
        request.setResultListener(new HTTPSTask.ResultListener() {

            @Override
            public void onResult(int operation_ID, String result, String uuid) {
                if (operation_ID == HTTPSTask.OPER_ORDER) {

                    orderData.state = OrderData.STATE_SENT;
                    orderData.SaveAsPreferences(getApplicationContext());

                    processOrderSendRequest(result, uuid);
                }
            }

            @Override
            public void onCancel(int operation_ID) {
                if (operation_ID == HTTPSTask.OPER_ORDER) {

                    orderData.state = OrderData.STATE_SENT;
                    orderData.SaveAsPreferences(getApplicationContext());

                    tvOrderStatus.setText(getString(R.string.msg_operation_canceled));
                }
            }

            @Override
            public void onError(int operation_ID, Exception e, int code) {
                if (operation_ID == HTTPSTask.OPER_ORDER) {

                    tvOrderStatus.setText( HTTPSTask.getErrorMessageId(code) );
//
                }
            }
        });

        request.backendOrder(orderData);

        setOrderState(stateRefresh);

//        request.backendOrder(uuid, orderData.stationCode, orderData.dispenser, orderData.code, (float)goodCount, orderData.md5);

    }


    // 0 - ОК
    // 1 - ошибка обработки заказа
        //1 - не правильный формат количества
        //2 - нет АЗС с таким кодом
        //3 - не правильный пин
    // 2 - ошибка сервера АЗС
    // 3 - ошибка обработки заказа на АЗС
        //1 - пустые параметры
        //2 - не правильный формат параметров
        //3 - заказ уже оформлен

    private void processOrderSendRequest(String result, String uuid) {

        try{
            JSONObject json = new JSONObject(result);
            int status = json.getInt(JSON_STATUS);
            if (status != 0){

                int errorNumber = json.getInt(JSON_ERROR);
                int message = R.string.msg_something_wrong;

                if (status == 1){
                    message = R.string.msg_order_application_server_error;

                }else if(status == 2) {
                    message = R.string.msg_order_no_azs_connection;

                }else if(status == 3){

                    switch(errorNumber){
                        case 1: {message = R.string.msg_order_azs_server_error; break;}
                        case 2: {message = R.string.msg_order_azs_server_error; break;}
                        case 3: {message = R.string.msg_order_order_is_executing; break;}
                    }
                }

                tvOrderStatus.setText(message);

            }
            else{
//                orderData.LoadFromPreferences(getApplicationContext());
//                orderData.accepted = 1;
//                orderData.SaveAsPreferences(getApplicationContext());

                tvOrderStatus.setText(R.string.msg_order_order_is_waiting);
                setOrderState(stateRefresh);
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
            tvOrderStatus.setText(R.string.msg_order_accepted);
        }


    }



    public void getOrderStatus(){

        HTTPSTask request = new HTTPSTask(this);
        request.setResultListener(new HTTPSTask.ResultListener() {

            @Override
            public void onResult(int operation_ID, String result, String param) {

                if (operation_ID == HTTPSTask.OPER_ORDER_STATUS) {
                    processOrderStatusRequest(result, param);
                }
            }

            @Override
            public void onCancel(int operation_ID) {
                if (operation_ID == HTTPSTask.OPER_ORDER_STATUS) {
                    tvOrderStatus.setText(getString(R.string.msg_operation_canceled));
                }
            }

            @Override
            public void onError(int operation_ID, Exception e, int code) {
                if (operation_ID == HTTPSTask.OPER_ORDER_STATUS) {
                    tvOrderStatus.setText(getString(HTTPSTask.getErrorMessageId(code)));
                }
            }
        });

        request.backendOrderStatus(orderData.uuid);

    }

    //Статус:
    //0 - ОК
    //  0 - выполнен
    //  1 - ожидает пуск
    //1 - ошибка сервера приложения
    //  1 - не передан Ид
    //  2 - нет заказа с Ид
    //  3 - нет азс с кодом заказа
    //2 - ошибка сервера АЗС (код ошибки берем с фронта)
    //  1 - нет связи
    //  2 - передан пустой ид на сервер АЗС
    //  3 - разные номенклатуры
    //  4 - нет заказа с таким ид
    //100 - ошибка пуска заказа на АЗС
    //100 - неизвестно
    //101 - недостаточно остатка

    private void processOrderStatusRequest(String result, String uuid){

        try{
            JSONObject json = new JSONObject(result);
            int status = json.getInt(JSON_STATUS);
            mOptions = new AppOptions(this);
            mOptions.read();

            goodName =  mOptions.orderGoodName;
            goodCount = mOptions.orderGoodCount;

            if (status != 0){

                int errorNumber = json.getInt(JSON_ERROR);
                String message = "";

                if (status == 1){
                    message = getString(R.string.msg_something_wrong);
                }else if(status == 2){
                    message = getString(R.string.msg_order_get_balance_error) + ":\n";
                    switch(errorNumber){
                        case 1: {message = getString(R.string.msg_order_no_azs_connection); break;}
                        case 2: {message = getString(R.string.msg_order_wrong_uuid); break;}
                        case 3: {message = getString(R.string.msg_order_different_goods); break;}
                        case 4: {message = getString(R.string.msg_order_wrong_uuid); break;}
                        case 100: {message = getString(R.string.msg_order_start_error); break;}
                        case 101: {message = getString(R.string.msg_order_insufficient_balance); break;}
                        case 102: {message = getString(R.string.msg_order_no_loyalty_connection); break;}
                        case 103: {message = getString(R.string.msg_order_start_error); break;}
                        case 104: {message = getString(R.string.msg_order_time_out); break;}
                        default: {message = getString(R.string.msg_something_wrong); break;}
                    }
                }

                setOrderState(stateError);

                tvOrderStatus.setText(message);

            }
            else{

                int info = json.getInt(JSON_INFO);
                if (info == 0) {

                    orderData.state = OrderData.STATE_DONE;
                    orderData.SaveAsPreferences(getApplicationContext());

                    setOrderState(stateDone);
                    tvOrderStatus.setText(R.string.msg_order_accepted);

                }else{

                    setOrderState(stateRefresh);
                    tvOrderStatus.setText(R.string.msg_order_order_is_waiting);

                }


            }

            setGoodText();

        }
        catch (JSONException e) {
            e.printStackTrace();

            setOrderState(stateError);
            tvOrderStatus.setText(R.string.msg_wrong_answer);

        }

    }


    public void onOrderRefresh(View v){
        getOrderStatus();
    }

    public void onOrderBack(View v){
        finish();
    }

    public void onOrderCancel(View v){
        orderData.state = OrderData.STATE_DONE;
        orderData.SaveAsPreferences(getApplicationContext());
        finish();
    }

    private void setOrderState(int state){

        if (mState == state)return;

        mState = state;

        switch (state){
            case stateRefresh:
                btnOrderRefresh.setVisibility(View.VISIBLE);
                btnOrderCancel.setVisibility(View.GONE);
                break;

            case stateError:
                btnOrderRefresh.setVisibility(View.GONE);
                btnOrderCancel.setVisibility(View.VISIBLE);
                break;

            case stateDone:
                btnOrderRefresh.setVisibility(View.GONE);
                btnOrderCancel.setVisibility(View.GONE);
                break;

            case stateWaiting:
                btnOrderRefresh.setVisibility(View.GONE);
                btnOrderCancel.setVisibility(View.GONE);
                break;

        }

    }


    private void setGoodText(){

        tvOrderGoodName.setText(orderData.name);
        tvOrderOrdered.setText( String.valueOf( orderData.orderedCount));

    }

}
