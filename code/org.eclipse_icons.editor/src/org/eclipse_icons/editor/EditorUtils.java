package org.eclipse_icons.editor;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.themes.ColorUtil;

/**
 * Editor Utils
 * 
 * @author Jabier Martinez
 */
public class EditorUtils {

	private IconsEditorPart editor;

	public EditorUtils(IconsEditorPart editor) {
		this.editor = editor;
	}

	/**
	 * Create a fancy square image
	 * 
	 * @param colorPickerSelection
	 * @return
	 */
	public static Image createImageForColorSelection(RGB rgb, int alpha) {
		PaletteData paletteData = new PaletteData(new RGB[] {
				new RGB(255, 255, 255), rgb, new RGB(10, 10, 10) });
		ImageData imageData = new ImageData(16, 16, 2, paletteData);

		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 16; y++) {
				if (x == 15 || y == 15) {
					imageData.setPixel(x, y, 2);
					imageData.setAlpha(x, y, 255);
				} else if (x == 0 || y == 0 || x == 14 || y == 14) {
					imageData.setPixel(x, y, 0);
					imageData.setAlpha(x, y, 255);
				} else {
					imageData.setPixel(x, y, 1);
					imageData.setAlpha(x, y, alpha);
				}
			}
		}
		return new Image(Display.getCurrent(), imageData);
	}

	/**
	 * Compare color and alpha
	 * 
	 * @param pixelItem1
	 * @param pixelItem2
	 * @return is different color
	 */
	public static boolean isDifferentColor(PixelItem pixelItem1,
			PixelItem pixelItem2) {
		return (pixelItem1.alpha != pixelItem2.alpha || !pixelItem1.color
				.equals(pixelItem2.color));
	}

	/**
	 * We paint the selected pixel and then we recursively visit the left,
	 * right, up and down pixels.
	 * 
	 * @param referencePixel
	 * @param pixel
	 */
	public boolean fillPixels(PixelItem referencePixel, PixelItem pixel) {

		// check if change is needed
		if (paintPixel(editor.colorPickerSelection, pixel)) {

			PixelItem rightPixel = getRightPixel(pixel);
			if (rightPixel != null) {
				if (!EditorUtils.isDifferentColor(referencePixel, rightPixel)) {
					fillPixels(referencePixel, rightPixel);
				}
			}

			PixelItem leftPixel = getLeftPixel(pixel);
			if (leftPixel != null) {
				if (!EditorUtils.isDifferentColor(referencePixel, leftPixel)) {
					fillPixels(referencePixel, leftPixel);
				}
			}

			PixelItem upPixel = getUpPixel(pixel);
			if (upPixel != null) {
				if (!EditorUtils.isDifferentColor(referencePixel, upPixel)) {
					fillPixels(referencePixel, upPixel);
				}
			}

			PixelItem downPixel = getDownPixel(pixel);
			if (downPixel != null) {
				if (!EditorUtils.isDifferentColor(referencePixel, downPixel)) {
					fillPixels(referencePixel, downPixel);
				}
			}
			return true;
		}
		return false;
	}

	public PixelItem getDownPixel(PixelItem pixel) {
		if (pixel.realPosition.y == editor.iconHeight - 1) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return editor.pixels.get(position + editor.iconWidth);
		}
	}

	public PixelItem getLeftPixel(PixelItem pixel) {
		if (pixel.realPosition.x == 0) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return editor.pixels.get(position - 1);
		}
	}

	public int getPixelPositionInTheArray(int pixelX, int pixelY) {
		// Get the actual position in the array
		int positionInArray = pixelY * editor.iconWidth + pixelX;
		return positionInArray;
	}

	public PixelItem getRightPixel(PixelItem pixel) {
		if (pixel.realPosition.x == editor.iconWidth - 1) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return editor.pixels.get(position + 1);
		}
	}

	public PixelItem getUpPixel(PixelItem pixel) {
		if (pixel.realPosition.y == 0) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return editor.pixels.get(position - editor.iconWidth);
		}
	}

	public void paintFilledRectangle() {
		// get corner pixels
		PixelItem origin = getCanvasPixel(editor.paintRectangle.x,
				editor.paintRectangle.y);
		PixelItem end = getCanvasPixel(editor.paintRectangle.x
				+ editor.paintRectangle.width, editor.paintRectangle.y
				+ editor.paintRectangle.height);
		PixelItem bottomLeft = editor.pixels.get(getPixelPositionInTheArray(
				origin.realPosition.x, end.realPosition.y));
		PixelItem upRight = editor.pixels.get(getPixelPositionInTheArray(
				end.realPosition.x, origin.realPosition.y));
		// paint horizontal lines
		while (origin != null
				&& origin.realPosition.y != bottomLeft.realPosition.y) {
			paintHorizontalLine(origin, upRight);
			if (origin.realPosition.y < bottomLeft.realPosition.y) {
				origin = getDownPixel(origin);
				upRight = getDownPixel(upRight);
			} else {
				origin = getUpPixel(origin);
				upRight = getUpPixel(upRight);
			}
		}
		// paint last line
		paintHorizontalLine(origin, upRight);
	}

	public void paintHorizontalLine(PixelItem originPixel, PixelItem otherPixel) {
		paintPixel(editor.colorPickerSelection, originPixel);
		paintPixel(editor.colorPickerSelection, otherPixel);
		// we should go from left to right
		if (otherPixel.realPosition.x > originPixel.realPosition.x) {
			PixelItem rightPixel = getRightPixel(originPixel);
			while (rightPixel != null
					&& rightPixel.realPosition.x != otherPixel.realPosition.x) {
				paintPixel(editor.colorPickerSelection, rightPixel);
				rightPixel = getRightPixel(rightPixel);
			}
		} else if (otherPixel.realPosition.x < originPixel.realPosition.x) {
			// from right to left
			PixelItem leftPixel = getLeftPixel(originPixel);
			while (leftPixel != null
					&& leftPixel.realPosition.x != otherPixel.realPosition.x) {
				paintPixel(editor.colorPickerSelection, leftPixel);
				leftPixel = getLeftPixel(leftPixel);
			}
		}
	}

	public boolean paintPixel(PixelItem referencePixel, PixelItem targetPixel) {
		// check if change is needed
		if (EditorUtils.isDifferentColor(referencePixel, targetPixel)) {
			targetPixel.alpha = referencePixel.alpha;
			targetPixel.color = referencePixel.color;
			editor.notifyPixelModification(targetPixel);
			return true;
		}
		return false;
	}

	public static boolean paintTransparentPixel(PixelItem pixel) {
		// check if change is needed
		if (pixel.alpha != 0) {
			// set alpha to 0
			pixel.alpha = 0;
			return true;
		}
		return false;
	}

	public void paintUnfilledRectangle() {
		PixelItem origin = getCanvasPixel(editor.paintRectangle.x,
				editor.paintRectangle.y);
		PixelItem end = getCanvasPixel(editor.paintRectangle.x
				+ editor.paintRectangle.width, editor.paintRectangle.y
				+ editor.paintRectangle.height);
		PixelItem bottomLeft = editor.pixels.get(getPixelPositionInTheArray(
				origin.realPosition.x, end.realPosition.y));
		PixelItem upRight = editor.pixels.get(getPixelPositionInTheArray(
				end.realPosition.x, origin.realPosition.y));
		paintHorizontalLine(origin, upRight);
		paintHorizontalLine(bottomLeft, end);
		paintVerticalLine(origin, bottomLeft);
		paintVerticalLine(end, upRight);
	}

	public void paintVerticalLine(PixelItem originPixel, PixelItem otherPixel) {
		paintPixel(editor.colorPickerSelection, originPixel);
		paintPixel(editor.colorPickerSelection, otherPixel);
		// we should go from top to bottom
		if (otherPixel.realPosition.y > originPixel.realPosition.y) {
			PixelItem downPixel = getDownPixel(originPixel);
			while (downPixel != null
					&& downPixel.realPosition.y != otherPixel.realPosition.y) {
				paintPixel(editor.colorPickerSelection, downPixel);
				downPixel = getDownPixel(downPixel);
			}
		} else if (otherPixel.realPosition.y < originPixel.realPosition.y) {
			// from bottom to up
			PixelItem upPixel = getUpPixel(originPixel);
			while (upPixel != null
					&& upPixel.realPosition.y != otherPixel.realPosition.y) {
				paintPixel(editor.colorPickerSelection, upPixel);
				upPixel = getUpPixel(upPixel);
			}
		}
	}

	/**
	 * Get the pixel at a given canvas relative position
	 * 
	 * @param x
	 * @param y
	 * @return the pixel at this position or null
	 */
	public PixelItem getCanvasPixel(int x, int y) {

		// Check that is inside the boundaries
		// when moving the mouse you can for example negative values from the
		// event
		if (x < 0 || y < 0 || x > editor.iconWidth * editor.pixelLength
				|| y > editor.iconHeight * editor.pixelLength) {
			return null;
		}

		// Calculate actual coordinates
		int pixelX = x / editor.pixelLength;
		int pixelY = y / editor.pixelLength;

		// Re-check after the / operation
		if (pixelX >= editor.iconWidth || pixelY >= editor.iconHeight) {
			return null;
		}

		int position = getPixelPositionInTheArray(pixelX, pixelY);
		return editor.pixels.get(position);
	}

	/**
	 * Change the rectangle
	 * 
	 * @param rectangle
	 */
	public void adjustRectangle(Rectangle rectangle) {
		PixelItem topLeft = getRectangleTopLeftPixelItem(rectangle);
		PixelItem bottomRight = getRectangleBottomRightPixelItem(rectangle);
		if (bottomRight != null && topLeft != null) {
			rectangle.x = topLeft.pixelRectangle.x;
			rectangle.y = topLeft.pixelRectangle.y;
			rectangle.width = (bottomRight.pixelRectangle.x - topLeft.pixelRectangle.x)
					+ editor.pixelLength;
			rectangle.height = (bottomRight.pixelRectangle.y - topLeft.pixelRectangle.y)
					+ editor.pixelLength;
		}
	}

	public PixelItem getRectangleBottomRightPixelItem(Rectangle rectangle) {
		positivizeRectangleData(rectangle);
		return getCanvasPixel(rectangle.x + rectangle.width, rectangle.y
				+ rectangle.height);
	}

	public PixelItem getRectangleTopLeftPixelItem(Rectangle rectangle) {
		positivizeRectangleData(rectangle);
		return getCanvasPixel(rectangle.x, rectangle.y);
	}

	/**
	 * Keep the same rectangle but positivize width and height to ease further
	 * calculations
	 * 
	 * @param rectangle
	 */
	public static void positivizeRectangleData(Rectangle rectangle) {
		if (rectangle == null) {
			return;
		}
		// negative width
		if (rectangle.width < 0) {
			rectangle.width = -rectangle.width;
			rectangle.x = rectangle.x - rectangle.width;
		}
		// negative height
		if (rectangle.height < 0) {
			rectangle.height = -rectangle.height;
			rectangle.y = rectangle.y - rectangle.height;
		}
	}

	/**
	 * Move selection
	 * 
	 * @param direction
	 */
	public boolean moveSelectedPixels(int direction) {
		// Check if possible, then move rectangle, then move pixels
		if (direction == SWT.ARROW_RIGHT) {
			// if left border => iconWidth
			if (editor.selectionRectangle.x >= editor.iconWidth) {
				return false;
			}
			editor.selectionRectangle.x++;
			for (PixelItem pixel : editor.selectedPixels) {
				pixel.realPosition.x++;
				pixel.pixelRectangle.x += editor.pixelLength;
			}

		} else if (direction == SWT.ARROW_LEFT) {
			// if right border <= 0
			if (editor.selectionRectangle.x + editor.selectionRectangle.width <= 0) {
				return false;
			}
			editor.selectionRectangle.x--;
			for (PixelItem pixel : editor.selectedPixels) {
				pixel.realPosition.x--;
				pixel.pixelRectangle.x -= editor.pixelLength;
			}

		} else if (direction == SWT.ARROW_UP) {
			// if bottom border <= 0
			if (editor.selectionRectangle.y + editor.selectionRectangle.height <= 0) {
				return false;
			}
			editor.selectionRectangle.y--;
			for (PixelItem pixel : editor.selectedPixels) {
				pixel.realPosition.y--;
				pixel.pixelRectangle.y -= editor.pixelLength;
			}

		} else if (direction == SWT.ARROW_DOWN) {
			// if up border >= iconHeight
			if (editor.selectionRectangle.y >= editor.iconHeight) {
				return false;
			}
			editor.selectionRectangle.y++;
			for (PixelItem pixel : editor.selectedPixels) {
				pixel.realPosition.y++;
				pixel.pixelRectangle.y += editor.pixelLength;
			}
		}
		return true;
	}

	/**
	 * Select all
	 */
	public void selectAll() {
		editor.selectToolItem(editor.selectToolItem);
		editor.selectionRectangle = new Rectangle(0, 0, editor.iconWidth,
				editor.iconHeight);
		// Create selectedPixels
		editor.selectedPixels = new ArrayList<PixelItem>();
		for (int y = 0; y < editor.iconHeight; y++) {
			for (int x = 0; x < editor.iconWidth; x++) {
				PixelItem originalPixel = editor.pixels
						.get(getPixelPositionInTheArray(x, y));
				PixelItem selectedPixel = (PixelItem) originalPixel.clone();
				editor.selectedPixels.add(selectedPixel);
			}
		}
		editor.paintRectangle = null;
		editor.selected = true;
		editor.selectedAndMoved = false;

		// TODO For some reason, first applyZoom call in createPartControl does
		// not prevent the moved selections outside of the icon bounds to be
		// hided. So this is a hack to be fixed. Same hack in
		// selectToolItem.addSelectionListener
		editor.zoomUtils.applyZoom(editor.zoomUtils.zoomScale.getSelection());

		editor.canvas.redraw();
		editor.canvas.setFocus();
	}

	/**
	 * Delete
	 */
	public void delete(boolean stopWithSelection) {
		// If the selection is not in movement, we delete the real pixels
		// else the selection was in movement, we do not delete anything and we deactivate selection
		if (!editor.selectedAndMoved) {
			// We change the colorPickerSelection color temporarily to perform the fill
			// so we store the previous
			PixelItem previousColor = (PixelItem) editor.colorPickerSelection
					.clone();
			editor.paintRectangle = new Rectangle(editor.selectionRectangle.x
					* editor.pixelLength, editor.selectionRectangle.y
					* editor.pixelLength, editor.selectionRectangle.width
					* editor.pixelLength - 1, editor.selectionRectangle.height
					* editor.pixelLength - 1);
			// Image has transparency
			if (editor.imageData.getTransparencyType() != SWT.TRANSPARENCY_NONE) {
				editor.colorPickerSelection.alpha = 0;
			} else {
				// No transparency
				// TODO Check images without transparency but that it could have transparency.
				// White for direct images
				if (editor.imageData.palette.isDirect) {
					editor.colorPickerSelection = getWhitePixelItem();
					// Indirect image. Closest color to white color in the palette
				} else {
					// TODO check first getAvailablePositionInThePalette to put the white color.
					editor.colorPickerSelection = getSimilarPixelItemInThePalette(255,255,255);
				}
			}
			paintFilledRectangle();

			// Restore previous color
			editor.colorPickerSelection = previousColor;
			
		}
		if (stopWithSelection){
			editor.paintRectangle = null;
			editor.selected = false;
			editor.selectedAndMoved = false;
			editor.selectionRectangle = null;
		}
		// Redraw
		editor.canvas.redraw();
		editor.canvas.setFocus();
	}

	/**
	 * Get from the palette the color more similar to the one defined by RGB
	 * @param R
	 * @param G
	 * @param B
	 * @return
	 */
	public PixelItem getSimilarPixelItemInThePalette(int R, int G, int B) {
		if (editor.imageData.palette==null || editor.imageData.palette.getRGBs()==null){
			return null;
		}
		int distance = Integer.MAX_VALUE;
		int moreSimilarIndex = 0;
		for (int i = 0; i<editor.imageData.palette.getRGBs().length; i++){
			RGB rgb = editor.imageData.palette.getRGB(i);
			int currentRGBDistance = Math.abs(R-rgb.red) + Math.abs(G-rgb.green) + Math.abs(B-rgb.blue);
			if (currentRGBDistance < distance){
				moreSimilarIndex = i;
			}
		}
		PixelItem pixelItem = new PixelItem();
		pixelItem.alpha = 255;
		RGB rgb = editor.imageData.palette.getRGB(moreSimilarIndex);
		pixelItem.color = new Color(Display.getCurrent(), rgb.red, rgb.green, rgb.red);
		return pixelItem;
	}
	

	/**
	 * Get white pixel item
	 * @return
	 */
	public PixelItem getWhitePixelItem() {
		PixelItem white = new PixelItem();
		white.alpha = 255;
		white.color = new Color(Display.getCurrent(), 255, 255, 255);
		return white;
	}

	/**
	 * Blend selection rectangle into Pixels
	 */
	public void blendSelection() {
		// Loop selection
		for (int y = editor.selectionRectangle.y; y < editor.selectionRectangle.y + editor.selectionRectangle.height; y++){
			for (int x = editor.selectionRectangle.x; x < editor.selectionRectangle.x + editor.selectionRectangle.width; x++){
				// Do nothing with positions out of the canvas
				if (x < editor.iconWidth && y < editor.iconHeight){
					// Get pixels
					PixelItem originalPixel = editor.pixels.get(getPixelPositionInTheArray(x,y));
					PixelItem selectionPixel = editor.selectedPixels.get((y-editor.selectionRectangle.y)*editor.selectionRectangle.width + (x-editor.selectionRectangle.x));

					// ratio for blending, selection is superposed
					int ratio = new Double((selectionPixel.alpha / 255.0) * 100.0).intValue();
					RGB blendedRGB = ColorUtil.blend(selectionPixel.color.getRGB(), originalPixel.color.getRGB(), ratio);
					
					// newAlpha is the maximum of both pixels
					int newAlpha = Math.min(255, Math.max(selectionPixel.alpha, originalPixel.alpha));
					PixelItem blendedPixelItem = new PixelItem();
					blendedPixelItem.alpha = newAlpha;
					blendedPixelItem.color = new Color(Display.getDefault(), blendedRGB);
					
					// update data
					paintPixel(blendedPixelItem, originalPixel);
				}
			}
		}
	}
	
	
}
