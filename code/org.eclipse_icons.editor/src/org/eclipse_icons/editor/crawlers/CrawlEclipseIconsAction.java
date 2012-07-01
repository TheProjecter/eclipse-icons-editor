package org.eclipse_icons.editor.crawlers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse_icons.editor.Activator;

public class CrawlEclipseIconsAction extends Action {
	
	public CrawlEclipseIconsAction() {
		this.setImageDescriptor(Activator.getImageDescriptor(
		           "icons/crawlEclipseIconsAction.png"));
		this.setText("Crawl Eclipse Icons");
		this.setToolTipText("Crawl Eclipse Icons");
	}
	
	public void run() {
		CrawlEclipseIconsWizard wizard = new CrawlEclipseIconsWizard();
        
        WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
        dialog.setBlockOnOpen(true);
        dialog.open();
	}
}
