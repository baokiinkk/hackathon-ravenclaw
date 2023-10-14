package com.baokiin.hackathon.ui.main.detail

import android.annotation.SuppressLint
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.adapter.BaseRclvAdapter
import com.baokiin.hackathon.bases.adapter.BaseRclvHolder
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage.load
import com.baokiin.hackathon.databinding.ItemBitmapDetailBinding
import com.baokiin.hackathon.ui.main.BitmapAdapter

class BitmapDetailAdapter : BaseRclvAdapter<BitmapAdapter.BitmapVHData>() {

    companion object {
        const val PAYLOAD_ITEM_SELECTED = 123
    }

    var oldPositionItem = 0
    var currentPosition = 0

    private var onItemClick: ((BitmapModel) -> Unit)? = null

    override fun getLayoutResource(viewType: Int): Int {
        return R.layout.item_bitmap_detail
    }

    override fun onCreateVH(itemView: ViewDataBinding, viewType: Int): BaseRclvHolder<*, *> {
        return BitmapViewHolder(itemView as ItemBitmapDetailBinding)
    }


    fun setOnListener(onItemClick: ((BitmapModel) -> Unit)? = null) {
        this.onItemClick = onItemClick
    }


    fun updateList(list: List<BitmapModel>?) {
        list?.let {
            val tmp = it.mapIndexed { index, bitmapModel ->
                if (index == 0)
                    BitmapAdapter.BitmapVHData(bitmapModel, isSelected = true)
                else
                    BitmapAdapter.BitmapVHData(bitmapModel)
            }
            reset(tmp)
        }
    }

    fun updateItemSelected(currentPosition: Int) {
        dataSet[oldPositionItem].isSelected = false
        notifyItemChanged(oldPositionItem, PAYLOAD_ITEM_SELECTED)
        dataSet[currentPosition].isSelected = true
        notifyItemChanged(currentPosition, PAYLOAD_ITEM_SELECTED)
        oldPositionItem = currentPosition
    }

    fun nextItem() {
        currentPosition += 1
        dataSet[oldPositionItem].isSelected = false
        notifyItemChanged(oldPositionItem, PAYLOAD_ITEM_SELECTED)
        dataSet[currentPosition].isSelected = true
        notifyItemChanged(currentPosition, PAYLOAD_ITEM_SELECTED)
        oldPositionItem = currentPosition
    }

    fun prevItem() {
        currentPosition -= 1
        dataSet[oldPositionItem].isSelected = false
        notifyItemChanged(oldPositionItem, PAYLOAD_ITEM_SELECTED)
        dataSet[currentPosition].isSelected = true
        notifyItemChanged(currentPosition, PAYLOAD_ITEM_SELECTED)
        oldPositionItem = currentPosition
    }


    inner class BitmapViewHolder(
        val binding: ItemBitmapDetailBinding
    ) : BaseRclvHolder<ItemBitmapDetailBinding, BitmapAdapter.BitmapVHData>(binding) {

        init {
            binding.apply {
                itemView.setOnClickListener {
                    currentPosition = adapterPosition
                    onItemClick?.invoke(dataSet[adapterPosition].realData)
                    updateItemSelected(adapterPosition)
                }
            }
        }

        override fun onBind(
            vhData: BitmapAdapter.BitmapVHData
        ) {
            binding.apply {
                val drawable = if (vhData.isSelected) {
                    R.drawable.shape_boder_image_blue
                } else {
                    R.drawable.shape_boder_image
                }
                binding.frameRoot.setBackgroundResource(drawable)
                imvInfoItmAvatar.load(vhData.getPath())
            }
        }

        override fun onBind(vhData: BitmapAdapter.BitmapVHData, payloads: List<Any>) {
            super.onBind(vhData, payloads)
            if (payloads.isNotEmpty()) {
                when (payloads[0]) {
                    PAYLOAD_ITEM_SELECTED -> {
                        val drawable = if (vhData.isSelected) {
                            R.drawable.shape_boder_image_blue
                        } else {
                            R.drawable.shape_boder_image
                        }
                        binding.frameRoot.setBackgroundResource(drawable)
                    }
                }
            }
        }

        override fun clearData() {
            super.clearData()
            binding.imvInfoItmAvatar.setBackgroundResource(R.color.black)
        }
    }
}

@SuppressLint("ClickableViewAccessibility")
abstract class VNPOnSwipeTouchListener(ctx: Context?) :
    View.OnTouchListener {
    val KEYBOARD_VISIBLE_THRESHOLD_DP = 100
    val SWIPE_THRESHOLD = 100
    val SWIPE_VELOCITY_THRESHOLD = 100

    private val gestureDetector: GestureDetector

    init {
        gestureDetector = GestureDetector(ctx, GestureListener())
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        v.performClick()
        return gestureDetector.onTouchEvent(event)
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            var result = false
            try {
                val diffY = e2.y?.minus(e1?.y ?: 0f)
                val diffX = e2.x?.minus(e1?.x ?: 0f)
                if (Math.abs(diffX ?: 0f) > Math.abs(diffY ?: 0f)) {
                    if (Math.abs(
                            diffX ?: 0f
                        ) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD
                    ) {
                        if (diffX != null) {
                            if (diffX > 0f) {
                                onSwipeRight()
                            } else {
                                onSwipeLeft()
                            }
                        }
                        result = true
                    }
                } else if (Math.abs(
                        diffY ?: 0f
                    ) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
                ) {
                    if (diffY != null) {
                        if (diffY > 0f) {
                            onSwipeBottom()
                        } else {
                            onSwipeTop()
                        }
                    }
                    result = true
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            return result
        }
    }

    abstract fun onSwipeRight()
    abstract fun onSwipeLeft()
    abstract fun onSwipeTop()
    abstract fun onSwipeBottom()
}
