package com.neo.fbrules.main.presenter.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoricModel(
    val list: MutableList<Pair<Int, String>>,
    var point: Int = list.size
) : Parcelable
