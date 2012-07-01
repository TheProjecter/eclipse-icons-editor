package org.eclipse_icons.editor.crawlers;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.wizard.Wizard;

public class CrawlEclipseIconsWizard extends Wizard {
	
	CrawlEcipseIconsWizardPage crawlEclipseIconsWizardPage = null;
	
	public CrawlEclipseIconsWizard(){
		setWindowTitle("Crawl Eclipse Icons");
	    setNeedsProgressMonitor(true);
	    crawlEclipseIconsWizardPage = new CrawlEcipseIconsWizardPage("Crawl Eclipse Icons");
	}
	
	@Override
    public void addPages() {
		addPage(crawlEclipseIconsWizardPage);
    }

	@Override
	public boolean performFinish() {
		String srcDir = crawlEclipseIconsWizardPage.getSrcDir();
		String filterOption = crawlEclipseIconsWizardPage.getFilter();
		String[] filters = new String[]{ filterOption };
		String destDir = crawlEclipseIconsWizardPage.getDestDir();
		String workspacePath = Platform.getLocation().toOSString();
		destDir = workspacePath + destDir;
		
		CrawlEclipseIconsJob job = new CrawlEclipseIconsJob(srcDir,destDir,filters);
		job.setUser(true);
		job.schedule();
		
		return true;
	}
	
	
	
	

}
