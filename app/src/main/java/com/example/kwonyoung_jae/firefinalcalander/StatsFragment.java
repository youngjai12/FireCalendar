package com.example.kwonyoung_jae.firefinalcalander;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Color;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;


public class StatsFragment extends Fragment {


    public StatsFragment() {
        // Required empty public constructor
    }
    private static final String TAG = "MainActivity";

    private TextView txtAccomplish;
    private TextView txtProgress;
    private TextView txtName;
    private PieChart pChart;
    private LineChart lChart;
    String habitname;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_stats, container, false);

        txtName = view.findViewById(R.id.habit_name);
        txtName.setText(habitname);


        txtAccomplish = view.findViewById(R.id.accomplishment);
        txtAccomplish.setText("성취도(%)");

        txtProgress = view.findViewById(R.id.progress);
        txtProgress.setText("Track of Your Habit");


        pChart = (PieChart)view.findViewById(R.id.piechart);

        pChart.setUsePercentValues(true);
        pChart.getDescription().setEnabled(false);
        pChart.setExtraOffsets(5,10,5,5);
        pChart.setDragDecelerationFrictionCoef(0.95f);
        pChart.setDrawHoleEnabled(true);
        pChart.setHoleColor(Color.WHITE);
        pChart.setTransparentCircleRadius(61f);
        pChart.getLegend().setEnabled(false);
        pChart.setCenterTextSize(35f);

        lChart = (LineChart) view.findViewById(R.id.linechart);

        lChart.setDragEnabled(true);
        lChart.setScaleEnabled(false);
        lChart.getAxisRight().setDrawGridLines(false);
        lChart.getAxisRight().setEnabled(false);
        lChart.getAxisLeft().setDrawGridLines(false);
        lChart.getXAxis().setDrawGridLines(false);
        lChart.setExtraOffsets(10,10,10,5);
        lChart.getDescription().setEnabled(false);

        if(getArguments().getString("SelectedHabit")==null){
            habitname = getArguments().getString("DefaultHabit");
            txtName.setText(habitname);
            //Log.d("### 과연 ## ","habit_list잘 채워짐? "+habit_list.size());

        }else {
            habitname = getArguments().getString("SelectedHabit");
            txtName.setText(habitname);
            //Log.d("### 과연 ## ","habit_list잘 채워짐? "+habit_list.size());
        }
        final String finalname = habitname;
        Log.d("#### 과연 ###","stats frag 에서는 finalname ? "+finalname);
        FirebaseFirestore.getInstance().collection("degree").document(finalname).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Log.d("###### 과연 #######", "일단 성공적으로 " + finalname + " document에 접근했는가?");
                ArrayList<PieEntry> pyValues = new ArrayList<>();
                if (documentSnapshot == null) {
                    Log.d("## 과연 ###", "실패했을 때를 뜻함");
                } else {
                    Log.d("### 과연 ###", " 도대체 어디로 접근한다는거임 ㅅㅂ");
                    ArrayList<Integer> records;
                    ArrayList<String> dates;
                    Log.d("### 과연 ###", " 도대체 어디로 접근한다는거임 2222");

                    DegreeDTO data2 = documentSnapshot.toObject(DegreeDTO.class);  // documentsnapshot 이라는 것이 데이터베이스에서 우리가정한 habitname에 해당하는 문서를 말함. 그래서 그 문서를
                    //degreeDTO class로 변경해줘야, 그 문서에서 항목 즉 habit degree 같은거에 쉽게 접근할 수 있음.

                    Iterator<String> it = data2.done_date.keySet().iterator(); // 이건 우리가 날짜 - 정도 를 hashmap 형태로 입력했기 때문에 입력된 날짜를 모두 조회하려고 , itertor 사용 한 것임.

                    records = new ArrayList<>(); // 그 기록정보들을 담기 위한 arraylist
                    dates = new ArrayList<>();
                    int count = 0;
                    while (it.hasNext()) {

                        count++;
                        String date = it.next(); // iterator는 key를 말하는 것이고, 그 key가 next가 존재한다면
                        Log.d("### 과연 ####", "DB에 있는 값이 잘 안들어가나? " + data2.done_date.get(date));
                        records.add(data2.done_date.get(date)); //key인 date에 해당하는 data를 넘겨준다.// )
                        dates.add(date);
                    }
                    Log.d("### 과연 ###", " 도대체 어디로 접근한다는거임 333");

                    //여기까지 하면 records라는 array에 모든 것들이 다 담겨있음.
                    double achieve = 0;
                    if (count == 0) {
                        achieve = 0;
                    } else {
                        int sum = 0;
                        for (int i = 0; i < count; i++) {
                            //Log.d("### 과연 ####","DB에 있는 값이 잘 안들어가나? "+records.get(i));
                            sum = sum + records.get(i);
                        }
                        double temp1 = count * 100;
                        double temp2 = sum;
                        achieve = (temp2 / temp1) * 100;
                        Log.d("### 과연 ###", "성취율이 잘 구해지는가? " + achieve + " 합은 잘 구해졌나? " + sum);

                        pyValues.add(new PieEntry((int) achieve, "")); //성취한 정도(첫번째 값으로 입력)
                        pyValues.add(new PieEntry(100 - (int) achieve, ""));

                        pChart.setCenterText(pyValues.get(0).getValue() + "%");
                        pChart.setCenterTextSize(30f);
                        pChart.setCenterTextColor(Color.DKGRAY);
                        PieDataSet dataSet = new PieDataSet(pyValues, "Progress");

                        dataSet.setSliceSpace(3f);
                        dataSet.setSelectionShift(5f);
                        dataSet.setColors(Color.argb(96,255,0,0),Color.LTGRAY);
                        dataSet.setDrawValues(false);

                        PieData data = new PieData((dataSet));

                        pChart.setData(data);
                        pChart.animateY(3000, Easing.EasingOption.EaseInBack);

                        //##############################################################
                        //############### 꺾은 선 그래프 부분 ####################

                        ArrayList<Entry> yValues = new ArrayList<>();

                        for (int i = 0; i < records.size(); i++) {
                            yValues.add(new Entry(i, (float) records.get(i)));
                        }


                        LineDataSet set2 = new LineDataSet(yValues, "Habit1");

                        set2.setColor(Color.argb(96,255,0,0));
                        set2.setLineWidth(2.5f);
                        set2.setCircleColor(Color.rgb(240, 238, 70));
                        set2.setCircleRadius(3f);
                        set2.setFillColor(Color.rgb(240, 238, 70));
                        set2.setMode(LineDataSet.Mode.LINEAR);
                        set2.setDrawValues(false);

                        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                        dataSets.add(set2);

                        final ArrayList<String> xLabels = new ArrayList<>();

                        for (int i = 0; i < dates.size(); i++) {
                            xLabels.add(dates.get(i));
                        }

                        XAxis xAxis = lChart.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setValueFormatter(new IAxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                return xLabels.get((int) value);
                            }
                        });

                        LineData data3 = new LineData(dataSets);
                        lChart.setData(data3);
                        lChart.animateX(3000, Easing.EasingOption.Linear);
                    }
                }

            }
        });




        return view;
    }

}