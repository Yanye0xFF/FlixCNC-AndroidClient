package com.yanye.flixcnc.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.adapter.FileListAdapter;
import com.yanye.flixcnc.model.FileItem;
import com.yanye.flixcnc.thread.ListFileThread;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class FilePickerActivity extends AppCompatActivity {

    private TextView tvCurrentPath;
    private RecyclerView fileRecycler;

    private List<FileItem> dataSet;
    private FileListAdapter fileAdapter;

    private String initialPath;
    private Stack<String> pathStack;

    private ExecutorService fixedThreadPool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);

        initView();
        initParam();

        findViewById(R.id.iv_picker_back).setOnClickListener((View view) -> finish());

        fileAdapter.setItemClickListener((int position, int arg0) -> {
            if(position == 0) {
                if(pathStack.size() <= 1) {
                    return;
                }
                pathStack.clear();
                pathStack.push(initialPath);
                dataSet.subList(2, dataSet.size()).clear();
                fixedThreadPool.execute(new ListFileThread(initialPath, dataSet,handler));
            }else if(position == 1) {
                if(pathStack.size() <= 1) {
                    return;
                }
                pathStack.pop();
                String path = pathStack.peek();
                if(path != null) {
                    tvCurrentPath.setText(path);
                    dataSet.subList(2, dataSet.size()).clear();
                    fixedThreadPool.execute(new ListFileThread(path, dataSet,handler));
                }
            }else {
                FileItem item = dataSet.get(position);
                if(item.isFile()) {
                    Intent intent = new Intent();
                    intent.putExtra("file", item.getFilePath());
                    intent.putExtra("size", item.getSize());
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }else {
                    tvCurrentPath.setText(item.getFilePath());
                    pathStack.push(item.getFilePath());
                    dataSet.subList(2, dataSet.size()).clear();
                    fixedThreadPool.execute(new ListFileThread(item.getFilePath(), dataSet,handler));
                }
            }
        });
        fileRecycler.setAdapter(fileAdapter);
        fixedThreadPool.execute(new ListFileThread(initialPath, dataSet,handler));
    }

    private Handler handler = new Handler((Message msg) -> {
        if(msg.what == 200) {
            // load file success
            fileAdapter.notifyDataSetChanged();
        }else if(msg.what == 100) {
            // empty folder
            dataSet.add(new FileItem(FileItem.TYPE_PLACE_HOLDER, null, 0));
            fileAdapter.notifyDataSetChanged();
        }
        return true;
    });

    private void initView() {

        tvCurrentPath = findViewById(R.id.tv_current_path);
        fileRecycler = findViewById(R.id.file_recycler);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        fileRecycler.setLayoutManager(manager);
    }

    private void initParam() {
        fixedThreadPool = Executors.newFixedThreadPool(1);

        dataSet = new ArrayList<>(72);
        pathStack = new Stack<>();

        initialPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        tvCurrentPath.setText(initialPath);
        pathStack.push(initialPath);

        fileAdapter = new FileListAdapter(dataSet);

        dataSet.add(0, new FileItem(FileItem.TYPE_OPERATOR, "根目录", R.mipmap.ic_file_home));
        dataSet.add(1, new FileItem(FileItem.TYPE_OPERATOR, "上一级", R.mipmap.ic_file_backward));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(KeyEvent.KEYCODE_BACK == keyCode && event.getAction() == KeyEvent.ACTION_DOWN) {
            if(pathStack.size() <= 1) {
                return super.onKeyDown(keyCode, event);
            }else {
                pathStack.pop();
                String path = pathStack.peek();
                if(path != null) {
                    tvCurrentPath.setText(path);
                    dataSet.subList(2, dataSet.size()).clear();
                    fixedThreadPool.execute(new ListFileThread(path, dataSet,handler));
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        pathStack.clear();
        dataSet.clear();
        fixedThreadPool.shutdown();
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
