package org.eclipse_icons.editor.utils.ui;

import java.io.File;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * WorkspaceContainerForFilesDialog allows to select a workspace container
 * and if some of the files already exists the users will be asked if
 * they want to overwrite or not.
 * @param title (folder selection objective)
 * @param array of fileNames
 * @return null if canceled or an array with the file full paths (with null
 * in the indexes where the user decided not to overwrite).
 * 
 * @author Jabier Martinez
 */
public class WorkspaceContainerForFilesDialog {

	public static String[] open(String[] fileNames) {
		return open("Select output folder", fileNames);
	}

	public static String[] open(String title, String[] fileNames) {
		String[] _return = new String[fileNames.length];
		Shell shell = Display.getCurrent().getActiveShell();
			
			// Get the output folder
			String pathName = null;
			ContainerSelectionDialog dialog = new ContainerSelectionDialog(
					shell, ResourcesPlugin
							.getWorkspace().getRoot(), false, title);
			if (dialog.open() == ContainerSelectionDialog.OK) {
				Object[] result = dialog.getResult();
				if (result.length == 1) {
					pathName = (((Path) result[0]).toString());
				}
			}
			// If Cancel pressed return null
			if (pathName==null){
				return null;
			}
			
			String workspacePath = Platform.getLocation().toString();		
			// Loop for each of the files
			for (int x=0; x<fileNames.length; x++){
				String relativePath = pathName + "/" + fileNames[x];
				String resourceFullPath = workspacePath + relativePath;
				File file = new File(resourceFullPath); 
				if (file.exists()){
					// File already exists
					// Ask for confirmation
					// todo Add "Yes to all" and "Not to all" options
					boolean overwrite = MessageDialog.openQuestion(shell, "File already exists", "The file " + relativePath + " already exists.\nDo you want to overwrite it?");
					if (overwrite){
						_return[x] = resourceFullPath;
					} else {
						_return[x] = null;
					}
				} else {
					// File doesn't exist
					_return[x] = resourceFullPath;
				}		
		}
		return _return;
	}

}
