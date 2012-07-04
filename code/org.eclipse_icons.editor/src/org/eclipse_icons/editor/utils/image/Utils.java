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
	
}
