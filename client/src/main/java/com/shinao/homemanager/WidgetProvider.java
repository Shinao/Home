package com.shinao.homemanager;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RemoteViews;

public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        remoteViews.setOnClickPendingIntent(R.id.widget_button_alarmon, buildButtonPendingIntent(context, "com.shinao.intent.action.ALARMON" ));
        remoteViews.setOnClickPendingIntent(R.id.widget_button_alarmoff, buildButtonPendingIntent(context, "com.shinao.intent.action.ALARMOFF"));
        remoteViews.setOnClickPendingIntent(R.id.widget_button_unlock, buildButtonPendingIntent(context, "com.shinao.intent.action.UNLOCK"));

        pushWidgetUpdate(context, remoteViews);
    }

    public static PendingIntent buildButtonPendingIntent(Context context, String str_intent) {
        Intent intent = new Intent();
        intent.setAction(str_intent);
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void pushWidgetUpdate(Context context, RemoteViews remoteViews) {
        ComponentName myWidget = new ComponentName(context, WidgetProvider.class);
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(myWidget, remoteViews);
    }
}