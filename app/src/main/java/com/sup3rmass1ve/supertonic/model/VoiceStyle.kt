package com.sup3rmass1ve.supertonic.model

data class VoiceStyle(
    val name: String,
    val styleTtl: StyleData,
    val styleDp: StyleData
)

data class StyleData(
    val dims: List<Int>,
    val data: List<List<List<Float>>>
)

data class VoiceStyleJson(
    val style_ttl: StyleDataJson,
    val style_dp: StyleDataJson
)

data class StyleDataJson(
    val dims: List<Int>,
    val data: List<List<List<Float>>>
)
