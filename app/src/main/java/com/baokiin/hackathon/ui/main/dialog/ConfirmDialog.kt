package com.baokiin.hackathon.ui.main.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import com.baokiin.hackathon.R
import com.baokiin.hackathon.extension.clear
import com.baokiin.hackathon.extension.gone
import com.baokiin.hackathon.extension.setSafeOnClickListener
import com.baokiin.hackathon.extension.visible

class ConfirmDialog(context: Context) : Dialog(context, R.style.AppTheme) {
    private var btnNoticeDlgRight: Button? = null
    private var btnNoticeDlgLeft: Button? = null
    private var tvNoticeDlgTitle: TextView? = null
    private var tvNoticeDlgContent: TextView? = null
    private var ivNoticeDlgLogo: ImageView? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_confirm)
        findView()
        setTouchArea(false)

    }

    fun setTouchArea(b: Boolean) {
        setCancelable(b)
        setCanceledOnTouchOutside(b)
    }

    fun newBuild(): ConfirmDialog {
        show()
        tvNoticeDlgContent?.clear()
        btnNoticeDlgLeft?.gone()
        btnNoticeDlgRight?.visible()
        tvNoticeDlgTitle?.text = context.getString(R.string.title_noti)
        btnNoticeDlgRight?.text = context.getString(R.string.label_close)
        ivNoticeDlgLogo?.setImageResource(R.drawable.ic_error)
        return this
    }

    fun setTitleDialog(content: String?): ConfirmDialog {
        tvNoticeDlgTitle?.text = content
        return this
    }

    fun setTitleDialog(@StringRes content: Int?): ConfirmDialog {
        content?.let {
            tvNoticeDlgTitle?.setText(it)
        }
        return this
    }

    fun setNotice(content: String?, nameButton: String? = null): ConfirmDialog {
        try {
            configShowDialog(content, nameButton)
            btnNoticeDlgRight?.setSafeOnClickListener {
                dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun setNotice(
        content: String?,
        nameButton: String? = null,
        callback: () -> Unit
    ): ConfirmDialog {
        try {
            configShowDialog(content, nameButton)
            btnNoticeDlgRight?.setSafeOnClickListener {
                callback.invoke()
                dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun setNotice(@StringRes content: Int): ConfirmDialog {
        try {
            configShowDialog(context.getString(content))
            btnNoticeDlgRight?.setSafeOnClickListener {
                dismiss()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this
    }

    fun showSuccessDialog() {
        btnNoticeDlgLeft?.text = getContext().getString(R.string.title_continues)
        tvNoticeDlgTitle?.text = context.getString(R.string.title_success)
        ivNoticeDlgLogo?.setImageResource(R.drawable.ic_success)
    }

    fun showErrorDialog() {
        btnNoticeDlgLeft?.text = getContext().getString(R.string.label_close)
        tvNoticeDlgTitle?.text = context.getString(R.string.title_error)
        ivNoticeDlgLogo?.setImageResource(R.drawable.ic_error)
    }

    fun addButtonRight(
        name: String? = null,
        onClick: () -> Unit
    ): ConfirmDialog {
        btnNoticeDlgRight?.apply {
            setSafeOnClickListener {
                dismiss()
                onClick()
            }
            visibility = View.VISIBLE
            name?.let { text = it }
        }
        return this
    }

    fun addButtonLeft(name: String? = null, onClick: (() -> Unit)? = null): ConfirmDialog {
        btnNoticeDlgLeft?.apply {
            setSafeOnClickListener {
                dismiss()
                onClick?.invoke()
            }
            visibility = View.VISIBLE
            text = name ?: context.getString(R.string.label_close)
        }
        return this
    }

    private fun configShowDialog(content: String?, nameButton: String? = null) {
        tvNoticeDlgContent?.text = content
        nameButton?.let {
            btnNoticeDlgRight?.text = it
        }
    }

    private fun findView() {
        btnNoticeDlgRight = findViewById(R.id.btnNoticeDlgRight)
        btnNoticeDlgLeft = findViewById(R.id.btnNoticeDlgLeft)
        tvNoticeDlgTitle = findViewById(R.id.tvNoticeDlgTitle)
        tvNoticeDlgContent = findViewById(R.id.tvNoticeDlgContent)
        ivNoticeDlgLogo = findViewById(R.id.ivNoticeDlgLogo)
    }
}
