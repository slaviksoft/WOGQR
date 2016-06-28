package ua.wog.wogqr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import ua.wog.shared.BalanceData;
import ua.wog.shared.Utils;


public class BalanceActivity extends ActionBarActivity {

//    final private String JSON_STATUS   = "Status";
//    final private String JSON_FUEL_WALLET     = "FuelWallets";
//    final private String JSON_GOODS_WALLET    = "GoodsWallets";
//    final private String JSON_COLUMN_GOODNAME = "GoodsName";
//    final private String JSON_COLUMN_REMAIN   = "Remain";
//
//
//    final private String JSON_BONUSES_WALLET    = "Bonuses";
//    final private String JSON_BONUSES_REMAIN    = "Remains";
//
//    final private String ATTRIBUTE_NAME_GOOD    = "good";
//    final private String ATTRIBUTE_NAME_COUNT   = "count";



//    private AppOptions options;
    private TextView tvBonusesBalance;
    private ListView lvWallets;
    //private ListView lvGoodsWallets;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        Utils.setActionBarUp(getSupportActionBar(), false);

        tvBonusesBalance = (TextView)findViewById(R.id.tvBonusesBalance);
        lvWallets = (ListView) findViewById(R.id.lvWallets);
        //lvGoodsWallets = (ListView) findViewById(R.id.lvGoodsWallets);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

//        if (!options.LOGGED){
//            return;
//        }

//        String card  = options.CARD;
//        String pass  = options.MD5;

        HTTPSTask request = new HTTPSTask(this);
        request.setResultListener(new HTTPSTask.ResultListener() {
            @Override
            public void onResult(int operation_ID, String result, String param) {
                if (operation_ID == HTTPSTask.OPER_BALANCE) {
                    doResult(result);
                }
            }

            @Override
            public void onCancel(int operation_ID) {
                if (operation_ID == HTTPSTask.OPER_BALANCE) {
                    Utils.showError(getApplicationContext(), getString(R.string.msg_operation_canceled) );
                }
            }

            @Override
            public void onError(int operation_ID, Exception e, int code) {
                if (operation_ID == HTTPSTask.OPER_BALANCE) {
                    Utils.showError(getApplicationContext(), getString(HTTPSTask.getErrorMessageId(code)) );
                }
            }
        });

        request.backendBalance();

    }

    public void doResult(String result) {

        try{
            JSONObject json = new JSONObject(result);
            int status = json.getInt(BalanceData.JSON_STATUS);
            if (status != 0){

                String message = "";
                switch (status){
                    case 1: {message = getString(R.string.msg_no_card); break;}
                    case 2: {message = getString(R.string.msg_wrong_pass);break;}
                    case 3: {message = json.getString("Error");break;}
                    default: {message = getString(R.string.msg_something_wrong); break;}
                }
                Utils.showError(getApplicationContext(), message);
            }
            else{

                JSONObject bonuses = (JSONObject) json.get(BalanceData.JSON_BONUSES_WALLET);
                String balance = bonuses.getString(BalanceData.JSON_BONUSES_REMAIN);
                tvBonusesBalance.setText( balance );

                boolean hasFuelWallet  = json.has(BalanceData.JSON_FUEL_WALLET);
                boolean hasGoodsWallet = json.has(BalanceData.JSON_GOODS_WALLET);

                if ( hasFuelWallet || hasGoodsWallet) {

                    JSONArray arrayWallets = new JSONArray();
                    JSONArray arrayGoodsWallets = new JSONArray();

                    if (hasFuelWallet)  {arrayWallets = (JSONArray) json.get(BalanceData.JSON_FUEL_WALLET);}
                    if (hasGoodsWallet) {arrayGoodsWallets = (JSONArray) json.get(BalanceData.JSON_GOODS_WALLET);}

                    createListView(arrayWallets, arrayGoodsWallets);
                }
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
            Utils.showError(getApplicationContext(), getString(R.string.msg_wrong_answer));
        }

    }

    private void setListAdapter(JSONArray array, ListView lv, int resCountUnitText) throws JSONException {

        String[] goods  = new String[array.length()];
        String[] counts = new String[array.length()];

        for (int i = 0; i < array.length(); i++) {
            goods[i]  = array.getJSONObject(i).getString(BalanceData.JSON_COLUMN_GOODNAME);
            counts[i] = array.getJSONObject(i).getString(BalanceData.JSON_COLUMN_REMAIN)+" "+getString(resCountUnitText);
        }

        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>(goods.length);
        Map<String, Object> m;
        for (int i = 0; i < goods.length; i++) {
            m = new HashMap<String, Object>();
            m.put(BalanceData.ATTRIBUTE_NAME_GOOD, goods[i]);
            m.put(BalanceData.ATTRIBUTE_NAME_COUNT, counts[i]);
            data.add(m);
        }

        String[] from = { BalanceData.ATTRIBUTE_NAME_GOOD, BalanceData.ATTRIBUTE_NAME_COUNT};

        int[] to = { R.id.tvBalanceName, R.id.tvBalanceCount};

        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.listitem_balances, from, to);
        lv.setAdapter(sAdapter);

    }

    private void createListView(JSONArray arrayWallets, JSONArray arrayGoodsWallets) throws JSONException {

        setListAdapter(arrayWallets, lvWallets, R.string.text_fuel_count_units);

        //setListAdapter(arrayGoodsWallets, lvGoodsWallets, R.string.text_goods_count_units);

    }

}
