package com.baokiin.hackathon.ui.main


import android.view.View
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
        const val PAGE_LIMIT = 48
        const val MAX_CACHE_PAGE_SIZE = 50
        const val PAYLOAD_COUNTER = 123
    }

    var counter = 0
    var handleOther: (() -> Unit)? = null
    val cacheFirst = Stack<List<BitmapVHData>>()
    val cacheLast = Stack<List<BitmapVHData>>()
    var isLoading = false
    var clickCheckedItem: ((List<BitmapModel>) -> Unit)? = null
    var clickItem: ((View,BitmapModel) -> Unit)? = null

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

    override fun onViewRecycled(holder: BaseRclvHolder<ViewDataBinding, BitmapVHData>) {
        super.onViewRecycled(holder)
        holder.clearData()
    }

    fun setItemCheckedClick(action: (List<BitmapModel>) -> Unit) {
        clickCheckedItem = action
    }
    fun setItemClick(action: (View,BitmapModel) -> Unit){
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

    fun reduceChecked(currentCounter: Int) {
        dataSet.forEachIndexed { index, item ->
            if (item.counter != 0 && item.counter > currentCounter) {
                item.counter--
                notifyItemChanged(index, PAYLOAD_COUNTER)
            }
        }
    }

    fun getItemChecked() = dataSet.filter {
        it.counter != 0
    }.map {
        it.realData
    }

    inner class InfoViewHolder(
        val binding: ItemInfoBinding
    ) : BaseRclvHolder<ItemInfoBinding, BitmapVHData>(binding) {

        init {
            binding.apply {
                itemView.setOnClickListener {
                    clickItem?.invoke(it,getItem(adapterPosition).realData)
                }
                llInfoItmCheck.setOnClickListener {
                    if (getItem(adapterPosition).counter == 0) {
                        counter++
                        getItem(adapterPosition).counter = counter
                        cbInfoItmCheck.text = counter.toString()
                        cbInfoItmCheck.isChecked = true
                    } else {
                        counter--
                        reduceChecked(getItem(adapterPosition).counter)
                        cbInfoItmCheck.text = ""
                        getItem(adapterPosition).counter = 0
                        cbInfoItmCheck.isChecked = false
                    }
                    clickCheckedItem?.invoke(getItemChecked())
                }
            }
        }

        override fun onBind(
            vhData: BitmapVHData
        ) {
            binding.apply {
                cbInfoItmCheck.isChecked = vhData.counter != 0
                cbInfoItmCheck.text = if(vhData.counter != 0) vhData.counter.toString() else ""
                imvInfoItmAvatar.transitionName = vhData.getPath()
                imvInfoItmAvatar.load(vhData.getPath())
            }
        }

        override fun onBind(vhData: BitmapVHData, payloads: List<Any>) {
            super.onBind(vhData, payloads)
            binding.apply {
                cbInfoItmCheck.isChecked = vhData.counter != 0
                cbInfoItmCheck.text = if(vhData.counter != 0) vhData.counter.toString() else ""
                imvInfoItmAvatar.transitionName = vhData.getPath()
                imvInfoItmAvatar.load(vhData.getPath())
            }
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
            binding.cbInfoItmCheck.isChecked = false
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

    class BitmapVHData(realData: BitmapModel,isSelected:Boolean = false) : BaseVHData<BitmapModel>(realData) {
        var counter: Int = 0
        var isSelected: Boolean = isSelected
        fun getPath() = realData.path
        fun getSize() = realData.size

    }
}
