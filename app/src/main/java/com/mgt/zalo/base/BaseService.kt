package com.mgt.zalo.base

import dagger.android.DaggerService
import com.mgt.zalo.ZaloApplication
import com.mgt.zalo.common.AlertDialog
import com.mgt.zalo.common.ProcessingDialog
import com.mgt.zalo.manager.*
import com.mgt.zalo.repository.Database
import com.mgt.zalo.repository.Storage
import com.mgt.zalo.service.NotificationService
import com.mgt.zalo.util.Utils
import javax.inject.Inject

abstract class BaseService :DaggerService(){
    @Inject
    lateinit var application: ZaloApplication

    @Inject
    lateinit var sharedPrefsManager: SharedPrefsManager

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var externalIntentManager: ExternalIntentManager

    @Inject
    lateinit var messageManager: MessageManager

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var utils: Utils

    @Inject
    lateinit var database: Database

    @Inject
    lateinit var storage: Storage

    @Inject
    lateinit var notificationService: NotificationService

    @Inject
    lateinit var callService: CallService

    @Inject
    lateinit var alertDialog: AlertDialog

    @Inject
    lateinit var processingDialog: ProcessingDialog

    @Inject
    lateinit var backgroundWorkManager: BackgroundWorkManager

    @Inject
    lateinit var playbackManager: PlaybackManager
}