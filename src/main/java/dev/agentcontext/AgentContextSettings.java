package dev.agentcontext;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

@Service(Service.Level.APP)
@State(name = "AgentContextSettings", storages = @Storage("agentContext.xml"))
public final class AgentContextSettings implements PersistentStateComponent<AgentContextSettings.SettingsState> {
    public static final class SettingsState {
        public boolean focusTerminal = true;
    }

    private SettingsState state = new SettingsState();

    public static AgentContextSettings getInstance() {
        return ApplicationManager.getApplication().getService(AgentContextSettings.class);
    }

    public boolean isFocusTerminal() {
        return state.focusTerminal;
    }

    public void setFocusTerminal(boolean focusTerminal) {
        state.focusTerminal = focusTerminal;
    }

    @Override
    public @NotNull SettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState state) {
        this.state = state;
    }
}
