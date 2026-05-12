# AI Context Assistant

Small AI-related IntelliJ plugin.

The plugin adds a **Tools -> Explain Code with AI Context** action. It reads the current editor selection, or the whole current file when nothing is selected, and builds an IDE-aware prompt with project name, file path, language, and code context.

If `OPENAI_API_KEY` is configured, the plugin sends the prompt to an OpenAI-compatible chat completions endpoint and opens the result in a scratch file. If no API key is configured, it still opens the generated prompt so the user can inspect what would be sent to the model.

## Why this project

I wanted to create a small plugin that is relevant to the AI Assistant Chat team. The main idea is that a useful IDE AI assistant should not behave like a generic chatbot. It should use IDE context such as the selected code, current file, project name, and language to produce a better request for the model.

## Features

- IntelliJ Platform action available from the Tools menu
- Works with selected code or the whole current file
- Builds a structured AI prompt with IDE context
- Optional OpenAI-compatible API call
- Opens the result in an IDE scratch file
- Keeps the implementation small and easy to review

## Technologies

- Kotlin
- IntelliJ Platform SDK
- Gradle IntelliJ Plugin
- Java HTTP Client

## Configuration

The plugin can work without an API key by showing the generated prompt.

To enable real AI responses, set:

```bash
OPENAI_API_KEY=your_api_key
```

Optional:

```bash
OPENAI_MODEL=gpt-4o-mini
OPENAI_BASE_URL=https://api.openai.com/v1/chat/completions
```

`OPENAI_BASE_URL` can point to another OpenAI-compatible chat completions endpoint.

## Running locally

```bash
./gradlew runIde
```

On Windows:

```powershell
gradlew.bat runIde
```

## Project structure

```text
src/main/kotlin/com/marija/aicontext/
  ExplainSelectionAction.kt  # IntelliJ action and UI flow
  PromptBuilder.kt           # Creates the IDE-aware prompt
  OpenAiClient.kt            # Minimal OpenAI-compatible HTTP client

src/main/resources/META-INF/plugin.xml
  Plugin metadata and action registration
```

## Possible improvements

- Add a proper tool window for chat history
- Support streaming responses
- Add project-wide context retrieval
- Add actions for tests, refactoring suggestions, and code review
- Use a JSON library instead of the small manual parser
- Add settings UI for API key, model, and endpoint
