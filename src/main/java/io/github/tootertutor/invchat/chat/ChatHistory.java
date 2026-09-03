package io.github.tootertutor.invchat.chat;

import java.util.ArrayList;
import java.util.List;

/**
 * Small session-local history for messages submitted through InvChat.
 *
 * <p>
 * This intentionally does not depend on Minecraft's internal chat history
 * classes. Those classes
 * have moved and changed shape several times across InvChat's supported
 * versions, while this class
 * can remain identical from 1.16.5 through current releases.
 * </p>
 */
public final class ChatHistory {
    private static final int MAX_ENTRIES = 100;
    private static final List<String> ENTRIES = new ArrayList<>();

    private ChatHistory() {
    }

    /** Adds a successfully submitted line to the end of the session history. */
    public static void add(String input) {
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        ENTRIES.add(input);
        if (ENTRIES.size() > MAX_ENTRIES) {
            ENTRIES.remove(0);
        }
    }

    public static int size() {
        return ENTRIES.size();
    }

    public static String get(int index) {
        return ENTRIES.get(index);
    }
}
