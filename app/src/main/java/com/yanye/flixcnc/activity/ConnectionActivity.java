package com.yanye.flixcnc.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.widget.EditText;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.handler.StreamSubscriber;
import com.yanye.flixcnc.model.ConnectionInfo;
import com.yanye.flixcnc.thread.FlixAgency;
import com.yanye.flixcnc.utils.Constant;
import com.yanye.flixcnc.view.LoadingDialog;
import com.yanye.flixcnc.view.MessageDialog;
import com.yanye.flixcnc.view.QToast;
import com.yanye.zxing.android.CaptureActivity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionActivity extends AppCompatActivity {

    private EditText edDevIp, edDevPort;

    private QToast toast;
    private LoadingDialog loadingDialog;
    private MessageDialog messageDialog;

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final int REQUEST_PERMISSION_CODE = 100;
    private static final int REQUEST_SCAN_CODE = 101;

    private ConnectionInfo connectionInfo;
    private FlixAgency flixAgency;
    private StreamSubscriber subscriber;

    private Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        initView();
        initParam();

        findViewById(R.id.iv_conn_back).setOnClickListener((View view) -> finish());

        findViewById(R.id.iv_scan_code).setOnClickListener((View view) -> {
            if(checkPermissions()) {
                startActivityForResult(new Intent(ConnectionActivity.this, CaptureActivity.class), REQUEST_SCAN_CODE);
            }else {
                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(ConnectionActivity.this, PERMISSIONS, REQUEST_PERMISSION_CODE);
                }
            }
        });

        findViewById(R.id.btn_conn).setOnClickListener((View view) -> doConnect());

        subscriber = new StreamSubscriber() {
            @Override
            public void onMessageReceived(int type, String message) {
                // 稍稍延时
                handler.postDelayed(() -> {
                    if(type == 200) {
                        flixAgency.sendAck(Constant.CONTROLLER_NAME, false);
                        handler.sendEmptyMessage(50);
                        timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                timer.cancel();
                                timer = null;
                                if(flixAgency.getSystemState(FlixAgency.ACK_BIT)) {
                                    return;
                                }
                                Message msg = handler.obtainMessage();
                                msg.what = 100;
                                msg.obj = "通信验证超时\n请检查雕刻机是否正常启动";
                                handler.sendMessage(msg);
                            }
                        }, 3000);
                    }else {
                        Message msg = handler.obtainMessage();
                        msg.what = type;
                        msg.obj = message;
                        handler.sendMessage(msg);
                    }
                }, 300);
            }
            @Override
            public void onDataReceived(byte[] array) {
                if(array[0] == 0x00) {
                    flixAgency.updateSystemState(FlixAgency.ACK_BIT, true);
                    flixAgency.setMachineVersion(array[1], array[2]);

                    StringBuilder builder = new StringBuilder(17);
                    for(int i = 3; i < array.length; i++) {
                        if(array[i] == 0x00) break;
                        builder.append((char)array[i]);
                    }
                    flixAgency.setMachineName(builder.toString());
                    builder.setLength(0);
                    // 通信成功
                    handler.postDelayed(() -> handler.sendEmptyMessage(200), 300);
                }
            }
        };

        boolean result = flixAgency.registerSubscriber(subscriber);
        if(!result) {
            toast.showMessage( getResources().getString(R.string.subscriber_register_fail), QToast.WARNING);
        }
    }

    private void initView() {
        edDevIp = findViewById(R.id.ed_dev_ip);
        edDevPort = findViewById(R.id.ed_dev_port);

        final String digitFilter = "0123456789.";
        edDevIp.setInputType(InputType.TYPE_CLASS_NUMBER);
        edDevIp.setKeyListener(DigitsKeyListener.getInstance(digitFilter));

        toast = new QToast(ConnectionActivity.this);
    }

    private void initParam() {
        SharedPreferences preferences = getSharedPreferences("app_env", Context.MODE_PRIVATE);
        if(preferences.getBoolean("remember",false)) {
            String ip = preferences.getString("ip",null);
            int port = preferences.getInt("port",0);
            autoFillForm(ip, String.valueOf(port));
        }
        flixAgency = FlixAgency.getInstance();
    }

    private Handler handler = new Handler((Message msg) -> {
        if(msg.what == 50) {
            loadingDialog.updateMessage("通信中...");
            return true;
        }

        if(loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        if(msg.what == 100) {
            flixAgency.closeConnection();
            if(messageDialog == null) {
                messageDialog = new MessageDialog(ConnectionActivity.this, MessageDialog.SINGLE_BUTTON);
                messageDialog.setDialogTitle("连接失败");
            }
            messageDialog.setDialogMessage((String)msg.obj);
            messageDialog.show();

        }else if(msg.what == 200) {
            toast.showMessage("连接成功", QToast.SUCCESS);
            updatePreferences();
            Intent intent = new Intent();
            intent.putExtra("state", true);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
        return true;
    });

    private void doConnect() {
        connectionInfo = checkInputs();
        if(connectionInfo != null) {
            if(loadingDialog == null) {
                loadingDialog = new LoadingDialog(this);
            }
            loadingDialog.showMessage("连接中...");
            flixAgency.openConnection(connectionInfo.getIp(), connectionInfo.getPort());
        }
    }

    private ConnectionInfo checkInputs() {
        final String[] hints = new String[]{"请输入IP地址", "请输入端口"};
        String[] inputs = new String[2];
        inputs[0] = edDevIp.getText().toString();
        inputs[1] = edDevPort.getText().toString();
        // 检查输入
        for(int i = 0; i < inputs.length; i++) {
            if(inputs[i].isEmpty()) {
                toast.showMessage(hints[i]);
                return null;
            }
        }
        // IP格式
        if(!isIpValid(inputs[0])) {
            toast.showMessage("IP格式不正确");
            return null;
        }
        // 端口范围
        int portNum;
        try {
            portNum = Integer.parseInt(inputs[1]);
        }catch (NumberFormatException e) {
            toast.showMessage("端口格式不正确");
            return null;
        }
        if(portNum < 1 || portNum > 65535) {
            toast.showMessage("端口范围1~65535");
            return null;
        }
        return new ConnectionInfo(inputs[0], portNum);
    }

    private boolean isIpValid(String ip) {
        final String regex = "([1-9]|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])(\\.(\\d|[1-9]\\d|1\\d{2}|2[0-4]\\d|25[0-5])){3}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(ip);
        return matcher.find();
    }

    private void autoFillForm(String ip, String port) {
        edDevIp.setText(ip);
        edDevPort.setText(port);
        if(ip != null && ip.length() > 0) {
            edDevIp.setSelection(ip.length() - 1);
        }
        if(port != null && port.length() > 0) {
            edDevIp.setSelection(port.length() - 1);
        }
    }

    private void updatePreferences() {
        SharedPreferences preferences = getSharedPreferences("app_env", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("remember", true);
        editor.putString("ip", connectionInfo.getIp());
        editor.putInt("port", connectionInfo.getPort());
        editor.apply();
    }

    private boolean checkPermissions() {
        for(String per : PERMISSIONS) {
            if(ContextCompat.checkSelfPermission(this, per) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CODE) {
            for(int grant : grantResults) {
                if(grant != PackageManager.PERMISSION_GRANTED) {
                    toast.showMessage("请先授予相机权限", QToast.WARNING);
                    return;
                }
            }
            startActivityForResult(new Intent(ConnectionActivity.this, CaptureActivity.class),
                    REQUEST_SCAN_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == REQUEST_SCAN_CODE && data != null) {
            String content = data.getStringExtra("result");
            String[] array = content.split(":");
            if(array.length != 2) {
                toast.showMessage("二维码不正确");
                return;
            }
            autoFillForm(array[0], array[1]);
            doConnect();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        flixAgency.unregisterSubscriber(subscriber);
        subscriber = null;
        flixAgency = null;
        handler = null;
        toast = null;
        loadingDialog = null;
        messageDialog = null;
        connectionInfo = null;
        if(timer != null) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }
}
