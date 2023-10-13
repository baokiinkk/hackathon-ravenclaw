package com.baokiin.hackathon.extension.launch

import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

abstract class IActivityResultLauncher<I, O> {

    var isRegister: Boolean = false

    private var TAG = javaClass.simpleName
    private lateinit var launcher: ActivityResultLauncher<I>
    private var callback: ((O) -> Unit)? = null

    protected abstract fun getActivityContract(): ActivityResultContract<I, O>


    fun register(activity: FragmentActivity) {
        launcher = activity.registerForActivityResult(getActivityContract()) {
            callback?.invoke(it)
        }
        isRegister = true
    }
    fun unRegister(){
        launcher.unregister()
    }

    /**
     * Must call before onCreate in fragment
     */
    fun register(fragment: Fragment) {
        launcher = fragment.registerForActivityResult(getActivityContract()) {
            callback?.invoke(it)
        }
        isRegister = true
    }

    fun launch(input: I? = null, callback: ((O) -> Unit) = {}) {
        if (!isRegister) {
            return
        }
        this.callback = callback
        launcher.launch(input)
    }


}
