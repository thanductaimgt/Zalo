package com.mgt.zalo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.ScaleAnimation
import android.view.animation.Transformation
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import com.mgt.zalo.R


internal class PausableProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet?, @AttrRes defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {
    private var frontProgressView: View? = null
    private var maxProgressView: View? = null
    private var animation: PausableScaleAnimation? = null
    private var duration = DEFAULT_PROGRESS_DURATION
    private var callback: Callback? = null

    internal interface Callback {
        fun onStartProgress()
        fun onFinishProgress()
    }

    constructor(context: Context) : this(context, null)

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    fun setCallback(callback: Callback) {
        this.callback = callback
    }

    fun setMax() {
        finishProgress(true)
    }

    fun setMin() {
        finishProgress(false)
    }

    fun setMinWithoutCallback() {
        maxProgressView!!.setBackgroundResource(R.color.progress_secondary)
        maxProgressView!!.visibility = View.VISIBLE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    fun setMaxWithoutCallback() {
        maxProgressView!!.setBackgroundResource(R.color.progress_max_active)
        maxProgressView!!.visibility = View.VISIBLE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
        }
    }

    private fun finishProgress(isMax: Boolean) {
        if (isMax) maxProgressView!!.setBackgroundResource(R.color.progress_max_active)
        maxProgressView!!.visibility = if (isMax) View.VISIBLE else View.GONE
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
            if (callback != null) {
                callback!!.onFinishProgress()
            }
        }
    }

    fun startProgress() {
        maxProgressView!!.visibility = View.GONE
        animation = PausableScaleAnimation(0f, 1f, 1f, 1f, Animation.ABSOLUTE, 0f, Animation.RELATIVE_TO_SELF, 0f)
        animation!!.duration = duration.toLong()
        animation!!.interpolator = LinearInterpolator()
        animation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                frontProgressView!!.visibility = View.VISIBLE
                if (callback != null) callback!!.onStartProgress()
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                if (callback != null) callback!!.onFinishProgress()
            }
        })
        animation!!.fillAfter = true
        frontProgressView!!.startAnimation(animation)
    }

    fun pauseProgress() {
        if (animation != null) {
            animation!!.pause()
        }
    }

    fun resumeProgress() {
        if (animation != null) {
            animation!!.resume()
        }
    }

    fun clear(doHideViews:Boolean = false) {
        if(doHideViews){
            maxProgressView!!.visibility = View.GONE
            frontProgressView!!.visibility = View.GONE
        }
        if (animation != null) {
            animation!!.setAnimationListener(null)
            animation!!.cancel()
            animation = null
        }
    }

    private inner class PausableScaleAnimation internal constructor(fromX: Float, toX: Float, fromY: Float,
                                                                    toY: Float, pivotXType: Int, pivotXValue: Float, pivotYType: Int,
                                                                    pivotYValue: Float) : ScaleAnimation(fromX, toX, fromY, toY, pivotXType, pivotXValue, pivotYType,
            pivotYValue) {
        private var mElapsedAtPause: Long = 0
        private var mPaused = false
        override fun getTransformation(currentTime: Long, outTransformation: Transformation, scale: Float): Boolean {
            if (mPaused && mElapsedAtPause == 0L) {
                mElapsedAtPause = currentTime - startTime
            }
            if (mPaused) {
                startTime = currentTime - mElapsedAtPause
            }
            return super.getTransformation(currentTime, outTransformation, scale)
        }

        /***
         * pause animation
         */
        fun pause() {
            if (mPaused) return
            mElapsedAtPause = 0
            mPaused = true
        }

        /***
         * resume animation
         */
        fun resume() {
            mPaused = false
        }
    }

    companion object {
        /***
         * progress満了タイマーのデフォルト時間
         */
        private const val DEFAULT_PROGRESS_DURATION = 2000
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.pausable_progress, this)
        frontProgressView = findViewById(R.id.front_progress)
        maxProgressView = findViewById(R.id.max_progress) // work around
    }
}