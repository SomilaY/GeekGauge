package com.somila.geekgauge.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.somila.geekgauge.domain.enums.SyncStatus
import com.somila.geekgauge.domain.models.Report
import com.somila.geekgauge.domain.models.Recommendation
import com.somila.geekgauge.domain.models.Topic
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val sessionId: String,
    val geekId: String,
    val summary: String,
    val feedback: String,
    val topics: String,           // JSON string
    val recommendations: String,  // JSON string
    val manualNotes: String = "",
    val createdAt: Long,
    val syncStatus: String
)

fun ReportEntity.toDomain(): Report = Report(
    id = id,
    sessionId = sessionId,
    geekId = geekId,
    summary = summary,
    feedback = feedback,
    topics = parseTopics(topics),
    recommendations = parseRecommendations(recommendations),
    manualNotes = manualNotes,
    createdAt = createdAt,
    syncStatus = SyncStatus.valueOf(syncStatus)
)

fun Report.toEntity(): ReportEntity = ReportEntity(
    id = id,
    sessionId = sessionId,
    geekId = geekId,
    summary = summary,
    feedback = feedback,
    topics = serializeTopics(topics),
    recommendations = serializeRecommendations(recommendations),
    manualNotes = manualNotes,
    createdAt = createdAt,
    syncStatus = syncStatus.name
)

private fun serializeTopics(topics: List<Topic>): String {
    val array = JSONArray()
    topics.forEach { topic ->
        val obj = JSONObject()
        obj.put("name", topic.name)
        obj.put("confidence", topic.confidence.name)
        array.put(obj)
    }
    return array.toString()
}

private fun parseTopics(json: String): List<Topic> {
    val array = JSONArray(json)
    return (0 until array.length()).map { i ->
        val obj = array.getJSONObject(i)
        Topic(
            name = obj.getString("name"),
            confidence = com.somila.geekgauge.domain.enums.ConfidenceLevel.valueOf(
                obj.getString("confidence")
            )
        )
    }
}

private fun serializeRecommendations(recommendations: List<Recommendation>): String {
    val array = JSONArray()
    recommendations.forEach { rec ->
        val obj = JSONObject()
        obj.put("priority", rec.priority.name)
        obj.put("action", rec.action)
        obj.put("resource", rec.resource)
        array.put(obj)
    }
    return array.toString()
}

private fun parseRecommendations(json: String): List<Recommendation> {
    val array = JSONArray(json)
    return (0 until array.length()).map { i ->
        val obj = array.getJSONObject(i)
        Recommendation(
            priority = com.somila.geekgauge.domain.enums.RecommendationPriority.valueOf(
                obj.getString("priority")
            ),
            action = obj.getString("action"),
            resource = obj.getString("resource")
        )
    }
}