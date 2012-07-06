package org.eclipse_icons.editor.views;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEffect;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;
import org.eclipse.ui.part.ResourceTransfer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse_icons.editor.Activator;
import org.eclipse_icons.editor.crawlers.CrawlEclipseIconsAction;
import org.eclipse_icons.editor.utils.image.Utils;
import org.eclipse_icons.editor.utils.ui.UIUtils;


/**
 * Eclipse Icons Editor View
 * @author Jabier Martinez
 */
public class EclipseIconsView extends ViewPart {
	
	public EclipseIconsView(){
		super();
		String defaultIconsPath = UIUtils.getFileAbsolutePathFromPlugin("icons/default");
		String baseDefaultPath = defaultIconsPath + "/base.png";
		String overlayDefaultPath = defaultIconsPath + "/overlay.png";
		baseIcons = new String[]{ baseDefaultPath };
		overlayIcons = new String[]{ overlayDefaultPath };
	}

	public static final String ID = "org.eclipse_icons.editor.views.EclipseIconsEditor";

	private TreeViewer viewer;
	private Action selectOverlayIconAction;
	private Action selectBaseIconAction;
	private Action saveAction;
	
	private String[] baseIcons;
	private String[] overlayIcons;
	
	private Image currentImage = null;
	
	private int outputFormat = SWT.IMAGE_PNG;
	
