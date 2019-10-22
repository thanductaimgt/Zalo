package vng.zalo.tdtai.zalo.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_diary.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.models.UserInfo
import vng.zalo.tdtai.zalo.networks.Database
import vng.zalo.tdtai.zalo.networks.Storage
import vng.zalo.tdtai.zalo.services.NotificationService
import vng.zalo.tdtai.zalo.utils.Constants
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
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.logoutButton -> {
                val editor = context!!.getSharedPreferences(Constants.SHARE_PREFERENCES_NAME, Context.MODE_PRIVATE).edit()
                editor.putBoolean(UserInfo.FIELD_IS_LOGIN, false)
                editor.apply()

                startActivity(Intent(context, LoginActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                setStatusToOffline()

                context!!.stopService(Intent(context, NotificationService::class.java))
                (context!! as Activity).finish()
            }
            R.id.addStickerSet -> {
                val name = editText.text.toString()
                if (name != "") {
                    Storage.addStickerSet(name = name, localPaths = ArrayList()) { bucketName ->
                        Storage.getStickerSetUrls(bucketName = bucketName) { stickerUrls ->
                            Database.addStickerSet(name = name, bucketName = bucketName, stickerUrls = stickerUrls)
                        }
                    }
                } else {
                    Toast.makeText(context, "sticker set name can not be empty", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setStatusToOffline(){
        Database.setCurrentUserOnlineState(false)
    }
}