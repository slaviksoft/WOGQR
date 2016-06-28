package ua.wog.wogqr;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import ua.wog.shared.AppOptions;
import ua.wog.shared.Utils;


public class LoginActivity extends ActionBarActivity {

    private AppOptions options;
    private EditText edCardNumber;
    private EditText edPassword;
    private TextView tvEmail;
    private TextView tvPhone;
    private TextView tvCard;
    private Button   btLogin;


    private LinearLayout llAuthorized;
    private LinearLayout llNonAuthorized;

    private void initControls(){

        llAuthorized    = (LinearLayout)findViewById(R.id.llAuthorized);
        llNonAuthorized = (LinearLayout)findViewById(R.id.llNonAuthorized);

        edCardNumber    = (EditText)findViewById(R.id.edCardNumber);
        edPassword      = (EditText)findViewById(R.id.edPassword);
        tvEmail         = (TextView)findViewById(R.id.tvEmail);
        tvPhone         = (TextView)findViewById(R.id.tvPhone);
        tvCard          = (TextView)findViewById(R.id.tvCard);

        btLogin         = (Button)findViewById(R.id.btnLogin);

    }

    private void updateControls(){

        edCardNumber.setText(options.CARD);
        tvEmail.setText(options.EMAIL);
        tvPhone.setText(options.PHONE);
        tvCard.setText(options.CARD);

        if (options.LOGGED){

            llNonAuthorized.setVisibility(View.GONE);
            llAuthorized.setVisibility(View.VISIBLE);

            btLogin.setText(R.string.btn_logout);

        }else{

            llNonAuthorized.setVisibility(View.VISIBLE);
            llAuthorized.setVisibility(View.GONE);

            btLogin.setText(R.string.btn_login);
        }


    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Utils.setActionBarUp(getSupportActionBar());

        initControls();

        options = new AppOptions(this);
        options.read();

        updateControls();

    }


    public void OnLoginClick(View v){

        InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

        String appId = options.APP_ID;
        if (appId == ""){
            appId = UUID.randomUUID().toString();
        }

        if (options.LOGGED){

            options.MD5  = "";
            options.LOGGED = false;
            options.save();
        }
        else
        {

            String card  = edCardNumber.getText().toString();
            String pass  = Utils.md5(edPassword.getText().toString());

            options.CARD = card;
            options.save();

            HTTPSTask request = new HTTPSTask(this);
            request.setResultListener(new HTTPSTask.ResultListener() {
                @Override
                public void onResult(int operation_ID, String result, String param) {
                    if (operation_ID == HTTPSTask.OPER_LOGIN) {
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
                        Utils.showError(getApplicationContext(), e.getLocalizedMessage() );
                    }
                }
            });



            request.backendLogin(card, pass, appId);

        }

        updateControls();

    }

    public void doResult(String result){

        String pass  = Utils.md5(edPassword.getText().toString());
        String email="";
        String tel="";

        int status;
        Log.d("debug", result);

        if ((result.indexOf("status") > -1)||(result.indexOf("error") > -1)||(result.indexOf("canceled") > -1)) {
            Utils.showError(getApplicationContext(), result);
            return;
        }

        try{

            JSONObject json = new JSONObject(result);
            status = json.getInt("Status");
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
            else {

                String appID = json.getString("ID");

                JSONObject info = (JSONObject) json.get("Info");
                email = info.getString("EMAIL");
                tel = info.getString("TELEPHONE");

                if (info.getInt("HAVEPIN") == 0){
                    Utils.showError(getApplicationContext(), getString(R.string.msg_login_need_pin));
                    return;

                }

                options.APP_ID = appID;
                options.MD5 = pass;
                options.LOGGED = true;
                options.EMAIL = email;
                options.PHONE = tel;
                options.save();
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showError(getApplicationContext(), "Не правильный формат ответа");
        }

        //onOptionsChange();
        updateControls();

    }

}
