package com.kivous.passwordmanager.util

import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment

object Extensions {

    fun View.visible() {
        this.visibility = View.VISIBLE
    }

    fun View.invisible() {
        this.visibility = View.INVISIBLE
    }

    fun View.gone() {
        this.visibility = View.GONE
    }

    fun Fragment.toast(msg: Any?) {
        Toast.makeText(requireContext(), msg.toString(), Toast.LENGTH_SHORT).show()
    }

    fun logD(msg: Any?) {
        Log.d("SSSS", msg.toString())
    }

    fun timeStamp(): Long = System.currentTimeMillis()

}