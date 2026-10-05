package dev.agentcontext;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTab;
import com.intellij.terminal.frontend.toolwindow.TerminalToolWindowTabsManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.List;

public final class SendSelectionAction extends DumbAwareAction {
    @Override
    public void update(@NotNull AnActionEvent event) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        event.getPresentation().setEnabled(
            event.getProject() != null && editor != null && file != null
                && editor.getSelectionModel().hasSelection()
        );
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        Project project = event.getProject();
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        VirtualFile file = event.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || editor == null || file == null) {
            return;
        }

        SelectionModel selection = editor.getSelectionModel();
        int startOffset = selection.getSelectionStart();
        int endOffset = selection.getSelectionEnd();
        if (endOffset <= startOffset) {
            return;
        }

        ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("Terminal");
        if (toolWindow == null) {
            warn(project, "Open the Terminal tool window first.");
            return;
        }

        List<TerminalToolWindowTab> selectedTabs = TerminalToolWindowTabsManager
            .getInstance(project)
            .getTabs()
            .stream()
            .filter(tab -> tab.getContent().getManager() != null
                && tab.getContent().getManager().getSelectedContent() == tab.getContent())
            .toList();

        if (selectedTabs.size() != 1) {
            warn(project, selectedTabs.isEmpty()
                ? "Select an open Reworked Terminal tab first (2025.3+)."
                : "More than one terminal split is selected; choose a single terminal tab.");
            return;
        }

        Document document = editor.getDocument();
        int startLine = document.getLineNumber(startOffset) + 1;
        int endLine = document.getLineNumber(endOffset - 1) + 1;
        FileDocumentManager.getInstance().saveDocument(document);

        String path = file.getPath();
        String basePath = project.getBasePath();
        if (basePath != null) {
            Path base = Path.of(basePath);
            Path full = Path.of(path);
            if (full.startsWith(base)) {
                path = base.relativize(full).toString();
            }
        }

        String reference = " " + path + ":" + startLine
            + (endLine == startLine ? "" : "-" + endLine) + " ";
        selectedTabs.get(0).getView().createSendTextBuilder()
            .useBracketedPasteMode()
            .send(reference);
    }

    private static void warn(Project project, String message) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("Agent Context")
            .createNotification(message, NotificationType.WARNING)
            .notify(project);
    }
}
