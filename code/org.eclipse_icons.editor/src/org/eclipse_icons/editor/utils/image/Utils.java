package org.eclipse_icons.editor.utils.image;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class Utils {

	public static Image createOverlapedImage(String overlayImagePath,
			String baseImagePath, Point overlayPoint) {
		Image baseImage = new Image(Display.getCurrent(), baseImagePath);
		Image overlayImage = new Image(Display.getCurrent(), overlayImagePath);
		return createOverlapedImage(overlayImage, baseImage, overlayPoint);
	}

	public static Image createOverlapedImage(Image overlayImage,
			Image baseImage, Point overlayPoint) {
		OverlayImageDescriptor oid = new OverlayImageDescriptor(
				ImageDescriptor.createFromImage(baseImage),
				ImageDescriptor.createFromImage(overlayImage), overlayPoint);
		return oid.createImage();
	}

	public static Image createOverlapedImage(String overlayImagePath,
			Image baseImage, Point overlayPoint) {
		Image overlayImage = new Image(Display.getCurrent(), overlayImagePath);
		OverlayImageDescriptor oid = new OverlayImageDescriptor(
				ImageDescriptor.createFromImage(baseImage),
				ImageDescriptor.createFromImage(overlayImage), overlayPoint);
		return oid.createImage();
	}

	public static void saveIconToFile(Image image, String imagePath, int format) {
		ImageLoader loader = new ImageLoader();
		loader.data = new ImageData[] { image.getImageData() };
		loader.save(imagePath, format);
	}

	public static String getExtension(int outputFormat) {
		if (outputFormat == SWT.IMAGE_PNG) {
			return "png";
		}
		return null;
	}

	public static Image cropImage(Image sourceImage, int x, int y, int height,
			int width) {
		Image croppedImage = new Image(Display.getCurrent(), width, height);
		GC gc = new GC(sourceImage);
		gc.copyArea(croppedImage, x, y);
		gc.dispose();
		return croppedImage;
	}


	/**
	 * 
	 * @param image
	 * @param direction Rotate: SWT.LEFT, SWT.RIGHT, SWT.DOWN for 180�, Flip: SWT.HORIZONTAL, SWT.VERTICAL
	 * @return
	 */
	public static Image rotateOrFlip(Image image, int direction) {
		try{
		// Get the current display
		Display display = Display.getCurrent();
		if (display == null)
			SWT.error(SWT.ERROR_THREAD_INVALID_ACCESS);

		// Use the image's data to create a rotated image's data
		ImageData sd = image.getImageData();

		// Manage alpha layer
		boolean containsAlpha = sd.alphaData != null;
		byte[] newAlphaData = null;
		if (containsAlpha) {
			newAlphaData = new byte[sd.alphaData.length];
		}

		ImageData dd = new ImageData(sd.height, sd.width, sd.depth, sd.palette);

		if (containsAlpha) {
			dd.alphaData = newAlphaData;
		}
		dd.alpha = sd.alpha;
		dd.transparentPixel = sd.transparentPixel;

		// Run through the horizontal pixels
		for (int sx = 0; sx < sd.width; sx++) {
			// Run through the vertical pixels
			for (int sy = 0; sy < sd.height; sy++) {
				int dx = 0, dy = 0;
				switch (direction) {
				case SWT.LEFT: // left 90 degrees
					dx = sy;
					dy = sd.width - sx - 1;
					break;
				case SWT.RIGHT: // right 90 degrees
					dx = sd.height - sy - 1;
					dy = sx;
					break;
				case SWT.DOWN: // 180 degrees
					dx = sd.width - sx - 1;
					dy = sd.height - sy - 1;
					break;
				case SWT.HORIZONTAL: // flip horizontal
					dx = sd.width - sx - 1;
					dy = sy;
					break;
				case SWT.VERTICAL: // flip vertical
					dx = sx;
					dy = sd.height - sy - 1;
					break;
				}

				// Swap the x, y source data to y, x in the destination
				dd.setPixel(dx, dy, sd.getPixel(sx, sy));
				// Swap also the alpha layer
				if (containsAlpha) {
					dd.setAlpha(dx, dy, sd.getAlpha(sx, sy));
				}
			}
		}

		// Create the vertical image
		return new Image(display, dd);
		} catch (Exception e){
			// Exception. Return original image
			e.printStackTrace();
			return image;
		}
	}

}
