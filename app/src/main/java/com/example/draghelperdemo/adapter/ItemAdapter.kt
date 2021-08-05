package com.example.draghelperdemo.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.draghelperdemo.R
import kotlinx.android.synthetic.main.item_layout.view.*
import java.util.*

class ItemAdapter(val mList: ArrayList<String>): RecyclerView.Adapter<ItemAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_layout, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.itemView.item_text.text = "ITEM $position"
  }

  override fun getItemCount(): Int {
    return mList.size
  }

  inner class ViewHolder(rootView: View): RecyclerView.ViewHolder(rootView)
}