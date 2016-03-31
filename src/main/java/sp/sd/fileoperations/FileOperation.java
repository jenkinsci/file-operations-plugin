package sp.sd.fileoperations;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractDescribableImpl;

public abstract class FileOperation extends AbstractDescribableImpl<FileOperation> {
	public abstract boolean RunOperation(AbstractBuild build, Launcher launcher, BuildListener listener);  
}