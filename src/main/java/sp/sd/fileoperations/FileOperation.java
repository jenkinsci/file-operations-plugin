package sp.sd.fileoperations;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Run;
import hudson.model.TaskListener;

public abstract class FileOperation extends AbstractDescribableImpl<FileOperation> {
    public abstract boolean runOperation(
            Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener);
}
