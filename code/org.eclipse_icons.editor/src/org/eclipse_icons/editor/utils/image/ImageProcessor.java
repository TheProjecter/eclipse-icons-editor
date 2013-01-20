package org.eclipse_icons.editor.utils.image;

import org.eclipse.draw2d.ImageUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse_icons.editor.Activator;
import org.eclipse_icons.editor.views.Icon;

public class ImageProcessor {

	public static Image process(String id, String overlayIcon, String baseIcon) {

		Image baseImage = new Image(Display.getCurrent(), baseIcon);
		Image overlayImage = new Image(Display.getCurrent(), overlayIcon);

		int baseWidth = baseImage.getBounds().width;
		int baseHeight = baseImage.getBounds().height;
		int overlayWidth = overlayImage.getBounds().width;
		int overlayHeight = overlayImage.getBounds().height;

		if (id.equals(Icon.BASE_ICON)) {
			return baseImage;
		}

		if (id.equals(Icon.CENTERED_OVERLAY_ICON)) {
			int x = (baseWidth / 2) - (overlayWidth / 2);
			int y = (baseHeight / 2) - (overlayHeight / 2);
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}

		if (id.equals(Icon.TOP_LEFT_CORNER_OVERLAY_ICON)) {
			int x = 0;
			int y = 0;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.TOP_RIGHT_CORNER_OVERLAY_ICON)) {
			int x = baseWidth - overlayWidth;
			int y = 0;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.BOTTOM_RIGHT_CORNER_OVERLAY_ICON)) {
			int x = baseWidth - overlayWidth;
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.BOTTOM_LEFT_CORNER_OVERLAY_ICON)) {
			int x = 0;
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}

		if (id.equals(Icon.TOP_SIDE_OVERLAY_ICON)) {
			int x = (baseWidth / 2) - (overlayWidth / 2);
			int y = 0;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.RIGHT_SIDE_OVERLAY_ICON)) {
			int x = baseWidth - overlayWidth;
			int y = (baseHeight / 2) - (overlayHeight / 2);
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.BOTTOM_SIDE_OVERLAY_ICON)) {
			int x = (baseWidth / 2) - (overlayWidth / 2);
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}
		if (id.equals(Icon.LEFT_SIDE_OVERLAY_ICON)) {
			int x = 0;
			int y = (baseHeight / 2) - (overlayHeight / 2);
			return Utils.createOverlapedImage(overlayImage, baseImage,
					new Point(x, y));
		}

		if (id.equals(Icon.ROTATE_LEFT_BASE_ICON)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 2).getImageData());
		}
		if (id.equals(Icon.ROTATE_RIGHT_BASE_ICON)) {
			return new Image(baseImage.getDevice(), Utils.rotateOrFlip(
					baseImage, SWT.RIGHT).getImageData());
		}
		if (id.equals(Icon.ROTATE_180_BASE_ICON)) {
			return new Image(baseImage.getDevice(), Utils.rotateOrFlip(
					baseImage, SWT.DOWN).getImageData());
		}
		if (id.equals(Icon.FLIP_HORIZONTAL_BASE_ICON)) {
			return new Image(baseImage.getDevice(), Utils.rotateOrFlip(
					baseImage, SWT.HORIZONTAL).getImageData());
		}
		if (id.equals(Icon.FLIP_VERTICAL_BASE_ICON)) {
			return new Image(baseImage.getDevice(), Utils.rotateOrFlip(
					baseImage, SWT.VERTICAL).getImageData());
		}
		if (id.equals(Icon.COLOR_DISABLED)) {
			return new Image(baseImage.getDevice(), baseImage,
					SWT.IMAGE_DISABLE);
		}
		if (id.equals(Icon.COLOR_GRAY)) {
			return new Image(baseImage.getDevice(), baseImage, SWT.IMAGE_GRAY);
		}
		if (id.contains(Icon.COLOR_RGB)) {
			// input example COLOR_RGB255,0,0
			String[] rgb = id.substring(Icon.COLOR_RGB.length()).split(",");
			return new Image(
					baseImage.getDevice(),
					ImageUtilities.createShadedImage(baseImage, new Color(
							baseImage.getDevice(), Integer.parseInt(rgb[0]),
							Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]))));
		}
		
		if (id.equals(Icon.SCALE_200)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 2).getImageData());
		}
		if (id.equals(Icon.SCALE_75)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 0.75).getImageData());
		}
		if (id.equals(Icon.SCALE_60)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 0.6).getImageData());
		}
		if (id.equals(Icon.SCALE_50)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 0.5).getImageData());
		}
		if (id.equals(Icon.SCALE_40)) {
			return new Image(baseImage.getDevice(), Utils.scaleImage(baseImage, 0.4).getImageData());
		}


		else {
			return Activator.getImageDescriptor("icons/default/base.png")
					.createImage();
		}
	}

}
