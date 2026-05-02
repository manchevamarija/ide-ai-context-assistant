package com.marija.aicontext

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

class ExplainSelectionAction : AnAction() {
    private val promptBuilder = PromptBuilder()
    private val aiClient = OpenAiClient()

    override fun update(event: AnActionEvent) {
        event.presentation.isEnabledAndVisible =
            event.project != null && event.getData(CommonDataKeys.EDITOR) != null
    }

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project ?: return
        val editor = event.getData(CommonDataKeys.EDITOR) ?: return
        val file = event.getData(CommonDataKeys.VIRTUAL_FILE)

        val prompt = createPrompt(project, editor, file)
        val answer = if (aiClient.isConfigured()) {
            askAi(prompt)
        } else {
            "OPENAI_API_KEY is not configured.\n\nGenerated prompt:\n\n$prompt"
        }

        showResult(project, answer)
    }

    private fun createPrompt(project: Project, editor: Editor, file: VirtualFile?): String {
        val document = editor.document
        val selectedText = editor.selectionModel.selectedText
        val language = file?.extension?.uppercase() ?: "TEXT"

        val context = promptBuilder.createContext(
            project = project,
            file = file,
            selectedText = selectedText,
            fullText = document.text,
            language = language
        )

        return promptBuilder.build(context)
    }

    private fun askAi(prompt: String): String {
        return aiClient.ask(prompt).fold(
            onSuccess = { it },
            onFailure = { "AI request failed: ${it.message}\n\nGenerated prompt:\n\n$prompt" }
        )
    }

    private fun showResult(project: Project, text: String) {
        ApplicationManager.getApplication().invokeLater {
            val scratchFile = com.intellij.ide.scratch.ScratchRootType.getInstance()
                .createScratchFile(
                    project,
                    "ai-context-assistant-result.md",
                    com.intellij.openapi.fileTypes.PlainTextFileType.INSTANCE,
                    text
                )

            if (scratchFile == null) {
                Messages.showInfoMessage(project, text.take(MAX_DIALOG_CHARS), "AI Context Assistant")
                return@invokeLater
            }

            val document = FileDocumentManager.getInstance().getDocument(scratchFile)
            if (document != null) {
                WriteCommandAction.runWriteCommandAction(project) {
                    document.setText(text)
                }
            }
            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project).openFile(scratchFile, true)
        }
    }

    private companion object {
        const val MAX_DIALOG_CHARS = 4_000
    }
}
