package com.yanye.flixcnc.activity;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.SparseIntArray;
import android.view.MenuItem;

import com.yanye.flixcnc.R;
import com.yanye.flixcnc.fragment.SettingFragment;
import com.yanye.flixcnc.fragment.EngravingFragment;
import com.yanye.flixcnc.fragment.MotionFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final int FRAGMENT_AMOUNT = 3;

    private List<Fragment> fragments;
    private FragmentManager fragmentManager;
    private volatile int currentPage = 0;

    private SparseIntArray navigationMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 允许ui线程执行网络操作
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        navigationMap = new SparseIntArray(FRAGMENT_AMOUNT);
        navigationMap.put(R.id.navigation_jog, 0);
        navigationMap.put(R.id.navigation_engraving, 1);
        navigationMap.put(R.id.navigation_setting, 2);

        BottomNavigationView navigation = findViewById(R.id.main_navigation);
        navigation.setOnNavigationItemSelectedListener((@NonNull MenuItem item) -> {
            onNavigationItemChecked(navigationMap.get(item.getItemId()));
            return true;
        });

        initFragments();
    }

    private void initFragments() {
        fragmentManager = getSupportFragmentManager();

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        fragments = new ArrayList<>(FRAGMENT_AMOUNT);
        fragments.add(new MotionFragment());
        for(int i = 0; i < (FRAGMENT_AMOUNT - 1); i++) {
            fragments.add(null);
        }

        transaction.add(R.id.layout_container, fragments.get(0));
        transaction.commit();
    }

    public void onNavigationItemChecked(int position) {
        if(position == currentPage){
            return;
        }

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.hide(fragments.get(currentPage));

        if(fragments.get(position) == null){
            switch (position){
                case 0:
                    fragments.set(position,new MotionFragment());
                    break;
                case 1:
                    fragments.set(position,new EngravingFragment());
                    break;
                case 2:
                    fragments.set(position,new SettingFragment());
                    break;
            }
            transaction.add(R.id.layout_container, fragments.get(position));
        }else{
            transaction.show(fragments.get(position));
        }

        currentPage = position;
        transaction.commit();
    }




}
