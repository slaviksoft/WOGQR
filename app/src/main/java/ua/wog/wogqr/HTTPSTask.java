package ua.wog.wogqr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import ua.wog.shared.AppOptions;
import ua.wog.shared.OrderData;

/**
 * Created by Slavik on 29.12.2014.
 */
public class HTTPSTask extends AsyncTask<String, String, String>{

    static public final int OPER_NONE    = 0;
    static public final int OPER_LOGIN   = 1;
    static public final int OPER_BALANCE = 2;
    static public final int OPER_BEGIN_ORDER  = 3;
    static public final int OPER_ORDER  = 4;
    static public final int OPER_ORDER_STATUS  = 5;
    static public final int OPER_CHECK_PIN = 6;
    static public final int OPER_ORDER_BONUSES  = 7;

    static public final int ERROR_ENCRYPTING = 1;
    static public final int ERROR_CONNECTION = 2;
    static public final int ERROR_BAD_RESPONSE = 3;


    private ResultListener mResultListener;
    private Activity mActivity;
    private ProgressDialog prgDialog;
    private int currOperation=0;
    private AppOptions mOptions;
    private Exception lastError;
    private int lastErrorCode;
    private boolean showProgress = true;

    private final String host = "0.0.0.0";
    private final String server = "MobileBackend";
    private final String service = "hs/QR";
    private final String user = "customer";
    private final String pass = "1111111";
    private String param;


    public interface ResultListener{
        public void onResult(int operation_ID, String result, String param);
        public void onCancel(int operation_ID);
        public void onError(int operation_ID, Exception e, int code);
    }

    public HTTPSTask(Activity activity){
        mActivity = activity;
        mOptions = new AppOptions(mActivity);
        mOptions.read();
    }

    public void setResultListener(ResultListener l){
        mResultListener = l;
    }

    private HostnameVerifier getHostnameVerifier(){

        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                HostnameVerifier hv =
                        HttpsURLConnection.getDefaultHostnameVerifier();
                //return hv.verify("https://10.0.100.17", session);
                return true;
            }
        };

    }

    private SSLSocketFactory getSSLSocketFactory() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = new BufferedInputStream(mActivity.getResources().openRawResource(R.raw.server));
        Certificate ca;
        try {
            ca = cf.generateCertificate(caInput);
            System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
        } finally {
            caInput.close();
        }

        String keyStoreType = KeyStore.getDefaultType();
        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, tmf.getTrustManagers(), null);

        return context.getSocketFactory();
    }

    public String doRequestHTTPS(String strUrl, SSLSocketFactory sslSocketFactory, HostnameVerifier hostnameVerifier) throws IOException {

        URL url = new URL(strUrl);

        HttpsURLConnection urlConnection = null;

        urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(10000);
        urlConnection.setSSLSocketFactory(sslSocketFactory);
        urlConnection.setHostnameVerifier(hostnameVerifier);
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Authorization", "basic " + Base64.encodeToString((user+":"+pass).getBytes(), 0));

        InputStream in = urlConnection.getInputStream();

        int respCode = urlConnection.getResponseCode();
        String respMsg = urlConnection.getResponseMessage();

        if (respCode / 100 != 2){
            lastErrorCode = ERROR_BAD_RESPONSE;
            throw new IOException("Response code = "+respCode);
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(in));

        String line;
        StringBuilder total = new StringBuilder();
        while ((line = r.readLine()) != null) {
            total.append(line);
        }

        urlConnection.disconnect();

        return total.toString();

    }

    public static int getErrorMessageId(int errorCode){

        switch (errorCode) {
            case HTTPSTask.ERROR_ENCRYPTING: {return R.string.msg_http_request_encryption_failed;}
            case HTTPSTask.ERROR_CONNECTION: {return R.string.msg_http_request_failed;}
            case HTTPSTask.ERROR_BAD_RESPONSE: {return R.string.msg_http_request_failed;}
            default:{return R.string.msg_something_wrong;}
        }
    }

    // *****************************************************************
    //                          overridden
    // *****************************************************************

    @Override
    protected void onPreExecute () {

        if (showProgress) {

            prgDialog = new ProgressDialog(mActivity);
            prgDialog.setMessage(mActivity.getString(R.string.msg_http_request));
            prgDialog.setCancelable(true);
            prgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    cancel(false);
                }
            });
            prgDialog.show();
        }
    }

    @Override
    protected String doInBackground(String... params){

        lastError = null;

        SSLSocketFactory sslSocketFactory = null;
        try {
            sslSocketFactory = getSSLSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            lastErrorCode = ERROR_ENCRYPTING;
            lastError = e;
            return "";
        }

        HostnameVerifier hostnameVerifier = getHostnameVerifier();

        try {

            return doRequestHTTPS(params[0], sslSocketFactory, hostnameVerifier);

//        }catch (SocketTimeoutException e){
//            e.printStackTrace();
//            lastError = e;
//            lastErrorCode = ERROR_TIMEOUT;

        } catch (IOException e) {
            e.printStackTrace();
            lastError = e;
            lastErrorCode = ERROR_CONNECTION;
        }

        return "";
    }

    @Override
    protected void onPostExecute(String result) {

        if ((prgDialog != null)&&(prgDialog.isShowing())) {prgDialog.dismiss();}

        if (lastError != null){
            mResultListener.onError(currOperation, lastError, lastErrorCode);
            return;
        }

        // - BOM
        if ( (int) result.charAt(0) > 65000) result = result.substring(1);
        mResultListener.onResult(currOperation, result, param);

    }

    @Override
    protected void onCancelled() {
        mResultListener.onCancel(currOperation);
    }


    // *****************************************************************
    //                              calls
    // *****************************************************************

    private String getStrUrl(String method, String[] params){

        String strUrl = "https://"+host+"/"+server+"/"+service+"/"+method;
        for (String value : params) {
            strUrl = strUrl + "/"+value;
        }
        return strUrl;
    }

    public void backendLogin(String card, String MD5, String appID){

        String deviceName = android.os.Build.MANUFACTURER + "-" + android.os.Build.MODEL;

        String[] params = {card, MD5, appID, deviceName};
        String query = getStrUrl("autorize", params);
        System.out.println("login query = "+query);
        currOperation = OPER_LOGIN;

        execute(query);

    }


    public void backendCheckPin(String pinMD5){

        String[] params = {mOptions.CARD, pinMD5};
        String query = getStrUrl("checkpin", params);
        System.out.println("checkpin query = "+query);
        currOperation = OPER_CHECK_PIN;

        execute(query);

    }


    public void backendBalance(){

        String[] params = {mOptions.CARD, mOptions.MD5};
        String query = getStrUrl("balance", params);
        System.out.println("balance query = "+query);
        currOperation = OPER_BALANCE;

        execute(query);

    }

    public void backendPreOrder(String Station, String Dispenser){

        String[] params = {mOptions.CARD, mOptions.MD5, Station, Dispenser};
        String query = getStrUrl("preorder", params);
        System.out.println("preorder query = "+query);
        currOperation = OPER_BEGIN_ORDER;

        execute(query);



    }

