package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.Serializable;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;

public class FileZipOperation extends FileOperation implements Serializable {

    private final String folderPath;
    private final String outputFolderPath;

    @DataBoundConstructor
    public FileZipOperation(String folderPath, String outputFolderPath) {
        this.folderPath = folderPath;
        this.outputFolderPath = outputFolderPath;
    }

    public String getFolderPath() {
        return folderPath;
    }

    public String getOutputFolderPath() {
        return outputFolderPath;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Zip Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(
                        listener, envVars.expand(folderPath), envVars.expand(outputFolderPath), envVars));
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                return false;
            }
        } catch (Exception e) {
            listener.fatalError(e.getMessage());
        }
        return result;
    }

    private static final class TargetFileCallable implements FilePath.FileCallable<Boolean> {
        private static final long serialVersionUID = 1;
        private final TaskListener listener;
        private final EnvVars environment;
        private final String resolvedFolderPath;
        private final String outputFolderPath;

        public TargetFileCallable(
                TaskListener Listener, String ResolvedFolderPath, String outputFolderPath, EnvVars environment) {
            this.listener = Listener;
            this.resolvedFolderPath = ResolvedFolderPath;
            this.outputFolderPath = outputFolderPath;
            this.environment = environment;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpWSOutput = new FilePath(fpWS, (outputFolderPath != null ? outputFolderPath : ""));
                FilePath fpTL = new FilePath(fpWS, resolvedFolderPath);
                listener.getLogger().println("Creating Zip file of " + fpTL.getRemote() + " in " + fpWSOutput);
                fpTL.zip(new FilePath(fpWSOutput, fpTL.getName() + ".zip"));
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
    @Symbol("fileZipOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "File Zip";
        }
    }
}
