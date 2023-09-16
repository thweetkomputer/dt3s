// Chen Zhao 1427714
package common;

import javax.swing.*;
import java.awt.*;

/**
 * A text field with placeholder.
 *
 * @author Chen Zhao
 */
public class PlaceholderTextField extends JTextField {

    /** The placeholder. */
    private final String placeholder;

    /**
     * Instantiates a new placeholder text field.
     *
     * @param placeholder the placeholder
     */
    public PlaceholderTextField(String placeholder) {
        super();
        this.placeholder = placeholder;
    }

    /**
     * Instantiates a new placeholder text field.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getText().isEmpty() && placeholder != null) {
            g.setFont(new Font("Comic Sans MS", Font.PLAIN, 16));
            g.setColor(Color.GRAY);
            FontMetrics fm = g.getFontMetrics();
            int baseline = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(placeholder, 0, baseline);
        }
    }
}
