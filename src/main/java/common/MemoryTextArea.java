package common;

import lombok.Data;

import javax.swing.*;
import java.util.LinkedList;
import java.util.List;

/**
 * DIY text area.
 */
@Data
public class MemoryTextArea extends JTextArea {
    private int messageCount = 0;
    private int messageLimit = 10;
    private List<String> messages = new LinkedList<>();

    public MemoryTextArea() {
        super();
    }

    public MemoryTextArea(int messageLimit) {
        super();
        this.messageLimit = messageLimit;
    }

    @Override
    public void append(String str) {
        if (messages.size() < messageLimit) {
            messages.add(str);
        } else {
            messages.remove(0);
            messages.add(str);
        }
        setText("");
        for (String message : messages) {
            super.append(message);
        }
        int lineCount = getLineCount();
        for (String message : messages) {
            if (message.length() > 20) {
                lineCount++;
            }
        }
        for (int i = 0; i < 17 - lineCount; i++) {
            insert("\n", 0);
        }
    }
}
