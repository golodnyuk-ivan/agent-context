# Agent Context

Выделите текст в редакторе Rider, WebStorm, PyCharm, GoLand или другой IDE на IntelliJ Platform и нажмите `⌘'`(⌘ плюс одинарная кавычка). Плагин вставит в выбранную вкладку **Terminal** путь к файлу относительно корня проекта и диапазон строк включительно, например `src/Foo.cs:44-46`. Перед вставкой файл сохраняется.

Нужна версия IDE 2025.3 или новее с установленным и включённым плагином Terminal и режимом Reworked Terminal. Если сочетание `⌘'` уже занято, назначьте другое для действия **Add Selected Lines to Agent Context** в Settings → Keymap.

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
