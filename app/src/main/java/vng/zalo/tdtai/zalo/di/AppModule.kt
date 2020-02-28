package vng.zalo.tdtai.zalo.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.Multibinds
import vng.zalo.tdtai.zalo.common.AlertDialog
import vng.zalo.tdtai.zalo.common.AlertDialogFragment
import vng.zalo.tdtai.zalo.common.ProcessingDialog
import vng.zalo.tdtai.zalo.common.ProcessingDialogFragment
import vng.zalo.tdtai.zalo.managers.*
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.repo.FirebaseDatabase
import vng.zalo.tdtai.zalo.repo.FirebaseStorage
import vng.zalo.tdtai.zalo.repo.Storage
import vng.zalo.tdtai.zalo.services.AlwaysRunningNotificationService
import vng.zalo.tdtai.zalo.services.NotificationService

@Module
interface AppModule{
    @Binds
    fun bindDatabase(firebaseDatabase: FirebaseDatabase):Database

    @Binds
    fun bindStorage(firebaseStorage: FirebaseStorage):Storage

    @Binds
    fun bindCallService(sipCallService: SipCallService):CallService

    @Binds
    fun bindExternalIntentManager(externalIntentManagerImpl: ExternalIntentManagerImpl):ExternalIntentManager

    @Binds
    fun bindMessageManager(messageManagerImpl: MessageManagerImpl):MessageManager

    @Binds
    fun bindPermissionManager(permissionManagerImpl: PermissionManagerImpl):PermissionManager

    @Binds
    fun bindResourceManager(resourceManagerImpl: ResourceManagerImpl):ResourceManager

    @Binds
    fun bindSessionManager(sessionManagerImpl: SessionManagerImpl):SessionManager

    @Binds
    fun bindSharedPrefsManager(sharedPrefsManagerImpl: SharedPrefsManagerImpl):SharedPrefsManager

    @Binds
    fun bindNotificationService(alwaysRunningNotificationService: AlwaysRunningNotificationService):NotificationService

    @Binds
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    fun bindAlertDialog(alertDialogFragment: AlertDialogFragment): AlertDialog

    @Binds
    fun bindProcessingDialog(processingDialogFragment: ProcessingDialogFragment): ProcessingDialog

    @Multibinds
    fun bindViewModelMap():Map<Class<out ViewModel>, ViewModel>
}