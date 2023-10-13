package com.baokiin.hackathon.bases.adapter

import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

open class BaseRclvHolder<VB: ViewDataBinding, DATA>(val viewBinding: VB) : RecyclerView.ViewHolder(viewBinding.root) {

    fun clickOn(view: View, listener: View.OnClickListener){
    }

    open fun onBind(vhData: DATA) {}
    open fun onBind(vhData: DATA, payloads: List<Any>) {}
    open fun clearData(){}
}