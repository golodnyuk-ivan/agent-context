# Agent Context

В Rider, WebStorm, PyCharm, GoLand или другой IDE на IntelliJ Platform нажмите `⌘'` (⌘ плюс одинарная кавычка), чтобы вставить ссылку на файл в выбранную вкладку **Terminal**:

- Если в редакторе есть выделение, вставляется путь относительно корня проекта и диапазон строк включительно, например `src/Foo.cs:44-46`.
- Если выделения нет, вставляется путь и номер строки курсора, например `src/Foo.cs:44`.
- Если редактор не в фокусе, вставляется путь к текущему открытому файлу без номера строки, например `src/Foo.cs`.

Перед вставкой файл сохраняется. По умолчанию после вставки фокус переходит в терминал. Это можно отключить в Settings → Tools → Agent Context флажком **Focus Terminal after adding context**.

Нужна версия IDE 2025.3 или новее с установленным и включённым плагином Terminal и режимом Reworked Terminal. Если сочетание `⌘'` уже занято, назначьте другое для действия **Add File Reference to Agent Context** в Settings → Keymap.

## Сборка на macOS

1. Установите Gradle и убедитесь, что версия — 9.x:

   ```bash
   brew install gradle
   gradle --version
   ```

2. Соберите плагин с установленной IDE. Пример ниже использует GoLand из `~/Applications`; замените путь на Rider, WebStorm или PyCharm, если собираете с другой IDE. Её встроенный JDK будет использован автоматически. В папке с проектом выполнить:

   ```bash
   IDE_APP="$HOME/Applications/GoLand.app"
   JAVA_HOME="$IDE_APP/Contents/jbr/Contents/Home" gradle buildPlugin "-PlocalIdePath=$IDE_APP"
   ```

3. Найдите готовый архив и установите его через Settings → Plugins → значок шестерёнки → Install Plugin from Disk:

   ```bash
   ls -lh build/distributions/agent-context-*.zip
   ```

ID плагина: `dev.agentcontext`.
