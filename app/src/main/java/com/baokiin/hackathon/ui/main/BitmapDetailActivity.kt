package com.baokiin.hackathon.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.databinding.ActivityBitmapDetailBinding

class BitmapDetailActivity :
    BaseActivity<ActivityBitmapDetailBinding>(R.layout.activity_bitmap_detail) {

    companion object{
        val EXTRA_LIST_MODEL = "EXTRA_LIST_MODEL"
    }
    override fun onInitView() {
        super.onInitView()

    }
}
