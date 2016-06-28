package ua.wog.shared;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.security.MessageDigest;

import ua.wog.wogqr.R;

/**
 * Created by Slavik on 31.12.2014.
 */
public class Utils {

    public static String KEY_QR_DATA = "dataQR";
    public static int QR_DATA_LENGTH = 9;

    public enum PayMethod{
        PAY_METHOD_BONUSES,
        PAY_METHOD_WALLETS,
        PAY_METHOD_ONLINE;
    }

    public static class CodeQR implements Parcelable {

        public String Code;
        public String Dispenser;

        public CodeQR(String Code, String Dispenser){

            this.Code = Code;
            this.Dispenser = Dispenser;

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(Code);
            dest.writeString(Dispenser);
        }

        public static final Parcelable.Creator<CodeQR> CREATOR = new Parcelable.Creator<CodeQR>() {

            public CodeQR createFromParcel(Parcel in) {
                return new CodeQR(in.readString(), in.readString());
            }

            @Override
            public CodeQR[] newArray(int size) {
                return new CodeQR[0];
            }

        };


    }


    public static String md5(String s) {

        s = s.toLowerCase();
        s = s.replace("&", "&amp;").replace(">", "&gt;").replace("<", "&lt;").replace("\"", "&quot;");

        String res = "";
        for (int i = 0; i < s.length(); i++) {

            int code = s.codePointAt(i);

            if (code > 159 )
                res = res + "&#"+String.valueOf(code)+";";
            else
                res = res + s.charAt(i);

        }


        String hash;
        try {
            MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(res.getBytes("UTF8"), 0, res.length());
            hash = new BigInteger(1, m.digest()).toString(16);
        } catch (Exception e) {
            return "";
        }
        if(hash.length() == 31) hash = "0" + hash;

        return hash;
    }

    public static void showError(Context context, String message){

        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    public static CodeQR getQRData(String textQR){

        if (textQR.length() != Utils.QR_DATA_LENGTH){
            return null;
        }
        return new CodeQR(textQR.substring(0,7), textQR.substring(7,9));
    }

    public static String getStringFloat(float value){
        return String.format("%.2f", value).replace(",", ".");
    }

    public static String getStringFloat(double value){
        return String.format("%.2f", value).replace(",", ".");
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static void setActionBarUp(android.support.v7.app.ActionBar actionBar){
        setActionBarUp(actionBar, false);
    }

    public static void setActionBarUp(android.support.v7.app.ActionBar actionBar, boolean showDrawable){

        actionBar.setDisplayShowHomeEnabled(true);
        if (showDrawable) actionBar.setIcon(R.drawable.wog);
        actionBar.setDefaultDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator( R.drawable.ab_back);

    }

}

