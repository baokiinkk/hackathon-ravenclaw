package com.baokiin.hackathon.ui.main


import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.adapter.BaseRclvAdapter
import com.baokiin.hackathon.bases.adapter.BaseRclvHolder
import com.baokiin.hackathon.bases.adapter.BaseVHData
import com.baokiin.hackathon.data.Info
import com.baokiin.hackathon.data.network.LoadImage
import com.baokiin.hackathon.data.network.LoadImage.load
import com.baokiin.hackathon.databinding.ItemInfoBinding

class InfoAdapter: BaseRclvAdapter<InfoAdapter.InfoVHData>() {

    override fun getLayoutResource(viewType: Int): Int {
        return R.layout.item_info
    }

    override fun onCreateVH(
        itemView: ViewDataBinding,
        viewType: Int
    ): BaseRclvHolder<*, *> {
        return InfoViewHolder(itemView as ItemInfoBinding)
    }

    fun updateList(list: List<Info>?) {
        list?.let {
            val tmp = it.map { item ->
                InfoVHData(item)
            }
            reset(tmp)
        }
    }

    override fun onViewDetachedFromWindow(holder: BaseRclvHolder<ViewDataBinding, InfoVHData>) {
        super.onViewDetachedFromWindow(holder)
        holder.clearData()
    }
    inner class InfoViewHolder(
        val binding: ItemInfoBinding
    ) : BaseRclvHolder<ItemInfoBinding, InfoVHData>(binding) {

        init {
            binding.apply {
            }
        }

        override fun onBind(
            vhData: InfoVHData
        ) {
            binding.apply {
                tvInfoItmName.text = vhData.getName()
                imvInfoItmAvatar.load(vhData.getAvatar())
            }
        }

        override fun clearData() {
            super.clearData()
            binding.imvInfoItmAvatar.setImageResource(R.drawable.ic_launcher_background)
        }
    }

    class InfoVHData(realData: Info) : BaseVHData<Info>(realData) {
        var isEye: Boolean = false
        fun getAvatar() = realData.avatar
        fun getName() = realData.login
    }
}