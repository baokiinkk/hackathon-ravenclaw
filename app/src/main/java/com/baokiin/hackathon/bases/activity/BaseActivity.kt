package com.baokiin.hackathon.bases.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.extension.launch.VnpayLaunch
import com.baokiin.hackathon.ui.main.dialog.ConfirmDialog
import com.baokiin.hackathon.ui.main.dialog.LoadingDialog

abstract class BaseActivity<DB : ViewDataBinding>(@LayoutRes open val layoutRes: Int) :
    AppCompatActivity() {

    /**
     * Binding view
     */
    protected lateinit var binding: DB

    protected val vnpayLaunch by lazy {
        VnpayLaunch(this, lifecycle)
    }

    val confirm by lazy { ConfirmDialog(this) }

    val loading by lazy { LoadingDialog(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        onPrepareInitView()
        super.onCreate(savedInstanceState)
        vnpayLaunch.register()
        binding = DataBindingUtil.setContentView(this, layoutRes)
        binding.lifecycleOwner = this


        onInitBinding()
        onInitView()
        observerData()
        listenerView()

    }

    fun hideLoading() {
        if (loading.isShowing) {
            loading.dismiss()
        }
    }

    fun showLoading() {
        if (!loading.isShowing) {
            loading.show()
        }
    }

    open fun onInitBinding() {}

//    /**
//     * Call before create view
//     */
//    fun onPrepareInitView() {}

    /**
     * Call after finish create view
     */
    open fun onInitView() {}
    open fun onPrepareInitView() {}

    /**
     * Call after init view , observer data changed
     */
    open fun observerData() {}
    open fun listenerView() {}

    open fun handleBack() {

    }
}
