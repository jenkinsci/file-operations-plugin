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

public class FileUnTarOperation extends FileOperation implements Serializable {
    private final String filePath;
    private final String targetLocation;
    private final boolean isGZIP;

    @DataBoundConstructor
    public FileUnTarOperation(String filePath, String targetLocation, boolean isGZIP) {
        this.filePath = filePath;
        this.targetLocation = targetLocation;
        this.isGZIP = isGZIP;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public boolean getIsGZIP() {
        return isGZIP;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("Untar File Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(
                        listener, envVars.expand(filePath), envVars.expand(targetLocation), isGZIP));
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
        private final boolean isGZIP;

        public TargetFileCallable(
                TaskListener Listener, String ResolvedFilePath, String ResolvedTargetLocation, boolean isGZIP) {
            this.listener = Listener;
            this.resolvedFilePath = ResolvedFilePath;
            this.resolvedTargetLocation = ResolvedTargetLocation;
            this.isGZIP = isGZIP;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpSrcTar = new FilePath(fpWS, resolvedFilePath);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                listener.getLogger().println("Untarring " + resolvedFilePath + " to " + fpTL.getRemote());
                if (!fpTL.exists()) {
                    fpTL.mkdirs();
                }
                if (isGZIP) {
                    fpSrcTar.untar(fpTL, FilePath.TarCompression.GZIP);
                    result = true;
                    listener.getLogger().println("Untar completed.");
                } else {
                    fpSrcTar.untar(fpTL, FilePath.TarCompression.NONE);
                    result = true;
                    listener.getLogger().println("Untar completed.");
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
    @Symbol("fileUnTarOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "Untar";
        }
    }
}
