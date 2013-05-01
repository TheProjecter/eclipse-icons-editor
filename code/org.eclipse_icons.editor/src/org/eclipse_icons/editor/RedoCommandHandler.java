package org.eclipse_icons.editor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * RedoCommandHandler
 * @author Jabier Martinez
 */
public class RedoCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	    IWorkbenchWindow wwindow = HandlerUtil.getActiveWorkbenchWindow(event);
	    IWorkbenchPage page = wwindow.getActivePage();
	    IEditorPart editorPart = page.getActiveEditor();
	    if (editorPart instanceof IconsEditorPart) {
	    	IconsEditorPart editor = (IconsEditorPart)editorPart;
	    	editor.editorUtils.redo();
	    	if (editor.selected){
	    		editor.deactivateSelection();
	    	}
	    }
		return null;
	}

}
