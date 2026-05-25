package com.example.ui

import com.example.R

/**
 * 三国人生模拟器图像资源索引。
 * 当前已接入天下地图；头像和战场图会继续分批补入。
 * build trigger: visual-pack-v1-map
 */
object GameImageAssets {
    object Maps {
        val SANGUO_OVERVIEW = R.drawable.map_sanguo_overview
    }

    fun imageForEvent(eventId: String): Int? = when (eventId) {
        else -> null
    }

    fun portraitForFigure(name: String): Int? = when (name) {
        else -> null
    }
}
