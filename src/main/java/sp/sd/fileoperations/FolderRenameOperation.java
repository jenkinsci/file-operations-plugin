package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.Serializable;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;

public class FolderRenameOperation extends FileOperation implements Serializable {
    private final String source;
    private final String destination;

    @DataBoundConstructor
    public FolderRenameOperation(String source, String destination) {
        this.source = source;
        this.destination = destination;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("Folder Rename Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener, envVars.expand(source), envVars.expand(destination)));
            } catch (RuntimeException e) {
                listener.getLogger().println(e.getMessage());
                throw e;
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                return false;
            }

        } catch (Exception e) {
            listener.fatalError(e.getMessage());
        }
        return result;
    }

    private static final class TargetFileCallable implements FileCallable<Boolean> {
        private static final long serialVersionUID = 1;
        private final TaskListener listener;
        private final String resolvedSource;
        private final String resolvedDestination;

        public TargetFileCallable(TaskListener Listener, String ResolvedSource, String ResolvedDestination) {
            this.listener = Listener;
            this.resolvedSource = ResolvedSource;
            this.resolvedDestination = ResolvedDestination;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result = false;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpSL = new FilePath(fpWS, resolvedSource);
                if (fpSL.exists()) {
                    FilePath fpDL = new FilePath(fpWS, resolvedDestination);
                    fpSL.renameTo(fpDL);
                    result = true;
                } else {
                    listener.fatalError("The source folder" + fpSL.getRemote() + " doesn't exist.");
                }
            } catch (RuntimeException e) {
                listener.fatalError(e.getMessage());
                throw e;
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                result = false;
            }
            return result;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {}
    }

    @Extension
    @Symbol("folderRenameOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "Folder Rename";
        }
    }
}
