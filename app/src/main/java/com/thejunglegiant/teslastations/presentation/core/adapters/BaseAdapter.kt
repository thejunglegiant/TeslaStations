package com.thejunglegiant.teslastations.presentation.core.adapters

import android.annotation.SuppressLint
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<P> : RecyclerView.Adapter<BaseViewHolder<P>>() {
    private var mDataList: MutableList<P> = ArrayList()
    private val mFilteredDataList: List<P>
        get() = mDataList.filter { filterPredicate?.invoke(it) ?: true }

    protected open var mCallback: BaseAdapterCallback<P>? = null
    private var filterPredicate: ((item: P) -> Boolean)? = null

    fun attachCallback(callback: BaseAdapterCallback<P>) {
        this.mCallback = callback
    }

    fun detachCallback() {
        this.mCallback = null
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterData(filterPredicate: (item: P) -> Boolean) {
        this.filterPredicate = filterPredicate
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setData(dataList: List<P>) {
        mDataList.clear()
        mDataList.addAll(dataList)
        notifyDataSetChanged()
    }

    fun addData(dataList: List<P>) {
        val lastOldItemPosition = mDataList.size - 1
        mDataList.addAll(dataList)
        notifyItemRangeChanged(lastOldItemPosition, dataList.size)
    }

    fun addItem(newItem: P) {
        mDataList.add(newItem)
        notifyItemInserted(mDataList.size - 1)
    }

    fun updateItem(newItem: P) {
        val position = mDataList.indexOf(newItem)
        if (position > -1) {
            mDataList[position] = newItem
            notifyItemChanged(position)
        }
    }

    fun removeItem(newItem: P): Boolean {
        val position = mDataList.indexOf(newItem)
        if (position > -1) {
            mDataList.remove(newItem)
            notifyItemRemoved(position)
        }
        return position > -1
    }

    fun addItemToTop(newItem: P) {
        mDataList.add(0, newItem)
        notifyItemInserted(0)
    }

    fun getItemSafe(position: Int): P? {
        return position.takeIf { it in 0..itemCount }?.let { mFilteredDataList[position] }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<P>, position: Int) {
        holder.bind(mFilteredDataList[position])

        holder.itemView.setOnClickListener {
            mCallback?.onItemClick(mFilteredDataList[position], holder.itemView)
        }
        holder.itemView.setOnLongClickListener {
            mCallback?.onLongClick(mFilteredDataList[position], holder.itemView) ?: false
        }
    }

    override fun getItemCount(): Int = mFilteredDataList.count()
}
