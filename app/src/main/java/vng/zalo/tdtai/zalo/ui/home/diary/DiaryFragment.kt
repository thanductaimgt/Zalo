package vng.zalo.tdtai.zalo.ui.home.diary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_diary.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.managers.SessionManager
import vng.zalo.tdtai.zalo.managers.SharedPrefsManager
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.Storage
import vng.zalo.tdtai.zalo.services.NotificationService
import vng.zalo.tdtai.zalo.ui.login.LoginActivity
import javax.inject.Inject

class DiaryFragment : DaggerFragment(), View.OnClickListener {
    @Inject lateinit var database: Database
    @Inject lateinit var storage: Storage
    @Inject lateinit var sessionManager: SessionManager
    @Inject lateinit var notificationService: NotificationService
    @Inject lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logoutButton.setOnClickListener(this)
        addStickerSet.setOnClickListener(this)
        addFriendButton.setOnClickListener(this)
        startNotiServiceButton.setOnClickListener(this)
        stopNotiServiceButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logoutButton -> {
                logOut()

                startActivity(Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                (requireContext() as Activity).finish()
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
                    phone == sessionManager.curUser!!.phone -> {
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
        sharedPrefsManager.setIsLogin(requireContext(), false)

        sessionManager.removeCurrentUser()
    }
}