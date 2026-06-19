package com.somila.geekgauge.presentation.report

import java.io.File

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val file: File) : ExportState()
    data class Error(val message: String) : ExportState()
}