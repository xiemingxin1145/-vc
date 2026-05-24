package com.example.ui

import com.example.R

/**
 * 三国插图库索引。
 *
 * 用于标题页轮播、事件弹窗、人物传记背景图。
 */
object SanguoGallery {

    data class GalleryArt(
        val id: String,
        val title: String,
        val resId: Int,
        val caption: String
    )

    val eventArts: List<GalleryArt> = listOf(
        GalleryArt("yellow_turban", "黄巾乱起", R.drawable.illust_yellow_turban_oath, "乱世第一声惊雷，草莽与豪杰同场登台。"),
        GalleryArt("luoyang_night", "洛阳夜行", R.drawable.illust_luoyang_night, "宫阙深处藏着朝廷旧梦，也藏着一生转机。"),
        GalleryArt("longzhong", "隆中对策", R.drawable.illust_longzhong_strategy, "一张地图，三分天下。"),
        GalleryArt("red_cliff", "赤壁东风", R.drawable.illust_red_cliff_wind, "江风一起，天下格局改写。"),
        GalleryArt("silk_road", "丝路奇货", R.drawable.illust_market_silk_road, "商队带来的不只是货物，也是情报和野心。"),
        GalleryArt("palace_shadow", "宫墙黑影", R.drawable.illust_palace_shadow, "朝堂无刀，却处处见锋。")
    )
}
