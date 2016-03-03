package org.chrisle.netbeans.plugins.nbfilestructurenode;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageMerger {
    private BufferedImage mergeImage;

    /**
     *
     */
    private Image processImages(String image1, String image2) {
        try {
            // load source images
            BufferedImage image = ImageIO.read(new File(image1));
            BufferedImage overlay = ImageIO.read(new File(image2));

            // create the new image, canvas size is the max. of both image sizes
            int w = Math.max(image.getWidth(), overlay.getWidth());
            int h = Math.max(image.getHeight(), overlay.getHeight());
            BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);

            // paint both images, preserving the alpha channels
            Graphics g = combined.getGraphics();
            g.drawImage(image, 0, 0, null);
            g.drawImage(overlay, 0, 0, null);

            // Save as new image
            ImageIO.write(combined, "PNG", new File("combined.png"));

        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return null; // TODO: Init value, has to be removed;
    }
}