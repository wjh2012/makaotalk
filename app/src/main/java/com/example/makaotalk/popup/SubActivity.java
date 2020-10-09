package com.example.makaotalk.popup;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.makaotalk.R;
import com.example.makaotalk.WifiReceiver;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SubActivity extends Activity {

    private static final String MODEL_PATH = "mobilenet_quant_v1_224.tflite";
    private static final boolean QUANT = true;
    private static final String LABEL_PATH = "labels.txt";
    private static final int INPUT_SIZE = 224;

    private Classifier classifier;

    private Executor executor = Executors.newSingleThreadExecutor();
    private TextView textViewResult;
    private Button btnDetectObject;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        Log.d("popup", "팝업창 표시");
        WifiReceiver.checkPop = false;  //포그라운드서비스에서 notification 기능 건너뜀
        WifiReceiver.tt.cancel();    //알림 반복 종료



        cameraView = findViewById(R.id.cameraView);
        textViewResult = findViewById(R.id.textViewResult);
        btnDetectObject = findViewById(R.id.btnDetectObject);

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {

                Bitmap bitmap = cameraKitImage.getBitmap();

                bitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false);

                final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);

                textViewResult.setText(results.toString());

                /**
                 * results는
                 * 추론 결과가 10퍼센트를 넘는 것 중
                 * 최대 3개의 결과를 가져오는데
                 *
                 * mask(mask, oxygen mask, ski mask, gasmask)
                 * diaper(기저귀라는 뜻: 난 흰색 덴탈 마스크 끼면 diaper로 인식하더라)
                 * 라는 단어를 포함하면
                 *
                 * 토스트 메시지 출력
                 * */
                for(int i=0;i<results.size();i++){
                    String title = results.get(i).getTitle();

                    if(title.contains("mask")||title.contains("diaper")) {
                        Toast.makeText(getApplicationContext(), "마스크 인식 완료~!", Toast.LENGTH_LONG).show();

                        WifiReceiver.checkPop=true;  //마스크 인증 완료 시
                        WifiReceiver.tt.cancel();    //알림 반복 종료

                        finish();
                    }
                }
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });

        cameraView.toggleFacing();
        btnDetectObject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.captureImage();
            }
        });

        initTensorFlowAndLoadModel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.start();

        WifiReceiver.checkPop = false;  //포그라운드서비스에서 notification 기능 건너뜀
        WifiReceiver.tt.cancel();    //알림 반복 종료
    }

    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    //하단버튼
    @Override
    protected void onStop() {
        super.onStop();

        //알람 재가동
        if(!WifiReceiver.checkPop){
            WifiReceiver.repeatNotification(getBaseContext());
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.execute(new Runnable() {
            @Override
            public void run() {
                classifier.close();
            }
        });
    }

    private void initTensorFlowAndLoadModel() {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    classifier = TensorFlowImageClassifier.create(getAssets(), MODEL_PATH, LABEL_PATH, INPUT_SIZE, QUANT);
                    makeButtonVisible();
                } catch (final Exception e) {
                    throw new RuntimeException("Error initializing TensorFlow!", e);
                }
            }
        });
    }

    private void makeButtonVisible() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnDetectObject.setVisibility(View.VISIBLE);
            }
        });
    }
}