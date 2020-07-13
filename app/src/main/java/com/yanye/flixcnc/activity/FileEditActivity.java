package com.yanye.flixcnc.activity;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.adapter.ToolBarAdapter;
import com.yanye.flixcnc.adapter.ToolBarItemDecoration;
import com.yanye.flixcnc.thread.LoadFileThread;
import com.yanye.flixcnc.view.LoadingDialog;
import com.yanye.flixcnc.view.QToast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileEditActivity extends AppCompatActivity {

    private TextView tvFilename;

    private RecyclerView recyclerToolsBar;
    private ToolBarAdapter toolBarAdapter;
    private List<String> dataSet;

    private EditText edFileContent;

    private QToast toast;
    private LoadingDialog loadingDialog;

    private File mFile;
    private StringBuilder stringBuilder;
    private ExecutorService fixedThreadPool = Executors.newFixedThreadPool(1);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_edit);

        Intent intent = getIntent();
        String filePath = intent.getStringExtra("file_path");
        initView();
        initParam(filePath);

        findViewById(R.id.iv_edit_back).setOnClickListener((View view) -> finish());

        findViewById(R.id.iv_edit_save).setOnClickListener((View view) -> {
            if(loadingDialog == null) {
                loadingDialog = new LoadingDialog(this);
            }
            loadingDialog.showMessage("保存中...");
            doSave();
        });

        toolBarAdapter.setItemClickListener((int position, int arg0) -> {

        });
    }

    private Handler handler = new Handler((Message msg) -> {
        if(loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
        switch (msg.what) {
            case 100:
                toast.showMessage("文件不存在", QToast.WARNING);
                break;
            case 101:
                toast.showMessage("未知文本编码", QToast.WARNING);
                break;
            case 102:
                toast.showMessage("读写文件失败, 请检查存储权限", QToast.WARNING);
                break;
            case 200:
                toast.showMessage("保存成功", QToast.SUCCESS);
                finish();
                break;
            case 201:
                edFileContent.setText(stringBuilder.toString());
                stringBuilder.setLength(0);
                stringBuilder = null;
                break;
            default:
                break;
        }
        return true;
    });

    private void initView() {
        tvFilename = findViewById(R.id.tv_filename);
        recyclerToolsBar = findViewById(R.id.recycler_tools_bar);
        edFileContent = findViewById(R.id.ed_file_content);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerToolsBar.setLayoutManager(manager);
        recyclerToolsBar.addItemDecoration(new ToolBarItemDecoration());

        toast = new QToast(this);
    }

    private static final String[] ToolTitles = new String[]{"撤销", "粘贴", "替换", "快速插入", "删除行", "抬刀", "配置"};

    private void initParam(String path) {
        dataSet = new ArrayList<>(16);
        Collections.addAll(dataSet, ToolTitles);
        toolBarAdapter  = new ToolBarAdapter(dataSet);
        recyclerToolsBar.setAdapter(toolBarAdapter);

        if(path == null || path.isEmpty()) {
            toast.showMessage("文件路径异常");
            return;
        }
        mFile = new File(path);
        if(!mFile.exists()) {
            toast.showMessage("文件不存在");
            return;
        }

        tvFilename.setText(mFile.getName());
        stringBuilder = new StringBuilder((int)mFile.length());
        fixedThreadPool.execute(new LoadFileThread(mFile, stringBuilder, handler));
    }

    private void doSave() {

        FileOutputStream fops;
        BufferedWriter writer;
        StringBuilder builder = new StringBuilder(edFileContent.getText());
        // 覆盖写入
        try {
            fops = new FileOutputStream(mFile);
            writer = new BufferedWriter(new OutputStreamWriter(fops, "UTF-8"));
            writer.write(builder.toString());
            writer.flush();
            writer.close();
            fops.flush();
            fops.close();
        } catch (FileNotFoundException e) {
            handler.sendEmptyMessage(100);
            return;
        } catch (UnsupportedEncodingException e) {
            handler.sendEmptyMessage(101);
            return;
        } catch (IOException e) {
            handler.sendEmptyMessage(102);
            return;
        }
        handler.sendEmptyMessageDelayed(200, 300);
    }

    @Override
    protected void onDestroy() {
        toast = null;
        loadingDialog = null;
        fixedThreadPool.shutdown();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
