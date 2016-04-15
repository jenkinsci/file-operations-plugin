package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Serializable;

public class FileUnZipOperation extends FileOperation implements Serializable {
    private final String filePath;
    private final String targetLocation;

    @DataBoundConstructor
    public FileUnZipOperation(String filePath, String targetLocation) {
        this.filePath = filePath;
        this.targetLocation = targetLocation;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("Unzip File Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener, envVars.expand(filePath), envVars.expand(targetLocation)));
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
        private final String resolvedFilePath;
        private final String resolvedTargetLocation;

        public TargetFileCallable(TaskListener Listener, String ResolvedFilePath, String ResolvedTargetLocation) {
            this.listener = Listener;
            this.resolvedFilePath = ResolvedFilePath;
            this.resolvedTargetLocation = ResolvedTargetLocation;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result = false;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpSrcZip = new FilePath(fpWS, resolvedFilePath);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                listener.getLogger().println("Unzipping " + resolvedFilePath + " to " + fpTL.getRemote());
                if (!fpTL.exists()) {
                    fpTL.mkdirs();
                    //fpTL.chmod(0644);
                }
                fpSrcZip.unzip(fpTL);
                result = true;
                listener.getLogger().println("Unzip completed.");
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
        public void checkRoles(RoleChecker checker) throws SecurityException {

        }
    }

    @Extension
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "Unzip";
        }

    }
}