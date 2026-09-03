package io.github.tootertutor.invchat.chat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
//? if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import io.github.tootertutor.invchat.InvChat;
import io.github.tootertutor.invchat.config.InvChatConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if <1.20 {
/*import net.minecraft.client.gui.GuiComponent;
*///?}
//? if >=1.20 && <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?}
//? if >=26.1 {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;

import org.lwjgl.glfw.GLFW;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Direct Brigadier completion and dropdown state for InvChat.
 *
 * <p>This deliberately does not use Minecraft's {@code CommandSuggestions} widget. That widget
 * owns the vanilla chat screen's focus and suggestion lifecycle, while InvChat lives inside an
 * inventory screen. Keeping the Brigadier query, selection state, replacement snapshot and
 * renderer here prevents stale vanilla suggestion ranges from being applied to newer text.</p>
 */
public final class CommandCompleter {
    private static final int ROW_HEIGHT = 12;
    private static final int EDGE_PADDING = 4;
    private static final int MIN_DROPDOWN_WIDTH = 60;
    private static final int BACKGROUND_COLOR = 0xD0000000;
    private static final int SELECTED_BACKGROUND_COLOR = 0xE0505050;
    private static final int TEXT_COLOR = 0xFFE0E0E0;
    private static final int SELECTED_TEXT_COLOR = 0xFFFFFF55;

    private final Minecraft minecraft;
    private final Screen screen;
    private final InventoryChatEditBox input;
    private final Font font;
    private final int inputX;
    private final int inputY;
    private final int inputWidth;
    private final int inputHeight;

    private int requestGeneration;
    private String sourceSnapshot = "";
    private String commandSnapshot = "";
    private int cursorSnapshot = -1;
    private List<Suggestion> suggestions = Collections.emptyList();
    private int selectedIndex;
    private int scrollOffset;
    private double wheelAccumulator;
    private boolean visible;
    private boolean applyingSuggestion;
    private int lastMouseX = Integer.MIN_VALUE;
    private int lastMouseY = Integer.MIN_VALUE;

    public CommandCompleter(
            Minecraft minecraft,
            Screen screen,
            InventoryChatEditBox input,
            Font font,
            int inputX,
            int inputY,
            int inputWidth,
            int inputHeight
    ) {
        this.minecraft = minecraft;
        this.screen = screen;
        this.input = input;
        this.font = font;
        this.inputX = inputX;
        this.inputY = inputY;
        this.inputWidth = inputWidth;
        this.inputHeight = inputHeight;
    }

    /** Called by the edit box whenever its text changes. */
    public void onTextChanged(String value) {
        if (this.applyingSuggestion) {
            return;
        }

        this.requestSuggestions(value, this.input.getCursorPosition());
    }

    /** Refreshes suggestions after focus/cursor changes without changing the text. */
    public void refresh() {
        String value = this.input.getValue();
        int cursor = this.input.getCursorPosition();

        if (!value.equals(this.sourceSnapshot) || cursor != this.cursorSnapshot) {
            this.requestSuggestions(value, cursor);
        }
    }

    /** Hides the window without altering the command text. */
    public void hide() {
        this.visible = false;
        this.wheelAccumulator = 0.0D;
        this.input.setSuggestion(null);
    }

    /**
     * Handles selection keys before the edit box/history layer sees them.
     *
     * @return {@code true} only when the key was consumed by command completion.
     */
    public boolean keyPressed(int keyCode, boolean shiftDown) {
        String value = this.input.getValue();
        if (!this.isCommandInput(value)) {
            return false;
        }

        this.refresh();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.visible) {
            this.hide();
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_UP && this.visible && !this.suggestions.isEmpty()) {
            this.moveSelection(-1);
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DOWN && this.visible && !this.suggestions.isEmpty()) {
            this.moveSelection(1);
            return true;
        }

        if (keyCode != GLFW.GLFW_KEY_TAB) {
            return false;
        }

        if (this.suggestions.isEmpty()) {
            this.requestSuggestions(value, this.input.getCursorPosition());
            return true;
        }

        if (!this.visible) {
            this.visible = true;
            this.selectedIndex = shiftDown ? this.suggestions.size() - 1 : 0;
            this.ensureSelectionVisible();
            this.updateInlineSuggestion();
            return true;
        }

        if (shiftDown) {
            this.moveSelection(-1);
            return true;
        }

        this.applySelectedSuggestion();
        return true;
    }

    /**
     * Cycles the highlighted suggestion using the mouse wheel. Positive vertical scroll moves up
     * the list; negative vertical scroll moves down, matching the vanilla chat suggestion window.
     */
    public boolean mouseScrolled(double amount) {
        String value = this.input.getValue();
        if (amount == 0.0D
                || !this.visible
                || this.suggestions.isEmpty()
                || !this.isCommandInput(value)) {
            return false;
        }

        this.refresh();
        if (!this.visible || this.suggestions.isEmpty()) {
            return false;
        }

        // GLFW may report fractional wheel deltas on high-resolution mice/touchpads. Accumulate
        // those deltas so one logical notch still advances one suggestion, while consuming the
        // whole gesture so the underlying inventory does not scroll at the same time.
        this.wheelAccumulator += amount;
        int steps = (int) this.wheelAccumulator;
        if (steps != 0) {
            this.wheelAccumulator -= steps;
            this.moveSelection(-steps);
        }

        return true;
    }

    /** Accepts a suggestion when the user clicks one of the visible rows. */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT || !this.visible || this.suggestions.isEmpty()) {
            return false;
        }

        DropdownLayout layout = this.calculateLayout();
        if (layout == null || !layout.contains(mouseX, mouseY)) {
            return false;
        }

        int visibleRow = ((int) mouseY - layout.y - 1) / ROW_HEIGHT;
        int suggestionIndex = this.scrollOffset + visibleRow;
        if (suggestionIndex < 0 || suggestionIndex >= this.suggestions.size()) {
            return false;
        }

        this.selectedIndex = suggestionIndex;
        this.applySelectedSuggestion();
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void requestSuggestions(String value, int cursor) {
        final int generation = ++this.requestGeneration;

        this.sourceSnapshot = value == null ? "" : value;
        this.cursorSnapshot = cursor;
        this.commandSnapshot = "";
        this.suggestions = Collections.emptyList();
        this.selectedIndex = 0;
        this.scrollOffset = 0;
        this.wheelAccumulator = 0.0D;
        this.visible = false;
        this.input.setSuggestion(null);

        if (!this.isCommandInput(value)) {
            return;
        }

        ClientPacketListener connection = this.minecraft.getConnection();
        if (connection == null) {
            return;
        }

        String command = value.substring(1);
        int commandCursor = cursor - 1;
        if (commandCursor < 0) {
            commandCursor = 0;
        } else if (commandCursor > command.length()) {
            commandCursor = command.length();
        }

        final String source = value;
        final String commandSource = command;
        final int sourceCursor = cursor;

        CommandDispatcher dispatcher = connection.getCommands();
        ParseResults parse = dispatcher.parse(commandSource, connection.getSuggestionsProvider());
        CompletableFuture<Suggestions> future = dispatcher.getCompletionSuggestions(parse, commandCursor);

        future.thenAccept(result -> this.minecraft.execute(() -> this.acceptSuggestions(
                generation,
                source,
                commandSource,
                sourceCursor,
                result
        )));
    }

    private void acceptSuggestions(
            int generation,
            String source,
            String commandSource,
            int sourceCursor,
            Suggestions result
    ) {
        if (generation != this.requestGeneration
                || !source.equals(this.input.getValue())
                || sourceCursor != this.input.getCursorPosition()) {
            return;
        }

        List<Suggestion> resultList = result == null ? Collections.emptyList() : result.getList();
        if (resultList.isEmpty()) {
            this.suggestions = Collections.emptyList();
            this.visible = false;
            this.input.setSuggestion(null);
            return;
        }

        this.sourceSnapshot = source;
        this.commandSnapshot = commandSource;
        this.cursorSnapshot = sourceCursor;
        this.suggestions = resultList;
        this.selectedIndex = 0;
        this.scrollOffset = 0;
        this.wheelAccumulator = 0.0D;
        this.visible = true;
        this.updateInlineSuggestion();
    }

    private boolean isCommandInput(String value) {
        return value != null
                && value.startsWith("/")
                && this.input.getCursorPosition() > 0
                && this.minecraft.getConnection() != null;
    }

    private void moveSelection(int amount) {
        if (this.suggestions.isEmpty()) {
            return;
        }

        int size = this.suggestions.size();
        this.selectedIndex = (this.selectedIndex + amount) % size;
        if (this.selectedIndex < 0) {
            this.selectedIndex += size;
        }

        this.ensureSelectionVisible();
        this.updateInlineSuggestion();
    }

    private void ensureSelectionVisible() {
        int visibleRows = Math.min(this.getMaxVisibleRows(), this.suggestions.size());
        if (visibleRows <= 0) {
            this.scrollOffset = 0;
            return;
        }

        if (this.selectedIndex < this.scrollOffset) {
            this.scrollOffset = this.selectedIndex;
        } else if (this.selectedIndex >= this.scrollOffset + visibleRows) {
            this.scrollOffset = this.selectedIndex - visibleRows + 1;
        }

        int maxOffset = Math.max(this.suggestions.size() - visibleRows, 0);
        if (this.scrollOffset < 0) {
            this.scrollOffset = 0;
        } else if (this.scrollOffset > maxOffset) {
            this.scrollOffset = maxOffset;
        }
    }

    private void updateInlineSuggestion() {
        if (!this.visible
                || this.selectedIndex < 0
                || this.selectedIndex >= this.suggestions.size()
                || !this.sourceSnapshot.equals(this.input.getValue())) {
            this.input.setSuggestion(null);
            return;
        }

        String completedCommand = this.suggestions.get(this.selectedIndex).apply(this.commandSnapshot);
        String completedValue = "/" + completedCommand;
        String currentValue = this.input.getValue();

        if (completedValue.startsWith(currentValue)) {
            String suffix = completedValue.substring(currentValue.length());
            this.input.setSuggestion(suffix.isEmpty() ? null : suffix);
        } else {
            this.input.setSuggestion(null);
        }
    }

    private void applySelectedSuggestion() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.suggestions.size()) {
            return;
        }

        if (!this.sourceSnapshot.equals(this.input.getValue())
                || this.cursorSnapshot != this.input.getCursorPosition()) {
            this.requestSuggestions(this.input.getValue(), this.input.getCursorPosition());
            return;
        }

        String completedCommand = this.suggestions.get(this.selectedIndex).apply(this.commandSnapshot);
        String completedValue = "/" + completedCommand;

        this.applyingSuggestion = true;
        try {
            this.input.setValue(completedValue);
            this.input.setCursorPosition(completedValue.length());
        } finally {
            this.applyingSuggestion = false;
        }

        // Never reuse a Suggestion against text it has already modified. A fresh Brigadier query
        // owns the next Tab press and receives a fresh replacement range/snapshot.
        this.requestSuggestions(completedValue, completedValue.length());
    }

    private int getMaxVisibleRows() {
        return InvChat.getConfig().suggestionMaxVisible;
    }

    private DropdownLayout calculateLayout() {
        if (!this.visible || this.suggestions.isEmpty()) {
            return null;
        }

        int visibleRows = Math.min(this.getMaxVisibleRows(), this.suggestions.size());
        if (visibleRows <= 0) {
            return null;
        }

        int widest = MIN_DROPDOWN_WIDTH;
        for (Suggestion suggestion : this.suggestions) {
            widest = Math.max(widest, this.font.width(suggestion.getText()) + 8);
        }

        int maxWidth = Math.max(20, this.screen.width - EDGE_PADDING * 2);
        int width = Math.min(widest, maxWidth);
        int height = visibleRows * ROW_HEIGHT + 2;

        InvChatConfig config = InvChat.getConfig();
        String anchor = config.suggestionAnchor == null
                ? "ABOVE_CHAT"
                : config.suggestionAnchor.toUpperCase(Locale.ROOT);

        int x;
        int y;
        if ("BELOW_CHAT".equals(anchor)) {
            x = this.inputX;
            y = this.inputY + this.inputHeight + 2;
        } else if ("TOP_LEFT".equals(anchor)) {
            x = EDGE_PADDING;
            y = EDGE_PADDING;
        } else if ("TOP_RIGHT".equals(anchor)) {
            x = this.screen.width - width - EDGE_PADDING;
            y = EDGE_PADDING;
        } else if ("BOTTOM_LEFT".equals(anchor)) {
            x = EDGE_PADDING;
            y = this.screen.height - height - EDGE_PADDING;
        } else if ("BOTTOM_RIGHT".equals(anchor)) {
            x = this.screen.width - width - EDGE_PADDING;
            y = this.screen.height - height - EDGE_PADDING;
        } else {
            x = this.inputX;
            y = this.inputY - height - 2;
        }

        x += config.suggestionXOffset;
        y += config.suggestionYOffset;

        int maxX = Math.max(EDGE_PADDING, this.screen.width - width - EDGE_PADDING);
        int maxY = Math.max(EDGE_PADDING, this.screen.height - height - EDGE_PADDING);
        x = Math.max(EDGE_PADDING, Math.min(x, maxX));
        y = Math.max(EDGE_PADDING, Math.min(y, maxY));

        return new DropdownLayout(x, y, width, height, visibleRows);
    }

    private void updateHoveredSelection(DropdownLayout layout, int mouseX, int mouseY) {
        if (mouseX == this.lastMouseX && mouseY == this.lastMouseY) {
            return;
        }

        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (layout == null || !layout.contains(mouseX, mouseY)) {
            return;
        }

        int row = (mouseY - layout.y - 1) / ROW_HEIGHT;
        int index = this.scrollOffset + row;
        if (index >= 0 && index < this.suggestions.size()) {
            this.selectedIndex = index;
            this.updateInlineSuggestion();
        }
    }

    //? if >=26.1 {
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        DropdownLayout layout = this.calculateLayout();
        if (layout == null) {
            return;
        }

        this.updateHoveredSelection(layout, mouseX, mouseY);
        graphics.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, BACKGROUND_COLOR);

        for (int row = 0; row < layout.visibleRows; row++) {
            int index = this.scrollOffset + row;
            int rowY = layout.y + 1 + row * ROW_HEIGHT;
            boolean selected = index == this.selectedIndex;
            if (selected) {
                graphics.fill(layout.x + 1, rowY, layout.x + layout.width - 1, rowY + ROW_HEIGHT, SELECTED_BACKGROUND_COLOR);
            }

            String text = this.suggestions.get(index).getText();
            graphics.text(this.font, text, layout.x + 3, rowY + 2, selected ? SELECTED_TEXT_COLOR : TEXT_COLOR, false);
        }
    }
    //?}

    //? if >=1.20 && <26.1 {
    /*public void render(GuiGraphics graphics, int mouseX, int mouseY) {
        DropdownLayout layout = this.calculateLayout();
        if (layout == null) {
            return;
        }

        this.updateHoveredSelection(layout, mouseX, mouseY);
        graphics.fill(layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, BACKGROUND_COLOR);

        for (int row = 0; row < layout.visibleRows; row++) {
            int index = this.scrollOffset + row;
            int rowY = layout.y + 1 + row * ROW_HEIGHT;
            boolean selected = index == this.selectedIndex;
            if (selected) {
                graphics.fill(layout.x + 1, rowY, layout.x + layout.width - 1, rowY + ROW_HEIGHT, SELECTED_BACKGROUND_COLOR);
            }

            String text = this.suggestions.get(index).getText();
            graphics.drawString(this.font, text, layout.x + 3, rowY + 2, selected ? SELECTED_TEXT_COLOR : TEXT_COLOR, false);
        }
    }
    *///?}

    //? if <1.20 {
    /*public void render(PoseStack poseStack, int mouseX, int mouseY) {
        DropdownLayout layout = this.calculateLayout();
        if (layout == null) {
            return;
        }

        this.updateHoveredSelection(layout, mouseX, mouseY);
        GuiComponent.fill(poseStack, layout.x, layout.y, layout.x + layout.width, layout.y + layout.height, BACKGROUND_COLOR);

        for (int row = 0; row < layout.visibleRows; row++) {
            int index = this.scrollOffset + row;
            int rowY = layout.y + 1 + row * ROW_HEIGHT;
            boolean selected = index == this.selectedIndex;
            if (selected) {
                GuiComponent.fill(
                        poseStack,
                        layout.x + 1,
                        rowY,
                        layout.x + layout.width - 1,
                        rowY + ROW_HEIGHT,
                        SELECTED_BACKGROUND_COLOR
                );
            }

            String text = this.suggestions.get(index).getText();
            GuiComponent.drawString(
                    poseStack,
                    this.font,
                    text,
                    layout.x + 3,
                    rowY + 2,
                    selected ? SELECTED_TEXT_COLOR : TEXT_COLOR
            );
        }
    }
    *///?}

    private static final class DropdownLayout {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int visibleRows;

        private DropdownLayout(int x, int y, int width, int height, int visibleRows) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.visibleRows = visibleRows;
        }

        private boolean contains(double mouseX, double mouseY) {
            return mouseX >= this.x
                    && mouseX < this.x + this.width
                    && mouseY >= this.y
                    && mouseY < this.y + this.height;
        }
    }
}
