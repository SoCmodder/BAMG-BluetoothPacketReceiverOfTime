package org.brogrammer.packetreceiveroftime;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    BluetoothLeScanner leScanner;
    BluetoothDevice bluetoothDevice;

    UUID uotUUID = UUID.fromString("8F2A9690-FD82-4AA0-953E-79EF126BA95D");
    UUID dataUUID = UUID.fromString("00009690-0000-1000-8000-00805f9b34fb");

    boolean foundDevice = false;

    @InjectView(R.id.devices_textview) TextView devicesText;
    @InjectView(R.id.data_textview) TextView dataText;
    @InjectView(R.id.status_textview) TextView statusText;
    @InjectView(R.id.song_imageview) ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        enableBluetooth();

        leScanner = btAdapter.getBluetoothLeScanner();
        leScanner.flushPendingScanResults(leScanCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<ScanFilter> filters = new ArrayList<>();
        ScanFilter filter = new ScanFilter.Builder()
                .setServiceUuid(new ParcelUuid(uotUUID))
                .build();
        filters.add(filter);
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setReportDelay(0)
                .build();
        leScanner.startScan(filters, settings, leScanCallback);
        Log.d(TAG, "Startting LE Scanner");
        setStatus("Starting LE Scanner");
    }

    @Override
    protected void onStop() {
        super.onStop();
        leScanner.stopScan(leScanCallback);
        Log.d(TAG, "Stopping LE Scanner");
    }

    public void enableBluetooth(){
        setStatus("Checking Bluetooth State");
        btManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        btAdapter = btManager.getAdapter();
        if(btAdapter != null && !btAdapter.isEnabled()){
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 0);
        }
    }

    ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "onScanResult: Device Address: " + result.getDevice().getAddress());
            if(result.getScanRecord()!=null) {
                Map<ParcelUuid, byte[]> data = result.getScanRecord().getServiceData();
                byte[] actualData = data.get(new ParcelUuid(dataUUID));
                checkSong(actualData);
            }
            String deviceName = result.getDevice().getAddress();
            devicesText.setText(deviceName);
            bluetoothDevice = result.getDevice();
            foundDevice = true;
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Scan Failed: " + errorCode);
        }
    };

    public void setStatus(String status){
        statusText.setText("Status: " + status);
    }

    public void checkSong(final byte[] data){

        byte first = data[0];
        byte second = data[1];
        byte third = data[2];

        final String b1 = String.format("%8s", Integer.toBinaryString(first)).replace(' ', '0').substring(0, 4);
        final String b2 = String.format("%8s", Integer.toBinaryString(first)).replace(' ', '0').substring(4);
        final String b3 = String.format("%8s", Integer.toBinaryString(second)).replace(' ', '0').substring(0, 4);
        final String b4 = String.format("%8s", Integer.toBinaryString(second)).replace(' ', '0').substring(4);
        final String b5 = String.format("%8s", Integer.toBinaryString(third)).replace(' ', '0').substring(0, 4);
        final String b6 = String.format("%8s", Integer.toBinaryString(third)).replace(' ', '0').substring(4);



        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataText.setText(buttons(b1, b2, b3, b4, b5, b6));
                image.setImageResource(getSongImage(b1+b2+b3));
            }
        });
    }

    public String buttons(String one, String two, String three, String four, String five, String six){
        String buttonString = "";
        buttonString += convertButtonToString(one);
        buttonString += convertButtonToString(two);
        buttonString += convertButtonToString(three);
        buttonString += convertButtonToString(four);
        buttonString += convertButtonToString(five);
        buttonString += convertButtonToString(six);

        return buttonString;
    }

    public String convertButtonToString(String buttonCode){
        String button = "";
        switch (buttonCode){
            case "0001":
                button = "A ";
                break;
            case "0010":
                button = "C Down ";
                break;
            case "0011":
                button = "C Right ";
                break;
            case "0100":
                button = "C Up ";
                break;
            case "0101":
                button = "C Left ";
                break;
        }
        return button;
    }

    public int getSongImage(String song){
        int songId = 0;
        switch (song){
            case "010101000011":
                songId = R.mipmap.zeldas_lullaby;
                break;
            case "010001010011":
                songId = R.mipmap.eponas_song;
                break;
            case "001000110101":
                songId = R.mipmap.sarias_song;
                break;
            case "001100100100":
                songId = R.mipmap.suns_song;
                break;
            case "001100010010":
                songId = R.mipmap.song_of_time;
                break;
            case "000100100100":
                songId = R.mipmap.song_of_storms;
                break;
            case "001000010010":
                songId = R.mipmap.bolero_of_fire;
                break;
            case "000101000101":
                songId = R.mipmap.minuet_of_forest;
                break;
            case "000100100011":
                songId = R.mipmap.serenade_of_water;
                break;
            case "000100100001":
                songId = R.mipmap.requiem_of_spirit;
                break;
            case "010100110011":
                songId = R.mipmap.nocturne_of_shadow;
                break;
            case "010000110100":
                songId = R.mipmap.prelude_of_light;
                break;
        }
        return songId;
    }
}
