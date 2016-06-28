package ua.wog.shared;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Slavik on 02.01.2015.
 */
public class AppOptions {

    final private String PREF_NAME  = "AppStorage";
    final private String KEY_CARD   = "KEY_CARD";
    final private String KEY_MD5    = "KEY_MD5";
    final private String KEY_LOGGED = "KEY_LOGGED";
    final private String KEY_EMAIL  = "KEY_EMAIL";
    final private String KEY_PHONE  = "KEY_PHONE";
    final private String KEY_UUID   = "KEY_UUID";
    final private String KEY_APP_ID   = "KEY_APP_ID";

    final private String KEY_orderGoodCode  = "KEY_orderGoodCode";
    final private String KEY_orderGoodCount = "KEY_orderGoodCount";
    final private String KEY_orderGoodName  = "KEY_orderGoodName";

    final private String ORDER_PREF = "ORD_";

    private Context context;
    private SharedPreferences preferences;

    public String  CARD;
    public String  MD5;
    public Boolean LOGGED;
    public String  EMAIL;
    public String  PHONE;

    public String orderGoodCode;
    public float  orderGoodCount;
    public String orderGoodName;

    public String UUID;
    public String APP_ID;

    public AppOptions(Context context){

        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

    }

//    public void setOrder(String goodCode, float goodCount, String goodName){
//
//        orderGoodCode = goodCode;
//        orderGoodCount= goodCount;
//        orderGoodName = goodName;
//    }

    public void read(){

        CARD    = preferences.getString(KEY_CARD, "");
        MD5     = preferences.getString(KEY_MD5, "");
        LOGGED  = preferences.getBoolean(KEY_LOGGED, false);
        EMAIL   = preferences.getString(KEY_EMAIL, "");
        PHONE   = preferences.getString(KEY_PHONE, "");
        UUID    = preferences.getString(KEY_UUID, "");
        APP_ID  = preferences.getString(KEY_APP_ID, "");

        orderGoodCode = preferences.getString(KEY_orderGoodCode,  "");
        orderGoodCount= preferences.getFloat(KEY_orderGoodCount, 0);
        orderGoodName = preferences.getString(KEY_orderGoodName,  "");

    }

    public void save(){

        SharedPreferences.Editor ed = preferences.edit();

        ed.putString(KEY_CARD, CARD);
        ed.putString(KEY_MD5, MD5);
        ed.putBoolean(KEY_LOGGED, LOGGED);
        ed.putString(KEY_EMAIL, EMAIL);
        ed.putString(KEY_PHONE, PHONE);
        ed.putString(KEY_UUID, UUID);
        ed.putString(KEY_APP_ID, APP_ID);

        ed.putString(KEY_orderGoodCode, orderGoodCode);
        ed.putFloat(KEY_orderGoodCount, orderGoodCount);
        ed.putString(KEY_orderGoodName, orderGoodName);

        ed.commit();
    }


//    public void saveOrder(OrderData orderData){
//
//        SharedPreferences.Editor ed = preferences.edit();
//
//        ed.putInt(ORDER_PREF+"code", orderData.code);
//
//        ed.putString(ORDER_PREF+"name", orderData.name);
//
//        ed.putFloat(ORDER_PREF+"remain", (float)orderData.remain);
//        ed.putFloat(ORDER_PREF+"price", (float)orderData.price);
//        ed.putFloat(ORDER_PREF+"bonuses", (float)orderData.bonuses);
//
//        ed.putString(ORDER_PREF+"stationCode", orderData.stationCode);
//        ed.putString(ORDER_PREF+"dispenser", orderData.dispenser);
//        ed.putString(ORDER_PREF+"md5", orderData.md5);
//
//        ed.putString(ORDER_PREF+"payMethod", orderData.payMethod.name());
//
//        ed.putFloat(ORDER_PREF+"orderedCount", (float)orderData.orderedCount);
//        ed.putFloat(ORDER_PREF+"orderedMoney", (float)orderData.orderedMoney);
//
//        ed.putInt(ORDER_PREF+"accepted", orderData.accepted);
//
//        ed.putString(ORDER_PREF+"uuid", orderData.uuid);
//
//        ed.commit();
//
//    }
//
//    public void readOrder(OrderData orderData){
//
//        orderData.code = preferences.getInt(ORDER_PREF+"code", 0);
//
//        orderData.code = preferences.getInt(ORDER_PREF+"code", 0);
//        orderData.name = preferences.getString(ORDER_PREF + "name", "");
//
//        orderData.remain = preferences.getFloat(ORDER_PREF + "remain", 0.0f);
//        orderData.price = preferences.getFloat(ORDER_PREF+"price", 0.0f);
//        orderData.bonuses = preferences.getFloat(ORDER_PREF+"bonuses", 0.0f);
//
//        orderData.stationCode = preferences.getString(ORDER_PREF+"stationCode", "");
//        orderData.dispenser = preferences.getString(ORDER_PREF+"dispenser", "");
//        orderData.md5 = preferences.getString(ORDER_PREF+"md5", "");
//        orderData.payMethod = Utils.PayMethod.valueOf( preferences.getString(ORDER_PREF+"payMethod", ""));
//
//        orderData.orderedCount = preferences.getFloat(ORDER_PREF + "orderedCount", 0.0f);
//        orderData.orderedMoney = preferences.getFloat(ORDER_PREF+"orderedMoney", 0.0f);
//
//        orderData.accepted = (byte)preferences.getInt(ORDER_PREF+"accepted", 0);
//        orderData.uuid = preferences.getString(ORDER_PREF+"uuid", "");
//
//    }


}
