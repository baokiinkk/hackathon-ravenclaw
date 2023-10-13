package com.baokiin.hackathon.ui.main

import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.data.InfoResponse
import com.baokiin.hackathon.data.network.ApiResult
import com.baokiin.hackathon.data.network.HttpUtils
import com.baokiin.hackathon.data.network.LoadImage
import com.baokiin.hackathon.databinding.ActivityMainBinding

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val adapter by lazy {
        InfoAdapter()
    }

    override fun onInitView() {
        super.onInitView()
        binding.rcvMainInfo.adapter = adapter
        HttpUtils.g().requestApi(
            url = "https://api.github.com/search/users?q=a&page=1&per_page=100",
            apiResult = object : ApiResult<InfoResponse> {
                override fun loading(isLoading: Boolean) {

                }

                override fun successful(data: InfoResponse) {
                    adapter.updateList(data.items)
                }

                override fun error(error: String) {

                }

            })
    }

    override fun onDestroy() {
        LoadImage.clearDiskCache()
        super.onDestroy()
    }
}