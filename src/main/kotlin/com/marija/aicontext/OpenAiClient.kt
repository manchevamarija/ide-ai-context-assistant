package com.marija.aicontext

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class OpenAiClient(
    private val apiKey: String = System.getenv("OPENAI_API_KEY").orEmpty(),
    private val model: String = (System.getenv("OPENAI_MODEL") ?: "gpt-4o-mini").ifBlank { "gpt-4o-mini" },
    private val endpoint: String = (System.getenv("OPENAI_BASE_URL") ?: "https://api.openai.com/v1/chat/completions")
        .ifBlank { "https://api.openai.com/v1/chat/completions" }
) {
    private val client = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(20))
        .build()

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    fun ask(prompt: String): Result<String> {
        if (!isConfigured()) {
            return Result.failure(IllegalStateException("OPENAI_API_KEY is not configured."))
        }

        val request = HttpRequest.newBuilder()
            .uri(URI.create(endpoint))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $apiKey")
            .POST(HttpRequest.BodyPublishers.ofString(requestBody(prompt)))
            .build()

        return runCatching {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() !in 200..299) {
                error("AI request failed with status ${response.statusCode()}: ${response.body()}")
            }
            extractContent(response.body())
        }
    }

    private fun requestBody(prompt: String): String {
        return """
            {
              "model": ${json(model)},
              "messages": [
                {
                  "role": "system",
                  "content": "You are a concise coding assistant integrated into an IDE."
                },
                {
                  "role": "user",
                  "content": ${json(prompt)}
                }
              ],
              "temperature": 0.2
            }
        """.trimIndent()
    }

    private fun json(value: String): String {
        return buildString {
            append('"')
            value.forEach { char ->
                when (char) {
                    '\\' -> append("\\\\")
                    '"' -> append("\\\"")
                    '\n' -> append("\\n")
                    '\r' -> append("\\r")
                    '\t' -> append("\\t")
                    else -> append(char)
                }
            }
            append('"')
        }
    }

    private fun extractContent(body: String): String {
        val contentRegex = Regex(""""content"\s*:\s*"((?:\\.|[^"\\])*)"""")
        val match = contentRegex.find(body) ?: return body
        return match.groupValues[1]
            .replace("\\n", "\n")
            .replace("\\\"", "\"")
            .replace("\\\\", "\\")
    }
}
