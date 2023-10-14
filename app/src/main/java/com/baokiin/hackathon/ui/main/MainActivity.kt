package com.baokiin.hackathon.ui.main

import android.content.ContentResolver
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baokiin.hackathon.R
import com.baokiin.hackathon.bases.activity.BaseActivity
import com.baokiin.hackathon.data.BitmapModel
import com.baokiin.hackathon.data.network.LoadImage
import com.baokiin.hackathon.data.sql.BitmapDbHelper
import com.baokiin.hackathon.databinding.ActivityMainBinding
import com.baokiin.hackathon.extension.RecyclerViewExt.getFirstVisibleItemPosition
import com.baokiin.hackathon.extension.RecyclerViewExt.getLastVisibleItemPosition
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
    var page = 2

    override fun onInitView() {
        super.onInitView()
        setupRecyclerView()
        loadImageFromCache()
    }

    private fun setupRecyclerView() {
        binding.rcvMainInfo.apply {
            adapter = adapterBitmap
        }
        adapterBitmap.handleOther = {
            lifecycleScope.launch(Dispatchers.IO) {
                val data = bitmapDbHelper.getBitmapsByPage(page,BitmapAdapter.PAGE_LIMIT)
                withContext(Dispatchers.Main) {
                    adapterBitmap.addAllData(data)
                }
                page++
            }
        }
        initLoadMoreTwoWay()
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
                if (index < BitmapAdapter.PAGE_LIMIT + 1) {
                    bitmapTmp.add(bitmapModel)
                }
                if (index == BitmapAdapter.PAGE_LIMIT) {
                    withContext(Dispatchers.Main) {
                        adapterBitmap.updateList(bitmapTmp)
                    }
                }
            }
        }
    }

    private fun initLoadMoreTwoWay() {
        binding.rcvMainInfo.apply {
            addOnScrollListener(object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val totalItems = layoutManager?.itemCount ?: 0
                    val childCount = layoutManager?.childCount ?: 0
                    val first = getFirstVisibleItemPosition()
                    val last = getLastVisibleItemPosition()
                    val canScrollDown = dy > 0
                    val canScrollUp = dy < 0
                    if (!adapterBitmap.isLoading) {
                        adapterBitmap.isLoading = true
                        if (canScrollDown && last + childCount + BitmapAdapter.PAGE_LIMIT > totalItems) {
                            // neu index cua trang hien tai > 1 thi cache n item dau tien mList
                            if (first / BitmapAdapter.PAGE_LIMIT > 5) {
                                adapterBitmap.doCacheFirst()
                            }
                            adapterBitmap.insertBelow()
                        } else if (canScrollUp && first - childCount - BitmapAdapter.PAGE_LIMIT < 0) {
                            if ((totalItems / BitmapAdapter.PAGE_LIMIT) - (last / BitmapAdapter.PAGE_LIMIT) > 5) {
                                adapterBitmap.doCacheLast()
                            }
                            adapterBitmap.insertAbove()
                        }
                        println("Adapter size: ${adapterBitmap.itemCount} first: $first last: $last")
                        adapterBitmap.isLoading = false
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        LoadImage.clearDiskCache()
        super.onDestroy()
    }
}
