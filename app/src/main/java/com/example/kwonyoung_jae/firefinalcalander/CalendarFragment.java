package com.example.kwonyoung_jae.firefinalcalander;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;


/**
 * A simple {@link Fragment} subclass.
 */
public class CalendarFragment extends Fragment implements View.OnClickListener {
    MaterialCalendarView calendarView;
    String CurrentHabit;
    Button enroll_btn,low,mid,high;
    DocumentReference docref;
    String todayDate = new SimpleDateFormat("MM-dd").format(new Date());
    int year, month, day;
    TextView caltextview;
    String color="#ff0000";
    int[][] habitcal = new int[13][32];

    public CalendarFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_calendar, container, false);
        enroll_btn = view.findViewById(R.id.enroll_habit);

        calendarView = view.findViewById(R.id.calendarView);
        caltextview = view.findViewById(R.id.caltextview);

        calendarView.setSelectedDate(CalendarDay.today());
        calendarView.state().edit()
                .isCacheCalendarPositionEnabled(false)
                .setFirstDayOfWeek(Calendar.MONDAY)
                .setMinimumDate(CalendarDay.from(2017,0,1))
                .setMaximumDate(CalendarDay.from(2099,12,31))
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();
        calendarView.setShowOtherDates(MaterialCalendarView.SHOW_OUT_OF_RANGE);
        calendarView.addDecorator(new SaturdayDecorator());
        calendarView.addDecorator(new SundayDecorator());
        calendarView.addDecorator(new Todaydecorator());
        calendarView.setPadding(0,-20,0,-20);

        if(getArguments().getString("SelectedHabit")==null){
            Log.d("### 과연 ###","selectedHabit이 null은 아닌건가??");
            final ArrayList<HabitDTO> hab = new ArrayList<>();
            FirebaseFirestore.getInstance().collection("habits").addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    Log.d("#### 과연 ####","여기 firebase snapshot까지는 타나?");

                    if(queryDocumentSnapshots==null){

                    }else{

                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            Log.d("### 과연 "," 이 for loop은 얼마나 탈까?");
                            HabitDTO item = doc.toObject(HabitDTO.class);
                            hab.add(item);
                        }
                        CurrentHabit = hab.get(0).habit_name;
                    }
                    Log.d("### 과연 ### ","여기까지는나오나?");
                }
            });
        }else{
            Log.d("### 과연 ### ","여기까지는나오나 555555?");
            CurrentHabit = getArguments().getString("SelectedHabit");
            enroll_btn.setText(CurrentHabit);
            FirebaseFirestore.getInstance().collection("habits").document(CurrentHabit).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    Log.d("###### 과연 #######", "일단 성공적으로 " + CurrentHabit + " document에 접근했는가?");
                    if (documentSnapshot == null) {
                        Log.d("eunha", "실패");
                    } else {
                        HabitDTO data2 = documentSnapshot.toObject(HabitDTO.class);
                        String timestamp = data2.timestamp;
                        year = Integer.parseInt(timestamp.substring(0, 4));
                        month = Integer.parseInt(timestamp.substring(5, 7));
                        day = Integer.parseInt(timestamp.substring(8, 10));
                        if(data2.habit_color != null) {
                            color = data2.habit_color;
                            Log.d("은하","색상 : "+color);
                        }
                        Log.d("은하", year + "년" + month + "월" + day + "일");
                    }
                }
            });


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
                            Log.d("은하", "여기까지오나?!111");
                            Log.d("은하", "레코드의..." + Integer.parseInt(daterecords.get(idx).substring(0, 2)));
                            Log.d("은하", "이번달..." + CalendarDay.today().getMonth());
                            Log.d("은하", "레코드의...22" + daterecords.get(idx));
                            mmm = Integer.parseInt(daterecords.get(idx).substring(0, 2));
                            ddd = Integer.parseInt(daterecords.get(idx).substring(3, 5));
                            if(records.get(idx)>0) {
                                habitcal[mmm][ddd] = 1;
                            }
                            Log.d("은하", color);
                            if(mmm==Integer.parseInt(todayDate.substring(0,2))) {
                                calendarView.addDecorator(new HabitDecorator(getContext(), CalendarDay.from(CalendarDay.today().getYear(), mmm-1, ddd), records.get(idx), color));
                                Log.d("은하", "여기까지오나?!333");
                                Log.d("은하", "기록되는 날짜는 " + CalendarDay.today().getMonth() + "월" + ddd + "일");
                            }
                        }
                    }

                    int continueous = 0;
                    int mmm = Integer.parseInt(todayDate.substring(0,2));
                    Log.d("은하","mmm="+mmm);
                    int ddd = CalendarDay.today().getDay();
                    for(int idx = 1 ; idx<32; idx++) {
                        if (ddd-idx <= 0) {
                            break;
                        }
                        if(habitcal[mmm][ddd-idx]==1) {
                            continueous++;
                        }
                        else {
                            break;
                        }
                    }
                    if (continueous==0) {
                        caltextview.setText("이번 달 "+CurrentHabit+" 분발하세요!");
                        Log.d("은하","이번 달 "+CurrentHabit+" 분발하세요!");
                    }
                    else {
                        caltextview.setText("이번 달 "+CurrentHabit+" 연속 "+continueous+"일째 달성중!");
                        Log.d("은하","이번 달 "+CurrentHabit+" 연속 "+continueous+"일째 달성중!");
                    }

                }
            });
        }

        Log.d("###### 과연 #####","현재 current habit은 어떻게 되는가?"+CurrentHabit);

        //달력 넘길 때 매번 새로 데이터 가져와서 달력 drawable해줘야 함
        //calendarView.setOnMonthChangedListener();
        //만약 어떤 날 check하지 않았다면 자동으로 0으로 해줘야함?

        low = view.findViewById(R.id.zerobtn);
        low.setOnClickListener(this);

        mid = view.findViewById(R.id.midbtn);
        mid.setOnClickListener(this);

        high = view.findViewById(R.id.fullbtn);
        high.setOnClickListener(this);

        return view;
    }


    public void getData(){
    }


    @Override
    public void onClick(View view) {
        Log.d("###### 과연 ######","set click까지는 오는가?"+view.getId());
        int degree=0;
        switch (view.getId()){
            case R.id.fullbtn:
                Log.d("### 과연###","fullbtn 클릭");
                degree=100;
                calendarView.addDecorator(new HabitDecorator(getContext(), CalendarDay.today(), degree, color));
                break;
            case R.id.zerobtn:
                Log.d("### 과연###"," zero btn 클릭");
                degree=0;
                calendarView.addDecorator(new HabitDecorator(getContext(), CalendarDay.today(), degree, color));
                break;
            case R.id.midbtn:
                Log.d("### 과연###","midbtn 클릭");
                degree=50;
                calendarView.addDecorator(new HabitDecorator(getContext(), CalendarDay.today(), degree, color));
                break;
        }
        Log.d("##### 과연 ##########"," 제대로된 값이 들어가나?? "+degree);
        FirebaseFirestore.getInstance().collection("degree").document(CurrentHabit).update("done_date."+todayDate,degree);
    }

}