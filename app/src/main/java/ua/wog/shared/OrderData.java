package ua.wog.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.util.UUID;

/**
 * Created by Slavik on 20.03.2015.
 */
public class OrderData implements Parcelable {


    public static String EXTRA_NAME = "ORDER_DATA";
    private static String PREFERENCES_NAME = "CURRENT_ORDER";

    final static public int PM_BONUSES = 1;
    final static public int PM_WALLETS = 2;
    final static public int PM_ONLINE  = 3;

    final static public int STATE_NEW   = 0;
    final static public int STATE_SENT  = 1;
    final static public int STATE_DONE  = 2;

    public int state = STATE_NEW;

    public int    code;
    public String name;
    public float remain;
    public float price;
    public float bonuses;
    public String stationCode;
    public String dispenser;
    public String md5;
    public int payMethod;
    public float orderedCount;
    public float orderedMoney;
    public byte accepted;
    public String uuid;
    public int done;
    public int sent;


    @Override
    public int describeContents() {return 0;}

    public String Serialize(){
        Gson gjon = new Gson();
        return gjon.toJson(this);
    }

//    public static OrderData Create(String serializedData){
//
//        try {
//            Gson gson = new Gson();
//            return gson.fromJson(serializedData, OrderData.class);
//        } catch (JsonSyntaxException e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(code);
        dest.writeString(name);
        dest.writeDouble(remain);
        dest.writeDouble(price);
        dest.writeDouble(bonuses);
        dest.writeString(stationCode);
        dest.writeString(dispenser);
        dest.writeString(md5);
        dest.writeInt(payMethod);
        dest.writeDouble(orderedCount);
        dest.writeDouble(orderedMoney);
        dest.writeByte(accepted);
        dest.writeString(uuid);
        dest.writeInt(done);
        dest.writeInt(sent);
        dest.writeInt(state);
    }

    private OrderData(Parcel in) {
        code = in.readInt();
        name = in.readString();
        remain = (float)in.readDouble();
        price = (float)in.readDouble();
        bonuses = (float)in.readDouble();
        stationCode = in.readString();
        dispenser = in.readString();
        md5 = in.readString();
        payMethod = in.readInt();
        orderedCount = (float)in.readDouble();
        orderedMoney = (float)in.readDouble();
        accepted = in.readByte();
        uuid = in.readString();
        done = in.readInt();
        sent = in.readInt();
        state = in.readInt();
    }

    public void generateNewUUID(){
        uuid = UUID.randomUUID().toString();
    }

    public OrderData(){

    }

    public OrderData(Context context){
        LoadFromPreferences(context);
    }

    public static final Parcelable.Creator<OrderData> CREATOR
            = new Parcelable.Creator<OrderData>() {

        public OrderData createFromParcel(Parcel in) {
            return new OrderData(in);
        }

        public OrderData[] newArray(int size) {
            return new OrderData[size];
        }
    };

    public void SaveAsPreferences(Context context){

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        SharedPreferences.Editor ed = preferences.edit();

        ed.putInt("code", code);
        ed.putInt("accepted", accepted);
        ed.putInt("done", done);
        ed.putInt("sent", sent);
        ed.putInt("state", state);

        ed.putString("name", name);
        ed.putString("stationCode", stationCode);
        ed.putString("dispenser", dispenser);
        ed.putString("md5", md5);
        ed.putInt("payMethod", payMethod);
        ed.putString("uuid", uuid);

        ed.putFloat("remain", (float)remain);
        ed.putFloat("price", (float)price);
        ed.putFloat("bonuses", (float)bonuses);
        ed.putFloat("orderedCount", (float)orderedCount);
        ed.putFloat("orderedMoney", (float)orderedMoney);

        ed.commit();

    }

    public void LoadFromPreferences(Context context){

        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);

        code = preferences.getInt("code", 0);
        accepted = (byte)preferences.getInt("accepted", 0);
        done = preferences.getInt("done", 0);
        sent = preferences.getInt("sent", 0);
        state = preferences.getInt("state", STATE_NEW);

        name = preferences.getString("name", "");
        stationCode = preferences.getString("stationCode", "");
        dispenser = preferences.getString("dispenser", "");
        md5 = preferences.getString("md5", "");
        payMethod = preferences.getInt("payMethod", 0);
        uuid = preferences.getString("uuid", "");

        remain = preferences.getFloat("remain", 0.0f);
        price = preferences.getFloat("price", 0.0f);
        bonuses = preferences.getFloat("bonuses", 0.0f);
        orderedCount = preferences.getFloat("orderedCount", 0.0f);
        orderedMoney = preferences.getFloat("orderedMoney", 0.0f);

    }


//    public static OrderData Load(Context context){
//
//        SharedPreferences preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
//        return Create(preferences.getString("order", ""));
//
//    }

}
