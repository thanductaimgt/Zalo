package com.mgt.zalo.manager

import com.mgt.zalo.data_model.User
import com.mgt.zalo.repository.Database
import com.mgt.zalo.service.NotificationService
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
        private val lazyCallService: Lazy<CallService>,
        private val lazyDatabase: Lazy<Database>,
        private val lazyNotificationService: Lazy<NotificationService>,
        private val lazyResourceManager: Lazy<ResourceManager>
) {
    var isOnline: Boolean = false
    var curUser: User? = null
    var currentRoomId: String? = null

    fun initUser(user: User) {
        this.curUser = user
        lazyCallService.get().init()
//            Database.setCurrentUserOnlineState(true)
        lazyResourceManager.get().initPhoneNameMap()
//            notificationDispatcher?.onLogin()
    }

    fun removeCurrentUser() {
        lazyNotificationService.get().stop()
        lazyDatabase.get().setCurrentUserOnlineState(false)
        lazyCallService.get().stop()
//        curUser = null
    }

    fun isUserInit(): Boolean {
        return curUser != null
    }
}