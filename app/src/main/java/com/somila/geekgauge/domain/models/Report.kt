package com.somila.geekgauge.domain.models
data class Report(
    val id: String,
    val sessionId: String,
    val summary: String,
    val topics: List<String>,
    val feedback: String,
    val recommendations: List<String>
)