package dev.agentcontext;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public final class AgentContextConfigurable implements Configurable {
    private JCheckBox focusTerminalCheckBox;

    @Override
    public @NotNull String getDisplayName() {
        return "Agent Context";
    }

    @Override
    public @Nullable JComponent createComponent() {
        focusTerminalCheckBox = new JCheckBox("Focus Terminal after adding context");
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(focusTerminalCheckBox, BorderLayout.NORTH);
        reset();
        return panel;
    }

    @Override
    public boolean isModified() {
        return focusTerminalCheckBox != null
            && focusTerminalCheckBox.isSelected() != AgentContextSettings.getInstance().isFocusTerminal();
    }

    @Override
    public void apply() {
        if (focusTerminalCheckBox != null) {
            AgentContextSettings.getInstance().setFocusTerminal(focusTerminalCheckBox.isSelected());
        }
    }

    @Override
    public void reset() {
        if (focusTerminalCheckBox != null) {
            focusTerminalCheckBox.setSelected(AgentContextSettings.getInstance().isFocusTerminal());
        }
    }

    @Override
    public void disposeUIResources() {
        focusTerminalCheckBox = null;
    }
}
