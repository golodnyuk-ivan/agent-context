# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Что это

Плагин для IDE на IntelliJ Platform «Agent Context» (Rider, WebStorm, PyCharm, GoLand и другие). По шорткату `⌘'` (⌘ плюс одинарная кавычка, meta QUOTE) он вставляет project-relative путь файла в выбранную вкладку **Reworked Terminal**. При выделении добавляется диапазон строк (`src/Foo.cs:44-46`), без выделения — строка курсора (`src/Foo.cs:44`), без активного редактора — только текущий открытый файл (`src/Foo.cs`). Файл перед вставкой сохраняется. По умолчанию фокус переходит в терминал; это настраивается в Settings → Tools → Agent Context. Нужна IDE 2025.3+ с включёнными плагином Terminal и Reworked Terminal.

## Сборка и разработка

Wrapper'а нет — используется системный Gradle (9.x) и JDK 21+:

```bash
gradle buildPlugin
```

Пошаговая инструкция для macOS в `README.md` включает `brew install gradle`, сборку с локальной IDE через `-PlocalIdePath` и её встроенный JDK, а также вариант со скачиваемым SDK и `openjdk@21`.

`intellijIdea("2025.3.4")` в `build.gradle.kts` задаёт общий SDK для IDE на IntelliJ Platform; Gradle загружает его при необходимости. Параметр `-PlocalIdePath=/path/to/IDE.app` позволяет собрать плагин с локальной IDE, если SDK недоступен. Классы Reworked Terminal на компиляцию приходят из `bundledPlugin("org.jetbrains.plugins.terminal")`. Инструментация байткода отключена, так как в плагине нет UI-форм.

Результат: ZIP в `build/distributions/`. Установка: Settings → Plugins → шестерёнка → Install Plugin from Disk. Если `⌘'` уже занят — переназначить действие **Add File Reference to Agent Context** в Settings → Keymap.

Тестов и линтеров нет.

## Архитектура

Основные компоненты:

- `src/main/java/dev/agentcontext/SendSelectionAction.java` — `DumbAwareAction` (работает и во время индексации).
- `src/main/java/dev/agentcontext/AgentContextSettings.java` — application-level persisted флажок переноса фокуса, по умолчанию `true`.
- `src/main/java/dev/agentcontext/AgentContextConfigurable.java` — страница Settings → Tools → Agent Context.
- `src/main/resources/META-INF/plugin.xml` — id `dev.agentcontext`, зависимости `com.intellij.modules.platform` (доступность во всех IDE на платформе) и `org.jetbrains.plugins.terminal`, `since-build="253"`; регистрация действия `AgentContext.SendSelection` со шорткатом `meta QUOTE`, добавлением в `EditorPopupMenu` (anchor="last"), настройка и расширение `notificationGroup` «Agent Context» (BALLOON).

Поток `SendSelectionAction`:

1. `update` — действие активно при доступном файле редактора или текущем открытом файле из `FileEditorManager`.
2. `actionPerformed` — по `ToolWindowManager` ищет tool window «Terminal»; если не открыт — balloon-уведомление.
3. Через `TerminalToolWindowTabsManager.getInstance(project).getTabs()` выбирает вкладки Reworked Terminal, фильтруя по `tab.getContent().getManager().getSelectedContent() == tab.getContent()`. Должна быть ровно одна выбранная вкладка (idle splits дают ноль, сплиты — больше одной, в обоих случаях — предупреждение).
4. По выделению считает 1-based диапазон строк: `getLineNumber(startOffset) + 1` … `getLineNumber(endOffset - 1) + 1`; без выделения — строку курсора. Когда редактора в контексте действия нет, берёт текущий открытый файл без строк.
5. `FileDocumentManager.getInstance().saveDocument(document)` — документ сохраняется до вставки, если он есть.
6. Путь делается project-relative от `project.basePath` (`Path.relativize`), если файл вне проекта — остаётся абсолютный.
7. Отправка без Enter: `tab.getView().createSendTextBuilder().useBracketedPasteMode().send(reference)` — строка с обрамляющими пробелами. Если включена настройка, `ToolWindow.activate(null)` переводит фокус в терминал.

## Неочевидное

- Версия дублируется в двух местах: `version` в `build.gradle.kts` и `<version>` в `plugin.xml` — при смене версии обновлять оба.
- При изменении пользовательской инструкции в `README.md` обновлять `<description>` в `src/main/resources/META-INF/plugin.xml`, чтобы описание плагина в IDE соответствовало README.
