package com.baokiin.hackathon.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.TransitionInflater
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.databinding.ActivityBitmapDetailBinding
import com.baokiin.hackathon.extension.fromJson
import com.baokiin.hackathon.extension.fromJsonTypeToken

class BitmapDetailActivity :
    BaseActivity<ActivityBitmapDetailBinding>(R.layout.activity_bitmap_detail) {

    companion object{
        val EXTRA_LIST_MODEL = "EXTRA_LIST_MODEL"
    }
    private val data:List<BitmapModel>? by lazy {
        intent.getStringExtra(EXTRA_LIST_MODEL)?.fromJsonTypeToken()
    }
    override fun onInitView() {
        super.onInitView()
        window.sharedElementEnterTransition =
            TransitionInflater.from(this).inflateTransition(R.transition.shared_element_transition)
        binding.ivDetail.transitionName = data?.get(0)?.path
    }
}
