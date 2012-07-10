package org.eclipse_icons.editor.utils.image;


import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse_icons.editor.Activator;
import org.eclipse_icons.editor.views.Icon;

public class ImageProcessor {

	public static Image process(String id, String overlayIcon,
			String baseIcon) {
		
		Image baseImage = new Image(Display.getCurrent(), baseIcon);
		Image overlayImage = new Image(Display.getCurrent(), overlayIcon);
		
		int baseWidth = baseImage.getBounds().width;
		int baseHeight = baseImage.getBounds().height;
		int overlayWidth = overlayImage.getBounds().width;
		int overlayHeight = overlayImage.getBounds().height;
		
		if (id.equals(Icon.BASE_ICON)){
			return baseImage;
		}
		
		if (id.equals(Icon.CENTERED_OVERLAY_ICON)){
			int x = (baseWidth/2)-(overlayWidth/2);
			int y = (baseHeight/2)-(overlayHeight/2);
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		}
		
		if (id.equals(Icon.TOP_LEFT_CORNER_OVERLAY_ICON)){
			int x = 0;
			int y = 0;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.TOP_RIGHT_CORNER_OVERLAY_ICON)){
			int x = baseWidth - overlayWidth;
			int y = 0;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.BOTTOM_RIGHT_CORNER_OVERLAY_ICON)){
			int x = baseWidth - overlayWidth;
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.BOTTOM_LEFT_CORNER_OVERLAY_ICON)){
			int x = 0;
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		
		if (id.equals(Icon.TOP_SIDE_OVERLAY_ICON)){
			int x = (baseWidth/2)-(overlayWidth/2);
			int y = 0;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.RIGHT_SIDE_OVERLAY_ICON)){
			int x = baseWidth - overlayWidth;
			int y = (baseHeight/2)-(overlayHeight/2);
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.BOTTOM_SIDE_OVERLAY_ICON)){
			int x = (baseWidth/2)-(overlayWidth/2);
			int y = baseHeight - overlayHeight;
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		if (id.equals(Icon.LEFT_SIDE_OVERLAY_ICON)){
			int x = 0;
			int y = (baseHeight/2)-(overlayHeight/2);
			return Utils.createOverlapedImage(overlayImage,baseImage,new Point(x,y));
		} 
		
		
		else {
			return Activator.getImageDescriptor("icons/default/base.png").createImage();
		}
	}
	
//	Image image = null;
//	if (baseIcons != null && baseIcons.length>0 && overlayIcons != null && overlayIcons.length>0){
//		image = Utils.createOverlapedImage(overlayIcons[0],baseIcons[0]);
//	} else if (baseIcons != null && baseIcons.length>0){
//		image = new Image(Display.getCurrent(), baseIcons[0]);
//	} else if (overlayIcons != null && overlayIcons.length>0){
//		image = new Image(Display.getCurrent(), overlayIcons[0]);
//	}
//	if (image==null){
//		// It is needed to create a copy because we cannot dispose a shared image
//		image = new Image(Display.getCurrent(),PlatformUI.getWorkbench().
//			getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT),SWT.IMAGE_COPY);
//	}
//	if (currentImage != image){
//		if (currentImage != null){
//			currentImage.dispose();
//		}
//		currentImage = image;
//	}
//	
//	return image;

}