	class ViewContentProvider implements IStructuredContentProvider, 
	   ITreeContentProvider {
		

		private IconCategory invisibleRoot;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
			if (currentImage!=null){
				currentImage.dispose();
				currentImage = null;
			}
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(getViewSite())) {
				if (invisibleRoot==null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		
		public Object getParent(Object child) {
			if (child instanceof Icon) {
				return ((Icon)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent instanceof IconCategory) {
				return ((IconCategory)parent).getIcons();
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof IconCategory)
				return ((IconCategory)parent).hasIcons();
			return false;
		}
		
		private void initialize() {
			IconCategory centeredOverlayCategory = new IconCategory(IconCategory.CENTERED_OVERLAY.replaceAll(" ", "_"), IconCategory.CENTERED_OVERLAY);
			Icon iconCenteredOverlay = new Icon(Icon.CENTERED_OVERLAY_ICON,"base_overlay");
			centeredOverlayCategory.addIcon(iconCenteredOverlay);
			
			IconCategory cornersOverlayCategory = new IconCategory(IconCategory.CORNERS_OVERLAY,IconCategory.CORNERS_OVERLAY);
			Icon iconCorners1Overlay = new Icon(Icon.TOP_LEFT_CORNER_OVERLAY_ICON,"base_top_left_corner_overlay");
			Icon iconCorners2Overlay = new Icon(Icon.TOP_RIGHT_CORNER_OVERLAY_ICON,"base_top_right_corner_overlay");
			Icon iconCorners3Overlay = new Icon(Icon.BOTTOM_RIGHT_CORNER_OVERLAY_ICON,"base_bottom_right_corner_overlay");
			Icon iconCorners4Overlay = new Icon(Icon.BOTTOM_LEFT_CORNER_OVERLAY_ICON,"base_bottom_left_corner_overlay");
			cornersOverlayCategory.addIcon(iconCorners1Overlay);
			cornersOverlayCategory.addIcon(iconCorners2Overlay);
			cornersOverlayCategory.addIcon(iconCorners3Overlay);
			cornersOverlayCategory.addIcon(iconCorners4Overlay);
			
			IconCategory sidesOverlayCategory = new IconCategory(IconCategory.SIDES_OVERLAY,IconCategory.SIDES_OVERLAY);
			Icon iconSide1Overlay = new Icon(Icon.TOP_SIDE_OVERLAY_ICON,"base_top_side_overlay");
			Icon iconSide2Overlay = new Icon(Icon.RIGHT_SIDE_OVERLAY_ICON,"base_right_side_overlay");
			Icon iconSide3Overlay = new Icon(Icon.BOTTOM_SIDE_OVERLAY_ICON,"base_bottom_side_overlay");
			Icon iconSide4Overlay = new Icon(Icon.LEFT_SIDE_OVERLAY_ICON,"base_left_side_overlay");
			sidesOverlayCategory.addIcon(iconSide1Overlay);
			sidesOverlayCategory.addIcon(iconSide2Overlay);
			sidesOverlayCategory.addIcon(iconSide3Overlay);
			sidesOverlayCategory.addIcon(iconSide4Overlay);
		
			invisibleRoot = new IconCategory("","");
			invisibleRoot.addIcon(centeredOverlayCategory);
			invisibleRoot.addIcon(cornersOverlayCategory);
			invisibleRoot.addIcon(sidesOverlayCategory);
		}
	}

	
	class ViewLabelProvider extends LabelProvider {
		public Image getImage(Object obj) {
			if (obj instanceof IconCategory){
				return ((IconCategory)obj).getImage();
			} else { // It is an Icon
				return ((Icon)obj).processImage(baseIcons,overlayIcons);
			}
		}
		public String getText(Object obj) {
			if (obj instanceof IconCategory){
				return super.getText(obj);
			} else {
				return computeIconName((Icon)obj);
			}
		}
	}
	

	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
 		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setInput(getViewSite());
		createActions();
		createContextMenu();
		contributeToActionBars();
		viewer.expandAll();
		addDropSupport();
	}
	

	private void addDropSupport() {
		
		// Listener for menuItems
		// selected will be null if the user doesn't select this option
		// in the popup menu
		class PopUpMenuSelectionListener implements Listener {
			private String selected;
	        public void handleEvent(Event e) {
	        	selected = e.widget.toString();
	        }
	        public String getSelected(){
	        	return selected;
	        }
		}
		
		// Create the dropTargetEffect
		DropTargetEffect drop = new DropTargetEffect(viewer.getControl()){
			
			@Override
			public void drop(DropTargetEvent event){
				if (ResourceTransfer.getInstance().isSupportedType(event.currentDataType)){
					// Get the dropped resources
					IResource[] resources = (IResource[])event.data;

			        Menu menu = new Menu(viewer.getControl().getShell(), SWT.POP_UP);
			        // These listeners will be used to know which was the selected option
					PopUpMenuSelectionListener baseMenuListener = new PopUpMenuSelectionListener();
					PopUpMenuSelectionListener overlayMenuListener = new PopUpMenuSelectionListener();
					
					// Check if it is a image resource			        
			        List<String> imageResources = new ArrayList<String>();
					for (IResource resource : resources){
						if (UIUtils.isImageFile(resource)){
							String path = UIUtils.getWorkspacePath().toOSString() + resource.getFullPath();
							imageResources.add(path);
						}
					}
					// Open menupop if there is at least one image
					if (!imageResources.isEmpty()){
						// create menuItems
				        MenuItem overlayItem = new MenuItem(menu, SWT.PUSH);
				        overlayItem.setText("Overlay");
				        overlayItem.setImage(Activator.getImageDescriptor("icons/selectOverlayIcon.png").createImage());
						MenuItem baseItem = new MenuItem(menu, SWT.PUSH);
				        baseItem.setText("Base");
				        baseItem.setImage(Activator.getImageDescriptor("icons/selectBaseIcon.png").createImage());
				        // add listeners
				        baseItem.addListener(SWT.Selection, baseMenuListener);
				        overlayItem.addListener(SWT.Selection, overlayMenuListener);
				        //show the menu
				        menu.setLocation(event.x, event.y);
				        menu.setVisible(true);
				        // wait for the user to select
				        while (!menu.isDisposed() && menu.isVisible()) {
				            if (!Display.getCurrent().readAndDispatch())
				            	Display.getCurrent().sleep();
				        }
					}
					// update view based on selection
					if (baseMenuListener.getSelected()!=null){
						baseIcons = imageResources.toArray(baseIcons);
						viewer.refresh();
					}
					else if (overlayMenuListener.getSelected()!=null){
						overlayIcons = imageResources.toArray(overlayIcons);
						viewer.refresh();
					}
					// dispose
					menu.dispose();
				}
			}
		};
		// Add the drop listener to the viewer
		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_LINK;
		Transfer[] types = new Transfer[] { ResourceTransfer.getInstance() };
		DropTarget target = new DropTarget(viewer.getControl(), operations);
		target.setTransfer(types);
		target.addDropListener(drop);
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
		fillLocalPullDown(bars.getMenuManager());
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
	
	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(new CrawlEclipseIconsAction());
	}

	private String[] selectIcons(String type){
        FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
        fd.setText("Select "+ type +" Image");
        String[] filterExt = UIUtils.IMAGE_EXTENSIONS;
        if (type.equalsIgnoreCase("base")){
        	fd.setFilterPath(UIUtils.getFileAbsolutePathFromPlugin("gallery/base"));
        } else if (type.equalsIgnoreCase("overlay")){
        	fd.setFilterPath(UIUtils.getFileAbsolutePathFromPlugin("gallery/overlay"));
        }
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
				String[] selected = selectIcons("Overlay");
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
				String[] selected = selectIcons("Base");
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
				// No selection
				if (selection==null || selection.isEmpty()){
					MessageDialog.openInformation(viewer.getControl().getShell(),
					"Eclipse Icons Editor",
					"No icons were selected");
				} else {
					// Select output folder
					String path;
					ContainerSelectionDialog dialog = new ContainerSelectionDialog(
							viewer.getControl().getShell(), ResourcesPlugin
									.getWorkspace().getRoot(), false, "Select output folder");
					if (dialog.open() == ContainerSelectionDialog.OK) {
						// Dialog OK
						Object[] result = dialog.getResult();
						path = (((Path) result[0]).toString());
						String workspacePath = UIUtils.getWorkspacePath().toOSString();
						path = workspacePath + path;
						@SuppressWarnings("unchecked")
						List<Icon> selectionList = selection.toList();
						for (Icon selectedElement : selectionList){
							save(selectedElement, path);
						}
						UIUtils.refreshWorkspace(path);		
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
	
	private void save(Icon icon, String outputPath){
		if (icon instanceof IconCategory){
			for (Icon child : ((IconCategory)icon).getIcons()){
				save(child,outputPath);
			}
		} else {
			try {
				// Create new icon absolute path
				String newIconAbsPath = computeIconName(icon);
				newIconAbsPath = outputPath + File.separator + newIconAbsPath + "." + Utils.getExtension(outputFormat);
				// Save it
				Utils.saveIconToFile(icon.processImage(baseIcons, overlayIcons), newIconAbsPath, outputFormat);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	private String getFileNameFromAbsolutePath(String string) {
		File file = new File(string);
		String name = file.getName();
		return name.substring(0,name.lastIndexOf("."));
	}
	
	private String computeIconName(Icon icon){
		String iconName = icon.getName();
		iconName = iconName.replaceAll("base", getFileNameFromAbsolutePath(baseIcons[0]));
		iconName = iconName.replaceAll("overlay", getFileNameFromAbsolutePath(overlayIcons[0]));
		return iconName;
	}
}