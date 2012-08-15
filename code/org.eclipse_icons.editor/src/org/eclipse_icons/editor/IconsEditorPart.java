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
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse_icons.editor.utils.image.Utils;
import org.eclipse_icons.editor.utils.ui.UIUtils;

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

	@Override
	public void doSave(IProgressMonitor monitor) {
		monitor.beginTask("Save", 1);
		ImageData newImageData = (ImageData) imageData.clone();
		for (PixelItem pixelItem: pixels){
			newImageData.setAlpha(pixelItem.realPosition.x, pixelItem.realPosition.y, pixelItem.alpha);
		}
		
		
		Image image = new Image(Display.getCurrent(),newImageData);
		String fileAbsPath = input.getFile().getLocation().toOSString();
		int imageFormat = Utils.getImageFormatFromExtension(input.getFile().getFileExtension());
		Utils.saveIconToFile(image, fileAbsPath, imageFormat);
		
		// no dirty
		modified=false;
		firePropertyChange(IEditorPart.PROP_DIRTY);
		
		monitor.worked(1);
		monitor.done();
		
		// refresh
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
		setPartName("Icon: " + ((FileEditorInput) input).getName());
		
		imageData = UIUtils.getImageFromResource(this.input.getFile()).getImageData();
		iconWidth = imageData.width;
		iconHeight = imageData.height;
		intializePixels(imageData);
	}

	/**
	 * @param file
	 */
	private void intializePixels(ImageData imageData) {
		for (int y=0; y<imageData.height; y++){
			for (int x=0; x<imageData.width; x++){
				PixelItem pixel = new PixelItem();
				int paletteInt = imageData.getPixel(x, y);
				RGB rgb = imageData.palette.getRGB(paletteInt);
				pixel.color = new Color(Display.getCurrent(),rgb);
				pixel.alpha = imageData.getAlpha(x, y);
				pixel.realPosition = new Point(x,y);
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
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		
		canvas = createCanvas(parent, new PaintListener() {
            public void paintControl(PaintEvent e) {
                GC gc = e.gc;
                Point cext = canvas.getSize();
                gc.fillRectangle(0, 0, cext.x, cext.y);
                for (Iterator<PixelItem> i = pixels.iterator(); i.hasNext();) {
                    PixelItem pi = (PixelItem)i.next();
                    pi.paint(gc);
                }
            }
		});
		
		canvas.addListener(SWT.MouseDown, new Listener() {
		      public void handleEvent(Event e) {
		          Tracker tracker = new Tracker(canvas, SWT.NONE);
		          tracker.setRectangles(new Rectangle[] { new Rectangle(e.x - 5, e.y - 5,
		              10, 10), });
		          tracker.open();
		          for (PixelItem pixel : pixels){
		        	  if (pixel.pixelRectangle.contains(e.x, e.y)){
		        		  pixel.alpha = 0;
		        		  modified=true;
		        		  firePropertyChange(IEditorPart.PROP_DIRTY);
		        		  break;
		        	  }
		          }
		          canvas.redraw();
		        }
		      });
		
		canvas.setLayout(new FillLayout());

		doDraw();
	}

	@Override
	public void setFocus() {
	}
	

protected Canvas createCanvas(Composite parent, int style, 
                              PaintListener pl) {
    Canvas c = new Canvas(parent, style);
    if (pl != null) {
        c.addPaintListener(pl);
    }
    return c;
}
protected Canvas createCanvas(Composite parent, PaintListener pl) {
    return createCanvas(parent, SWT.NONE, pl);
}

	protected class PixelItem  {
		public Rectangle pixelRectangle;
		public Color color;
		public int alpha;
		public Point realPosition;
		
		public void paint(GC gc) {
			gc.setAlpha(alpha);
			gc.setBackground(color);
            gc.fillRectangle(pixelRectangle.x, pixelRectangle.y, pixelRectangle.width, pixelRectangle.height);
        }
    }

	public void doClear() {
		pixels.clear();
		canvas.redraw();
	}

	public void doDraw() {
		int x = 0;
		int y = 0;
		for (PixelItem pixelItem : pixels){
			pixelItem.pixelRectangle = new Rectangle(x, y, pixelLength , pixelLength);
			x += pixelLength;
			if (x >= pixelLength*iconWidth){
				x=0;
				y += pixelLength;
			}
		}
		canvas.redraw();
	}
	

}


