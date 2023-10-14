package com.baokiin.hackathon.ui.main


import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.adapter.BaseRclvAdapter
import com.baokiin.hackathon.bases.adapter.BaseRclvHolder
import com.baokiin.hackathon.bases.adapter.BaseVHData
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage.load
import com.baokiin.hackathon.databinding.ItemInfoBinding
import java.util.*

class BitmapAdapter : BaseRclvAdapter<BitmapAdapter.BitmapVHData>() {
    companion object {
        const val PAGE_LIMIT = 50
        const val MAX_CACHE_PAGE_SIZE = 50
        const val PAYLOAD_COUNTER = 123
    }

    var counter = 0
    var handleOther: (() -> Unit)? = null
    val cacheFirst = Stack<List<BitmapVHData>>()
    val cacheLast = Stack<List<BitmapVHData>>()
    var isLoading = false
    var clickItem: ((Int) -> Unit)? = null

    override fun getLayoutResource(viewType: Int): Int {
        return R.layout.item_info
    }

    override fun onCreateVH(
        itemView: ViewDataBinding,
        viewType: Int
    ): BaseRclvHolder<*, *> {
        return InfoViewHolder(itemView as ItemInfoBinding)
    }

    override fun onViewDetachedFromWindow(holder: BaseRclvHolder<ViewDataBinding, BitmapVHData>) {
        super.onViewDetachedFromWindow(holder)
        holder.clearData()
    }

    fun setItemClick(action: (Int) -> Unit) {
        clickItem = action
    }

    fun updateList(list: List<BitmapModel>?) {
        list?.let {
            val tmp = it.map { item ->
                BitmapVHData(item)
            }
            reset(tmp)
        }
    }

    fun getItem(position: Int) = getItemDataAtPosition(position)
    fun addAllData(list: List<BitmapModel>) {
        dataSet.addAll(list.map { BitmapVHData(it) })
        notifyItemRangeInserted(dataSet.size, list.size)
    }

    fun reduceChecked(currentCounter:Int) {
        dataSet.forEachIndexed { index, item ->
            if (item.counter != 0 && item.counter > currentCounter) {
                item.counter--
                notifyItemChanged(index, PAYLOAD_COUNTER)
            }
        }
    }

    inner class InfoViewHolder(
        val binding: ItemInfoBinding
    ) : BaseRclvHolder<ItemInfoBinding, BitmapVHData>(binding) {

        init {
            binding.apply {
                itemView.setOnClickListener {
                    clickItem?.invoke(adapterPosition)
                    if (getItem(adapterPosition).counter == 0) {
                        counter++
                        getItem(adapterPosition).counter = counter
                        cbInfoItmCheck.text = counter.toString()
                        cbInfoItmCheck.isChecked = true
                    } else {
                        counter--
                        reduceChecked(getItem(adapterPosition).counter)
                        cbInfoItmCheck.setText("")
                        getItem(adapterPosition).counter = 0
                        cbInfoItmCheck.isChecked = false
                    }
                }
            }
        }

        override fun onBind(
            vhData: BitmapVHData
        ) {
            binding.apply {
                imvInfoItmAvatar.load(vhData.getPath())
            }
        }

        override fun onBind(vhData: BitmapVHData, payloads: List<Any>) {
            super.onBind(vhData, payloads)
            if (payloads.isNotEmpty()) {
                when (payloads[0]) {
                    PAYLOAD_COUNTER -> {
                        if (getItem(adapterPosition).counter == 0)
                            binding.cbInfoItmCheck.text = ""
                        else
                            binding.cbInfoItmCheck.text =
                                getItem(adapterPosition).counter.toString()
                    }
                }
            }
        }

        override fun clearData() {
            super.clearData()
            binding.imvInfoItmAvatar.setBackgroundResource(R.color.black)
        }
    }

    fun insertBelow(data: List<BitmapModel>? = null) {
        val preSize = dataSet.size
        if (cacheLast.isNotEmpty()) {
            cacheLast.pop().also {
                it.forEach { itemData ->
                    dataSet.add(itemData)
                    notifyItemInserted(itemCount)
                }
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
                it.forEach { itemData ->
                    dataSet.add(0, itemData)
                    notifyItemInserted(0)
                }
                // Call add to cacheFirst
            }
        }
        println("${cacheFirst.size} insertBelow cacheFirst: $cacheFirst")
        println("${cacheLast.size} insertBelow cacheLast: $cacheLast")
    }

    fun doCacheLast() {
        dataSet.takeLast(PAGE_LIMIT).also {
            if (it.isNotEmpty()) {
                cacheLast.push(it)
            }
        }

        for (i in 0 until PAGE_LIMIT) {
            dataSet.removeLast()
            notifyItemRemoved(dataSet.size)
        }
    }

    fun doCacheFirst() {
        cacheFirst.push(dataSet.take(PAGE_LIMIT))
        for (i in 0 until PAGE_LIMIT) {
            dataSet.removeFirst()
            notifyItemRemoved(0)
        }
    }

    class BitmapVHData(realData: BitmapModel) : BaseVHData<BitmapModel>(realData) {
        var counter: Int = 0
        fun getPath() = realData.path
        fun getSize() = realData.size
    }
}
