package com.yanye.flixcnc.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.activity.ConnectionActivity;
import com.yanye.flixcnc.adapter.DpadAdapter;
import com.yanye.flixcnc.adapter.DpadItemDecoration;
import com.yanye.flixcnc.adapter.StatusBarAdapter;
import com.yanye.flixcnc.adapter.StatusBarItemDecoration;
import com.yanye.flixcnc.handler.ItemTouchListener;
import com.yanye.flixcnc.handler.StreamSubscriber;
import com.yanye.flixcnc.model.DpadItem;
import com.yanye.flixcnc.model.StatusBarItem;
import com.yanye.flixcnc.thread.FlixAgency;
import com.yanye.flixcnc.thread.QueryStateThread;
import com.yanye.flixcnc.utils.Misc;
import com.yanye.flixcnc.view.DragSeekBar;
import com.yanye.flixcnc.view.MessageDialog;
import com.yanye.flixcnc.view.PositionView;
import com.yanye.flixcnc.view.QToast;
import com.yanye.flixcnc.view.SurfacePreView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.yanye.flixcnc.utils.Constant.*;

public class MotionFragment extends Fragment implements View.OnClickListener{

    private RelativeLayout layoutHeader;
    private ScrollView motionScroll;


    private RecyclerView recyclerStatusBar;
    private StatusBarAdapter statusBarAdapter;
    private List<StatusBarItem> statusBarItems;

    private PositionView positionPreview;
    private SurfacePreView surfacePreview;
    private RadioButton rbAccurate, rbManual;
    private SeekBar sbDistance;
    private TextView tvUnit;

    private CheckBox cbImmediate;

    private RecyclerView recyclerControl;
    private DpadAdapter dpadAdapter;
    private List<DpadItem> dpadItems;

    // 主轴启停
    private TextView btnSpindleStart, btnSpindleStop;
    // 主轴速度
    private TextView tvSpindleSpeed;
    private DragSeekBar dragSeekBar;
    // 主轴VSS时间
    private TextView tvVssTime;
    private SeekBar sbVssTime;
    // 主轴方向
    private RadioButton rbDirForward, rbDirBackward;

    private View parentView;

    private MessageDialog actionMsgDialog;
    private QToast toast;

    private static final int REQUEST_CONNECT = 100;

    private FlixAgency flixAgency;
    private StreamSubscriber subscriber;

    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    public MotionFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentView = inflater.inflate(R.layout.fragment_motion, container, false);
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initView();
        initParam();
        // 顶部状态栏
        statusBarAdapter = new StatusBarAdapter(statusBarItems);
        recyclerStatusBar.addItemDecoration(new StatusBarItemDecoration(0, 15));
        statusBarAdapter.setItemClickListener(this::statusBarClickHandler);
        recyclerStatusBar.setAdapter(statusBarAdapter);
        // 雕刻机运动控制十字键
        dpadAdapter = new DpadAdapter(dpadItems);
        dpadAdapter.setItemTouchListener((int position, View view, MotionEvent event) -> {
            dpadTouchHandler(position, view, event);
            return true;
        });
        recyclerControl.setAdapter(dpadAdapter);
        // 双选对话框
        actionMsgDialog.setItemClickListener(this::actionDialogEventHandler);

