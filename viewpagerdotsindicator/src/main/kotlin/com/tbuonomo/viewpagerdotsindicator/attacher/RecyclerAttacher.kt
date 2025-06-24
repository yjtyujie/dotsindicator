package com.tbuonomo.viewpagerdotsindicator.attacher

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.tbuonomo.viewpagerdotsindicator.BaseDotsIndicator
import com.tbuonomo.viewpagerdotsindicator.OnPageChangeListenerHelper
import kotlin.math.max

internal class RecyclerAttacher(val snapHelper: PagerSnapHelper) :
    DotsIndicatorAttacher<RecyclerView, RecyclerView.Adapter<*>>() {
    override fun getAdapterFromAttachable(attachable: RecyclerView): RecyclerView.Adapter<RecyclerView.ViewHolder>? =
        attachable.adapter

    override fun registerAdapterDataChangedObserver(
        attachable: RecyclerView,
        adapter: RecyclerView.Adapter<*>,
        onChanged: () -> Unit
    ) {
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                super.onItemRangeChanged(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
                super.onItemRangeChanged(positionStart, itemCount, payload)
                onChanged()
            }

            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                onChanged()
            }

            override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount)
                onChanged()
            }
        })
    }

    override fun buildPager(
        attachable: RecyclerView,
        adapter: RecyclerView.Adapter<*>
    ): BaseDotsIndicator.Pager {
        return object : BaseDotsIndicator.Pager {
            var onScrollListener: RecyclerView.OnScrollListener? = null
            val initialDx = 0 // this is supposed to be the scroll position of the recyclerview when you initialize the helper. I think

            override val isNotEmpty: Boolean
                get() = adapter.itemCount > 0
            override val currentItem: Int
                get() = snapHelper.findSnapView(attachable.layoutManager)
                    ?.let { (attachable.layoutManager as LinearLayoutManager).getPosition(it) } ?: 0
            override val isEmpty: Boolean
                get() = adapter.itemCount == 0
            override val count: Int
                get() = adapter.itemCount

            override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
                if (smoothScroll) {
                    attachable.smoothScrollToPosition(item)
                } else {
                    attachable.scrollToPosition(item)
                }
            }

            override fun removeOnPageChangeListener() {
                onScrollListener?.let(attachable::removeOnScrollListener)
            }

//            override fun addOnPageChangeListener(
//                onPageChangeListenerHelper: OnPageChangeListenerHelper
//            ) {
//                onScrollListener = object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(attachable: RecyclerView, dx: Int, dy: Int) {
//                        snapHelper.findSnapView(attachable.layoutManager)
//                            ?.let { attachable.layoutManager?.getPosition(it) }
//                            ?.let { snapPosition ->
//                                onPageChangeListenerHelper.onPageScrolled(
//                                    snapPosition,
//                                    dx.toFloat()
//                                )
//                            }
//                    }
//                }
//                attachable.addOnScrollListener(onScrollListener!!)
//            }

            override fun addOnPageChangeListener(
                onPageChangeListenerHelper: OnPageChangeListenerHelper
            ) {
                onScrollListener = object : RecyclerView.OnScrollListener() {
                    private var currentDx = initialDx
                    private var currentPosition: Int? = null

                    override fun onScrolled(attachable: RecyclerView, dx: Int, dy: Int) {
                        this.currentDx += dx

                        val view = snapHelper.findSnapView(attachable.layoutManager)
                        if (view != null) {
                            val width = view.width

                            if (currentPosition == null && initialDx > width) {
                                setCurrentItem(max(this.currentDx / width, count), false) // reset
                            }

                            val newPosition = this.currentDx / width
                            this.currentPosition = newPosition

                            onPageChangeListenerHelper.onPageScrolled(
                                newPosition,
                                try {
                                    (this.currentDx % width).toFloat() / width
                                } catch (e: ArithmeticException) {
                                    0f
                                }
                            )
                        }
                    }
                }
                attachable.addOnScrollListener(onScrollListener!!)
            }
        }
    }
}