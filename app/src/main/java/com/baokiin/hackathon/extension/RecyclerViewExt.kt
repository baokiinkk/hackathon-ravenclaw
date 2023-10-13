package com.baokiin.hackathon.extension

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

object RecyclerViewExt {
    fun RecyclerView.setItemDecoratorDrawable(
        @DrawableRes img: Int,
        isLastDivider: Boolean = false
    ) {
        if (isLastDivider) {
            val itemDecorator = DividerItemDecoration(context, LinearLayoutManager.VERTICAL)
            ContextCompat.getDrawable(context, img)?.let { itemDecorator.setDrawable(it) }
            addItemDecoration(itemDecorator)
        } else {
            val itemDecorator = DividerItemDecorationCustom(context, img)
            addItemDecoration(itemDecorator)
        }
    }

    fun RecyclerView.setItemDecoratorSpaceLinear(
        @DimenRes spaceDimen: Int,
        isLastSpace: Boolean = true
    ) {

        val space = resources.getDimensionPixelSize(spaceDimen)
        if (isLastSpace) {
            addItemDecoration(LinearSpaceItemLastDecoration(space))
        } else {
            addItemDecoration(LinearSpaceItemDecoration(space))
        }
    }

    fun RecyclerView.setItemDecoratorSpaceLinearHorizontal(@DimenRes spaceDimen: Int) {
        val space = resources.getDimensionPixelSize(spaceDimen)
        addItemDecoration(LinearHorizontalSpaceItemDecoration(space))
    }

    fun RecyclerView.setItemDecoratorSpaceGrid(
        @DimenRes spaceDimen: Int,
        spanCount: Int,
        includeEdge: Boolean
    ) {
        val space = resources.getDimensionPixelSize(spaceDimen)
        addItemDecoration(GridSpaceItemDecoration(space, spanCount, includeEdge))
    }

    fun RecyclerView.getFirstVisibleItemPosition() =
        (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()

    fun RecyclerView.getLastVisibleItemPosition() =
        (layoutManager as LinearLayoutManager).findLastVisibleItemPosition()

    fun RecyclerView.loadMore(loadMoreLast: (Int) -> Unit,loadMoreFirst: (Int) -> Unit) {
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val lastVisibleItemPosition = getLastVisibleItemPosition()
                val firstVisibleItemPosition = getFirstVisibleItemPosition()
                val mLayoutManager = layoutManager
                val visibleItemCount = mLayoutManager?.childCount
                val totalItemCount = mLayoutManager?.itemCount
                if (dy > 0 && lastVisibleItemPosition + (visibleItemCount ?: 0) >= (totalItemCount ?: 0))
                    loadMoreLast(lastVisibleItemPosition)
                if(dy < 0 && firstVisibleItemPosition - (visibleItemCount ?: 0) <= 0)
                    loadMoreFirst(firstVisibleItemPosition)
            }
        })
    }


    class LinearSpaceItemLastDecoration(private val space: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            outRect.bottom = space
            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = space
            } else {
                outRect.top = 0
            }
        }
    }

    class LinearHorizontalSpaceItemDecoration(private val space: Int) :
        RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            if (parent.getChildAdapterPosition(view) != 0) {
                outRect.left = space
            }
        }
    }

    class GridSpaceItemDecoration(
        private val space: Int,
        private val spanCount: Int,
        private val includeEdge: Boolean
    ) : RecyclerView.ItemDecoration() {

        init {
            require(spanCount > 0) { "spanCount should be greater than 0" }
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount

            if (includeEdge) {
                outRect.left = space - column * space / spanCount
                outRect.right = (column + 1) * space / spanCount

                if (position < spanCount) {
                    outRect.top = space
                }
                outRect.bottom = space
            } else {
                outRect.left = column * space / spanCount
                outRect.right = space - (column + 1) * space / spanCount
                if (position >= spanCount) {
                    outRect.top = space
                }
            }
        }
    }

    class DividerItemDecorationCustom(context: Context, @DrawableRes img: Int) : ItemDecoration() {
        private val divider: Drawable?

        init {
            divider = ContextCompat.getDrawable(context, img)
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == parent.adapter!!.itemCount - 1) {
                outRect[0, 0, 0] = 0
            } else {
                outRect[0, 0, 0] = divider!!.intrinsicHeight
            }
        }

        override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            val childCount = parent.childCount
            val width = parent.width
            for (i in 0 until childCount - 1) {
                val child = parent.getChildAt(i)
                val position = parent.getChildAdapterPosition(child)
                if (position == parent.adapter!!.itemCount - 1) {
                    // Không vẽ cuối
                    continue
                }
                val top = child.bottom
                val bottom = top + divider!!.intrinsicHeight
                divider.setBounds(0, top, width, bottom)
                divider.draw(c)
            }
        }
    }

    class LinearSpaceItemDecoration(private val space: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position != parent.adapter!!.itemCount - 1) {
                outRect.bottom = space
            }
        }
    }
}
