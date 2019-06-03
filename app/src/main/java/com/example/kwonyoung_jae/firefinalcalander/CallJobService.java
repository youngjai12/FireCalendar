package com.example.kwonyoung_jae.firefinalcalander;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.example.kwonyoung_jae.firefinalcalander.util.Logger;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class CallJobService extends JobIntentService {

    private static final int JOB_ID = 10102;

    private WindowManager windowManager;
    private View floatyView;

    int[][] habitcal = new int[13][32];

    String todayDate = new SimpleDateFormat("MM-dd").format(new Date());

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    public void onCallStateChanged(int state, String number) {
        if(lastState == state){
            //No change, debounce extras
            return;
        }
        Log.d("## 과연 ","무슨상태 ? "+state);
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                Message msg = new Message();
                msg.what =12345612;
                msg.obj = number;
//                handler.sendMessageDelayed(msg, 500);
                handler.sendMessage(msg);
//                onIncomingCallStarted(number);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if(lastState != TelephonyManager.CALL_STATE_RINGING){
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if(lastState == TelephonyManager.CALL_STATE_RINGING){
                    //Ring but no pickup-  a miss
                    onMissedCall(savedNumber, callStartTime);
                } else if(isIncoming){
                    onIncomingCallEnded(savedNumber, callStartTime, new Date());
                } else{
                    onOutgoingCallEnded(savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

    protected void onOutgoingCallStarted(String number, Date start) {

    }

    protected void onIncomingCallEnded(String number, Date start, Date end) {

    }

    protected void onOutgoingCallEnded(String number, Date start, Date end) {

    }

    protected void onMissedCall(String number, Date start) {

    }

    protected void onIncomingCallStarted(String number) {
        int LAYOUT_FLAG;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        }

        final WindowManager.LayoutParams params =
                new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        LAYOUT_FLAG,
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER | Gravity.START;
        params.x = 0;
        params.y = 0;

        floatyView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.layout_call, null);
        TextView tvNumber = floatyView.findViewById(R.id.tv_number);
        tvNumber.setText(number);
        Button btnClose = floatyView.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                windowManager.removeView(floatyView);
                floatyView = null;
            }
        });

//        floatyView.setOnTouchListener(this);

        windowManager.addView(floatyView, params);
        Logger.debug("Create Overlay for call.......");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Logger.debug("Service is created.....");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.debug("Service is destroyed....");
//        if (floatyView != null) {
//            windowManager.removeView(floatyView);
//            floatyView = null;
//        }
    }

    public static void enqueueWork(Context ctx, Intent intent) {
        Log.d("## 과연 ","enqueuwork 부분인데, jobservice 안에..");
        enqueueWork(ctx, CallJobService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        // 여기서 background로 계속 work를 불러주는 역할을 한다.

        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else{
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if(stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                state = TelephonyManager.CALL_STATE_IDLE;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            }
            else if(stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)){
                state = TelephonyManager.CALL_STATE_RINGING;
            }

            Logger.debug("State : " + state + ", Number : " + number);

            onCallStateChanged( state, number);
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 12345612) {

                //이부분에서 선택적으로 Incoming Call 이 오면 하는일을 하도록 해주었다.그러기 위해서는 조건을 걸어줘야한다.
                //OnIncomingCall 이 부분이 조건을 만족할 때만 실행이 될 수 있도로 ㄱ해주기 위해서 그 위에까지 연속성을 체크하는 부분을 만들었다.
                final String number = (String) msg.obj;
                final String CurrentHabit = "Programming";
                FirebaseFirestore.getInstance().collection("degree").document(CurrentHabit).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Log.d("###### 과연 #######", "일단 성공적으로 " + CurrentHabit + " document에 접근했는가?");
                        if (documentSnapshot == null) {
                            Log.d("## 과연 ###", "실패했을 때를 뜻함");
                        } else {
                            ArrayList<String> daterecords;
                            ArrayList<Integer> records;
                            DegreeDTO data2 = documentSnapshot.toObject(DegreeDTO.class);  // documentsnapshot 이라는 것이 데이터베이스에서 우리가정한 habitname에 해당하는 문서를 말함. 그래서 그 문서를
                            //degreeDTO class로 변경해줘야, 그 문서에서 항목 즉 habit degree 같은거에 쉽게 접근할 수 있음.
                            Iterator<String> it = data2.done_date.keySet().iterator(); // 이건 우리가 날짜 - 정도 를 hashmap 형태로 입력했기 때문에 입력된 날짜를 모두 조회하려고 , itertor 사용 한 것임.

                            daterecords = new ArrayList<>();
                            records = new ArrayList<>(); // 그 기록정보들을 담기 위한 arraylist
                            int count = 0;
                            while (it.hasNext()) {
                                count++;
                                String date = it.next(); // iterator는 key를 말하는 것이고, 그 key가 next가 존재한다면
                                //Log.d("### 과연 ####","DB에 있는 값이 잘 안들어가나? "+data2.done_date.get(date));
                                daterecords.add(date);
                                Log.d("은하", "저장되는 날짜??" + date);
                                records.add(data2.done_date.get(date)); //key인 date에 해당하는 data를 넘겨준다.
                            }
                            //여기까지 하면 records라는 array에 모든 것들이 다 담겨있음.
                            int todayday = CalendarDay.today().getDay();
                            Log.d("은하", "오늘 : " + todayday);
                            int mmm = 1;
                            int ddd = 1;
                            Log.d("은하", "카운트 : " + count);
                            for (int idx = 0; idx < count; idx++) {
                                mmm = Integer.parseInt(daterecords.get(idx).substring(0, 2));
                                ddd = Integer.parseInt(daterecords.get(idx).substring(3, 5));
                                if (records.get(idx) > 0) {
                                    habitcal[mmm][ddd] = 1;
                                }
                            }
                        }
                        int noncontinueous = 0;
                        int mmm = Integer.parseInt(todayDate.substring(0, 2));
                        int ddd = CalendarDay.today().getDay();
                        for (int idx = 1; idx < 32; idx++) {
                            if (ddd - idx <= 0) {
                                break;
                            }
                            if (habitcal[mmm][ddd - idx] != 1) {
                                noncontinueous++;
                            } else {
                                break;
                            }
                        }
                        if (noncontinueous > 4) {
                            Log.d("### 과연 ##", "연속성 체크 거릴나?");
                            onIncomingCallStarted(number);
                        }
                    }
                });

            }//if문 - 맞는 message가 오는지 확인하는 부분.
        }
    };
}
