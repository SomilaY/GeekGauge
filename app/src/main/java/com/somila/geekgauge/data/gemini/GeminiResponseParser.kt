package com.somila.geekgauge.data.gemini

import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.models.Topic
import com.somila.geekgauge.domain.models.Recommendation
import com.somila.geekgauge.domain.enums.ConfidenceLevel
import com.somila.geekgauge.domain.enums.RecommendationPriority
import com.somila.geekgauge.domain.enums.SyncStatus
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiResponseParser @Inject constructor() {

    fun parse(
        jsonString: String,
        sessionId: String,
        geekId: String
    ): Result<Report> {
        return try {
            val json = JSONObject(jsonString)

            val topics = json.getJSONArray("topics").let { array ->
                (0 until array.length()).map { i ->
                    val topic = array.getJSONObject(i)
                    Topic(
                        name = topic.getString("name"),
                        confidence = ConfidenceLevel.valueOf(
                            topic.getString("confidence").uppercase()
                        )
                    )
                }
            }

            val recommendations = json.getJSONArray("recommendations").let { array ->
                (0 until array.length()).map { i ->
                    val rec = array.getJSONObject(i)
                    Recommendation(
                        priority = RecommendationPriority.valueOf(
                            rec.getString("priority").uppercase()
                        ),
                        action = rec.getString("action"),
                        resource = rec.getString("resource")
                    )
                }
            }

            val report = Report(
                id = UUID.randomUUID().toString(),
                sessionId = sessionId,
                geekId = geekId,
                summary = json.getString("summary"),
                feedback = json.getString("feedback"),
                topics = topics,
                recommendations = recommendations,
                syncStatus = SyncStatus.PENDING
            )

            Result.success(report)
        } catch (e: Exception) {
            Result.failure(
                GeminiException.ParseError("Failed to parse Gemini response: ${e.message}")
            )
        }
    }
}