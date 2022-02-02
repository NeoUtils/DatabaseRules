package com.neo.fbrules.main.presenter.model

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData

/**
 * @author Irineu A. Silva
 */
class MutableSingleLiveData<T>(value: T? = null) {

    private val liveData = if (value == null)
        MutableLiveData<T?>()
    else
        MutableLiveData<T?>(value)

    fun singleObserve(owner: LifecycleOwner, function: (T) -> Unit) {
        liveData.observe(owner) { value ->
            value?.let {
                liveData.value = null
                function.invoke(value)
            }
        }
    }

    fun postValue(value: T) {
        liveData.postValue(value)
    }

    fun setValue(value: T) {
        liveData.value = value
    }

    fun getValue() = liveData.value
}
