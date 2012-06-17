package org.eclipse_icons.editor.utils.image;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

public class OverlayImageDescriptor extends CompositeImageDescriptor {
	private ImageDescriptor imageDescriptor;
	private ImageDescriptor overlayImage;
	Point size;
	Point overlaySize;

	public OverlayImageDescriptor(ImageDescriptor imgDescriptor,
			ImageDescriptor overlayImage) {
		setImageDescriptor(imgDescriptor);
		setOverlayImage(overlayImage);
	}

	protected void drawCompositeImage(int arg0, int arg1) {
		drawImage(getImageDescriptor().getImageData(), 0, 0);
		ImageData overlayImageData = getOverlayImage().getImageData();
		int xValue = size.x - overlaySize.x;
		int yValue = size.y - overlaySize.y;
		;
		drawImage(overlayImageData, xValue, yValue);
	}

	protected Point getSize() {
		return size;
	}

	public void setImageDescriptor(ImageDescriptor imageDescriptor) {
		this.imageDescriptor = imageDescriptor;
		Rectangle bounds = imageDescriptor.createImage().getBounds();
		size = new Point(bounds.width, bounds.height);
	}

	public ImageDescriptor getImageDescriptor() {
		return imageDescriptor;
	}

	public void setOverlayImage(ImageDescriptor overlayImage) {
		this.overlayImage = overlayImage;
		Rectangle bounds = overlayImage.createImage().getBounds();
		overlaySize = new Point(bounds.width, bounds.height);
	}

	public ImageDescriptor getOverlayImage() {
		return overlayImage;
	}

}
