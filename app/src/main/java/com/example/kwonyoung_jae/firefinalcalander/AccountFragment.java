package com.example.kwonyoung_jae.firefinalcalander;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kwonyoung_jae.firefinalcalander.util.RecyclerTouchListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class AccountFragment extends Fragment {






    public AccountFragment() {
        // Required empty public constructor
    }
    String name;
    Button habit_btn;
    RecyclerView recyclerView;
    FirebaseFirestore firestore;
    EditText habitname;
    private MainActivity activity;
    int HABIT_SELECTOR = 111;
    String selectedIcon;
    private ArrayList<HabitDTO> habit_list ;
    private AccountRecyclerViewAdapater mAdapter = new AccountRecyclerViewAdapater();
    private TextView noHabitView;


    Bundle args = getArguments();



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_account, container, false);
        habit_btn= (Button) view.findViewById(R.id.button_habit);
        habitname=(EditText) view.findViewById(R.id.note) ;
        habit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNoteDialog(false, null, -1);
            }
        });
        noHabitView = view.findViewById(R.id.empty_notes_view);
        recyclerView = view.findViewById(R.id.account_recycler);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                showActionDialog(position);
            }
        }));

        return view;
    }
    /*
    public void showEnrollDialog(){
        DialogFragment dialog = new EnrollDialogFragment();
        dialog.show(getActivity().getSupportFragmentManager(), "enroll_habit");
    }
    */




    public class AccountRecyclerViewAdapater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        AccountRecyclerViewAdapater(){
            firestore = FirebaseFirestore.getInstance();
            firestore.collection("habits").orderBy("realtime", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                    Log.d("### 과연 ","snapshotlistener가 작동했는가?");
                    if(queryDocumentSnapshots==null){
                        Log.d("#### 과연 3##","아무것도 없나봄..habit에 추가된게 아무것도 없나봄");
                    }else{
                        habit_list=  new ArrayList<>();
                        for(QueryDocumentSnapshot doc : queryDocumentSnapshots){
                            HabitDTO item = doc.toObject(HabitDTO.class);
                            habit_list.add(item);
                        }
                        notifyDataSetChanged();
                    }
                }
            });
        }


        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.habit_list,viewGroup,false);

            return new CustomViewholder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, final int position) {
            String timestamp = "";
            String thiscolor = "#ff0000";
            timestamp = habit_list.get(position).timestamp.substring(0,10);
            String url = habit_list.get(position).iconUrl;

            if(habit_list.get(position).habit_color != null) {
                thiscolor = habit_list.get(position).habit_color;
            }

            Glide.with(getActivity())
                    .load(url)
                    .into(((CustomViewholder)viewHolder).habitIcon);

            ((CustomViewholder)viewHolder).habitname.setText(habit_list.get(position).habit_name);
            ((CustomViewholder)viewHolder).timestamp.setText(timestamp);
            ((CustomViewholder)viewHolder).habitname.setTextColor(Color.parseColor(thiscolor));
            ((CustomViewholder)viewHolder).habitname.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("#### 과연 ###","habit의 이름은 뭔가? "+habit_list.get(position).habit_name);
                    ((MainActivity)getActivity()).saveHabit(habit_list.get(position).habit_name);
                    Toast.makeText(getActivity(),habit_list.get(position).habit_name+"이 선택되었습니다.",Toast.LENGTH_LONG).show();
                    /*

                    Fragment fragment = new CalendarFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("SelectedHabit",habit_list.get(position).habit_name);
                    fragment.setArguments(bundle);
                     activity.getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment)
                            .commit();
                    */


                }
            });

        }

        @Override
        public int getItemCount() {
            return null!=habit_list?habit_list.size():0;
        }

        private class CustomViewholder extends RecyclerView.ViewHolder {
            public TextView habitname,timestamp;
            public ImageView habitIcon;
            public RelativeLayout background;
            public ImageView check;
            public CustomViewholder(View view) {
                super(view);
                habitname = view.findViewById(R.id.habitname);
                timestamp = view.findViewById(R.id.timestamp);
                habitIcon = view.findViewById(R.id.habitIcon);
                //check= view.findViewById(R.id.checksign);
                background = view.findViewById(R.id.background);
            }
        }
    }
    private void showIconDialog(){
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity().getApplicationContext());
        View view = layoutInflater.inflate(R.layout.icon_dialog,null);
        final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getActivity());
        alertDialogBuilderUserInput.setView(view);
        RecyclerView recyclerView2 = view.findViewById(R.id.wrapper);
        recyclerView2.setAdapter(new GridFragmentRecyclerViewAdapter());
        recyclerView2.setLayoutManager(new GridLayoutManager(getActivity(),3));


        alertDialogBuilderUserInput.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                alertDialog.dismiss();
            }
        });
    }

    private void showActionDialog(final int position){
        CharSequence colors[] = new CharSequence[]{"Edit","Delete"};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose option");
        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which ==0) {
                    showNoteDialog(true, habit_list.get(position), position);
                } else {
                    Log.d("#### 과연 ####", "포지션은 무엇인가" + position);
                    deleteHabit(position);
                }
            }
        });
        builder.show();
    }


    private void showNoteDialog(final boolean shouldUpdate, final HabitDTO habit, final int position){
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getActivity().getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.note_dialog,null);
        final String[] habit_color = {"#b02513"};
        Button colorRed, colorOrange, colorYellow, colorGreen, colorBlue;
        final TextView colortext;
        final Button iconselect;
        ImageView iconpreview;

        final AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(getActivity());
        alertDialogBuilderUserInput.setView(view);
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        dialogTitle.setText(!shouldUpdate ? getString(R.string.new_habit_title) : getString(R.string.edit_habit_title));

        final EditText inputHabit = view.findViewById(R.id.note);
        colortext = view.findViewById(R.id.color_tv);
        colorRed = view.findViewById(R.id.colorRed);
        colorOrange = view.findViewById(R.id.colorOrange);
        colorYellow = view.findViewById(R.id.colorYellow);
        colorGreen = view.findViewById(R.id.colorGreen);
        colorBlue = view.findViewById(R.id.colorBlue);

        iconselect = view.findViewById(R.id.icon_select);
        iconselect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showIconDialog();
            }
        });

        colorRed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colortext.setTextColor(Color.RED);
                habit_color[0] ="#b02513";
            }
        });

        colorOrange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colortext.setTextColor(Color.parseColor("#ff7f00"));
                habit_color[0]="#ff7f00";
            }
        });

        colorYellow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colortext.setTextColor(Color.YELLOW);
                habit_color[0]="#ffff00";
            }
        });

        colorGreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colortext.setTextColor(Color.GREEN);
                habit_color[0]="#00ff00";
            }
        });

        colorBlue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colortext.setTextColor(Color.BLUE);
                habit_color[0]="#0000ff";
            }
        });

        if (shouldUpdate && habit != null){
            inputHabit.setText(habit.habit_name);
        }

        alertDialogBuilderUserInput.setCancelable(false);
        alertDialogBuilderUserInput.setPositiveButton(shouldUpdate ? "UPDATE" : "OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        /*
        if(selectedIcon!=null){
            Log.d("## 여기는","안거치나? "+selectedIcon);
            Glide.with(getActivity()).load(selectedIcon).apply(new RequestOptions().centerCrop()).into(iconpreview);
        }
        */
        alertDialog.show();
        Log.d("## 과연 여기는 ##"," selectedIcon "+selectedIcon);
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(inputHabit.getText().toString().isEmpty()){
                    Toast.makeText(getActivity(), "Enter Habit", Toast.LENGTH_SHORT).show();
                    return ;
                }
                else{
                    alertDialog.dismiss();
                }
                if (shouldUpdate && habit != null){
                    updateHabit(inputHabit.getText().toString(), habit_color[0],selectedIcon,position);
                } else {
                    createHabit(inputHabit.getText().toString(), habit_color[0],selectedIcon);
                }
            }
        });
    }

    private void createHabit(String input, String color,String url){
        Log.d("##### 과연 #### ","button clicked");
        final HabitDTO habitDTO = new HabitDTO();
        habitDTO.habit_name = input;
        habitDTO.realtime = System.currentTimeMillis();
        habitDTO.timestamp =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        habitDTO.habit_color = color;
        habitDTO.iconUrl = url;
        firestore.collection("habits").document(habitDTO.habit_name).set(habitDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("#### 과연 #####","성공적으로 추가되었음."+habitDTO.habit_name);
                DegreeDTO degreeDTO = new DegreeDTO();
                firestore.collection("degree").document(habitDTO.habit_name).set(degreeDTO).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(" #######과연 ","여기까지는 절대 안올껄?");
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("##### 과연 ####","제대로 추가안되었음.");
            }
        });
        mAdapter.notifyDataSetChanged();
        toggleEmptyList();
    }

    private void updateHabit(String input, String color, String url, int position){
        HabitDTO h = habit_list.get(position);
        DocumentReference habitRef = firestore.collection("habits").document(h.habit_name);
        habitRef.update("habit_name", input, "habit_color", color, "iconUrl", url).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d("#### 과연 #####", "성공적으로 수정되었음.");
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("#### 과연 ####", "제대로 수정안되었음.");
                    }
                });
        mAdapter.notifyItemChanged(position);
        toggleEmptyList();
    }

    private void deleteHabit(int position){
        HabitDTO h = habit_list.get(position);
        firestore.collection("habits").document(h.habit_name)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("####과연#####", "성공적으로 삭제되었음.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("####과연#####", "제대로 삭제 안 되었음.");
                    }
                });
        mAdapter.notifyItemRemoved(position);
        toggleEmptyList();
    }

    private class GridFragmentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>   {
        ArrayList<IconDTO> images ;
        // String habitIconUrl;
        public GridFragmentRecyclerViewAdapter(){
            firestore.collection("iconUrl").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        images = new ArrayList<>();
                        for(DocumentSnapshot doc : task.getResult()){
                            IconDTO item = doc.toObject(IconDTO.class);
                            images.add(item);
                        }
                        notifyDataSetChanged();
                    }
                }
            }); //아이콘들을 일단 다 담았음.
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            int width = getResources().getDisplayMetrics().widthPixels / 5;
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new LinearLayoutCompat.LayoutParams(width,width));

            return new IconViewholder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            Glide.with(holder.itemView.getContext()).load(images.get(position).imageURL)
                    .apply(new RequestOptions().centerCrop()).into(((IconViewholder)holder).imageView);

            ((IconViewholder)holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selectedIcon = images.get(position).imageURL.toString();
                    Toast.makeText(getContext(),"아이콘이 잘 추가되었습니다. ", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return null!=images?images.size():0;
        }

        class IconViewholder extends RecyclerView.ViewHolder {
            private ImageView imageView;

            public IconViewholder(ImageView imageView2) {
                super(imageView2);
                imageView = imageView2;
            }
        }
    }
    private void toggleEmptyList(){
        if(mAdapter.getItemCount()>0){
            Log.d("### 과연 ###", "리스트 초기화를 위해");
            noHabitView.setVisibility(View.GONE);
        } else{
            noHabitView.setVisibility(View.VISIBLE);
        }
    }
}