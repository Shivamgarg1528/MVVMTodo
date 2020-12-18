package com.codinginflow.mvvmtodo.util

import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

inline fun SearchView.onQueryTextChanged(crossinline listener: (String) -> Unit) {
    this.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            listener(newText.orEmpty())
            return true
        }
    })
}

fun <T> LiveData<T>.debounce(duration: Long = 10000L) = MediatorLiveData<T>().also { mld ->
    val source = this
    val handler = Handler(Looper.getMainLooper())

    val runnable = Runnable {
        mld.value = source.value
    }

    mld.addSource(source) {
        handler.removeCallbacks(runnable)
        handler.postDelayed(runnable, duration)
    }
}