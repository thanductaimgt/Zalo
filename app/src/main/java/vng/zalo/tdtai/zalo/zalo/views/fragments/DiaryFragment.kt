package vng.zalo.tdtai.zalo.zalo.views.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_diary.*
import vng.zalo.tdtai.zalo.R
import vng.zalo.tdtai.zalo.zalo.networks.Database
import vng.zalo.tdtai.zalo.zalo.networks.Storage
import vng.zalo.tdtai.zalo.zalo.utils.Utils
import vng.zalo.tdtai.zalo.zalo.views.activities.LoginActivity

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
                startActivity(Intent(context, LoginActivity::class.java))
            }
            R.id.addStickerSet -> {
                val bucketName = Utils.getStickerBucketNameFromName("Hot Cherry")
                Log.d(TAG, bucketName)
                Storage.addStickerSet(bucketName = bucketName, localPaths = ArrayList()) {
                    Storage.getStickerSetUrls(bucketName) { stickerUrls ->
                        Database.addStickerSet(bucketName = bucketName, stickerUrls = stickerUrls)
                    }
                }
            }
        }
    }

    companion object{
        private val TAG = DiaryFragment::class.java.simpleName
    }
}