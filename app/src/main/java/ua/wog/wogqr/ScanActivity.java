package ua.wog.wogqr;

import android.content.Intent;
import android.graphics.PointF;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import com.dlazaro66.qrcodereaderview.QRCodeReaderView;

import ua.wog.shared.Utils;

public class ScanActivity extends ActionBarActivity implements QRCodeReaderView.OnQRCodeReadListener{

    private String QRCode = "...";
    private final int ResultEnterQR = 0;
    private QRCodeReaderView decoderview;
    private static final String TAG = QRCodeReaderView.class.getName();

    private boolean firstLaunch = true;
    public void doEmulate(View v){

//        String textQR = "016011001";
//        onQRCode(textQR);

        }

    @Override
    protected void onResume() {
        super.onResume();
        if (!firstLaunch)finish();
        firstLaunch = false;
    }

    private void onQRCode(String textQR){

        QRCode = textQR;

        MediaPlayer mPlayer = MediaPlayer.create(this, R.raw.beep);
        mPlayer.start();

        Utils.CodeQR data = Utils.getQRData(textQR);

        if (data == null) {
            Utils.showError(getApplicationContext(), getString(R.string.msg_wrong_QR));
            return;
        }

//        Intent i = new Intent(this, OrderActivity.class);
//        i.putExtra(Utils.KEY_QR_DATA, data);

        Intent i = new Intent(this, PaySelectionActivity.class);
        i.putExtra(Utils.KEY_QR_DATA, data);
        startActivity(i);

        finish();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

//        if (savedInstanceState != null && savedInstanceState.getBoolean("reopen")){finish();}

        Utils.setActionBarUp(getSupportActionBar());

        decoderview = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        decoderview.setOnQRCodeReadListener(this);
        decoderview.setFocusable(true);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("reopen", true);
    }

    @Override
    public void onQRCodeRead(String text, PointF[] points) {

        if (!QRCode.equals(text)) {
            onQRCode(text);
        }
    }

    @Override
    public void cameraNotFound() {}

    @Override
    public void QRCodeNotFoundOnCamImage() {}

    public void onEnterQRManual(View v) {

        decoderview.getCameraManager().stopPreview();

        Intent i = new Intent(this, EnterQRActivity.class);
        startActivityForResult(i, ResultEnterQR);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ResultEnterQR){

            if (resultCode == RESULT_OK) {
                String qrEntered = data.getStringExtra(EnterQRActivity.extra_QR);
                onQRCode(qrEntered);
            }

            decoderview.getCameraManager().startPreview();

        }
    }
}
