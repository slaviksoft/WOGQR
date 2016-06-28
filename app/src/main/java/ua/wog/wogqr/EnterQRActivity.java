package ua.wog.wogqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import ua.wog.shared.Utils;

public class EnterQRActivity extends Activity {

    static final String extra_QR = "QRCode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_qr);
    }

    public void onContinue(View v){

        EditText edQR = (EditText) findViewById(R.id.editTextEnterQR);
        String txt = ((EditText) findViewById(R.id.editTextEnterQR)).getText().toString();

        if (txt.length() == Utils.QR_DATA_LENGTH){
            Intent intent = new Intent();
            intent.putExtra(extra_QR, txt);
            setResult(RESULT_OK, intent);
            finish();
        }

    }


    public void onCancel(View v){
        setResult(RESULT_CANCELED);
        finish();
    }

}
