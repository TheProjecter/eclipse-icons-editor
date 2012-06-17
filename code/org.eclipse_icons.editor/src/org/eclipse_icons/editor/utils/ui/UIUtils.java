package org.eclipse_icons.editor.utils.ui;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse_icons.editor.Activator;
import org.osgi.framework.Bundle;

public class UIUtils {

	public static void refreshWorkspace(String fullPath) {

		try {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IWorkspaceRoot root = workspace.getRoot();
			File file = new File(fullPath);
			IResource[] toRefresh = null;
			if (file.isDirectory()) {
				toRefresh = root.findContainersForLocationURI(file.toURI());
			} else if (file.isFile()) {
				toRefresh = root.findFilesForLocationURI(file.toURI());
			}
			for (IResource c : toRefresh) {
				c.refreshLocal(IResource.DEPTH_INFINITE,
						new NullProgressMonitor());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getFileAbsolutePathFromPlugin(String relativePathToFile){
        Bundle bundle = Activator.getDefault().getBundle();
        IPath path = new Path(relativePathToFile);
        URL galleryUrl = FileLocator.find(bundle, path, Collections.EMPTY_MAP);
        try {
			File galleryFile = new File(FileLocator.toFileURL(galleryUrl).toURI());
			return galleryFile.getAbsolutePath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        return null;
	}
	
}
