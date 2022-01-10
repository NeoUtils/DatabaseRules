package com.neo.fbrules.main.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class HistoricModel(
    val list: MutableList<Pair<Int, String>>,
    var point: Int = list.size
) : Parcelable
