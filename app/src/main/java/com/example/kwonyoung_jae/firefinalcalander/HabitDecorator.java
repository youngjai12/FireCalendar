package com.example.kwonyoung_jae.firefinalcalander;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.graphics.drawable.Drawable;
import android.app.Activity;
import android.util.Log;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Date;

public class HabitDecorator implements DayViewDecorator {
    private CalendarDay date;
    private Drawable drawable;

    public HabitDecorator(Context context, CalendarDay day, int colordegree, String color) {
        date = day;
        switch(colordegree) {
            case 100:
                if(color.equals("#b02513")||color.equals("#ff0000")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recred01);
                }
                else if(color.equals("#00ff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recgreen01);
                }
                else if(color.equals("#0000ff")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recblue01);
                }
                else if(color.equals("#ffff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recyellow01);
                }
                else if(color.equals("#ff7f00")) {
                    drawable = ContextCompat.getDrawable(context, R.drawable.recorange01);
                }
                break;
            case 50:
                if(color.equals("#b02513")||color.equals("#ff0000")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recred02);
                }
                else if(color.equals("#00ff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recgreen02);
                }
                else if(color.equals("#0000ff")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recblue02);
                }
                else if(color.equals("#ffff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recyellow02);
                }
                else if(color.equals("#ff7f00")) { //임의
                    drawable = ContextCompat.getDrawable(context, R.drawable.recorange02);
                }
                break;
            case 0:
                if(color.equals("#b02513")||color.equals("#ff0000")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recred03);
                }
                else if(color.equals("#00ff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recgreen03);
                }
                else if(color.equals("#0000ff")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recblue03);
                }
                else if(color.equals("#ffff00")){
                    drawable = ContextCompat.getDrawable(context, R.drawable.recyellow03);
                }
                else if(color.equals("#ff7f00")) { //임의
                    drawable = ContextCompat.getDrawable(context, R.drawable.recorange03);
                }
                break;
        }
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        //view.addSpan(new DotSpan(20, Color.RED));
        view.setSelectionDrawable(drawable);
    }

    /**
     * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
     */
    public void setDate(Date date) {
        this.date = CalendarDay.from(date);
    }
}
