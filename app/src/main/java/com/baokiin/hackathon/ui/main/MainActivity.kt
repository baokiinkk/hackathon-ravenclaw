package com.baokiin.hackathon.ui.main

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage
import com.baokiin.hackathon.data.sql.BitmapDbHelper
import com.baokiin.hackathon.databinding.ActivityMainBinding
import com.baokiin.hackathon.extension.RecyclerViewExt.loadMore
import com.baokiin.hackathon.extension.launch.VnpayLaunch
import com.baokiin.hackathon.utils.FileUtils
import com.baokiin.hackathon.utils.PermissionUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : BaseActivity<ActivityMainBinding>(R.layout.activity_main) {

    private val adapterBitmap by lazy {
        BitmapAdapter()
    }

    private val bitmapDbHelper by lazy {
        BitmapDbHelper(this)
    }


    override fun onInitView() {
        super.onInitView()
        setupRecyclerView()
        loadImageFromCache()
    }

    var page = 2
    private fun setupRecyclerView() {
        binding.rcvMainInfo.apply {
            adapter = adapterBitmap
            loadMore {
                val data =  bitmapDbHelper.getBitmapsByPage(page,10)
                adapterBitmap.addAllData(data)
                page++
            }
        }
    }

    private fun loadImageFromCache() {
        vnpayLaunch.launchPermission(
            arrayPermission = PermissionUtils.permissionStorage(),
            callback = object : VnpayLaunch.PermissionsCallBack {
                override fun onSuccess() {
                    handleGetAllFilePaths()
                }

                override fun onDeny(listDenyPermission: ArrayList<String>) {
                    confirm.newBuild().setNotice(R.string.message_error_request_gallery_permission)
                }

                override fun onFail(listFailPermission: ArrayList<String>) {
                    confirm.newBuild().setNotice(R.string.message_error_request_gallery_permission)
                }
            }
        )
    }

    private fun handleGetAllFilePaths() {
        lifecycleScope.launch(Dispatchers.IO) {
            val paths = FileUtils.getAllFilePaths()
            val bitmapTmp = mutableListOf<BitmapModel>()
            paths.forEachIndexed { index, bitmapModel ->
                bitmapDbHelper.insertBitmap(bitmapModel)
                if (index < 21) {
                    bitmapTmp.add(bitmapModel)
                }
                if (index == 20) {
                    withContext(Dispatchers.Main) {
                        adapterBitmap.updateList(bitmapTmp)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        LoadImage.clearDiskCache()
        super.onDestroy()
    }
}
