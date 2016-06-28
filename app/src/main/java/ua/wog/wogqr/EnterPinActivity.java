package ua.wog.wogqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import ua.wog.shared.Utils;


public class EnterPinActivity extends Activity {

    final private String JSON_STATUS = "Status";
    static final String extra_MD5 = "MD5";
    static final int request_code = 1;

    private String pinMD5;
    private EditText edPin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_pin);

        edPin = (EditText)findViewById(R.id.edDialogPin);

    }

    public void onContinue(View v){

        String pin = ((EditText)findViewById(R.id.edDialogPin)).getText().toString();
        pinMD5 = Utils.md5(pin);

        HTTPSTask request = getHTTPSTask();
        request.backendCheckPin(pinMD5);

    }

    public void onCancel(View v){
        setResult(RESULT_CANCELED);
        finish();
    }

    public void doPinAccepted(String result){

        try{
            JSONObject json = new JSONObject(result);
            int status = json.getInt(JSON_STATUS);
            if (status == 0){
                Intent i = new Intent();
                i.putExtra(extra_MD5, pinMD5);
                setResult(RESULT_OK, i);
                finish();
            }
            else{
                edPin.setError(getString(R.string.msg_order_wrong_pin));
            }

        }
        catch (JSONException e) {
            e.printStackTrace();
            edPin.setError(getString(R.string.msg_wrong_answer));
        }

    }

    private HTTPSTask getHTTPSTask(){

        HTTPSTask request = new HTTPSTask(this);
        request.setResultListener(new HTTPSTask.ResultListener() {
            @Override
            public void onResult(int operation_ID, String result, String param) {
                if (operation_ID == HTTPSTask.OPER_CHECK_PIN) {
                    doPinAccepted(result);
                }
            }

            @Override
            public void onCancel(int operation_ID) {
                if (operation_ID == HTTPSTask.OPER_CHECK_PIN) {
                    //
                }
            }

            @Override
            public void onError(int operation_ID, Exception e, int code) {
                if (operation_ID == HTTPSTask.OPER_CHECK_PIN) {
                    edPin.setError(getString(R.string.msg_order_no_loyalty_connection));
                }
            }
        });

        return request;
    }

}
