package com.baokiin.hackathon.extension.launch

import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResult
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

class VnpayLaunch(val context: FragmentActivity, val lifeCycle: Lifecycle) : DefaultLifecycleObserver {
    private var resultLauncher: OpenResultLauncher? = null
    private var resultPermissionLauncher: OpenPermissionResultLauncher? = null
    override fun onDestroy(owner: LifecycleOwner) {
        unregister()
        super.onDestroy(owner)
    }

    fun register() {
        this.lifeCycle.addObserver(this)
        resultLauncher = OpenResultLauncher()
        resultLauncher?.register(context)
    }

    fun registerFragment(context: Fragment) {
        resultLauncher = OpenResultLauncher()
        resultLauncher?.register(context)
    }


    fun launch(intent: Intent? = null, callback: ((ActivityResult) -> Unit)) {
        resultLauncher?.launch(
            intent
        ) { result ->
            callback.invoke(result)
        }
    }

    fun registerPermission() {
        this.lifeCycle.addObserver(this)
        resultPermissionLauncher = OpenPermissionResultLauncher()
        resultPermissionLauncher?.register(context)
    }

    fun launchPermission(
        arrayPermission: Array<String>,
        callback: PermissionsCallBack

    ) {
        context?.let { activity ->
            resultPermissionLauncher?.launch(arrayPermission) { permissionResultMap ->
                var countSuccess = 0
                val arrayPermissionFail = arrayListOf<String>()
                permissionResultMap.forEach {
                    if (ActivityCompat.checkSelfPermission(
                            activity, it.key
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        countSuccess++
                    } else {
                        arrayPermissionFail.add(it.key)
                    }
                }
                if (countSuccess == arrayPermission.size) {
                    callback.onSuccess()
                } else {
                    if (arrayPermissionFail.isNotEmpty()) {
                        shouldShowRequestPermissionsRationale(activity, arrayPermissionFail).let {
                            if (it.isEmpty()) {
                                callback.onFail(arrayPermissionFail)
                            }
                            callback.onDeny(arrayPermissionFail)
                        }
                    }
                }
            }
        }
    }

    private fun unregister() {
        resultLauncher?.unRegister()
        resultPermissionLauncher?.unRegister()
        this.lifeCycle?.removeObserver(this)
    }

    private fun shouldShowRequestPermissionsRationale(
        context: FragmentActivity,
        permissions: ArrayList<String>
    ): Array<String> {
        val listFail = arrayListOf<String>()
        for (permission in permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                listFail.add(permission)
            }
        }
        val result = arrayOfNulls<String>(listFail.size)
        return listFail.toArray(result)
    }

    interface PermissionsCallBack {
        fun onSuccess()
        fun onDeny(listDenyPermission: ArrayList<String>)
        fun onFail(listFailPermission: ArrayList<String>)
    }
}