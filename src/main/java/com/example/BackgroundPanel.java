
import java.awt.Graphics;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

class BackgroundPanel extends JPanel {
    private Image img;

    public BackgroundPanel(String resourcePath) {
        try {
            img = new ImageIcon(getClass().getResource(resourcePath)).getImage();

            System.out.println("\n\n\n\n\n\nImage URL = " + getClass().getResource(resourcePath));

        } catch (Exception e) {
            System.out.println("Failed to load background image: " + resourcePath);
            e.printStackTrace();
        }
    }
    @Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (img != null) {
        g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
    }
}


}
