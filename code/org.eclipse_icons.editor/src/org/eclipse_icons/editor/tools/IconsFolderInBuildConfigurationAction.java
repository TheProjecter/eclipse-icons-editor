package org.eclipse_icons.editor.tools;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse_icons.editor.Activator;
import org.eclipse_icons.editor.utils.ui.UIUtils;

/**
 * 
 * @author Jabier Martinez
 */
public class IconsFolderInBuildConfigurationAction extends Action {
	
	public IconsFolderInBuildConfigurationAction() {
		this.setImageDescriptor(Activator.getImageDescriptor(
		           "icons/crawlEclipseIconsAction.png"));
		this.setText("Check Icon Folders in Build Configurations");
		this.setToolTipText("Check Icon Folders in Build Configurations");
	}
	
	public void run() {
		// Do it
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		IProject[] projects = workspace.getRoot().getProjects();
		String message = "";
		for (IProject project : projects){
			try {
				for (IResource resource : project.members()){
					if (resource instanceof IFolder){
						if (UIUtils.folderContainsImages((IFolder)resource)){
							if (!UIUtils.isResourceInBuildXML(project,resource)){
								message = message + "Project: " +project.getName() + " Resource: " + resource.getProjectRelativePath() + "\n";
							}
						}
					} else {
						if (UIUtils.isImageFile(resource)){
							if (!UIUtils.isResourceInBuildXML(project,resource)){
								message = message + "Project: " +project.getName() + " Resource: " + resource.getProjectRelativePath() + "\n";
							}
						}
					}
				}
			} catch (CoreException e) {
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "Error loading resources");
			}
		}
		MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Warning", message);
	}
}
