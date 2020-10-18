package com.example.makaotalk.popup;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.makaotalk.R;
import com.example.makaotalk.WifiReceiver;

public class TouchActivity extends Activity {
    private TextView textviewtouch;
    private Button btn;
    int count = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch);

        // SubActivity onPuase하기 위한 반투명 코드
        WindowManager.LayoutParams layoutParams= new WindowManager.LayoutParams();
        layoutParams.flags= WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount= 0.7f;getWindow().setAttributes(layoutParams);
        setContentView(R.layout.touch);

        textviewtouch = (TextView)findViewById(R.id.textviewtouch);
        btn = (Button)findViewById(R.id.btn);

        WifiReceiver.checkPop = true;
        WifiReceiver.tt.cancel();    //알림 반복 종료

        btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        WifiReceiver.checkPop = true;  //포그라운드서비스에서 notification 기능 건너뜀
        WifiReceiver.tt.cancel();    //알림 반복 종료
    }

    @Override
    protected void onPause() {
        super.onPause();
    }


    public boolean onTouchEvent(MotionEvent event){
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if(count==0){
                    WifiReceiver.checkPop=false;  //마스크 인증 완료 시
                    WifiReceiver.tt.cancel();    //알림 반복 종료
                    setResult(RESULT_OK);
                    finish();
                }

                else{
                    count--;
                    textviewtouch.setText(Integer.toString(count));
                    break;
                }
        }
        return super.onTouchEvent(event);
    }
}