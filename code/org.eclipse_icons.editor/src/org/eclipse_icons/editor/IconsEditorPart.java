package org.eclipse_icons.editor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse_icons.editor.utils.ui.SaveAsContainerSelectionDialog;
import org.eclipse_icons.editor.utils.ui.UIUtils;

/**
 * Eclipse Icons Editor
 * 
 * @author Jabier Martinez
 */
public class IconsEditorPart extends EditorPart implements ISaveablePart {

	// Editor
	public static final String ID = "org.eclipse_icons.editor.iconEditor";
	private FileEditorInput input;
	Canvas canvas = null;

	// The original icon dimension
	int iconHeight;
	int iconWidth;

	// used for isDirty editor property
	boolean modified = false;

	ImageData imageData;

	// pixels list. Size: iconWidth * iconHeight
	protected List<PixelItem> pixels = new ArrayList<PixelItem>();
	
	PixelItem colorPickerSelection = null;

	// Whether the user is drawing/erasing something
	boolean drawing = false;

	// States
	private ToolItem colorPickerToolItem;
	private ToolItem paintToolItem;
	private ToolItem eraseToolItem;

	// Zoom
	private static final int ZOOM_MAXIMUM = 50;
	private static final int ZOOM_MINIMUM = 1;
	private static final int ZOOM_INITIAL = 20;
	private Scale zoomScale = null;
	int pixelLength = ZOOM_INITIAL;

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		if (!(input instanceof FileEditorInput)) {
			// TODO improve error handling. For example dropping an icon from the icons view
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Icons editor", "Wrong input. Try to save the resource in the workspace first.");
			return;
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

	@Override
	public void createPartControl(Composite parent_original) {
	
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent_original.setLayout(gridLayout);
	
		ToolBar toolBar = new ToolBar(parent_original, SWT.FLAT);
	
		paintToolItem = new ToolItem(toolBar, SWT.CHECK);
		paintToolItem.setToolTipText("Paint");
		paintToolItem.setSelection(false);
		paintToolItem.setImage(Activator.getImageDescriptor(
				"icons/editor/paint.png").createImage());
		paintToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// for the moment, only selected colors with the color picker is allowed
				if (colorPickerSelection==null){
					MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Info", "Pick a color from the image before painting");
					paintToolItem.setSelection(false);
					colorPickerToolItem.setSelection(true);
					eraseToolItem.setSelection(false);
				} else {
					paintToolItem.setSelection(true);
					colorPickerToolItem.setSelection(false);
					eraseToolItem.setSelection(false);
				}
			}
		});
	
		colorPickerToolItem = new ToolItem(toolBar, SWT.CHECK);
		colorPickerToolItem.setToolTipText("Pick Color");
		colorPickerToolItem.setSelection(false);
		colorPickerToolItem.setImage(Activator.getImageDescriptor(
				"icons/editor/colorPicker.png").createImage());
		colorPickerToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				paintToolItem.setSelection(false);
				colorPickerToolItem.setSelection(true);
				eraseToolItem.setSelection(false);
			}
		});
	
		new ToolItem(toolBar, SWT.SEPARATOR);
	
		eraseToolItem = new ToolItem(toolBar, SWT.CHECK);
		eraseToolItem.setToolTipText("Erase");
		eraseToolItem.setSelection(true);
		eraseToolItem.setImage(Activator.getImageDescriptor(
				"icons/editor/erase.png").createImage());
		eraseToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				paintToolItem.setSelection(false);
				colorPickerToolItem.setSelection(false);
				eraseToolItem.setSelection(true);
			}
		});
	
		new ToolItem(toolBar, SWT.SEPARATOR);
	
		addZoomToolItems(toolBar);
	
		GridData gridData = new GridData(GridData.FILL, SWT.BEGINNING, true,
				false);
		toolBar.setLayoutData(gridData);
	
		zoomScale = new Scale(parent_original, SWT.NONE);
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
				
				// global information for painting the background
				int halfPixel = pixelLength/2;
				
				// paint pixels
				for (Iterator<PixelItem> i = pixels.iterator(); i.hasNext();) {
					PixelItem pixel = (PixelItem) i.next();
					// paint background squares for transparent (only if not original size)
					if (pixelLength != 1 && pixel.alpha!=255){
						gc.setAlpha(255);
						gc.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WHITE));
						gc.fillRectangle(pixel.pixelRectangle.x,pixel.pixelRectangle.y,halfPixel, halfPixel);
						gc.fillRectangle(pixel.pixelRectangle.x+halfPixel,pixel.pixelRectangle.y+halfPixel, halfPixel, halfPixel);
					}
					// paint the pixel itself
					pixel.paint(gc);
				}
				
				// paint boundary rectangle (only if not original size)
				if (pixelLength != 1) {
					gc.setAlpha(255);
					gc.drawRectangle(0, 0, pixelLength * iconWidth, pixelLength
							* iconHeight);
				}
			}

		});
	
		gridData = new GridData(GridData.FILL, GridData.FILL, true, true);
		gridData.horizontalSpan = 2;
		canvas.setLayoutData(gridData);
	
		// Mouse listeners to draw
		createCanvasMouseListeners();
	
		applyZoom(ZOOM_INITIAL);
	}

	public void performSave(String fileAbsPath, IProgressMonitor monitor) {
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
		int imageFormat = Utils.getImageFormatFromExtension(input.getFile()
				.getFileExtension());

		// Save it
		Utils.saveIconToFile(image, fileAbsPath, imageFormat);

		monitor.worked(1);
		monitor.done();

		// refresh workspace
		UIUtils.refreshWorkspace(fileAbsPath);
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		performSave(input.getFile().getLocation().toOSString(), monitor);
		
		// Set editor as no dirty
		modified = false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
	}
	
	@Override
	public void doSaveAs() {
		SaveAsContainerSelectionDialog dialog = new SaveAsContainerSelectionDialog(Display.getCurrent().getActiveShell(),ResourcesPlugin
				.getWorkspace().getRoot(), false, "Select image container and name",input.getFile().getName());
		if (dialog.open() == Dialog.OK){
			IPath selectedContainer = (IPath) dialog.getResult()[0];
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		    IResource res = root.findMember(selectedContainer);
		    String location = res.getLocation().append(dialog.getFileName()).toOSString();
			performSave(location, new NullProgressMonitor());
			
			// Open the file
			UIUtils.openFile(location);
		}
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
				
				// Take care of transparency types
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
		return true;
	}

	private void createCanvasMouseListeners() {
		// Mouse Down
		canvas.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event e) {

				PixelItem selectedPixel = getCanvasPixel(e.x, e.y);
				if (selectedPixel != null) {
					// Erase
					if (eraseToolItem.getSelection()) {
						drawing = true;
						paintTransparentPixel(selectedPixel);
					}
					// Paint
					else if (paintToolItem.getSelection()) {
						drawing = true;
						paintPixel(selectedPixel);
					}
					// Pick Color
					else if (colorPickerToolItem.getSelection()) {
						drawing = false;
						paintToolItem.setSelection(true);
						colorPickerToolItem.setSelection(false);
						eraseToolItem.setSelection(false);
						colorPickerSelection = (PixelItem) selectedPixel.clone();
					}
				}
			}

		});

		// Mouse Move
		canvas.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event e) {
				// Only process this if drawing
				if (drawing){
					PixelItem selectedPixel = getCanvasPixel(e.x, e.y);
					if (selectedPixel != null) {
						// Erase
						if (eraseToolItem.getSelection()) {
							paintTransparentPixel(selectedPixel);
						}
						// Paint
						else if (paintToolItem.getSelection()) {
							paintPixel(selectedPixel);
						}
					}
				}
			}
		});

		// Mouse Up
		canvas.addListener(SWT.MouseUp, new Listener() {
			public void handleEvent(Event e) {
				drawing = false;
			}
		});
	}

	private void addZoomToolItems(ToolBar toolBar) {
		ToolItem zoomOriginal = new ToolItem(toolBar, SWT.PUSH);
		zoomOriginal.setToolTipText("Original size");
		zoomOriginal.setImage(Activator.getImageDescriptor(
				"icons/editor/zoomOriginal.png").createImage());
		zoomOriginal.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(1);
			}
		});

		ToolItem zoomIn = new ToolItem(toolBar, SWT.PUSH);
		zoomIn.setToolTipText("Zoom In");
		zoomIn.setImage(Activator.getImageDescriptor("icons/editor/zoomIn.png")
				.createImage());
		zoomIn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(pixelLength + 10);
			}
		});

		ToolItem zoomOut = new ToolItem(toolBar, SWT.PUSH);
		zoomOut.setToolTipText("Zoom Out");
		zoomOut.setImage(Activator.getImageDescriptor(
				"icons/editor/zoomOut.png").createImage());
		zoomOut.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				applyZoom(pixelLength - 10);
			}
		});
	}

	@Override
	public void setFocus() {
	}

	private void paintTransparentPixel(PixelItem pixel) {
		// check if change is needed
		if (pixel.alpha != 0) {
			// set alpha to 0
			pixel.alpha = 0;
			notifyPixelModification(pixel);
		}
	}

	private void paintPixel(PixelItem pixel) {
		// check if change is needed
		if (pixel.alpha != colorPickerSelection.alpha ||
				!pixel.color.equals(colorPickerSelection.color)){
			pixel.alpha = colorPickerSelection.alpha;
			pixel.color = colorPickerSelection.color;
			notifyPixelModification(pixel);
		}
	}
	
	private void notifyPixelModification(PixelItem pixel){
		// force isDirty method of the EditPart to register the
		// modification
		if (!modified){
			modified = true;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		// only redraw the modified one
		canvas.redraw(pixel.pixelRectangle.x, pixel.pixelRectangle.y,
				pixel.pixelRectangle.width, pixel.pixelRectangle.height,
				false);
	}

	/**
	 * Get the pixel at a given canvas relative position
	 * @param x
	 * @param y
	 * @return the pixel at this position or null
	 */
	private PixelItem getCanvasPixel(int x, int y) {
		
		// Check that is inside the boundaries
		// when moving the mouse you can for example negative values from the event
		if (x < 0 || y < 0 || x > iconWidth*pixelLength || y > iconHeight*pixelLength){
			return null;
		}
		
		// Calculate actual coordinates
		int pixelX = x / pixelLength;
		int pixelY = y / pixelLength;
		
		// Re-check after the / operation
		if (pixelX >= iconWidth || pixelY >= iconHeight){
			return null;
		}
		
		// Get the actual position in the array
		int positionInArray = pixelY * iconWidth + pixelX;
		
		return pixels.get(positionInArray);
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
	protected class PixelItem implements Cloneable {
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
		
		public Object clone() {
			Object o = null;
			try {
				 o = super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return o;
		}
	}

	/**
	 * Update pixel positions based on new pixelLength
	 * 
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
	 * 
	 * @param zoomValue
	 */
	public void applyZoom(int zoomValue) {
		// zoomValue inside zoom boundaries
		if (zoomValue < ZOOM_MINIMUM) {
			zoomValue = ZOOM_MINIMUM;
		} else if (zoomValue > ZOOM_MAXIMUM) {
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
