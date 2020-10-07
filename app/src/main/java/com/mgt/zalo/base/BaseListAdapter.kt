package com.mgt.zalo.base

import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.common.AlertDialog
import com.mgt.zalo.common.ProcessingDialog
import com.mgt.zalo.manager.*
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.service.NotificationService
import com.mgt.zalo.util.ImageLoader
import com.mgt.zalo.util.Utils
import javax.inject.Inject

abstract class BaseListAdapter<T, VH : BaseViewHolder>(diffCallback: DiffUtil.ItemCallback<T>) : ListAdapter<T, VH>(diffCallback) {
    @Inject
    lateinit var application: ZaloApplication

    @Inject
    lateinit var processingDialog: ProcessingDialog

    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var externalIntentManager: ExternalIntentManager

    @Inject
    lateinit var messageManager: MessageManager

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var callService: CallService

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var zaloFragmentManager: ZaloFragmentManager

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject lateinit var playbackManager: PlaybackManager

    @Inject
    lateinit var imageLoader: ImageLoader

    init {
        @Suppress("UNCHECKED_CAST")
        ZaloApplication.appComponent.inject(this as BaseListAdapter<Any, BaseViewHolder>)
    }

    override fun submitList(list: List<T>?) {
        submitList(list, null)
    }

    override fun submitList(list: List<T>?, commitCallback: Runnable?) {
        super.submitList(list?.toMutableList(), commitCallback)
    }

    final override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        try {
            if (payloads.isNotEmpty()) {
                onBindViewHolder(holder, position, payloads[0] as ArrayList<*>)
            } else {
                onBindViewHolder(holder, position)
            }
        } catch (t: Throwable) {
            t.printStackTrace()
        }
    }

    open fun onBindViewHolder(holder: VH, position: Int, payloads: ArrayList<*>){}

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(position)
    }
}