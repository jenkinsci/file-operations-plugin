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

public class FolderCopyOperation extends FileOperation implements Serializable {
    private final String sourceFolderPath;
    private final String destinationFolderPath;

    @DataBoundConstructor
    public FolderCopyOperation(String sourceFolderPath, String destinationFolderPath) {
        this.sourceFolderPath = sourceFolderPath;
        this.destinationFolderPath = destinationFolderPath;
    }

    public String getSourceFolderPath() {
        return sourceFolderPath;
    }

    public String getDestinationFolderPath() {
        return destinationFolderPath;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("Folder Copy Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(
                        listener, envVars.expand(sourceFolderPath), envVars.expand(destinationFolderPath), envVars));
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
        private final EnvVars environment;
        private final String resolvedSourceFolderPath;
        private final String resolvedDestinationFolderPath;

        public TargetFileCallable(
                TaskListener Listener,
                String ResolvedSourceFolderPath,
                String ResolvedDestinationFolderPath,
                EnvVars environment) {
            this.listener = Listener;
            this.resolvedSourceFolderPath = ResolvedSourceFolderPath;
            this.resolvedDestinationFolderPath = ResolvedDestinationFolderPath;
            this.environment = environment;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpSF = new FilePath(fpWS, resolvedSourceFolderPath);
                FilePath fpTL = new FilePath(fpWS, resolvedDestinationFolderPath);
                listener.getLogger().println("Copying folder: " + fpSF.getRemote() + " to " + fpTL.getRemote());
                fpSF.copyRecursiveTo(fpTL);
                result = true;
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
    @Symbol("folderCopyOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "Folder Copy";
        }
    }
}
