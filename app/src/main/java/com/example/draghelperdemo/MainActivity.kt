package com.example.draghelperdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.draghelperdemo.adapter.ItemAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

  private var mList = ArrayList<String>()
  private var mAdapter: ItemAdapter? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main_2)
    initView()
  }

  private fun initView() {
    (1..100).forEach { mList.add(it.toString()) }
    mAdapter = ItemAdapter(mList)
    my_recyclerview.layoutManager = LinearLayoutManager(this)
    my_recyclerview.adapter = mAdapter
  }
}