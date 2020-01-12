package vng.zalo.tdtai.zalo.views.fragments

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_diary.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.SharedPrefsManager
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.views.activities.LoginActivity

class DiaryFragment : Fragment(), View.OnClickListener {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_diary, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        logoutButton.setOnClickListener(this)
        addStickerSet.setOnClickListener(this)
        addFriendButton.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logoutButton -> {
                logOut()

                startActivity(Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                (context!! as Activity).finish()
            }
            R.id.addStickerSet -> {
                val name = stickerSetNameEditText.text.toString()
                if (name != "") {
                    Storage.addStickerSet(name = name, localPaths = ArrayList()) { bucketName ->
                        Storage.getStickerSetUrls(bucketName = bucketName) { stickerUrls ->
                            Database.addStickerSet(name = name, bucketName = bucketName, stickerUrls = stickerUrls) {
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
                    phone == ZaloApplication.curUser!!.phone -> {
                        Toast.makeText(context, "cannot add yourself", Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        Database.addFriend(phone) { isSuccess ->
                            if (isSuccess) {
                                Toast.makeText(context, "add friend success", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "add friend fail", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun logOut() {
        SharedPrefsManager.setIsLogin(context!!, false)

        ZaloApplication.removeCurrentUser(context!!)
    }
}