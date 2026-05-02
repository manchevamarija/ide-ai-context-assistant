package com.marija.aicontext

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

data class CodeContext(
    val projectName: String,
    val filePath: String,
    val language: String,
    val content: String,
    val isSelection: Boolean
)

class PromptBuilder {
    fun build(context: CodeContext): String {
        val scope = if (context.isSelection) "selected code" else "current file"

        return """
            You are an AI coding assistant inside a JetBrains IDE.

            Project: ${context.projectName}
            File: ${context.filePath}
            Language: ${context.language}
            Scope: $scope

            Please explain what this code does, point out possible problems, and suggest one small improvement.
            Keep the answer practical and concise.

            ```${
                context.language.lowercase()
            }
            ${context.content.trim()}
            ```
        """.trimIndent()
    }

    fun createContext(
        project: Project,
        file: VirtualFile?,
        selectedText: String?,
        fullText: String,
        language: String
    ): CodeContext {
        val content = selectedText?.takeIf { it.isNotBlank() } ?: fullText

        return CodeContext(
            projectName = project.name,
            filePath = file?.path ?: "unknown",
            language = language,
            content = content.take(MAX_CONTEXT_CHARS),
            isSelection = !selectedText.isNullOrBlank()
        )
    }

    private companion object {
        const val MAX_CONTEXT_CHARS = 12_000
    }
}
