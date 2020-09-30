package com.mgt.zalo.ui.home.test

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_test.*
import com.mgt.zalo.R
import com.mgt.zalo.base.BaseFragment
import com.mgt.zalo.ui.login.LoginActivity

class TestFragment : BaseFragment() {
    override fun createView(inflater: LayoutInflater, container: ViewGroup?): View {
        return inflater.inflate(R.layout.fragment_test, container, false).apply {
            makeRoomForStatusBar(requireActivity(), this)
        }
    }

    override fun onBindViews() {
        logoutButton.setOnClickListener(this)
        addStickerSet.setOnClickListener(this)
        addFriendButton.setOnClickListener(this)
        startNotiServiceButton.setOnClickListener(this)
        stopNotiServiceButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.logoutButton -> {
                startActivity(Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                (requireContext() as Activity).finish()

                logOut()
            }
            R.id.addStickerSet -> {
                val name = stickerSetNameEditText.text.toString()
                if (name != "") {
                    storage.addStickerSet(name = name, localPaths = ArrayList()) { bucketName ->
                        storage.getStickerSetUrls(bucketName = bucketName) { stickerUrls ->
                            database.addStickerSet(name = name, bucketName = bucketName, stickerUrls = stickerUrls) {
                                Toast.makeText(context, "sticker set added", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(context, "sticker set name can not be empty", Toast.LENGTH_SHORT).show()
                }
            }
            R.id.addFriendButton -> {
                val phone = phoneEditText.text.toString()
                when {
                    phone.length < 10 -> {
                        Toast.makeText(context, "must be a 10-digit phone number", Toast.LENGTH_SHORT).show()
                    }
                    phone == sessionManager.curUser!!.id -> {
                        Toast.makeText(context, "cannot add yourself", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        database.addFriend(phone) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "add friend success", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "add friend fail", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            R.id.startNotiServiceButton-> notificationService.start()
            R.id.stopNotiServiceButton-> notificationService.stop()
        }
    }

    private fun logOut() {
        sharedPrefsManager.setIsLogin(false)

        sessionManager.removeCurrentUser()
    }
}