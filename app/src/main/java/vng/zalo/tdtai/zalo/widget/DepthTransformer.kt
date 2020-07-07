package vng.zalo.tdtai.zalo.widget

import android.view.View
import kotlin.math.abs

class DepthTransformer :ABaseTransformer(){
    override fun onTransform(page: View, position: Float) {
        val scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - abs(position))
        val rotation = MAX_ROTATION * abs(position)

        if (position <= 0f) {
            page.translationX = page.width * -position * 0.19f
            page.pivotY = 0.5f * page.height
            page.pivotX = 0.5f * page.width
            page.scaleX = scale
            page.scaleY = scale
            page.rotationY = rotation
        } else if (position <= 1f) {
            page.translationX = page.width * -position * 0.19f
            page.pivotY = 0.5f * page.height
            page.pivotX = 0.5f * page.width
            page.scaleX = scale
            page.scaleY = scale
            page.rotationY = -rotation
        }
    }

    companion object{
        private const val MIN_SCALE = 0.5f
        private const val MAX_ROTATION = 30f
    }
}