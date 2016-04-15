package sp.sd.fileoperations;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.AbstractDescribableImpl;
import hudson.model.TaskListener;
import hudson.model.Run;

public abstract class FileOperation extends AbstractDescribableImpl<FileOperation> {
    public abstract boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener);
}