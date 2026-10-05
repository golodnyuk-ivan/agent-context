# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Что это

Плагин для IDE на IntelliJ Platform «Agent Context» (Rider, WebStorm, PyCharm, GoLand и другие). По шорткату `⌘'(⌘ плюс одинарная кавычка)` (meta QUOTE) при выделенном тексте в редакторе он вставляет project-relative путь файла и диапазон строк (например, `src/Foo.cs:44-46`) в выбранную вкладку **Reworked Terminal** — так контекст файла уходит агенту в терминале. Файл перед вставкой сохраняется. Нужна IDE 2025.3+ с включёнными плагином Terminal и Reworked Terminal.

## Сборка и разработка

Wrapper'а нет — используется системный Gradle (9.x) и JDK 21+:

```bash
gradle buildPlugin
```

Пошаговая инструкция для macOS в `README.md` включает `brew install gradle`, сборку с локальной IDE через `-PlocalIdePath` и её встроенный JDK, а также вариант со скачиваемым SDK и `openjdk@21`.

`intellijIdea("2025.3.4")` в `build.gradle.kts` задаёт общий SDK для IDE на IntelliJ Platform; Gradle загружает его при необходимости. Параметр `-PlocalIdePath=/path/to/IDE.app` позволяет собрать плагин с локальной IDE, если SDK недоступен. Классы Reworked Terminal на компиляцию приходят из `bundledPlugin("org.jetbrains.plugins.terminal")`. Инструментация байткода отключена, так как в плагине нет UI-форм.

Результат: ZIP в `build/distributions/`. Установка: Settings → Plugins → шестерёнка → Install Plugin from Disk. Если `⌘'(⌘ плюс одинарная кавычка)` уже занят — переназначить действие **Add Selected Lines to Agent Context** в Settings → Keymap.

Тестов и линтеров нет.

## Архитектура

Проект состоит из одного действия и его регистрации:

- `src/main/java/dev/agentcontext/SendSelectionAction.java` — `DumbAwareAction` (работает и во время индексации).
- `src/main/resources/META-INF/plugin.xml` — id `dev.agentcontext`, зависимости `com.intellij.modules.platform` (доступность во всех IDE на платформе) и `org.jetbrains.plugins.terminal`, `since-build="253"`; регистрация действия `AgentContext.SendSelection` со шорткатом `meta QUOTE`, добавлением в `EditorPopupMenu` (anchor="last") и расширением `notificationGroup` «Agent Context» (BALLOON).

Поток `SendSelectionAction`:

1. `update` — действие активно только при открытом редакторе с выделением (`CommonDataKeys.EDITOR`/`VIRTUAL_FILE`, `hasSelection()`).
2. `actionPerformed` — по `ToolWindowManager` ищет tool window «Terminal»; если не открыт — balloon-уведомление.
3. Через `TerminalToolWindowTabsManager.getInstance(project).getTabs()` выбирает вкладки Reworked Terminal, фильтруя по `tab.getContent().getManager().getSelectedContent() == tab.getContent()`. Должна быть ровно одна выбранная вкладка (idle splits дают ноль, сплиты — больше одной, в обоих случаях — предупреждение).
4. По выделению считает 1-based диапазон строк: `getLineNumber(startOffset) + 1` … `getLineNumber(endOffset - 1) + 1`; одиночная строка схлопывается в `start` без `-end`.
5. `FileDocumentManager.getInstance().saveDocument(document)` — файл сохраняется до вставки.
6. Путь делается project-relative от `project.basePath` (`Path.relativize`), если файл вне проекта — остаётся абсолютный.
7. Отправка без Enter: `tab.getView().createSendTextBuilder().useBracketedPasteMode().send(reference)` — строка с обрамляющими пробелами: `" <relpath>:<start>-<end> "`.

## Неочевидное

- Версия дублируется в двух местах: `version` в `build.gradle.kts` и `<version>` в `plugin.xml` — при смене версии обновлять оба.
- При изменении пользовательской инструкции в `README.md` обновлять `<description>` в `src/main/resources/META-INF/plugin.xml`, чтобы описание плагина в IDE соответствовало README.
