package com.shinao.homemanager;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

import java.io.IOException;

public class WidgetReceiver extends BroadcastReceiver {
    public enum Action
    {
        ActivateAlarm,
        DisableAlarm,
        Nothing
    }

    public static Action alarmAction = Action.Nothing;
    public static boolean disableConnection = true;
    public static boolean unlockDoor = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("HomeManager", "WidgetReceiver -> " + intent.getAction());

        disableConnection = true;

        if(intent.getAction().equals("com.shinao.intent.action.ALARMON")){
            updateButtonListener(context);
            setMobileData(true);
            alarmAction = Action.ActivateAlarm;
        }
        else if(intent.getAction().equals("com.shinao.intent.action.ALARMOFF")){
            updateButtonListener(context);
            setMobileData(true);
            alarmAction = Action.DisableAlarm;
        }
        else if(intent.getAction().equals("com.shinao.intent.action.UNLOCK"))
        {
            updateButtonListener(context);
            BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
            bluetooth.enable();
            unlockDoor = true;
        }
    }

    private void updateButtonListener(Context context) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        //REMEMBER TO ALWAYS REFRESH YOUR BUTTON CLICK LISTENERS!!!
        remoteViews.setOnClickPendingIntent(R.id.widget_button_alarmon, WidgetProvider.buildButtonPendingIntent(context, "com.shinao.intent.action.ALARMON"));
        remoteViews.setOnClickPendingIntent(R.id.widget_button_alarmoff, WidgetProvider.buildButtonPendingIntent(context, "com.shinao.intent.action.ALARMOFF"));
        remoteViews.setOnClickPendingIntent(R.id.widget_button_unlock, WidgetProvider.buildButtonPendingIntent(context, "com.shinao.intent.action.UNLOCK"));

        WidgetProvider.pushWidgetUpdate(context.getApplicationContext(), remoteViews);
    }

    public static void setMobileData(boolean enable) {
        String command = "svc data ";
        command += enable ? "enable" : "disable";
        executeCommandViaSu("-c", command);
    }

    private static void executeCommandViaSu(String option, String command) {
        boolean success = false;
        String su = "su";
        for (int i=0; i < 3; i++) {
            // Default "su" command executed successfully, then quit.
            if (success) {
                break;
            }
            // Else, execute other "su" commands.
            if (i == 1) {
                su = "/system/xbin/su";
            } else if (i == 2) {
                su = "/system/bin/su";
            }
            try {
                // Execute command as "su".
                Runtime.getRuntime().exec(new String[]{su, option, command});
            } catch (IOException e) {
                success = false;
                // Oops! Cannot execute `su` for some reason.
                // Log error here.
            } finally {
                success = true;
            }
        }
    }
}
