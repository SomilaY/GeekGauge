package com.somila.geekgauge.domain.usecases

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.repository.ReportRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class ExportReportPdfUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(reportId: String, sessionId: String): Result<File> {
        val report = reportRepository.getReportBySessionId(sessionId)
            ?: return Result.failure(
                IllegalStateException("Report not found for session $sessionId")
            )

        return try {
            val file = createPdf(report)
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(PdfExportException.ExportFailed(e.message ?: "PDF export failed"))
        }
    }

    private fun createPdf(report: Report): File {
        val document = PdfDocument()
        val pageWidth = 595   // A4 width in points
        val pageHeight = 842  // A4 height in points
        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        var yPosition = margin

        // Paint styles
        val titlePaint = Paint().apply {
            color = Color.parseColor("#344e41")
            textSize = 24f
            isFakeBoldText = true
        }
        val headingPaint = Paint().apply {
            color = Color.parseColor("#344e41")
            textSize = 16f
            isFakeBoldText = true
        }
        val bodyPaint = Paint().apply {
            color = Color.parseColor("#2d2d2d")
            textSize = 12f
        }
        val mutedPaint = Paint().apply {
            color = Color.parseColor("#6b6b6b")
            textSize = 11f
        }
        val accentPaint = Paint().apply {
            color = Color.parseColor("#a3b18a")
            textSize = 12f
            isFakeBoldText = true
        }
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#dad7cd")
            strokeWidth = 1f
        }

        // Helper: start a new page when content overflows
        fun checkPageBreak(requiredSpace: Float) {
            if (yPosition + requiredSpace > pageHeight - margin) {
                document.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(
                    pageWidth, pageHeight, pageNumber
                ).create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = margin
            }
        }

        // Helper: draw wrapped text and return new yPosition
        fun drawWrappedText(
            text: String,
            paint: Paint,
            x: Float,
            startY: Float,
            maxWidth: Float
        ): Float {
            val words = text.split(" ")
            val lineBuilder = StringBuilder()
            var currentY = startY

            for (word in words) {
                val testLine = if (lineBuilder.isEmpty()) word else "$lineBuilder $word"
                val textWidth = paint.measureText(testLine)

                if (textWidth > maxWidth && lineBuilder.isNotEmpty()) {
                    checkPageBreak(paint.textSize + 4f)
                    canvas.drawText(lineBuilder.toString(), x, currentY, paint)
                    currentY += paint.textSize + 4f
                    lineBuilder.clear()
                    lineBuilder.append(word)
                } else {
                    lineBuilder.clear()
                    lineBuilder.append(testLine)
                }
            }

            if (lineBuilder.isNotEmpty()) {
                checkPageBreak(paint.textSize + 4f)
                canvas.drawText(lineBuilder.toString(), x, currentY, paint)
                currentY += paint.textSize + 4f
            }

            return currentY
        }

        // ── Title ──
        canvas.drawText("Geek Gauge", margin, yPosition, titlePaint)
        yPosition += 30f
        canvas.drawText("Learner Evaluation Report", margin, yPosition, headingPaint)
        yPosition += 20f

        val dateString = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            .format(Date(report.createdAt))
        canvas.drawText("Generated: $dateString", margin, yPosition, mutedPaint)
        yPosition += 6f

        canvas.drawLine(margin, yPosition, margin + contentWidth, yPosition, dividerPaint)
        yPosition += 20f

        // ── Summary ──
        checkPageBreak(40f)
        canvas.drawText("Summary", margin, yPosition, headingPaint)
        yPosition += 16f
        yPosition = drawWrappedText(report.summary, bodyPaint, margin, yPosition, contentWidth)
        yPosition += 16f

        // ── Topics ──
        checkPageBreak(40f)
        canvas.drawText("Topics Covered", margin, yPosition, headingPaint)
        yPosition += 16f

        report.topics.forEach { topic ->
            checkPageBreak(20f)
            val confidenceColor = when (topic.confidence.name) {
                "HIGH" -> Color.parseColor("#344e41")
                "MEDIUM" -> Color.parseColor("#a3b18a")
                else -> Color.parseColor("#6b6b6b")
            }
            accentPaint.color = confidenceColor
            canvas.drawText(
                "• ${topic.name}  [${topic.confidence.name}]",
                margin + 8f,
                yPosition,
                accentPaint
            )
            yPosition += 18f
        }
        yPosition += 8f

        // ── Feedback ──
        checkPageBreak(40f)
        canvas.drawLine(margin, yPosition, margin + contentWidth, yPosition, dividerPaint)
        yPosition += 16f
        canvas.drawText("Feedback", margin, yPosition, headingPaint)
        yPosition += 16f
        yPosition = drawWrappedText(report.feedback, bodyPaint, margin, yPosition, contentWidth)
        yPosition += 16f

        // ── Recommendations ──
        checkPageBreak(40f)
        canvas.drawLine(margin, yPosition, margin + contentWidth, yPosition, dividerPaint)
        yPosition += 16f
        canvas.drawText("Recommendations", margin, yPosition, headingPaint)
        yPosition += 16f

        report.recommendations.forEach { rec ->
            checkPageBreak(50f)

            val priorityColor = when (rec.priority.name) {
                "MUST" -> Color.parseColor("#c0392b")
                "SHOULD" -> Color.parseColor("#e67e22")
                else -> Color.parseColor("#344e41")
            }
            accentPaint.color = priorityColor
            canvas.drawText("[${rec.priority.name}]", margin + 8f, yPosition, accentPaint)
            yPosition += 16f

            yPosition = drawWrappedText(
                rec.action, bodyPaint, margin + 8f, yPosition, contentWidth - 8f
            )

            if (rec.resource.isNotBlank()) {
                yPosition = drawWrappedText(
                    "Resource: ${rec.resource}",
                    mutedPaint,
                    margin + 8f,
                    yPosition,
                    contentWidth - 8f
                )
            }
            yPosition += 8f
        }

        // ── Manual Notes ──
        if (report.manualNotes.isNotBlank()) {
            checkPageBreak(40f)
            canvas.drawLine(margin, yPosition, margin + contentWidth, yPosition, dividerPaint)
            yPosition += 16f
            canvas.drawText("Trainer Notes", margin, yPosition, headingPaint)
            yPosition += 16f
            yPosition = drawWrappedText(
                report.manualNotes, bodyPaint, margin, yPosition, contentWidth
            )
        }

        document.finishPage(page)

        // Save to app cache directory
        val outputDir = File(context.cacheDir, "reports").also { it.mkdirs() }
        val outputFile = File(outputDir, "report_${report.sessionId}.pdf")
        FileOutputStream(outputFile).use { document.writeTo(it) }
        document.close()

        return outputFile
    }
}

sealed class PdfExportException(message: String) : Exception(message) {
    class ExportFailed(message: String) : PdfExportException(message)
    class ReportNotFound(message: String) : PdfExportException(message)
}