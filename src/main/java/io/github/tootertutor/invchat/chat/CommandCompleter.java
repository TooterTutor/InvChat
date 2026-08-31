package io.github.tootertutor.invchat.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Provides server-aware Brigadier completion for slash commands typed into InvChat.
 *
 * <p>The implementation intentionally talks to the command dispatcher exposed by the active
 * {@link ClientPacketListener} instead of depending on Minecraft's chat-screen suggestion widget.
 * That keeps the feature independent of GUI rendering changes while still allowing argument
 * suggestion providers to ask the server for completions when necessary.</p>
 */
public final class CommandCompleter {
    private final Minecraft minecraft;
    private final InventoryChatEditBox input;

    private int requestGeneration;
    private String suggestionSource = "";
    private String suggestionCommand = "";
    private List<Suggestion> suggestions = Collections.emptyList();
    private int selectedSuggestion = -1;
    private String lastAppliedValue;
    private boolean applyFirstWhenReady;
    private boolean applyingSuggestion;

    public CommandCompleter(Minecraft minecraft, InventoryChatEditBox input) {
        this.minecraft = minecraft;
        this.input = input;
    }

    /** Called by the edit box whenever its text changes through ordinary user input/history. */
    public void onTextChanged(String value) {
        if (this.applyingSuggestion) {
            return;
        }

        this.resetCycle();
        this.requestSuggestions(value, false);
    }

    /**
     * Handles Tab for slash commands.
     *
     * @return true if Tab belongs to command completion, false for ordinary chat text.
     */
    public boolean complete() {
        String value = this.input.getValue();
        if (!this.isCompletableCommand(value)) {
            return false;
        }

        if (this.lastAppliedValue != null
                && this.lastAppliedValue.equals(value)
                && !this.suggestions.isEmpty()) {
            this.selectedSuggestion = (this.selectedSuggestion + 1) % this.suggestions.size();
            this.applySuggestion(this.selectedSuggestion);
            return true;
        }

        if (value.equals(this.suggestionSource) && !this.suggestions.isEmpty()) {
            this.selectedSuggestion = 0;
            this.applySuggestion(0);
            return true;
        }

        this.requestSuggestions(value, true);
        return true;
    }

    private boolean isCompletableCommand(String value) {
        return value != null
                && value.startsWith("/")
                && this.input.getCursorPosition() > 0
                && this.minecraft.getConnection() != null;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void requestSuggestions(String value, boolean applyFirst) {
        this.input.setSuggestion(null);
        this.applyFirstWhenReady = applyFirst;

        if (!this.isCompletableCommand(value)) {
            this.invalidatePendingRequest();
            return;
        }

        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection == null) {
            this.invalidatePendingRequest();
            return;
        }

        String command = value.substring(1);
        int commandCursor = this.input.getCursorPosition() - 1;
        if (commandCursor < 0) {
            commandCursor = 0;
        } else if (commandCursor > command.length()) {
            commandCursor = command.length();
        }

        final int generation = ++this.requestGeneration;
        final String sourceSnapshot = value;
        final String commandSnapshot = command;

        // The dispatcher source generic changed across supported Minecraft versions, while these
        // Brigadier operations stayed identical. Raw types keep that implementation shared.
        CommandDispatcher dispatcher = connection.getCommands();
        ParseResults parse = dispatcher.parse(command, connection.getSuggestionsProvider());
        CompletableFuture<Suggestions> future = dispatcher.getCompletionSuggestions(parse, commandCursor);

        future.thenAccept(result -> {
            if (generation != this.requestGeneration || !sourceSnapshot.equals(this.input.getValue())) {
                return;
            }

            this.suggestionSource = sourceSnapshot;
            this.suggestionCommand = commandSnapshot;
            this.suggestions = result == null ? Collections.emptyList() : result.getList();
            this.selectedSuggestion = -1;
            this.lastAppliedValue = null;

            if (this.suggestions.isEmpty()) {
                this.input.setSuggestion(null);
                this.applyFirstWhenReady = false;
                return;
            }

            this.updateInlineSuggestion();

            if (this.applyFirstWhenReady) {
                this.applyFirstWhenReady = false;
                this.selectedSuggestion = 0;
                this.applySuggestion(0);
            }
        });
    }

    private void updateInlineSuggestion() {
        if (this.suggestions.isEmpty()) {
            this.input.setSuggestion(null);
            return;
        }

        String applied = this.suggestions.get(0).apply(this.suggestionCommand);
        String currentCommand = this.suggestionSource.substring(1);

        if (applied.startsWith(currentCommand)) {
            String suffix = applied.substring(currentCommand.length());
            this.input.setSuggestion(suffix.isEmpty() ? null : suffix);
        } else {
            this.input.setSuggestion(null);
        }
    }

    private void applySuggestion(int index) {
        if (index < 0 || index >= this.suggestions.size()) {
            return;
        }

        String completedCommand = this.suggestions.get(index).apply(this.suggestionCommand);
        String completedValue = "/" + completedCommand;

        this.applyingSuggestion = true;
        try {
            this.input.setValue(completedValue);
            this.input.setCursorPosition(completedValue.length());
        } finally {
            this.applyingSuggestion = false;
        }

        this.lastAppliedValue = completedValue;
        this.input.setSuggestion(null);
    }

    private void resetCycle() {
        this.selectedSuggestion = -1;
        this.lastAppliedValue = null;
        this.suggestions = Collections.emptyList();
        this.suggestionSource = "";
        this.suggestionCommand = "";
        this.applyFirstWhenReady = false;
        this.invalidatePendingRequest();
    }

    private void invalidatePendingRequest() {
        this.requestGeneration++;
    }
}
