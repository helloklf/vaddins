package com.omarea.vaddin

import android.app.AlertDialog
import android.content.Context
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import com.omarea.vaddin.AsynSuShellUnit

/**
 * Created by Hello on 2018/02/27.
 */

open class ProgressBarDialog(private var context: Context) {
    private var alert:AlertDialog? = null

    class defaultHandler(private var alertDialog: AlertDialog?): Handler() {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)

            if (msg == null || alertDialog == null) {
                return
            }
            if (msg.what == 10) {
                alertDialog!!.dismiss()
                alertDialog!!.hide()
                Toast.makeText(alertDialog!!.context, "操作已执行", Toast.LENGTH_SHORT).show()
            } else if (msg.what == -1) {
                Toast.makeText(alertDialog!!.context, "执行失败！", Toast.LENGTH_SHORT).show()
            } else if (msg.what == 0 && msg.obj == false) {
                alertDialog!!.dismiss()
                alertDialog!!.hide()
                Toast.makeText(alertDialog!!.context, "执行失败！", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public fun execShell(cmd: String, handler: Handler? = null) {
        hideDialog()

        val layoutInflater = LayoutInflater.from(context)
        val dialog = layoutInflater.inflate(R.layout.dialog_app_options, null)
        val textView = (dialog.findViewById(R.id.dialog_app_details_pkgname) as TextView)
        textView.text = "正在执行，请稍等..."
        alert = AlertDialog.Builder(context).setView(dialog).setCancelable(false).create()
        if (handler == null) {
            AsynSuShellUnit(defaultHandler(alert)).exec(cmd).waitFor()
        } else {
            AsynSuShellUnit(handler).exec(cmd).waitFor()
        }
        alert!!.show()
    }

    public fun execShell(sb: StringBuilder, handler: Handler? = null) {
        execShell(sb.toString(), handler)
    }

    public fun isDialogShow(): Boolean {
        return this.alert != null
    }

    public fun hideDialog() {
        if (alert != null) {
            alert!!.dismiss()
            alert!!.hide()
            alert = null
        }
    }

    public fun showDialog(text:String = "正在加载，请稍等...") : AlertDialog? {
        hideDialog()
        val layoutInflater = LayoutInflater.from(context)
        val dialog = layoutInflater.inflate(R.layout.dialog_app_options, null)
        val textView = (dialog.findViewById(R.id.dialog_app_details_pkgname) as TextView)
        textView.text = text
        alert = AlertDialog.Builder(context).setView(dialog).setCancelable(false).create()
        alert!!.show()
        return alert
    }
}
