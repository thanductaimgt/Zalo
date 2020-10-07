package com.mgt.zalo.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.manager.SessionManager
import com.mgt.zalo.ui.camera.CameraFragment
import com.mgt.zalo.ui.home.chat.ChatFragment
import com.mgt.zalo.ui.home.diary.DiaryFragment
import com.mgt.zalo.ui.home.test.TestFragment
import com.mgt.zalo.ui.home.watch.WatchFragment
import com.mgt.zalo.ui.profile.ProfileFragment
import javax.inject.Inject

class HomeAdapter @Inject constructor(
        private val homeActivity: HomeActivity,
        private val sessionManager: SessionManager
) : FragmentStateAdapter(homeActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> DiaryFragment()
            2 -> ChatFragment()
            3 -> WatchFragment()
            4 -> ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(BaseFragment.ARG_1, this@HomeAdapter.sessionManager.curUser!!.id!!)
                }
            }
            5 -> TestFragment()
            else -> CameraFragment().also { homeActivity.cameraFragment = it }
        }
    }

    override fun getItemCount(): Int {
        return 6
    }
}