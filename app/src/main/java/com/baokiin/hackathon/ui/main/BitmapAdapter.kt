package com.baokiin.hackathon.ui.main


import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.adapter.BaseRclvAdapter
import com.baokiin.hackathon.bases.adapter.BaseRclvHolder
import com.baokiin.hackathon.bases.adapter.BaseVHData
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage.load
import com.baokiin.hackathon.databinding.ItemInfoBinding

class BitmapAdapter : BaseRclvAdapter<BitmapAdapter.BitmapVHData>() {

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

    class BitmapVHData(realData: BitmapModel) : BaseVHData<BitmapModel>(realData) {
        fun getPath() = realData.path
        fun getName() = realData.name
        fun getSize() = realData.size
    }
}
