import javax.swing.*;

public class Main
{
    public static void main(String[] args)
    {
        // Execute the GUI creation
        SwingUtilities.invokeLater(() ->
        {
            // Create and display the GUI
            new GUI().setVisible(true);
        });
    }
}
