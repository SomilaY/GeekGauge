package com.somila.geekgauge.domain.models

import com.somila.geekgauge.domain.enums.ConfidenceLevel

data class Topic(
    val name: String,
    val confidence: ConfidenceLevel
)