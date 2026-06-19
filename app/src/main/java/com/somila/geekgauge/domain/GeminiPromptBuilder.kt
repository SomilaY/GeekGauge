package com.somila.geekgauge.domain

import com.somila.geekgauge.data.gemini.Content
import com.somila.geekgauge.data.gemini.Part
import com.somila.geekgauge.data.gemini.SystemInstruction

object GeminiPromptBuilder {

    fun buildSystemInstruction(
        sessionType: String,
        geekName: String
    ): SystemInstruction {
        return SystemInstruction(
            parts = listOf(
                Part(
                    text = """
You are an expert technical education evaluator.

Analyze this learner evaluation transcript.

Session Type: $sessionType
Learner: $geekName

You must return ONLY valid JSON without any markdown formatting, code blocks, or additional text.

JSON Schema:
{
  "summary": "Brief session summary",
  "topics": [
    {
      "name": "Topic name",
      "confidence": "HIGH|MEDIUM|LOW"
    }
  ],
  "feedback": "Detailed feedback narrative",
  "recommendations": [
    {
      "priority": "MUST|SHOULD|GOOD",
      "action": "Specific action item",
      "resource": "Learning resource or reference"
    }
  ]
}
                    """.trimIndent()
                )
            )
        )
    }

    fun buildUserContent(transcript: String): Content {
        return Content(
            role = "user",
            parts = listOf(
                Part(text = transcript)
            )
        )
    }
}