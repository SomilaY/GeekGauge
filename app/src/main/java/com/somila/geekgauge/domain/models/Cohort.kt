package com.somila.geekgauge.domain.models

data class Cohort(
    val id: String,
    val name: String,
    val programme: String,
    val startDate: String,
    val geeks: List<User>
)

