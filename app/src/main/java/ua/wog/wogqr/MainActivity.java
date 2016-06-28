package ua.wog.wogqr;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

import ua.wog.shared.AppOptions;
import ua.wog.shared.OrderData;
import ua.wog.shared.Utils;


public class MainActivity extends ActionBarActivity {

    private AppOptions options;
    private Button btnCabinet;
    private Button btnScan;
    private Button btnBalance;

    private OrderData orderData;
    public String debugTag = "debug";

    public final int REQUEST_CABINET = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.setActionBarUp(getSupportActionBar(), true);

        btnCabinet = (Button)findViewById(R.id.btnCabinet);
        btnScan    = (Button)findViewById(R.id.btnScan);
        btnBalance = (Button)findViewById(R.id.btnBalance);

        options = new AppOptions(this);

    }

    @Override
    public void onResume(){

        super.onResume();

        orderData = new OrderData();
        orderData.LoadFromPreferences(getApplicationContext());

        options.read();
        onOptionsChange();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CABINET) {
            onOptionsChange();
        }

    }

    private void setButtonsAvailability(boolean availability){

        btnBalance.setEnabled(availability);
        btnScan.setEnabled(availability);

    }

    public void onScanClick(View v){

        if (OrderIsActive()){

            Intent i = new Intent(this, OrderExecuteActivity.class);
            //i.putExtra(OrderData.EXTRA_NAME, orderData);
            startActivity(i);

        }else{

            Intent scanIntent = new Intent(this, ScanActivity.class);
            startActivity(scanIntent);

        }

    }

    public void onCabinetClick(View v){

        Intent i = new Intent(this, LoginActivity.class);
        startActivityForResult(i, REQUEST_CABINET);

    }

    public void onBalanceClick(View v){

        Intent i = new Intent(this, BalanceActivity.class);
        startActivity(i);

    }


    public void onTestClick(View v){

        Intent i = new Intent(this, TestActivity.class);
        startActivity(i);

    }

    public void onOptionsChange(){

        if (options.LOGGED) {
            btnCabinet.setText(R.string.btn_cabinet);
            setButtonsAvailability(true);
        }
        else {
            btnCabinet.setText(R.string.btn_login);
            setButtonsAvailability(false);
        }

        if (OrderIsActive()){
            btnScan.setText(R.string.btn_scan_status);
        }else{
            btnScan.setText(R.string.btn_scan);
        }
    }

    private boolean OrderIsActive(){
        return orderData.state == OrderData.STATE_SENT;
    }



}
