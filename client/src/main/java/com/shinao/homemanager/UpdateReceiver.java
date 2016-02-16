package com.shinao.homemanager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.UUID;

public class UpdateReceiver extends BroadcastReceiver {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    private static final String BLUETOOTH_DEVICE_NAME = "LordOfTheLock";
    private static final byte[] TOKEN_REQUESTOR = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXX".getBytes();
    private static final byte[] TOKEN_VALIDATOR = "XXXXXXXXXXXXXXXX".getBytes();
    private static final String URL_ENABLE_CAMERA_ALARM = "http://XXXXX.ddns.net:5225/api/1/XXXXX/enable";
    private static final String DISABLE_CAMERA_ALARM = "http://XXXXX.ddns.net:5225/api/1/XXXXX/disable";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!WidgetReceiver.unlockDoor && WidgetReceiver.alarmAction == WidgetReceiver.Action.Nothing)
            return;

        // Manage unlock door action
        if (WidgetReceiver.unlockDoor) {
            unlockDoor();
            return;
        }

        // Only action linked to mobile data left to manage
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        boolean isConnected = activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();

        // Do something only if we are connected
        if (isConnected) {
            if (WidgetReceiver.alarmAction == WidgetReceiver.Action.ActivateAlarm)
                new RequestTask().execute(URL_ENABLE_CAMERA_ALARM);
            else if (WidgetReceiver.alarmAction == WidgetReceiver.Action.DisableAlarm)
                new RequestTask().execute(DISABLE_CAMERA_ALARM);
        }
    }

    public static final String md5(final byte toHash[]) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance(MD5);
            digest.update(toHash);
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void unlockDoor() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

        try {
            if (bluetooth == null || !bluetooth.isEnabled())
                return;

            Set<BluetoothDevice> bondedDevices = bluetooth.getBondedDevices();

            // Check each bluetooth devices
            if (bondedDevices.size() > 0) {
                for (BluetoothDevice device : bondedDevices) {
                    // Found our arduino bluetooth device and create socket
                    if (device.getName().equals(BLUETOOTH_DEVICE_NAME)) {
                        BluetoothSocket btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);

                        btSocket.connect();
                        OutputStream output = btSocket.getOutputStream();
                        InputStream input = btSocket.getInputStream();

                        output.write(TOKEN_REQUESTOR);

                        // Read token sent from door
                        byte[] token = new byte[32];
                        for (int i = 0; i < 32; ++i)
                            input.read(token, i, 1);

                        // Append our token validator to the token
                        byte[] c = new byte[token.length + TOKEN_VALIDATOR.length];
                        System.arraycopy(token, 0, c, 0, token.length);
                        System.arraycopy(TOKEN_VALIDATOR, 0, c, token.length, TOKEN_VALIDATOR.length);

                        // Generate its md5 and send it
                        String md5 = md5(c);

                        output.write(md5.getBytes(), 0, 32);

                        // Give it time to send it
                        Thread.sleep(1000);
                    }
                }

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        WidgetReceiver.unlockDoor = false;
        bluetooth.disable();
    }

    class RequestTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            try {
                URL url = new URL(uri[0]);

                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10000);
                urlc.connect();
            } catch (MalformedURLException e1) {
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            WidgetReceiver.alarmAction = WidgetReceiver.Action.Nothing;
            if (WidgetReceiver.disableConnection)
            {
                WidgetReceiver.disableConnection = false;
                WidgetReceiver.setMobileData(false);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }
}