package com.example.kwonyoung_jae.firefinalcalander;

import android.*;
import android.Manifest;
import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Activity;
import  android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.kwonyoung_jae.firefinalcalander.util.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements  BottomNavigationView.OnNavigationItemSelectedListener{
    BottomNavigationView nav;
    String commonHabit;
    String defaultHabit;

    ArrayList<String> arrs = new ArrayList<>();
    private static final int INITIAL_REQUEST=1237;
    private final static int REQUEST_CODE = 10101;

    private static final String[] INITIAL_PERMS={
            android.Manifest.permission.READ_PHONE_STATE,
            android.Manifest.permission.ACCESS_NETWORK_STATE,
            android.Manifest.permission.READ_CALL_LOG
    };


    int HABIT_SELECTOR = 111;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.main_navigation);
        nav.setOnNavigationItemSelectedListener(this);
        nav.setSelectedItemId(R.id.my_accont);
        final ArrayList<HabitDTO> hab = new ArrayList<>();
        crawl_update();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        } else {
            if (!checkPermission()) {
                requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
            }
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
        FirebaseFirestore.getInstance().collection("habits").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if(queryDocumentSnapshots==null){

                }else{
                    for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                        HabitDTO item = doc.toObject(HabitDTO.class);
                        hab.add(item);
                    }
                    defaultHabit = hab.get(0).habit_name;

                }
            }
        });

    }
    public void saveHabit(String habit){
        commonHabit = habit;
        Log.d("### 과연 mainactivity에서","제대로 잘 호출되었는가? "+commonHabit);
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){
            case R.id.my_accont:
                android.support.v4.app.Fragment fragment = new AccountFragment();
                loadFragment(fragment);
                return true;
            case R.id.calendar:
                android.support.v4.app.Fragment fragment1 = new CalendarFragment();
                Bundle bundle = new Bundle();
                Log.d("### 과연 ### ","설마 여기서 commonHabit이 "+commonHabit);
                bundle.putString("SelectedHabit",commonHabit);
                bundle.putString("defaultHabit", defaultHabit);

                fragment1.setArguments(bundle);
                loadFragment(fragment1);

                return true;
            case R.id.stats:
                android.support.v4.app.Fragment fragment2 = new StatsFragment();
                Log.d("### 과연 ### ","여긴 stats tab commonHabit이 "+commonHabit);
                Bundle bundle1 = new Bundle();

                bundle1.putString("SelectedHabit",commonHabit);
                bundle1.putString("DefaultHabit",defaultHabit);
                fragment2.setArguments(bundle1);
                loadFragment(fragment2);

                return true;
            default:
                return false;
        }
    }
    public void loadFragment(android.support.v4.app.Fragment fragment){
        getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment)
                .commit();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        Logger.debug("Permission Result : " + requestCode);

        switch(requestCode) {
            case INITIAL_REQUEST:
                if (!checkPermission()) {
                    if(permissions != null) {
                        for(int i=0;i<permissions.length;i++) {
                            Logger.debug("permission : " + permissions[i] + ", " + grantResults[i]);

                        }
                    }
                }
                break;
        }
    }

    private boolean checkPermission() {
        if (!hasPermission(android.Manifest.permission.READ_PHONE_STATE)
                || !hasPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
                || !hasPermission(Manifest.permission.READ_CALL_LOG)) {
            return false;
        } else {
            // Checks if app already has permission to draw overlays
            return true;
        }
    }
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    public void crawl_update(){
        final FirebaseFirestore firestore;
        firestore = FirebaseFirestore.getInstance();
        for(int i=1;i<=12;i++){
            StorageReference storageReference;
            try{
                storageReference = FirebaseStorage.getInstance().getReference().child("icons").child(i+".png");
            }
            catch (RuntimeException e){
                continue;
            }
            final String imagename = String.valueOf(i);
            storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    String url = task.getResult().toString();
                    IconDTO item = new IconDTO();
                    item.imageURL = url;
                    item.imagename = imagename;
                    Log.d("### 과연 ###","이미지 url잘 찍히나? "+url);

                    firestore.collection("iconUrl").document(imagename+".png").set(item);
                }
            });
        }
    }

    /*
    @Override
    public void onActivityResult(int requestCode , int resultCode,  Intent data){
        super.onActivityResult(requestCode,resultCode , data);
        Log.d("### 과연 mainacitivty","여기까지 오나 ? ");
        if(requestCode == HABIT_SELECTOR && requestCode == RESULT_OK){
            commonHabit = data.getStringExtra("Selected_habit");
            Log.d("과연 MainActivity에서? ","MainActivity result : "+ commonHabit);
        }
    }*/
}
