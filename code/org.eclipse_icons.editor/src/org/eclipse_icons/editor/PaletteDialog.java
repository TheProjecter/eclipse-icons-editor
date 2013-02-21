package org.eclipse_icons.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class PaletteDialog extends Dialog {

	  private String message;
	  private String input;
	  private PaletteData palette;

	  /**
	   * InputDialog constructor
	   * 
	   * @param parent the parent
	   */
	  public PaletteDialog(Shell parent) {
	    // Pass the default styles here
	    this(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	  }

	/**
	   * InputDialog constructor
	   * 
	   * @param parent the parent
	   * @param style the style
	   */
	  public PaletteDialog(Shell parent, int style) {
	    // Let users override the default styles
	    super(parent, style);
	    setText("Select color");
	    setMessage("");
	  }

	  /**
	   * Gets the message
	   * 
	   * @return String
	   */
	  public String getMessage() {
	    return message;
	  }

	  /**
	   * Sets the message
	   * 
	   * @param message the new message
	   */
	  public void setMessage(String message) {
	    this.message = message;
	  }

	  /**
	   * Gets the input
	   * 
	   * @return String
	   */
	  public String getInput() {
	    return input;
	  }

	  /**
	   * Sets the input
	   * 
	   * @param input the new input
	   */
	  public void setInput(String input) {
	    this.input = input;
	  }
	  
	  int SIZE = 300;
	  
	  /**
	   * Opens the dialog and returns the input
	   * 
	   * @return String
	   */
	  public String open() {
		  
	    // Create the dialog window
	    Shell shell = new Shell(getParent(), getStyle());
	    shell.setText(getText());
	    createContents(shell);
	    // shell.pack();
	    shell.setSize(SIZE + 10 ,SIZE + 10);
	    shell.open();
	    
	    Display display = getParent().getDisplay();
	    while (!shell.isDisposed()) {
	      if (!display.readAndDispatch()) {
	        display.sleep();
	      }
	    }
	    // Return the entered value, or null
	    return input;
	  }

	  
	  /**
	   * Creates the dialog's contents
	   * 
	   * @param shell the dialog window
	   */
	  private void createContents(final Shell shell) {
		  shell.setLayout(new GridLayout(1, false));
			Canvas canvas = createCanvas(shell, new PaintListener() {
				public void paintControl(PaintEvent e) {
					GC gc = e.gc;
					int x= 0;
					int y= 0;
					int width = SIZE;
					int size = (int) (width/Math.sqrt(palette.getRGBs().length));
					for (RGB rgb : palette.getRGBs()){
						gc.setBackground(new Color(Display.getCurrent(),rgb));
						gc.fillRectangle(x, y, size, size);
						if (x<=width){
							x = x + size;
						} else {
							x = 0;
							y = y + size;
						}
					}
				}
			}
			);
			GridData data;
			data = new GridData(GridData.FILL_BOTH);
		  canvas.setLayoutData(data);
	    // shell.setLayout(new GridLayout((int)Math.sqrt(palette.getRGBs().length), true));
	    
//	    for (RGB rgb : palette.getRGBs()) {
//		    Label label = new Label(shell, SWT.NONE);
//		    label.setText(" ");
//		    label.setBackground(new Color(Display.getCurrent(),rgb));
//		    data = new GridData();
//		    // data.horizontalSpan = 2;
//		    label.setLayoutData(data);
//	    }


	    // Display the input box
//	    final Text text = new Text(shell, SWT.BORDER);
//	    data = new GridData(GridData.FILL_HORIZONTAL);
//	    data.horizontalSpan = 2;
//	    text.setLayoutData(data);

	    // Create the OK button and add a handler
	    // so that pressing it will set input
	    // to the entered value
//	    Button ok = new Button(shell, SWT.PUSH);
//	    ok.setText("OK");
//	    data = new GridData(GridData.FILL_HORIZONTAL);
//	    ok.setLayoutData(data);
//	    ok.addSelectionListener(new SelectionAdapter() {
//	      public void widgetSelected(SelectionEvent event) {
//	        input = text.getText();
//	        shell.close();
//	      }
//	    });

//	    // Create the cancel button and add a handler
//	    // so that pressing it will set input to null
//	    Button cancel = new Button(shell, SWT.PUSH);
//	    cancel.setText("Cancel");
//	    data = new GridData(GridData.FILL_HORIZONTAL);
//	    cancel.setLayoutData(data);
//	    cancel.addSelectionListener(new SelectionAdapter() {
//	      public void widgetSelected(SelectionEvent event) {
//	        input = null;
//	        shell.close();
//	      }
//	    });
//
//	    // Set the OK button as the default, so
//	    // user can type input and press Enter
//	    // to dismiss
//	    shell.setDefaultButton(cancel);
			}

	public void setPalette(PaletteData paletteData) {
		this.palette = paletteData;
	}
	
	protected Canvas createCanvas(Composite parent, PaintListener pl) {
		Canvas c = new Canvas(parent, SWT.NONE);
		if (pl != null) {
			c.addPaintListener(pl);
		}
		return c;
	}

}
