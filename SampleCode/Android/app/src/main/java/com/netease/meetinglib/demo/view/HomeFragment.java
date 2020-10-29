/*
 * Copyright (c) 2014-2020 NetEase, Inc.
 * All right reserved.
 */

package com.netease.meetinglib.demo.view;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.netease.meetinglib.demo.R;
import com.netease.meetinglib.demo.SdkAuthenticator;
import com.netease.meetinglib.demo.adapter.MeetingListAdapter;
import com.netease.meetinglib.demo.base.BaseFragment;
import com.netease.meetinglib.demo.data.MeetingItem;
import com.netease.meetinglib.demo.databinding.FragmentHomeBinding;
import com.netease.meetinglib.demo.utils.SPUtils;
import com.netease.meetinglib.demo.viewmodel.HomeViewModel;
import com.netease.meetinglib.sdk.NECallback;
import com.netease.meetinglib.sdk.NEMeetingCode;
import com.netease.meetinglib.sdk.NEMeetingError;
import com.netease.meetinglib.sdk.NEMeetingItemStatus;
import com.netease.meetinglib.sdk.NEMeetingOptions;
import com.netease.meetinglib.sdk.NEScheduleMeetingStatusListener;
import com.netease.meetinglib.sdk.control.NEControlMenuItem;
import com.netease.meetinglib.sdk.control.NEControlOptions;
import com.netease.meetinglib.sdk.control.NEControlParams;

import java.util.ArrayList;
import java.util.List;


public class HomeFragment extends BaseFragment<FragmentHomeBinding> {
    private static final String TAG = HomeFragment.class.getSimpleName();
    private HomeViewModel mViewModel;
    private MeetingListAdapter mAdapter;

    @Override
    protected void initView() {
        mViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        setHandleOnBackDesktopPressed(true);
        mAdapter = new MeetingListAdapter(new ArrayList<>());
        binding.rvMeetingList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mAdapter.setHasStableIds(true);
        binding.rvMeetingList.setEmptyView(binding.imgEmpty);
        binding.rvMeetingList.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener((view, position) -> {
            MeetingItem item = mAdapter.getData().get(position);
            Bundle bundle = new Bundle();
            bundle.putLong("meetingUniqueId", item.getMeetingUniqueId());
            bundle.putString("meetingId", String.valueOf(item.getMeetingId()));
            bundle.putLong("startTime", item.getStartTime());
            bundle.putLong("endTime", item.getEndTime());
            bundle.putString("password", item.getPassword());
            bundle.putString("subject", item.getSubject());
            bundle.putSerializable("status", item.getStatus());
            Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_scheduleMeetingDetailFragment, bundle);
        });

        mViewModel.observeMeetingItems(this, neMeetingItems -> {
            if (neMeetingItems != null) {
                mAdapter.resetData(neMeetingItems);
            } else {
                mAdapter.clear();
            }
        });
        mViewModel.observeChangeMeetingItems(this, neMeetingItems -> {
            if (mAdapter.getData().size() <= 0) {
                mAdapter.resetData(neMeetingItems);
                return;
            }
            int len = mAdapter.getData().size();
            for (int i = 0; i < neMeetingItems.size(); i++) {
                for (int j = 0; j < len; j++) {
                    MeetingItem changeItem = neMeetingItems.get(i);
                    MeetingItem originalItem = mAdapter.getData().get(j);
                    if (changeItem.getMeetingUniqueId() == originalItem.getMeetingUniqueId()) {
                        switch (changeItem.getStatus()) {
                            case init:
                            case started:
                            case ended:
                                mAdapter.updateData(j, changeItem);
                                break;
                            default:
                                mAdapter.deleteItem(j);
                                break;
                        }
                    } else {
                        switch (changeItem.getStatus()) {
                            case init:
                            case started:
                            case ended:
                                mAdapter.addNewData(j, changeItem);
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });

        initListener();
    }

    @Override
    protected void initData() {
        if (mAdapter != null) {
            mAdapter.clear();
        }
        mViewModel.getMeetingList();
    }

    @Override
    protected FragmentHomeBinding getViewBinding() {
        return FragmentHomeBinding.inflate(getLayoutInflater());
    }

    private void initListener() {
        binding.btnStartMeeting.setOnClickListener(v -> Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_startMeetingFragment));
        binding.btnJoinMeeting.setOnClickListener(v -> Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_joinMeetingFragment));
        binding.btnScheduleMeeting.setOnClickListener(v -> Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_scheduleMeetingFragment));
        binding.btnSetting.setOnClickListener(v -> Navigation.findNavController(getView()).navigate(R.id.action_homeFragment_to_settingsFragment));
  }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.clear();
        }
    }
}
