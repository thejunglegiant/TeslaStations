package com.thejunglegiant.teslastations.presentation.core

import android.os.Handler
import android.os.Looper
import androidx.annotation.MainThread
import androidx.collection.ArraySet
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer

class EventLiveData<T> : MediatorLiveData<T>() {

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val observers = ArraySet<ObserverWrapper<in T>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun observeForever(observer: Observer<in T>) {
        val wrapper = ObserverWrapper(observer)
        observers.add(wrapper)
        super.observeForever(wrapper)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T>) {
        observers.indexOf(observer).takeIf { it != -1 }?.let { index ->
            observers.removeAt(index)
            super.removeObserver(observer)
            return
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        observers.forEach { it.newValue() }
        super.setValue(t)
    }

    override fun postValue(value: T) {
        mainHandler.post { setValue(value) }
    }

    private class ObserverWrapper<T>(val observer: Observer<T>) : Observer<T> {

        private var pending = false

        override fun onChanged(t: T?) {
            if (pending) {
                pending = false
                observer.onChanged(t)
            }
        }

        fun newValue() {
            pending = true
        }
    }
}
