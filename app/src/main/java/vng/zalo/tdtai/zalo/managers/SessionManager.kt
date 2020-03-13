package vng.zalo.tdtai.zalo.managers

import dagger.Lazy
import vng.zalo.tdtai.zalo.model.User
import vng.zalo.tdtai.zalo.repo.Database
import vng.zalo.tdtai.zalo.services.NotificationService
import javax.inject.Inject

interface SessionManager {
    var isOnline: Boolean
    var curUser: User?
    var currentRoomId: String?

    fun initUser(user: User)

    fun removeCurrentUser()

    fun isUserInit(): Boolean
}

class SessionManagerImpl @Inject constructor(
        private val lazyCallService: Lazy<CallService>,
        private val lazyDatabase: Lazy<Database>,
        private val lazyNotificationService: Lazy<NotificationService>,
        private val lazyResourceManager: Lazy<ResourceManager>
) : SessionManager {
    override var isOnline: Boolean = false
    override var curUser: User? = null
    override var currentRoomId: String? = null

    override fun initUser(user: User) {
        this.curUser = user
        lazyCallService.get().init()
//            Database.setCurrentUserOnlineState(true)
        lazyResourceManager.get().initPhoneNameMap()
//            notificationDispatcher?.onLogin()
    }

    override fun removeCurrentUser() {
        lazyNotificationService.get().stop()
        lazyDatabase.get().setCurrentUserOnlineState(false)
        lazyCallService.get().stop()
        curUser = null
    }

    override fun isUserInit(): Boolean {
        return curUser != null
    }
}