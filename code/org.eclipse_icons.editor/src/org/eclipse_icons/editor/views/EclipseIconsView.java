package org.eclipse_icons.editor.views;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse_icons.editor.Activator;
import org.eclipse_icons.editor.utils.image.Utils;
import org.eclipse_icons.editor.utils.ui.UIUtils;
import org.eclipse_icons.editor.utils.ui.WorkspaceContainerForFilesDialog;

/**
 * Eclipse Icons Editor View
 * @author Jabier Martinez
 */
public class EclipseIconsView extends ViewPart {

	private static final String NEW_ICON_PNG = "newIcon.png";

	public static final String ID = "org.eclipse_icons.editor.views.EclipseIconsEditor";

	private TableViewer viewer;
	private Action selectOverlayIconAction;
	private Action selectBaseIconAction;
	private Action saveAction;
	
	private String[] baseIcons;
	private String[] overlayIcons;
	
	private Image currentImage = null;
	
	private int outputFormat = SWT.IMAGE_PNG;
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
			if (currentImage!=null){
				currentImage.dispose();
				currentImage = null;
			}
		}
		public Object[] getElements(Object parent) {
			return new String[] { NEW_ICON_PNG };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj.toString());
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			Image image = null;
			if (baseIcons != null && baseIcons.length>0 && overlayIcons != null && overlayIcons.length>0){
				image = Utils.createOverlapedImage(overlayIcons[0],baseIcons[0]);
			} else if (baseIcons != null && baseIcons.length>0){
				image = new Image(Display.getCurrent(), baseIcons[0]);
			} else if (overlayIcons != null && overlayIcons.length>0){
				image = new Image(Display.getCurrent(), overlayIcons[0]);
			}
			if (image==null){
				// It is needed to create a copy because we cannot dispose a shared image
				image = new Image(Display.getCurrent(),PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT),SWT.IMAGE_COPY);
			}
			if (currentImage != image){
				if (currentImage != null){
					currentImage.dispose();
				}
				currentImage = image;
			}
			
			return image;
		}
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		createActions();
		createContextMenu();
		contributeToActionBars();
	}

	private void createContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				EclipseIconsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(saveAction);
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(selectOverlayIconAction);
		manager.add(selectBaseIconAction);
		manager.add(new Separator());
		manager.add(saveAction);
	}

	private String[] selectIcons(){
        FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        fd.setText("Select Image");
        String[] filterExt = { "*.gif", "*.png", "*.bmp", "*.jpg" };
        fd.setFilterPath(UIUtils.getFileAbsolutePathFromPlugin("gallery"));
        fd.setFilterExtensions(filterExt);
        String selection = fd.open();
        if (selection != null) {
            String[] files = fd.getFileNames();
            String filterPath = fd.getFilterPath();
            if (filterPath.charAt(filterPath.length() -1) != File.separatorChar){
            	filterPath = filterPath + File.separatorChar;
            }
            for (int i = 0, n = files.length; i < n; i++) {
            	files[i]= filterPath + files[i];
            }
            return files;
        }
        return null;
	}
	
	private void createActions() {
		selectOverlayIconAction = new Action() {
			public void run() {
				String[] selected = selectIcons();
				if (selected!=null){
					overlayIcons = selected;
					viewer.refresh();
				}
			}
		};
		selectOverlayIconAction.setText("Select Overlay Icon");
		selectOverlayIconAction.setToolTipText("Select Overlay Icon");
		selectOverlayIconAction.setImageDescriptor(Activator.getImageDescriptor(
		           "icons/selectOverlayIcon.png"));
		
		selectBaseIconAction = new Action() {
			public void run() {
				String[] selected = selectIcons();
				if (selected!=null){
					baseIcons = selected;
					viewer.refresh();
				}
			}
		};
		
		selectBaseIconAction.setText("Select Base Icon");
		selectBaseIconAction.setToolTipText("Select Base Icon");
		selectBaseIconAction.setImageDescriptor(Activator.getImageDescriptor(
		           "icons/selectBaseIcon.png"));
		
		saveAction = new Action() {
			public void run() {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection==null || selection.isEmpty()){
					MessageDialog.openInformation(viewer.getControl().getShell(),
					"Eclipse Icons Editor",
					"No icon was selected");
				} else {
					String[] resourceFullPath = WorkspaceContainerForFilesDialog
							.open("Select container folder",new String[] { NEW_ICON_PNG });
					if (resourceFullPath != null && resourceFullPath[0] != null) {
						try {
							Utils.saveIconToFile(currentImage, resourceFullPath[0], outputFormat);
							UIUtils.refreshWorkspace(resourceFullPath[0]);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		};
		
		saveAction.setText("Save");
		saveAction.setToolTipText("Save");
		saveAction.setImageDescriptor(PlatformUI.getWorkbench().
				getSharedImages().getImageDescriptor(ISharedImages.IMG_ETOOL_SAVE_EDIT));
	}

	public void setFocus() {
		viewer.getControl().setFocus();
	}
}