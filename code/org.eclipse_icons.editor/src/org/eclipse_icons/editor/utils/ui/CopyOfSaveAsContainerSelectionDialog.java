package org.eclipse_icons.editor.utils.ui;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

// TODO improve it
public class CopyOfSaveAsContainerSelectionDialog extends ContainerSelectionDialog {

	private String initialFileName;
	private String selectedFileName;
	
	public CopyOfSaveAsContainerSelectionDialog(Shell parentShell,
			IContainer initialRoot, boolean allowNewContainerName,
			String message, String initialFileName) {
		super(parentShell, initialRoot, allowNewContainerName, message);
		this.initialFileName = initialFileName;
		this.selectedFileName = initialFileName;
	}

	@Override
    protected Control createDialogArea(Composite parent) {
        super.createDialogArea(parent);
        
		// create a composite with standard margins and spacing
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		new Label(composite, SWT.NONE).setText("File name:");
		
		Text fileName = new Text(composite, SWT.SINGLE | SWT.BORDER);
		fileName.setText(initialFileName);
		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		fileName.setLayoutData(gridData);
		
		fileName.addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				selectedFileName = ((Text)e.getSource()).getText();
			}
			
		});
        return dialogArea;
    }

	
	public String getFileName(){
		return selectedFileName;
	}
	
	public IPath getSaveAsResult(){
		Object[] o = super.getResult();
		IPath path = (IPath) o[0];
		path = path.append(selectedFileName);
		return path;
	}
}
