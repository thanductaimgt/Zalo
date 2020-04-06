package vng.zalo.tdtai.zalo.base

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.core.view.*


interface BaseView {
    fun initAll(){
        onBindViews()
        onViewsBound()
    }

    // do set up all views here
    fun onBindViews() {}

    // called after onBindViews(), do set up other stuffs here
    fun onViewsBound() {}

    fun onActivityResult(requestCode: Int, intent: Intent?) {}

    fun makeRoomForStatusBar(context: Context, targetView: View, @MakeRoomType makeRoomType:Int = MAKE_ROOM_TYPE_PADDING) {
        var statusBarHeight:Int
        val resourceId: Int = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = context.resources.getDimensionPixelSize(resourceId)

            makeRoomForStatusBarInternal(targetView, statusBarHeight, makeRoomType)
        }else{
            var isInvoked = false

            ViewCompat.setOnApplyWindowInsetsListener(targetView) { _, insets ->
                insets.also {
                    if (!isInvoked) {
                        isInvoked = true
                        statusBarHeight = insets.systemWindowInsetTop

                        makeRoomForStatusBarInternal(targetView, statusBarHeight, makeRoomType)
                    }
                }
            }
        }
//        val rectangle = Rect()
//        activity.window.decorView.getWindowVisibleDisplayFrame(rectangle)
//        val statusBarHeight: Int = rectangle.top
////        val contentViewTop: Int = activity.window.findViewById<View>(Window.ID_ANDROID_CONTENT).top
////        val titleBarHeight = contentViewTop - statusBarHeight
//        rootView.updatePadding(top = statusBarHeight)
    }

    private fun makeRoomForStatusBarInternal(targetView: View, statusBarHeight:Int, @MakeRoomType makeRoomType:Int = MAKE_ROOM_TYPE_PADDING){
        if(makeRoomType == MAKE_ROOM_TYPE_PADDING){
            targetView.updatePadding(top = targetView.paddingTop + statusBarHeight)
        }else{
            targetView.updateLayoutParams {this as ViewGroup.MarginLayoutParams
                updateMargins(top = targetView.marginTop + statusBarHeight)
            }
        }
    }

    @IntDef(MAKE_ROOM_TYPE_PADDING, MAKE_ROOM_TYPE_MARGIN)
    @Retention(AnnotationRetention.SOURCE)
    annotation class MakeRoomType

    companion object{
        const val MAKE_ROOM_TYPE_PADDING = 0
        const val MAKE_ROOM_TYPE_MARGIN = 1
    }
}