//    public void backendOrder(String uuid, String Station, String Dispenser, int goodCode, float count, String pinMD5){
//        //order/{НомерКарты}/{МД5}/{АЗС}/{ТРК}/{КодТовара}/{Количество}/{ИД}/{МД5 Пин}
//
//        String[] params = {mOptions.CARD, mOptions.MD5, Station, Dispenser, String.valueOf(goodCode), String.valueOf(count), uuid, pinMD5};
//        String query = getStrUrl("order", params);
//        System.out.println("order query = "+query);
//        currOperation = OPER_ORDER;
//        param = uuid;
//
//        execute(query);
//
//    }

    private void orderWallets(OrderData orderData){
        //order/{НомерКарты}/{МД5}/{АЗС}/{ТРК}/{КодТовара}/{Количество}/{ИД}/{МД5 Пин}

        String[] params = {mOptions.CARD, mOptions.MD5, orderData.stationCode, orderData.dispenser, String.valueOf(orderData.code), String.valueOf(orderData.orderedCount), orderData.uuid, orderData.md5};
        String query = getStrUrl("order", params);
        System.out.println("order query = "+query);
        currOperation = OPER_ORDER;
        param = orderData.uuid;

        execute(query);

    }

    private void orderBonuses(OrderData orderData){
        //order/{НомерКарты}/{МД5}/{АЗС}/{ТРК}/{КодТовара}/{Количество}/{ИД}/{МД5 Пин}

        String[] params = {mOptions.CARD, mOptions.MD5, orderData.stationCode, orderData.dispenser, String.valueOf(orderData.code), String.valueOf(orderData.orderedMoney), orderData.uuid, orderData.md5};
        String query = getStrUrl("orderbonuses", params);
        System.out.println("order query = "+query);
        currOperation = OPER_ORDER;
        param = orderData.uuid;

        execute(query);

    }


    public void backendOrder(OrderData orderData){
        //order/{НомерКарты}/{МД5}/{АЗС}/{ТРК}/{КодТовара}/{Количество}/{ИД}/{МД5 Пин}

        switch (orderData.payMethod){
            case OrderData.PM_WALLETS: {orderWallets(orderData); break;}
            case OrderData.PM_BONUSES: {orderBonuses(orderData); break;}

        }

    }

    public void backendOrderStatus(String uuid){

        String[] params = {uuid};
        String query = getStrUrl("orderstatus", params);
        System.out.println("order status query = "+query);
        currOperation = OPER_ORDER_STATUS;

        execute(query);

    }

}
