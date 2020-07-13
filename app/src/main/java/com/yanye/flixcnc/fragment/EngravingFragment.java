package com.yanye.flixcnc.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.activity.FileEditActivity;
import com.yanye.flixcnc.activity.FilePickerActivity;
import com.yanye.flixcnc.adapter.ControlAdapter;
import com.yanye.flixcnc.handler.StreamSubscriber;
import com.yanye.flixcnc.model.ControlItem;
import com.yanye.flixcnc.thread.FlixAgency;
import com.yanye.flixcnc.utils.CRC16CCITT;
import com.yanye.flixcnc.utils.Misc;
import com.yanye.flixcnc.view.LoadingDialog;
import com.yanye.flixcnc.view.QToast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EngravingFragment extends Fragment implements View.OnClickListener {

    private EditText edFilePath;

    private LinearLayout layoutHolder;

    private android.support.v7.widget.RecyclerView recyclerControl;
    private List<ControlItem> dataSet;
    private ControlAdapter controlAdapter;

    private View parentView;

    private static final int REQUEST_PICK_FILE = 100;
    private static final int REQUEST_PERMISSION_CODE = 101;
    private static final int REQUEST_EDIT_FILE = 102;
    private QToast toast;

    private FlixAgency flixAgency;
    private StreamSubscriber subscriber;

    private byte[] byteArray;
    private int transmitOffset;

    private LoadingDialog dialog;

    private static final String[] FILE_MESSAGES = new String[]{"文件不完整", "CRC校验失败", "上传成功"};

    public EngravingFragment() {
    }

    @Override
    public View onCreateView(@NonNull  LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_engraving, container, false);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initParam();

        controlAdapter.setItemClickListener(this::optionHandler);

        subscriber = new StreamSubscriber() {
            @Override
            public void onMessageReceived(int type, String message) {

            }
            @Override
            public void onDataReceived(byte[] array) {

                if(array[0] == 0x50) {
                    if(array[1] == 0x00) {
                        int transmitSize = byteArray.length - transmitOffset;
                        transmitSize = (transmitSize < 0x1E) ? transmitSize : 0x1E;
                        flixAgency.sendFile(byteArray, transmitOffset, transmitSize);
                        transmitOffset += transmitSize;
                    }else {
                        handler.sendEmptyMessage(100);
                    }
                }else if( array[0] == 0x51) {
                    if(array[1] == 0x00) {
                        int transmitSize = byteArray.length - transmitOffset;
                        transmitSize = (transmitSize < 0x1E) ? transmitSize : 0x1E;
                        flixAgency.sendFile(byteArray, transmitOffset, transmitSize);
                        transmitOffset += transmitSize;
                    }else {
                        Message message = handler.obtainMessage();
                        message.what = 101;
                        message.arg1 = (array[1] - 1);
                        handler.sendMessage(message);
                    }
                }else if(array[0] == 0x52 && array[1] == 0x00) {
                    flixAgency.sendRunGcode();
                }
            }
        };

        boolean state = flixAgency.registerSubscriber(subscriber);
        if(!state) {
            toast.showMessage("注册接口失败");
        }
    }

    private void initView() {
        RelativeLayout layoutEngravingHeader = parentView.findViewById(R.id.layout_engraving_header);
        // 根据状态栏高度设置顶部statusBar高度
        Activity activity = getActivity();
        if(activity != null) {
            ViewGroup.LayoutParams params = layoutEngravingHeader.getLayoutParams();
            params.height += Misc.getStatusBarHeight(activity);
            layoutEngravingHeader.setLayoutParams(params);
        }

        edFilePath = parentView.findViewById(R.id.ed_file_path);
        edFilePath.setKeyListener(null);

        TextView tvSelect = parentView.findViewById(R.id.tv_select);
        tvSelect.setOnClickListener(this);

        layoutHolder = parentView.findViewById(R.id.layout_holder);

        recyclerControl = parentView.findViewById(R.id.recycler_control);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerControl.setLayoutManager(manager);

        toast = new QToast(getActivity());
        dialog = new LoadingDialog(getActivity());
    }

    private void initParam() {
        dataSet = new ArrayList<>(16);
        controlAdapter = new ControlAdapter(dataSet);
        recyclerControl.setAdapter(controlAdapter);
        flixAgency = FlixAgency.getInstance();
    }

    private Handler handler = new Handler((Message msg) -> {
        switch (msg.what) {
            case 100:
                dialog.dismiss();
                toast.showMessage("雕刻机分配内存失败");
                break;
            case 101:
                dialog.dismiss();
                toast.showMessage(FILE_MESSAGES[msg.arg1]);
                break;
            default:
                break;
        }
        return true;
    });

    private void optionHandler(int position, int arg0) {
        if(position < 3) {
            return;
        }else if(position == 3) {
            Intent intent = new Intent(getActivity(), FileEditActivity.class);
            intent.putExtra("file_path", edFilePath.getText().toString());
            startActivityForResult(intent, REQUEST_EDIT_FILE);
            return;
        }else if(position == (dataSet.size() - 1)) {
            edFilePath.setText("");
            dataSet.clear();
            controlAdapter.notifyItemRangeRemoved(0, dataSet.size());
            layoutHolder.setVisibility(View.VISIBLE);
            recyclerControl.setVisibility(View.GONE);
            return;
        }

        if(!flixAgency.getSystemState(FlixAgency.ACK_BIT)) {
            toast.showMessage("请先连接雕刻机");
            return;
        }

        switch (position) {
            case 4:
                dialog.showMessage("上传中...");
                handler.postDelayed(() -> {
                    String filePath = edFilePath.getText().toString();
                    parseFile(filePath);
                }, 300);
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                flixAgency.sendParseGcode();
                break;
            default:
                break;
        }
    }

    private void updateView(String filePath, long size) {

        edFilePath.setText(filePath);
        layoutHolder.setVisibility(View.GONE);
        recyclerControl.setVisibility(View.VISIBLE);

        if(dataSet.size() > 0) {
            dataSet.clear();
        }

        dataSet.add(new ControlItem("文件名", Misc.trimFileName(filePath), R.mipmap.ic_ctrl_filename));
        dataSet.add(new ControlItem("文件大小", Misc.generateReadableFileSize(size), R.mipmap.ic_ctrl_filesize));
        dataSet.add(new ControlItem("文件路径", filePath, R.mipmap.ic_ctrl_folder));

        dataSet.add(new ControlItem("编辑", "加工前手动修改/确认文件", R.mipmap.ic_ctrl_edit, 0xFF0091EA));
        dataSet.add(new ControlItem("上传", "上传文件至雕刻机", R.mipmap.ic_ctrl_upload, 0xFF0091EA));
        dataSet.add(new ControlItem("雕刻机配置", "配置雕刻机/主轴运行时参数", R.mipmap.ic_ctrl_config, 0xFF0091EA));
        dataSet.add(new ControlItem("回原点操作", "重定位至机器/工作区原点", R.mipmap.ic_ctrl_homing, 0xFF0091EA));
        dataSet.add(new ControlItem("开始加工", "...", R.mipmap.ic_ctrl_start, 0xFF0091EA));
        dataSet.add(new ControlItem("返回", "关闭当前文件, 回到初始页", R.mipmap.ic_ctrl_back, 0xFF3CB371));

        controlAdapter.notifyDataSetChanged();
    }

    private void parseFile(String filePath) {
        File file = new File(filePath);
        if(!file.exists()) {
            return;
        }
        transmitOffset = 0;
        byteArray = new byte[(int)file.length()];

        FileInputStream inputStream;
        try {
            int data;
            int i = 0;
            inputStream = new FileInputStream(file);
            while((data = inputStream.read()) != -1) {
                byteArray[i] = (byte)(data & 0xFF);
                i++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int crc16 = CRC16CCITT.calcCRC16(byteArray, byteArray.length);
        flixAgency.sendFileInfo(byteArray.length, crc16, null);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_select:
                if(getActivity() == null) {
                    toast.showMessage("getActivity异常");
                    break;
                }
                if(PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    startActivityForResult(new Intent(getActivity(), FilePickerActivity.class), REQUEST_PICK_FILE);
                }else if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION_CODE);
                }
                break;
            default:
                break;
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_PERMISSION_CODE) {
            for(int grant : grantResults) {
                if(grant != PackageManager.PERMISSION_GRANTED) {
                    toast.showMessage("请先授予存储权限");
                    return;
                }
            }
            startActivityForResult(new Intent(getActivity(), FilePickerActivity.class), REQUEST_PICK_FILE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((Activity.RESULT_OK != resultCode) || (data == null)) {
            return;
        }
        if(REQUEST_PICK_FILE == requestCode) {
            String filePath = data.getStringExtra("file");
            long size = data.getLongExtra("size", 0);
            updateView(filePath, size);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden) {
            flixAgency.unregisterSubscriber(subscriber);
        }else {
            flixAgency.registerSubscriber(subscriber);
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
