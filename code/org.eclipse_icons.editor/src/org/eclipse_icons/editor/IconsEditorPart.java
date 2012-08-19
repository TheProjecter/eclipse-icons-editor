package org.eclipse_icons.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse_icons.editor.utils.image.Utils;
import org.eclipse_icons.editor.utils.ui.UIUtils;

/**
 * Eclipse Icons Editor
 * 
 * @author Jabier Martinez
 */
public class IconsEditorPart extends EditorPart implements ISaveablePart {

	public static final String ID = "org.eclipse_icons.editor.iconEditor";
	private FileEditorInput input;
	Canvas canvas = null;
	int pixelLength = 20;
	int iconHeight;
	int iconWidth;
	boolean modified = false;
	ImageData imageData;
	protected List<PixelItem> pixels = new ArrayList<PixelItem>();
	boolean drawing = false;

	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Save", 1);
		ImageData newImageData = (ImageData) imageData.clone();

		// Modify imageData with pixels information
		for (PixelItem pixelItem : pixels) {
			newImageData.setAlpha(pixelItem.realPosition.x,
					pixelItem.realPosition.y, pixelItem.alpha);
			if (imageData.getTransparencyType() == SWT.TRANSPARENCY_PIXEL) {
				if (pixelItem.alpha == 0) {
					newImageData.setPixel(pixelItem.realPosition.x,
							pixelItem.realPosition.y,
							newImageData.transparentPixel);
				}
			}
		}

		// Create image
		Image image = new Image(Display.getCurrent(), newImageData);
		String fileAbsPath = input.getFile().getLocation().toOSString();
		int imageFormat = Utils.getImageFormatFromExtension(input.getFile()
				.getFileExtension());
		
		// Save it
		Utils.saveIconToFile(image, fileAbsPath, imageFormat);

		// Set editor as no dirty
		modified = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);

		monitor.worked(1);
		monitor.done();

		// refresh workspace
		UIUtils.refreshWorkspace(fileAbsPath);
	}

	@Override
	public void doSaveAs() {
		// TODO
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof FileEditorInput)) {
			throw new RuntimeException("Wrong input");
		}
		this.input = (FileEditorInput) input;
		setSite(site);
		setInput(input);

		// Sets the name of the editor with file name
		setPartName(((FileEditorInput) input).getName());

		imageData = UIUtils.getImageFromResource(this.input.getFile())
				.getImageData();
		iconWidth = imageData.width;
		iconHeight = imageData.height;
		intializePixels(imageData);
	}

	/**
	 * initialize Pixels information with imageData information
	 * 
	 * @param imageData
	 */
	private void intializePixels(ImageData imageData) {
		for (int y = 0; y < imageData.height; y++) {
			for (int x = 0; x < imageData.width; x++) {
				PixelItem pixel = new PixelItem();
				int paletteInt = imageData.getPixel(x, y);
				RGB rgb = imageData.palette.getRGB(paletteInt);
				pixel.color = new Color(Display.getCurrent(), rgb);

				if (imageData.getTransparencyType() == SWT.TRANSPARENCY_PIXEL
						&& imageData.transparentPixel == paletteInt) {
					pixel.alpha = 0;
				} else {
					pixel.alpha = imageData.getAlpha(x, y);
				}

				pixel.realPosition = new Point(x, y);
				pixels.add(pixel);
			}
		}
	}

	@Override
	public boolean isDirty() {
		return modified;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// not yet
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {

		canvas = createCanvas(parent, new PaintListener() {
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				gc.drawRectangle(0, 0, pixelLength * iconWidth, pixelLength
						* iconHeight);
				for (Iterator<PixelItem> i = pixels.iterator(); i.hasNext();) {
					PixelItem pixel = (PixelItem) i.next();
					pixel.paint(gc);
				}
			}
		});
		
		canvas.setLayout(new FillLayout());

		// Mouse listeners to draw
		canvas.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {
				drawing = true;
				paintTransparentPixel(e.x, e.y);
			}
		});

		canvas.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event e) {
				if (drawing) {
					paintTransparentPixel(e.x, e.y);
				}
			}
		});

		canvas.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				drawing = false;
			}
		});

		// initialize pixel positions
		updatePixelsPositions(pixelLength);

		// draw
		canvas.redraw();
	}

	@Override
	public void setFocus() {
	}

	
	/**
	 * Paint transparent pixel
	 * @param x
	 * @param y
	 */
	private void paintTransparentPixel(int x, int y) {
		// find the selected pixel
		for (PixelItem pixel : pixels) {
			if (pixel.pixelRectangle.contains(x, y)) {
				// check if change is needed
				if (pixel.alpha != 0) {
					// set alpha to 0
					pixel.alpha = 0;
					// force isDirty method of the EditPart to register the
					// modification
					modified = true;
					firePropertyChange(IEditorPart.PROP_DIRTY);
					// only redraw the modified one
					canvas.redraw(pixel.pixelRectangle.x,
							pixel.pixelRectangle.y, pixel.pixelRectangle.width,
							pixel.pixelRectangle.height, false);
				}
				// already processed
				break;
			}
		}
	}

	protected Canvas createCanvas(Composite parent, PaintListener pl) {
		Canvas c = new Canvas(parent, SWT.NONE);
		if (pl != null) {
			c.addPaintListener(pl);
		}
		return c;
	}

	/**
	 * Pixel item class to be shown in the canvas and also used to save the
	 * modified image
	 */
	protected class PixelItem {
		public Rectangle pixelRectangle;
		public Color color;
		public int alpha;
		public Point realPosition;

		public void paint(GC gc) {
			gc.setAlpha(alpha);
			gc.setBackground(color);
			gc.fillRectangle(pixelRectangle.x, pixelRectangle.y,
					pixelRectangle.width, pixelRectangle.height);
		}
	}

	/**
	 * Update pixel positions based on new pixelLength
	 * @param pixelLength_p
	 */
	public void updatePixelsPositions(int pixelLength_p) {
		int x = 0;
		int y = 0;
		for (PixelItem pixelItem : pixels) {
			pixelItem.pixelRectangle = new Rectangle(x, y, pixelLength_p,
					pixelLength_p);
			x += pixelLength_p;
			if (x >= pixelLength_p * iconWidth) {
				x = 0;
				y += pixelLength_p;
			}
		}
	}
	
	@Override
    public void dispose() {
    	super.dispose();
    	pixels.clear();
    	pixels = null;
    	imageData = null;
    }

}
