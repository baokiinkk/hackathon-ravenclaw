package com.baokiin.hackathon.bases.activity

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.baokiin.hackathon.extension.launch.VnpayLaunch

abstract class BaseActivity<DB : ViewDataBinding>(@LayoutRes open val layoutRes: Int) :
    AppCompatActivity() {

    /**
     * Binding view
     */
    protected lateinit var binding: DB

    protected val vnpayLaunch by lazy {
        VnpayLaunch(this,lifecycle)
    }

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

    open fun onInitBinding(){}

//    /**
//     * Call before create view
//     */
//    fun onPrepareInitView() {}

    /**
     * Call after finish create view
     */
    open fun onInitView(){}
    open fun onPrepareInitView(){}
    /**
     * Call after init view , observer data changed
     */
    open fun observerData(){}
    open fun listenerView(){}

    open fun handleBack() {

    }
}