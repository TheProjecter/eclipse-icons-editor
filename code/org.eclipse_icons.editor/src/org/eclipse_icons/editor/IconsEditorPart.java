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
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
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
	private ToolItem currentColorToolItem;
	private ToolItem colorPickerToolItem;
	private ToolItem paintToolItem;
	private ToolItem fillToolItem;
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
			// TODO improve error handling. For example dropping an icon from
			// the icons view
			MessageDialog
					.openError(Display.getCurrent().getActiveShell(),
							"Icons editor",
							"Wrong input. Try to save the resource in the workspace first.");
			return;
		}
		this.input = (FileEditorInput) input;
		setSite(site);
		setInput(input);

		// Sets the name of the editor with file name
		setPartName(((FileEditorInput) input).getName());
		Image image = UIUtils.getImageFromResource(this.input.getFile());
		
		// Error loading the resource
		if (image == null){
			throw new PartInitException("It was not possible to load the resource as a valid image.");
		}
		
		imageData = image.getImageData();
		iconWidth = imageData.width;
		iconHeight = imageData.height;
		intializePixels(imageData);
	}

	/**
	 * Activate only the selected toolItem
	 * 
	 * @param toolItem
	 */
	private void selectToolItem(ToolItem toolItem) {
		paintToolItem.setSelection(paintToolItem == toolItem);
		colorPickerToolItem.setSelection(colorPickerToolItem == toolItem);
		eraseToolItem.setSelection(eraseToolItem == toolItem);
		fillToolItem.setSelection(fillToolItem == toolItem);
	}

	@Override
	public void createPartControl(Composite parent_original) {

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		parent_original.setLayout(gridLayout);

		ToolBar toolBar = new ToolBar(parent_original, SWT.FLAT);

		currentColorToolItem = new ToolItem(toolBar, SWT.CHECK);
		currentColorToolItem.setToolTipText("Current color");
		currentColorToolItem.setSelection(false);
		
		currentColorToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				
				RGB selectedColor = null;
				if (imageData.palette.isDirect){
					// Direct palette
					ColorDialog colorDialog = new ColorDialog(Display.getCurrent().getActiveShell());
					selectedColor = colorDialog.open();
				} else {
					// Indirect palette
					MessageDialog.openInformation(Display.getDefault()
					.getActiveShell(), "Info",
					"Images with indirect palette are not supported yet...\nPick a color from the image");
					selectToolItem(colorPickerToolItem);
					// TODO implement indirect palette
					// PaletteDialog dialog = new PaletteDialog(Display.getCurrent().getActiveShell());
					// dialog.setPalette(imageData.palette);
					// dialog.open();
				}
				
				if (selectedColor != null){
					// Update selectedPixel
					colorPickerSelection.color = new Color(Display.getCurrent(), selectedColor);
					colorPickerSelection.alpha = 255; // opaque
					currentColorToolItem.setImage(createImageForColorSelection(colorPickerSelection));
				}
				
				// Never show it as selected
				currentColorToolItem.setSelection(false);
			}
		});

		// initialize color selection
		// get first non transparent one
		for (PixelItem item : pixels) {
			if (item.alpha == 255) {
				colorPickerSelection = (PixelItem) item.clone();
				break;
			}
		}
		// if not found, get the first color
		if (colorPickerSelection == null) {
			colorPickerSelection = (PixelItem) pixels.get(0).clone();
		}
		currentColorToolItem
				.setImage(createImageForColorSelection(colorPickerSelection));

		new ToolItem(toolBar, SWT.SEPARATOR);

		paintToolItem = new ToolItem(toolBar, SWT.CHECK);
		paintToolItem.setToolTipText("Paint");
		paintToolItem.setSelection(false);
		paintToolItem.setImage(Activator.getImageDescriptor(
				"icons/editor/paint.png").createImage());
		paintToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// for the moment, only selected colors with the color picker is
				// allowed
				if (colorPickerSelection == null) {
					MessageDialog.openInformation(Display.getDefault()
							.getActiveShell(), "Info",
							"Pick a color from the image before painting");
					selectToolItem(colorPickerToolItem);
				} else {
					selectToolItem(paintToolItem);
				}
			}
		});

		fillToolItem = new ToolItem(toolBar, SWT.CHECK);
		fillToolItem.setToolTipText("Paint");
		fillToolItem.setSelection(false);
		fillToolItem.setImage(Activator.getImageDescriptor(
				"icons/editor/fill.png").createImage());
		fillToolItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// for the moment, only selected colors with the color picker is
				// allowed
				if (colorPickerSelection == null) {
					MessageDialog.openInformation(Display.getDefault()
							.getActiveShell(), "Info",
							"Pick a color from the image before painting");
					selectToolItem(colorPickerToolItem);
				} else {
					selectToolItem(fillToolItem);
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
				selectToolItem(colorPickerToolItem);
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
				selectToolItem(eraseToolItem);
			}
		});

		// Not enabled if bmp
		if (imageData.getTransparencyType() == SWT.TRANSPARENCY_NONE) {
			eraseToolItem.setToolTipText("Erase disabled in bmp files and transparency disabled images");
			eraseToolItem.setEnabled(false);
			selectToolItem(paintToolItem);
		}

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
				int halfPixel = pixelLength / 2;

				// paint pixels
				for (Iterator<PixelItem> i = pixels.iterator(); i.hasNext();) {
					PixelItem pixel = (PixelItem) i.next();
					// paint background squares for transparent (only if not
					// original size)
					if (pixelLength != 1 && pixel.alpha != 255) {
						gc.setAlpha(255);
						gc.setBackground(Display.getDefault().getSystemColor(
								SWT.COLOR_WHITE));
						gc.fillRectangle(pixel.pixelRectangle.x,
								pixel.pixelRectangle.y, halfPixel, halfPixel);
						gc.fillRectangle(pixel.pixelRectangle.x + halfPixel,
								pixel.pixelRectangle.y + halfPixel, halfPixel,
								halfPixel);
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
			// Save alpha
			newImageData.setAlpha(pixelItem.realPosition.x,
					pixelItem.realPosition.y, pixelItem.alpha);
			if (imageData.getTransparencyType() == SWT.TRANSPARENCY_PIXEL) {
				if (pixelItem.alpha == 0) {
					newImageData.setPixel(pixelItem.realPosition.x,
							pixelItem.realPosition.y,
							newImageData.transparentPixel);
				}
			}

			// Save colors
			RGB color = pixelItem.color.getRGB();

			// The image has a non-direct color model
			if (!newImageData.palette.isDirect) {
				// Dont change the pixel data if it was already set as
				// transparency
				if ((imageData.getTransparencyType() == SWT.TRANSPARENCY_PIXEL && pixelItem.alpha != 0)
						|| imageData.getTransparencyType() != SWT.TRANSPARENCY_PIXEL) {
					// Get the index of the color in the palette
					for (int index = 0; index < newImageData.getRGBs().length; index++) {
						if (newImageData.getRGBs()[index].equals(color)) {
							// Save the new index
							newImageData.setPixel(pixelItem.realPosition.x,
									pixelItem.realPosition.y, index);
							break;
						}
					}
				}
			} else {
				// Direct color model
				newImageData.setPixel(pixelItem.realPosition.x,
						pixelItem.realPosition.y, color.hashCode());
			}

		}

		// Save it
		int imageFormat = Utils.getImageFormatFromExtension(input.getFile()
				.getFileExtension());		
		Utils.saveIconToFile(newImageData, fileAbsPath, imageFormat);

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
		SaveAsContainerSelectionDialog dialog = new SaveAsContainerSelectionDialog(
				Display.getCurrent().getActiveShell(), ResourcesPlugin
						.getWorkspace().getRoot(), false,
				"Select image container and name", input.getFile().getName());
		if (dialog.open() == Dialog.OK) {
			IPath selectedContainer = (IPath) dialog.getResult()[0];
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource res = root.findMember(selectedContainer);
			String location = res.getLocation().append(dialog.getFileName())
					.toOSString();
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
						paintPixel(colorPickerSelection, selectedPixel);
					}
					// Fill
					else if (fillToolItem.getSelection()) {
						drawing = false;
						fillPixels((PixelItem) selectedPixel.clone(),
								selectedPixel);
					}
					// Pick Color
					else if (colorPickerToolItem.getSelection()) {
						drawing = false;
						colorPickerSelection = (PixelItem) selectedPixel
								.clone();
						selectToolItem(paintToolItem);
						currentColorToolItem
								.setImage(createImageForColorSelection(colorPickerSelection));
					}
				}
			}

		});

		// Mouse Move
		canvas.addListener(SWT.MouseMove, new Listener() {
			public void handleEvent(Event e) {
				// Only process this if drawing
				if (drawing) {
					PixelItem selectedPixel = getCanvasPixel(e.x, e.y);
					if (selectedPixel != null) {
						// Erase
						if (eraseToolItem.getSelection()) {
							paintTransparentPixel(selectedPixel);
						}
						// Paint
						else if (paintToolItem.getSelection()) {
							paintPixel(colorPickerSelection, selectedPixel);
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

	/**
	 * Create a fancy square image
	 * @param colorPickerSelection
	 * @return
	 */
	private Image createImageForColorSelection(PixelItem colorPickerSelection) {
		PaletteData paletteData = new PaletteData(new RGB[] {
				new RGB(255, 255, 255), colorPickerSelection.color.getRGB(),
				new RGB(10, 10, 10) });
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
					imageData.setAlpha(x, y, colorPickerSelection.alpha);
				}
			}
		}
		return new Image(Display.getCurrent(), imageData);
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

	private boolean paintPixel(PixelItem referencePixel, PixelItem targetPixel) {
		// check if change is needed
		if (isDifferentColor(referencePixel, targetPixel)) {
			targetPixel.alpha = referencePixel.alpha;
			targetPixel.color = referencePixel.color;
			notifyPixelModification(targetPixel);
			return true;
		}
		return false;
	}

	private boolean isDifferentColor(PixelItem pixelItem1, PixelItem pixelItem2) {
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
	private void fillPixels(PixelItem referencePixel, PixelItem pixel) {

		// check if change is needed
		if (paintPixel(colorPickerSelection, pixel)) {

			PixelItem rightPixel = getRightPixel(pixel);
			if (rightPixel != null) {
				if (!isDifferentColor(referencePixel, rightPixel)) {
					fillPixels(referencePixel, rightPixel);
				}
			}

			PixelItem leftPixel = getLeftPixel(pixel);
			if (leftPixel != null) {
				if (!isDifferentColor(referencePixel, leftPixel)) {
					fillPixels(referencePixel, leftPixel);
				}
			}

			PixelItem upPixel = getUpPixel(pixel);
			if (upPixel != null) {
				if (!isDifferentColor(referencePixel, upPixel)) {
					fillPixels(referencePixel, upPixel);
				}
			}

			PixelItem downPixel = getDownPixel(pixel);
			if (downPixel != null) {
				if (!isDifferentColor(referencePixel, downPixel)) {
					fillPixels(referencePixel, downPixel);
				}
			}
		}
	}

	private PixelItem getRightPixel(PixelItem pixel) {
		if (pixel.realPosition.x == iconWidth - 1) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return pixels.get(position + 1);
		}
	}

	private PixelItem getLeftPixel(PixelItem pixel) {
		if (pixel.realPosition.x == 0) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return pixels.get(position - 1);
		}
	}

	private PixelItem getUpPixel(PixelItem pixel) {
		if (pixel.realPosition.y == 0) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return pixels.get(position - iconWidth);
		}
	}

	private PixelItem getDownPixel(PixelItem pixel) {
		if (pixel.realPosition.y == iconHeight - 1) {
			return null;
		} else {
			int position = getPixelPositionInTheArray(pixel.realPosition.x,
					pixel.realPosition.y);
			return pixels.get(position + iconWidth);
		}
	}

	private void notifyPixelModification(PixelItem pixel) {
		// force isDirty method of the EditPart to register the
		// modification
		if (!modified) {
			modified = true;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
		// only redraw the modified one
		canvas.redraw(pixel.pixelRectangle.x, pixel.pixelRectangle.y,
				pixel.pixelRectangle.width, pixel.pixelRectangle.height, false);
	}

	/**
	 * Get the pixel at a given canvas relative position
	 * 
	 * @param x
	 * @param y
	 * @return the pixel at this position or null
	 */
	private PixelItem getCanvasPixel(int x, int y) {

		// Check that is inside the boundaries
		// when moving the mouse you can for example negative values from the
		// event
		if (x < 0 || y < 0 || x > iconWidth * pixelLength
				|| y > iconHeight * pixelLength) {
			return null;
		}

		// Calculate actual coordinates
		int pixelX = x / pixelLength;
		int pixelY = y / pixelLength;

		// Re-check after the / operation
		if (pixelX >= iconWidth || pixelY >= iconHeight) {
			return null;
		}

		int position = getPixelPositionInTheArray(pixelX, pixelY);
		return pixels.get(position);
	}

	private int getPixelPositionInTheArray(int pixelX, int pixelY) {
		// Get the actual position in the array
		int positionInArray = pixelY * iconWidth + pixelX;
		return positionInArray;
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
