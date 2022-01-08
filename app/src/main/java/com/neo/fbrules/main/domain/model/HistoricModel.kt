package com.neo.fbrules.main.domain.model

data class HistoricModel(
    val list: MutableList<Pair<Int, String>>,
    var point: Int = list.size
)
