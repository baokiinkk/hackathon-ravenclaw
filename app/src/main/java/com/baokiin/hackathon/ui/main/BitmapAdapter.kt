package com.baokiin.hackathon.ui.main


import android.graphics.Bitmap
import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.adapter.BaseRclvAdapter
import com.baokiin.hackathon.bases.adapter.BaseRclvHolder
import com.baokiin.hackathon.bases.adapter.BaseVHData
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage.load
import com.baokiin.hackathon.databinding.ItemInfoBinding
import java.util.Stack

class BitmapAdapter : BaseRclvAdapter<BitmapAdapter.BitmapVHData>() {
    companion object {
        const val PAGE_LIMIT = 10
        const val MAX_CACHE_PAGE_SIZE = 3
    }
    var handleOther: (() -> Unit)? = null
    val cacheFirst = Stack<List<BitmapVHData>>()
    val cacheLast = Stack<List<BitmapVHData>>()

    var isLoading = false

    override fun getLayoutResource(viewType: Int): Int {
        return R.layout.item_info
    }

    override fun onCreateVH(
        itemView: ViewDataBinding,
        viewType: Int
    ): BaseRclvHolder<*, *> {
        return InfoViewHolder(itemView as ItemInfoBinding)
    }

    fun updateList(list: List<BitmapModel>?) {
        list?.let {
            val tmp = it.map { item ->
                BitmapVHData(item)
            }
            reset(tmp)
        }
    }

    fun addAllData(list: List<BitmapModel>) {
        dataSet.addAll(list.map { BitmapVHData(it) })
        notifyItemRangeInserted(dataSet.size, list.size)
    }

    override fun onViewDetachedFromWindow(holder: BaseRclvHolder<ViewDataBinding, BitmapVHData>) {
        super.onViewDetachedFromWindow(holder)
        holder.clearData()
    }

    inner class InfoViewHolder(
        val binding: ItemInfoBinding
    ) : BaseRclvHolder<ItemInfoBinding, BitmapVHData>(binding) {

        init {
            binding.apply {
            }
        }

        override fun onBind(
            vhData: BitmapVHData
        ) {
            binding.apply {
                tvInfoItmName.text = vhData.getName()
                imvInfoItmAvatar.load(vhData.getPath())
            }
        }

        override fun clearData() {
            super.clearData()
            binding.imvInfoItmAvatar.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    fun insertBelow(data: List<BitmapModel>? = null) {
        val preSize = dataSet.size
        if (cacheLast.isNotEmpty()) {
            cacheLast.pop().also {
                dataSet.addAll(it)
                notifyItemRangeInserted(preSize, it.size)
            }
        } else {
            if (data == null) {
                handleOther?.let { it() }
            } else {
                dataSet.addAll(data.map {
                    BitmapVHData(it)
                })
                notifyItemRangeInserted(preSize, data.size)
            }
        }
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
    }

    fun insertAbove() {
        if (cacheFirst.isNotEmpty()) {
            cacheFirst.pop().also {
                dataSet.addAll(0, it)
                notifyItemRangeInserted(0, it.size)
                // Call add to cacheFirst
            }
        }
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
    }

    fun doCacheLast() {
        dataSet.takeLast(PAGE_LIMIT).also {
            if (it.isNotEmpty()) {
                if (cacheLast.size == MAX_CACHE_PAGE_SIZE) {
                    cacheLast.removeFirst()
                }
                cacheLast.push(it)
            }
        }

        for (i in 0 until PAGE_LIMIT) {
            dataSet.removeLast()
            notifyItemRemoved(dataSet.size)
        }
    }

    fun doCacheFirst() {
        if (cacheFirst.size == MAX_CACHE_PAGE_SIZE) {
            cacheFirst.pop()
        }
        cacheFirst.push(dataSet.take(PAGE_LIMIT))
        for (i in 0 until PAGE_LIMIT) {
            dataSet.removeFirst()
            notifyItemRemoved(0)
        }
    }

    class BitmapVHData(realData: BitmapModel) : BaseVHData<BitmapModel>(realData) {
        fun getPath() = realData.path
        fun getName() = realData.name
        fun getSize() = realData.size
    }
}
