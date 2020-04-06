package vng.zalo.tdtai.zalo.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.android.AndroidInjection
import vng.zalo.tdtai.zalo.manager.SharedPrefsManager
import vng.zalo.tdtai.zalo.repository.Database
import javax.inject.Inject

class MessagingService : FirebaseMessagingService() {
    @Inject lateinit var database: Database
    @Inject lateinit var sharedPrefsManager: SharedPrefsManager

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(newToken: String) {
//        val oldToken = sharedPrefsManager.getFirebaseMessagingToken()
//        database.updateFirebaseMessagingToken(oldToken, newToken)
//        sharedPrefsManager.setFirebaseMessagingToken(newToken)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

    }
}
