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
import javax.inject.Singleton

@Module
interface AppModule{
    @Binds
    @Singleton
    fun bindDatabase(firebaseDatabase: FirebaseDatabase):Database

    @Binds
    @Singleton
    fun bindStorage(firebaseStorage: FirebaseStorage):Storage

    @Binds
    @Singleton
    fun bindCallService(sipCallService: SipCallService):CallService

    @Binds
    @Singleton
    fun bindExternalIntentManager(externalIntentManagerImpl: ExternalIntentManagerImpl):ExternalIntentManager

    @Binds
    @Singleton
    fun bindMessageManager(messageManagerImpl: MessageManagerImpl):MessageManager

    @Binds
    @Singleton
    fun bindPermissionManager(permissionManagerImpl: PermissionManagerImpl):PermissionManager

    @Binds
    @Singleton
    fun bindResourceManager(resourceManagerImpl: ResourceManagerImpl):ResourceManager

    @Binds
    @Singleton
    fun bindSessionManager(sessionManagerImpl: SessionManagerImpl):SessionManager

    @Binds
    @Singleton
    fun bindSharedPrefsManager(sharedPrefsManagerImpl: SharedPrefsManagerImpl):SharedPrefsManager

    @Binds
    @Singleton
    fun bindNotificationService(alwaysRunningNotificationService: AlwaysRunningNotificationService):NotificationService

    @Binds
//    @Singleton
    fun bindViewModelFactory(viewModelFactory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @Singleton
    fun bindAlertDialog(alertDialogFragment: AlertDialogFragment): AlertDialog

    @Binds
    @Singleton
    fun bindProcessingDialog(processingDialogFragment: ProcessingDialogFragment): ProcessingDialog

    @Multibinds
    fun bindViewModelMap():Map<Class<out ViewModel>, ViewModel>
}