package com.mgt.zalo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mgt.zalo.R
import com.mgt.zalo.widget.SpannedGridLayoutManager.GridSpanLookup
import com.mgt.zalo.widget.SpannedGridLayoutManager.SpanInfo


class MediaGridView : RecyclerView {
    private lateinit var gridLayoutManager: SpannedGridLayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var type = TYPE_FACEBOOK

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        type =
                attributeSet?.let {
                    val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.MediaGridView)
                    typedArray.getInt(R.styleable.MediaGridView_mgv_type, TYPE_FACEBOOK)
                } ?: TYPE_FACEBOOK

        init()
    }

    private fun init() {
        gridLayoutManager = SpannedGridLayoutManager(
                GridSpanLookup { position ->
                    if (type == TYPE_FACEBOOK) {
                        val spanCount = when (adapter?.itemCount ?: childCount) {
                            1 -> 6
                            2 -> return@GridSpanLookup SpanInfo(3, 6)
                            3 -> {
                                when (position) {
                                    0 -> return@GridSpanLookup SpanInfo(4, 6)
                                    else -> return@GridSpanLookup SpanInfo(2, 3)
                                }
                            }
                            4 -> {
                                when (position) {
                                    0 -> return@GridSpanLookup SpanInfo(4, 6)
                                    else -> 2
                                }
                            }
                            else -> {
                                when {
                                    position < 2 -> return@GridSpanLookup SpanInfo(3, 4)
                                    else -> 2
                                }
                            }
                        }
                        return@GridSpanLookup SpanInfo(spanCount, spanCount)
                    } else {
                        return@GridSpanLookup SpanInfo(2, 2)
                    }
                },
                6,
                1f
        )

        linearLayoutManager = LinearLayoutManager(context)

        layoutManager = gridLayoutManager
        overScrollMode = View.OVER_SCROLL_NEVER
        isNestedScrollingEnabled = type != TYPE_FACEBOOK
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                adjustLayoutManager()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                adjustLayoutManager()
            }
        })
    }

    private fun adjustLayoutManager() {
        layoutManager = if (adapter!!.itemCount == 1) {
            linearLayoutManager
        } else {
            gridLayoutManager
        }

        if (type == TYPE_FACEBOOK) {
            if (adapter!!.itemCount > 1) {
                post {
                    layoutParams = layoutParams.apply {
                        height = this@MediaGridView.width
                    }
                }
            } else {
                post {
                    layoutParams = layoutParams.apply {
                        height = ViewGroup.LayoutParams.WRAP_CONTENT
                    }
                }
            }
        }
    }

    companion object {
        const val TYPE_UNIFORM = 0
        const val TYPE_FACEBOOK = 1
    }
}