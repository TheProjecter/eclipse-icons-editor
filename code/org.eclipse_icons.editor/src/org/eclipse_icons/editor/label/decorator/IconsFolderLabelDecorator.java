package org.eclipse_icons.editor.label.decorator;

import org.eclipse.core.resources.IResource;
import org.eclipse_icons.editor.utils.ui.UIUtils;

/**
 * Icons Folder Label Decorator
 * @author Jabier Martinez
 */
public class IconsFolderLabelDecorator extends IconsLabelDecorator {

	@Override
	public boolean isGoingToBeDecorated(Object element){
		IResource file = (IResource)element;
		// check if it is an image
		if (UIUtils.isImageFile(file)){
			// check if it is in an icons folder
			if (UIUtils.isTheResourceContainedInAConcreteFolder(file, "icons")){
				return true;
			}
		}
		return false;
	}

}
