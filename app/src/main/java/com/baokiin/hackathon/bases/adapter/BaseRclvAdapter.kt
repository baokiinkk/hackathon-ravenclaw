package com.baokiin.hackathon.bases.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import java.util.*

abstract class BaseRclvAdapter<T> : RecyclerView.Adapter<BaseRclvHolder<ViewDataBinding, T>>(){
    protected var listTmp: MutableList<T> = mutableListOf()
    protected var dataSet: MutableList<T> = ArrayList<T>()
    var showEmptyCallBack: (show: Boolean) -> Unit = {}

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): BaseRclvHolder<ViewDataBinding, T> {
        val layout = getLayoutResource(viewType)
        val v = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(viewGroup.context), layout, viewGroup, false)
        return onCreateVH(v, viewType) as BaseRclvHolder<ViewDataBinding, T>
    }

    override fun onBindViewHolder(baseRclvHolder: BaseRclvHolder<ViewDataBinding, T>, position: Int) {
        baseRclvHolder.onBind(getItemDataAtPosition(position) )
    }

    override fun onBindViewHolder(
        baseRclvHolder: BaseRclvHolder<ViewDataBinding, T>,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(baseRclvHolder, position, payloads)
        } else {
            baseRclvHolder.onBind(getItemDataAtPosition(position), payloads)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    protected fun getItemDataAtPosition(position: Int): T {
        return dataSet[position]
    }
    abstract fun getLayoutResource(viewType: Int): Int
    abstract fun onCreateVH(itemView: ViewDataBinding, viewType: Int): BaseRclvHolder<*, *>

    protected fun addItem(item: T) {
        dataSet.add(item)
        listTmp.add(item)
    }

    protected fun addItems(items: List<T>) {
        dataSet.addAll(items)
        listTmp.addAll(items)
    }

    protected fun addItemAndNotify(item: T) {
        dataSet.add(item)
        listTmp.add(item)
        notifyItemInserted(dataSet.size - 1)
    }

    protected fun addItemAtIndexAndNotify(index: Int, item: T) {
        dataSet.add(index, item)
        listTmp.add(index, item)
        notifyItemInserted(index)
    }
    protected fun addItemsAtIndexAndNotify(index: Int, items: List<T>) {
        dataSet.addAll(index, items)
        listTmp.addAll(index, items)
        notifyItemRangeInserted(index,items.size)
    }
    protected fun addItemsAtIndex(items: List<T>, index: Int) {
        dataSet.addAll(index, items)
        listTmp.addAll(index, items)
    }

    protected fun addItemAtIndex(item: T, index: Int) {
        dataSet.add(index, item)
        listTmp.add(index, item)
    }

    protected fun addItemsAndNotify(items: List<T>) {
        val start = dataSet.size
        dataSet.clear()
        dataSet.addAll(items)
        listTmp.addAll(items)
        notifyItemRangeInserted(start, items.size)
    }

    protected fun addAllItems(items: List<T>) {
        val start = dataSet.size
        dataSet.addAll(items)
        notifyItemRangeInserted(start, items.size-1)
    }

    protected fun removeItemAndNotify(index: Int) {
        dataSet.removeAt(index)
        listTmp.removeAt(index)
        notifyItemRemoved(index)
    }

    protected fun reset(newItems: List<T>) {
        clearData()
        addItems(newItems)
        notifyDataSetChanged()
    }
    protected fun resetDataSet(newItems: List<T>){
        dataSet.clear()
        dataSet.addAll(newItems)
        notifyDataSetChanged()
    }

    protected fun remove(index: Int) {
        dataSet.removeAt(index)
        listTmp.removeAt(index)
        notifyItemRemoved(index)
    }

    protected fun clearData() {
        dataSet.clear()
        listTmp.clear()
    }
    fun setListenerIsListEmpty(calback:(show: Boolean) -> Unit){
        showEmptyCallBack = calback
    }
     fun baseFilter(handle:(Pair<T, CharSequence>)->Boolean): Filter {
        return object : Filter() {
            override fun performFiltering(charString: CharSequence): FilterResults {
                val charSequence = charString.toString()
                val list:ArrayList<T> = arrayListOf()
                val results = FilterResults()
                if (charSequence.isEmpty())
                    list.addAll(listTmp)
                else {
                    val _list = listTmp.filter {
                        handle(Pair(it,charString))
                    }
                    list.addAll(_list)
                }
                results.values = list
                return results
            }

            override fun publishResults(p0: CharSequence, filterResults: FilterResults) {
                val newlist = mutableListOf<T>()
                if (filterResults.values != null) {
                    newlist.addAll(filterResults.values as MutableList<T>)
                    showEmptyCallBack(newlist.isEmpty())
                } else {
                    newlist.addAll(listTmp)
                    showEmptyCallBack(false)
                }
                submitlist(newlist)

            }
        }
    }
    fun submitlist(newItems: List<T>){
        val diffResult = DiffUtil.calculateDiff(MyDiffUtil(newItems))
        dataSet.clear()
        dataSet.addAll(newItems)
        diffResult.dispatchUpdatesTo(this)
    }
    inner class MyDiffUtil(val newItems: List<T>) :
        DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return dataSet.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return dataSet[oldItemPosition] == newItems[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            // Kiểm tra xem 2 item có giống nhau hay không
            return dataSet[oldItemPosition] == newItems[newItemPosition]
        }
    }
}