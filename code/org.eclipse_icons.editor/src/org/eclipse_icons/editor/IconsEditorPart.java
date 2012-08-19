package org.eclipse_icons.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
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
 * @author Jabier Martinez
 */
public class IconsEditorPart extends EditorPart implements ISaveablePart {
	
	// Editor
	public static final String ID = "org.eclipse_icons.editor.iconEditor";
	private FileEditorInput input;
	Canvas canvas = null;
	int iconHeight;
	int iconWidth;
	boolean modified = false;
	ImageData imageData;
	protected List<PixelItem> pixels = new ArrayList<PixelItem>();
	boolean drawing = false;
	
	// Zoom
	private static final int ZOOM_MAXIMUM = 50;
	private static final int ZOOM_MINIMUM = 1;
	private static final int ZOOM_INITIAL = 20;
	private Scale zoomScale = null;
	int pixelLength = ZOOM_INITIAL;
	
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
	public void createPartControl(Composite parent_original) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent_original.setLayout(gridLayout);
		
		ToolBar toolBar = new ToolBar (parent_original, SWT.FLAT);
		ToolItem erase = new ToolItem (toolBar, SWT.CHECK);
		erase.setToolTipText("Erase");
		erase.setSelection(true);
		erase.setImage(Activator.getImageDescriptor("icons/editor/erase.png").createImage());
		erase.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				((ToolItem)e.getSource()).setSelection(true);
			}
		});
		
		@SuppressWarnings("unused")
		ToolItem separator = new ToolItem(toolBar, SWT.SEPARATOR);
		
		ToolItem zoomOriginal = new ToolItem (toolBar, SWT.PUSH);
		zoomOriginal.setToolTipText("Original size");
		zoomOriginal.setImage(Activator.getImageDescriptor("icons/editor/zoomOriginal.png").createImage());
		zoomOriginal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(1);
			}
		});
		
		ToolItem zoomIn = new ToolItem (toolBar, SWT.PUSH);
		zoomIn.setToolTipText("Zoom In");
		zoomIn.setImage(Activator.getImageDescriptor("icons/editor/zoomIn.png").createImage());
		zoomIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(pixelLength + 10);
			}
		});
		
		ToolItem zoomOut = new ToolItem (toolBar, SWT.PUSH);
		zoomOut.setToolTipText("Zoom Out");
		zoomOut.setImage(Activator.getImageDescriptor("icons/editor/zoomOut.png").createImage());
		zoomOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(pixelLength - 10);
			}
		});
		
		GridData gridData = new GridData(GridData.FILL, SWT.BEGINNING, true, false);
		toolBar.setLayoutData(gridData);
		
		zoomScale = new Scale (parent_original, SWT.NONE);
		zoomScale.setToolTipText("Zoom");
		zoomScale.setMinimum(ZOOM_MINIMUM);
		zoomScale.setMaximum(ZOOM_MAXIMUM);
		zoomScale.setSelection(ZOOM_INITIAL);
		zoomScale.setIncrement(1);
		
		zoomScale.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		        applyZoom(zoomScale.getSelection());
		      }
		    });
		
		
		gridData = new GridData(SWT.RIGHT, SWT.BEGINNING, false, false);
		zoomScale.setLayoutData(gridData);
		
		canvas = createCanvas(parent_original, new PaintListener() {
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
		
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		canvas.setLayoutData(gridData);

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

		applyZoom(ZOOM_INITIAL);
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
	
	/**
	 * Apply Zoom
	 * @param zoomValue
	 */
	public void applyZoom(int zoomValue){
		// zoomValue inside zoom boundaries
		if (zoomValue<ZOOM_MINIMUM){
			zoomValue = ZOOM_MINIMUM;
		} else if (zoomValue>ZOOM_MAXIMUM){
			zoomValue = ZOOM_MAXIMUM;
		}
		// update and redraw
		pixelLength = zoomValue;
		updatePixelsPositions(pixelLength);
		zoomScale.setSelection(pixelLength);
		canvas.redraw();
	}
	
	@Override
    public void dispose() {
    	super.dispose();
    	pixels.clear();
    	pixels = null;
    	imageData = null;
    }

}
