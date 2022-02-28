/**
 * 
 */
package ca.datamagic.noaa.radar.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.RGBImageFilter;

/**
 * @author Greg
 *
 */
public final class ImageConverter {  
	public static BufferedImage convertToTransparent(BufferedImage opaqueImage) {
		ImageFilter filter = new RGBImageFilter() {
			private int markerRGB = Color.BLACK.getRGB() | 0xFF000000;
			
			public int filterRGB(int x, int y, int rgb) {
				if ((rgb | 0xFF000000) == markerRGB) {
					// Mark the alpha bits as zero - transparent
					return 0x00FFFFFF & rgb;
				}
				return rgb;
			}
		};
	    ImageProducer producer = new FilteredImageSource(opaqueImage.getSource(), filter);
	    Image image = Toolkit.getDefaultToolkit().createImage(producer);
	    if (image instanceof BufferedImage)
	    {
	        return (BufferedImage) image;
	    }
	    BufferedImage transparentImage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
	    Graphics2D graphics2d = transparentImage.createGraphics();
	    graphics2d.drawImage(image, 0, 0, null);
	    graphics2d.dispose();
	    return transparentImage;	   
	}
}