        rbAccurate.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if(isChecked) {
                if(rbManual.isChecked()) {
                    rbManual.setChecked(false);
                }
            }
        });

        rbManual.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if(isChecked) {
                if(rbAccurate.isChecked()) {
                    rbAccurate.setChecked(false);
                }
            }
        });

        sbDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    tvUnit.setText(String.format(Locale.CHINA,"%dum", (progress * 10)));
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        dragSeekBar.setDragFinishListener(new DragSeekBar.DragFinishListener() {
            @Override
            public void onDragChanged(int lowValue, int highValue) {
                tvSpindleSpeed.setText(String.format(Locale.CHINA, "主轴速度[%d~%d]",
                        (lowValue * 10), (highValue * 10)));
            }
            @Override
            public void onDragFinished(int lowValue, int highValue) {
                flixAgency.setSpindleSpeed((lowValue * 10), (highValue * 10));
            }
        });

        sbVssTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvVssTime.setText(String.format(Locale.CHINA, "VSS时间[%dms]", (progress * 10)));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                flixAgency.setVssTime((seekBar.getProgress() * 10), cbImmediate.isChecked());
            }
        });

        rbDirForward.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if(isChecked) {
                if(rbDirBackward.isChecked()) {
                    rbDirBackward.setChecked(false);
                }
                flixAgency.switchSpindleDir((byte) 0x2);
            }
        });

        rbDirBackward.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            if(isChecked) {
                if(rbDirForward.isChecked()) {
                    rbDirForward.setChecked(false);
                }
                flixAgency.switchSpindleDir((byte) 0x1);
            }
        });

        motionScroll.setOnScrollChangeListener((View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) -> {
            int alpha = (0xFF - scrollY / 5);
            alpha = (alpha < 0x7F) ? 0x7F : alpha;
            layoutHeader.setBackgroundColor(Color.argb(alpha, 116,181,170));
        });

        subscriber = new StreamSubscriber() {
            @Override
            public void onMessageReceived(int type, String message) {
            }
            @Override
            public void onDataReceived(byte[] array) {
                dataReceiveHandler(array);
            }
        };
    }

    private void initView() {
        // 根据状态栏高度设置顶部statusBar高度
        layoutHeader = parentView.findViewById(R.id.layout_motion_header);
        Activity activity = getActivity();
        if(activity != null) {
            ViewGroup.LayoutParams params = layoutHeader.getLayoutParams();
            params.height += Misc.getStatusBarHeight(activity);
            layoutHeader.setLayoutParams(params);
        }

        motionScroll = parentView.findViewById(R.id.motion_scroll);
        recyclerStatusBar = parentView.findViewById(R.id.recycler_status_bar);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerStatusBar.setLayoutManager(manager);

        positionPreview = parentView.findViewById(R.id.position_preview);
        surfacePreview = parentView.findViewById(R.id.surface_preview);
        rbAccurate = parentView.findViewById(R.id.rb_accurate);
        rbManual = parentView.findViewById(R.id.rb_manual);
        sbDistance = parentView.findViewById(R.id.sb_distance);
        tvUnit = parentView.findViewById(R.id.tv_unit);
        recyclerControl = parentView.findViewById(R.id.recycler_controllers);

        // dpad键盘recycler禁止垂直滑动
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 4) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        recyclerControl.setLayoutManager(gridLayoutManager);
        recyclerControl.addItemDecoration(new DpadItemDecoration(40, 4));

        btnSpindleStart = parentView.findViewById(R.id.btn_spindle_start);
        btnSpindleStop = parentView.findViewById(R.id.btn_spindle_stop);
        btnSpindleStart.setOnClickListener(this);
        btnSpindleStop.setOnClickListener(this);

        tvSpindleSpeed = parentView.findViewById(R.id.tv_spindle_speed);
        dragSeekBar = parentView.findViewById(R.id.drag_spindle_speed);

        tvVssTime = parentView.findViewById(R.id.tv_vss_time);
        sbVssTime = parentView.findViewById(R.id.sb_vss_time);
        cbImmediate = parentView.findViewById(R.id.cb_immediate);

        rbDirForward = parentView.findViewById(R.id.rb_dir_forward);
        rbDirBackward = parentView.findViewById(R.id.rb_dir_backward);

        actionMsgDialog = new MessageDialog(getActivity(), MessageDialog.DOUBLE_BUTTON);
        toast = new QToast(getActivity().getApplicationContext());
    }

    private static final int[] ImageIds = new int[]{R.mipmap.ic_title_wifi, R.mipmap.ic_title_axis,
            R.mipmap.ic_title_spindle, R.mipmap.ic_title_task};
    private static final String[] TitleStrs = new String[]{"网络", "运动", "主轴", "任务"};
    private static final int[] Paddings = new int[]{0, 3, 1, 2};

    private static final int[] ControlImages = new int[]{R.mipmap.ic_dpad_topleft, R.mipmap.ic_dpad_up,
            R.mipmap.ic_dpad_topright, R.mipmap.ic_dpad_zup, R.mipmap.ic_dpad_left, R.mipmap.ic_dpad_mark,
            R.mipmap.ic_dpad_right, R.mipmap.ic_dpad_home, R.mipmap.ic_dpad_bottomleft, R.mipmap.ic_dpad_down,
            R.mipmap.ic_dpad_bottomright, R.mipmap.ic_dpad_zdown};
    // dpad轴使能能映射表
    private static final byte[] AxisTags = new byte[]{0x3, 0x2, 0x3, 0x4, 0x1, 0x0, 0x1, 0x0, 0x3, 0x2, 0x3, 0x4};
    // dpad轴方向映射表
    private static final byte[] DirectionTags = new byte[]{0x3, 0x2, 0x2, 0x4, 0x1, 0x0, 0x0, 0x0, 0x1, 0x0, 0x0, 0x0};
    private static final int MOTION_GROUP = 1;

    private void initParam() {
        // 状态栏数据源
        statusBarItems = new ArrayList<>(TitleStrs.length);
        for(int i = 0; i < TitleStrs.length; i++) {
            statusBarItems.add(new StatusBarItem(ImageIds[i], TitleStrs[i], Paddings[i]));
        }
        // 按钮数据源
        dpadItems = new ArrayList<>(ControlImages.length);
        for(int i = 0; i < ControlImages.length; i++) {
            dpadItems.add(new DpadItem(ControlImages[i], ((i == 5 || i == 7) ? 0 : MOTION_GROUP),
                    AxisTags[i], DirectionTags[i]));
        }
        // 网络连接实例化
        flixAgency = FlixAgency.getInstance();
    }

    private Handler handler = new Handler((Message msg) -> {
        if(msg.what == 100) {
            // 主轴方向位
            rbDirForward.setChecked(((msg.arg2 & 0x1) == 0));
            rbDirBackward.setChecked((((msg.arg2 >> 1) & 0x1) == 0));

            Bundle bundle = (Bundle)msg.obj;
            dragSeekBar.setProgressValue((bundle.getInt("dutyMin") / 10), (bundle.getInt("dutyMax") / 10));
            sbVssTime.setProgress(bundle.getInt("vssTime") / 10);
            tvSpindleSpeed.setText(String.format(Locale.CHINA, "主轴速度[%d~%d]",
                    bundle.getInt("dutyMin"), bundle.getInt("dutyMax")));
        }
        return true;
    });

    private void statusBarClickHandler(int position, int arg0) {
        if(position == 0) {
            if(flixAgency.getSystemState(1)) {
                if(actionMsgDialog.getTag() != CONNECTION_TAG) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("雕刻机名称: ");
                    builder.append(flixAgency.getMachineName()).append("\n");
                    builder.append("硬件版本: ");
                    builder.append(String.valueOf(flixAgency.getHardwareVersion())).append("\n");
                    builder.append("软件版本: ");
                    builder.append(String.valueOf(flixAgency.getSoftwareVersion()));
                    actionMsgDialog.setDialogTitle("设备信息");
                    actionMsgDialog.setDialogMessage(builder.toString());
                    actionMsgDialog.setButtonText("断开", "返回");
                    actionMsgDialog.setTag(CONNECTION_TAG);
                }
                actionMsgDialog.show();
            }else {
                startActivityForResult(new Intent(getActivity(), ConnectionActivity.class), REQUEST_CONNECT);
            }
        }
    }

    private void dpadTouchHandler(int position, View view, MotionEvent event) {
        DpadItem item = dpadItems.get(position);
        if(item.getGroup() == MOTION_GROUP) {
            if(rbManual.isChecked()) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        flixAgency.startLongMotion(item.getAxisId(), item.getAxisDir());
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        flixAgency.stopLongMotion(item.getAxisId());
                        break;
                    default:
                        break;
                }
            }else {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    int progress = sbDistance.getProgress();
                    if(progress <= 0) {
                        toast.showMessage("步进距离不能为0", QToast.WARNING);
                        return;
                    }
                    float distance = (progress * 10.0F / 1.25F);
                    int realDistance = Math.round(distance);
                    flixAgency.jogMotion(item.getAxisId(), item.getAxisDir(), realDistance);
                }
            }
        }else {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                if(position == 7) {
                    if(!flixAgency.getSystemState(FlixAgency.ACK_BIT)) {
                        return;
                    }
                    if(actionMsgDialog.getTag() != HOMING_TAG) {
                        StringBuilder builder = new StringBuilder();
                        builder.append("机器原点: 回到机器初始位置，触发各轴0限位停止。").append("\n");
                        builder.append("工作原点: 移至用户设定位置，要求较高精度可先回机器原点。");
                        actionMsgDialog.setDialogTitle("回原点操作选择");
                        actionMsgDialog.setDialogMessage(builder.toString());
                        actionMsgDialog.setButtonText("工作原点", "机器原点");
                        actionMsgDialog.setTag(HOMING_TAG);
                    }
                    actionMsgDialog.show();
                }else if(position == 5) {
                    flixAgency.setWorkHome();
                }
            }
        }
    }

    private void actionDialogEventHandler(int position, int arg0) {
        switch(arg0) {
            case CONNECTION_TAG:
                if(MessageDialog.BUTTON_POSITIVE == position) {
                    // 断开雕刻机连接
                    flixAgency.sendNAck(false);
                    flixAgency.closeConnection();
                    updateConnViewState(false);
                }
                break;
            case HOMING_TAG:
                if(MessageDialog.BUTTON_POSITIVE == position) {
                    flixAgency.goWorkHome();
                }else {
                    flixAgency.goMachineHome();
                }
                break;
            default:
                break;
        }
    }

    private void dataReceiveHandler(byte[] array) {
        int dataTag = array[0];
        int[] nsteps = new int[3];
        byte[] tempArray = new byte[4];

        final int[] offsetFinish = new int[]{3, 7, 11};
        final int[] offsetProgress = new int[]{1, 5, 9};
        final int[] offsetMachine = new int[]{2, 6, 10, 14, 18, 22};

        switch (dataTag) {
            case QUERY_MACHINE_STATE_RESULT:
            case SET_WORKHOME_RESULT: {
                for (int i = 0; i < offsetMachine.length; i++) {
                    System.arraycopy(array, offsetMachine[i], tempArray, 0, 4);
                    nsteps[((i < 3) ? i : (i - 3))] = Misc.bytes2Int(tempArray);
                    if (i == 2) {
                        positionPreview.setRealPosition(nsteps[0], nsteps[1], nsteps[2]);
                        surfacePreview.setCurrentPosition(nsteps[0], nsteps[1], nsteps[2]);
                    }
                }
                surfacePreview.setWorkHome(nsteps[0], nsteps[1], nsteps[2]);
                break;
            }
            case QUERY_SPINDLE_STATE_RESULT: {
                Message message = handler.obtainMessage();
                message.what = 100;
                // 主轴状态位, 方向位
                message.arg1 = array[1];
                message.arg2 = array[8];
                Bundle bundle = new Bundle();

                System.arraycopy(array, 2, tempArray, 0, 2);

                bundle.putInt("dutyMin", Misc.bytes2Short(tempArray));
                System.arraycopy(array, 4, tempArray, 0, 2);
                bundle.putInt("dutyMax", Misc.bytes2Short(tempArray));
                System.arraycopy(array, 6, tempArray, 0, 2);
                bundle.putInt("vssTime", Misc.bytes2Short(tempArray));
                message.obj = bundle;
                handler.sendMessage(message);
                break;
            }
            case MOTION_PROGRESS_RESULT:
                for (int i = 0; i < offsetProgress.length; i++) {
                    System.arraycopy(array, offsetProgress[i], tempArray, 0, 4);
                    nsteps[i] = Misc.bytes2Int(tempArray);
                }
                positionPreview.setRealPosition(nsteps[0], nsteps[1], nsteps[2]);
                surfacePreview.setCurrentPosition(nsteps[0], nsteps[1], nsteps[2]);
                break;
            case MOTION_FINISHED_RESULT:
                for (int i = 0; i < offsetFinish.length; i++) {
                    System.arraycopy(array, offsetFinish[i], tempArray, 0, 4);
                    nsteps[i] = Misc.bytes2Int(tempArray);
                }
                positionPreview.setRealPosition(nsteps[0], nsteps[1], nsteps[2]);
                surfacePreview.setCurrentPosition(nsteps[0], nsteps[1], nsteps[2]);
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_spindle_start) {
            flixAgency.openSpindle(true);
        }else if(view.getId() == R.id.btn_spindle_stop) {
            flixAgency.closeSpindle(true);
        }
    }

    /**
     * 雕刻机连接状态改变视图刷新
     * @param isConnected 是否连接雕刻机，socket连接且ack确认
     * */
    private void updateConnViewState(boolean isConnected) {
        if(isConnected) {
            statusBarItems.get(0).setColor(0xFF1d953f);
            surfacePreview.updateConnState(SurfacePreView.STATE_CONNECTED);
        }else {
            statusBarItems.get(0).setColor(Color.WHITE);
            surfacePreview.updateConnState(SurfacePreView.STATE_NO_CONNECT);
        }
        statusBarAdapter.notifyItemChanged(0);
    }

    @Override
    public void onPause() {
        flixAgency.unregisterSubscriber(subscriber);
        super.onPause();
    }

    @Override
    public void onResume() {
        boolean result = flixAgency.registerSubscriber(subscriber);
        if(!result && getActivity() != null) {
            toast.showMessage(getActivity().getResources().getString(R.string.subscriber_register_fail), QToast.WARNING);
        }
        super.onResume();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if(hidden) {
            flixAgency.unregisterSubscriber(subscriber);
        }else {
            boolean result = flixAgency.registerSubscriber(subscriber);
            if(!result && getActivity() != null) {
                toast.showMessage(getActivity().getResources().getString(R.string.subscriber_register_fail), QToast.WARNING);
            }
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        surfacePreview.releaseResource();
        flixAgency.unregisterSubscriber(subscriber);
        flixAgency.sendNAck(false);
        flixAgency.closeConnection();
        fixedThreadPool.shutdown();
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(REQUEST_CONNECT == requestCode && data != null) {
            // 更新界面
            updateConnViewState(data.getBooleanExtra("state", false));
            // 查询雕刻机状态
            fixedThreadPool.execute(new QueryStateThread(2));
        }
    }
}
