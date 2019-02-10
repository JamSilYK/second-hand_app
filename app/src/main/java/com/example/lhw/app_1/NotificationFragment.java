package com.example.lhw.app_1;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class NotificationFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private TextView text_search;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_notification, container, false);


//        text_search = v.findViewById(R.id.text_search);
//        text_search.setBackgroundResource(R.drawable.text_solid); //검색 text

        tabLayout = (TabLayout)v.findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("채팅"));
        tabLayout.addTab(tabLayout.newTab().setText("경매"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        //Initializing ViewPager
        viewPager = (ViewPager)v.findViewById(R.id.viewPager);

        TabPagerAdapter pagerAdapter = new TabPagerAdapter(getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(pagerAdapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


        return v;
    }

//    public void switchFragment(Fragment fragment){
//        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//        transaction.replace(R.id.ct_container, fragment);
//        transaction.commit();
//    }

}
