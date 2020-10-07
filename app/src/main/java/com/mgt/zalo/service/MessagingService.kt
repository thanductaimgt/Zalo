package com.mgt.zalo.service

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mgt.zalo.manager.SharedPrefsManager
import com.mgt.zalo.repository.Database
import dagger.android.AndroidInjection
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
