package vng.zalo.tdtai.zalo.base

import dagger.android.DaggerService
import vng.zalo.tdtai.zalo.ZaloApplication
import vng.zalo.tdtai.zalo.common.AlertDialog
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.manager.*
import vng.zalo.tdtai.zalo.repository.Database
import vng.zalo.tdtai.zalo.repository.Storage
import vng.zalo.tdtai.zalo.service.NotificationService
import vng.zalo.tdtai.zalo.util.Utils
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