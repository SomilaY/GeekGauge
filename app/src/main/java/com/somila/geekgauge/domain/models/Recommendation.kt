package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.RecommendationPriority

data class Recommendation(
    val priority: RecommendationPriority,
    val action: String,
    val resource: String
)