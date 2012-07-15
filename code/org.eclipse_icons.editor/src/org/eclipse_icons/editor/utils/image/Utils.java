package org.eclipse_icons.editor.utils.image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;


public class Utils {

	public static Image createOverlapedImage(String overlayImagePath, String baseImagePath, Point overlayPoint) {
		Image baseImage = new Image(Display.getCurrent(), baseImagePath);
		Image overlayImage = new Image(Display.getCurrent(), overlayImagePath);
		return createOverlapedImage(overlayImage, baseImage, overlayPoint);
	}
	
	public static Image createOverlapedImage(Image overlayImage, Image baseImage, Point overlayPoint) {
		OverlayImageDescriptor oid = new OverlayImageDescriptor(ImageDescriptor.createFromImage(baseImage),ImageDescriptor.createFromImage(overlayImage), overlayPoint);
		return oid.createImage();
	}
	
	public static Image createOverlapedImage(String overlayImagePath, Image baseImage, Point overlayPoint) {
		Image overlayImage = new Image(Display.getCurrent(), overlayImagePath);
		OverlayImageDescriptor oid = new OverlayImageDescriptor(ImageDescriptor.createFromImage(baseImage),ImageDescriptor.createFromImage(overlayImage), overlayPoint);
		return oid.createImage();
	}
	
	public static void saveIconToFile(Image image, String imagePath, int format){
	    ImageLoader loader = new ImageLoader();
	    loader.data = new ImageData[] {image.getImageData()};
	    loader.save(imagePath, format);
	}

	public static String getExtension(int outputFormat) {
		if (outputFormat == SWT.IMAGE_PNG){
			return "png";
		}
		return null;
	}
	

	public static ImageData rotate(ImageData srcData, int direction) {
	    int bytesPerPixel = srcData.bytesPerLine / srcData.width;
	    int destBytesPerLine = (direction == SWT.DOWN) ? srcData.width
	        * bytesPerPixel : srcData.height * bytesPerPixel;
	    byte[] newData = new byte[srcData.data.length];
	    int width = 0, height = 0, alpha = 0;
	    for (int srcY = 0; srcY < srcData.height; srcY++) {
	      for (int srcX = 0; srcX < srcData.width; srcX++) {
	        int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
	        switch (direction) {
	        case SWT.LEFT: // left 90 degrees
	          destX = srcY;
	          destY = srcData.width - srcX - 1;
	          width = srcData.height;
	          height = srcData.width;
	          break;
	        case SWT.RIGHT: // right 90 degrees
	          destX = srcData.height - srcY - 1;
	          destY = srcX;
	          width = srcData.height;
	          height = srcData.width;
	          break;
	        case SWT.DOWN: // 180 degrees
	          destX = srcData.width - srcX - 1;
	          destY = srcData.height - srcY - 1;
	          width = srcData.width;
	          height = srcData.height;
	          break;
	        }
	        destIndex = (destY * destBytesPerLine)
	            + (destX * bytesPerPixel);
	        srcIndex = (srcY * srcData.bytesPerLine)
	            + (srcX * bytesPerPixel);
	        System.arraycopy(srcData.data, srcIndex, newData, destIndex,
	            bytesPerPixel);
	      }
	    }
	    // destBytesPerLine is used as scanlinePad to ensure that no padding is
	    // required
	    return new ImageData(width, height, srcData.depth, srcData.palette,
	        destBytesPerLine, newData);
	  }

	  public static ImageData flip(ImageData srcData, boolean vertical) {
	    int bytesPerPixel = srcData.bytesPerLine / srcData.width;
	    int destBytesPerLine = srcData.width * bytesPerPixel;
	    byte[] newData = new byte[srcData.data.length];
	    for (int srcY = 0; srcY < srcData.height; srcY++) {
	      for (int srcX = 0; srcX < srcData.width; srcX++) {
	        int destX = 0, destY = 0, destIndex = 0, srcIndex = 0;
	        if (vertical) {
	          destX = srcX;
	          destY = srcData.height - srcY - 1;
	        } else {
	          destX = srcData.width - srcX - 1;
	          destY = srcY;
	        }
	        destIndex = (destY * destBytesPerLine)
	            + (destX * bytesPerPixel);
	        srcIndex = (srcY * srcData.bytesPerLine)
	            + (srcX * bytesPerPixel);
	        System.arraycopy(srcData.data, srcIndex, newData, destIndex,
	            bytesPerPixel);
	      }
	    }
	    // destBytesPerLine is used as scanlinePad to ensure that no padding is
	    // required
	    return new ImageData(srcData.width, srcData.height, srcData.depth,
	        srcData.palette, destBytesPerLine, newData);
	  }
	
}
