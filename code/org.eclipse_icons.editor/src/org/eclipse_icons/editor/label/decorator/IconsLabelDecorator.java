package org.eclipse_icons.editor.label.decorator;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse_icons.editor.utils.ui.UIUtils;

public class IconsLabelDecorator implements ILabelDecorator {

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image decorateImage(Image image, Object element) {
		if (isGoingToBeDecorated(element)){
			return UIUtils.getImageFromResource((IResource)element);
		}
		return null;
	}

	@Override
	public String decorateText(String text, Object element) {
		// null means no decoration
		return null;
	}
	
	public boolean isGoingToBeDecorated(Object element){
		IResource file = (IResource)element;
		// check if it is an image
		if (UIUtils.isImageFile(file)){
			return true;
		}
		return false;
	}

}

