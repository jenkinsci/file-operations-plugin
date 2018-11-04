package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.model.Run;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Serializable;

public class FileCopyOperation extends FileOperation implements Serializable {
    private final String includes;
    private final String excludes;
    private final String targetLocation;
    private final boolean flattenFiles;

    @DataBoundConstructor
    public FileCopyOperation(String includes, String excludes, String targetLocation, boolean flattenFiles) {
        this.includes = includes;
        this.excludes = excludes;
        this.targetLocation = targetLocation;
        this.flattenFiles = flattenFiles;
    }

    public String getIncludes() {
        return includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public boolean getFlattenFiles() {
        return flattenFiles;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Copy Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener, envVars.expand(includes), envVars.expand(excludes), envVars.expand(targetLocation), flattenFiles));
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
        private final String resolvedIncludes;
        private final String resolvedExcludes;
        private final String resolvedTargetLocation;
        private final boolean flattenFiles;

        public TargetFileCallable(TaskListener Listener, String ResolvedIncludes, String ResolvedExcludes, String ResolvedTargetLocation, boolean flattenFiles) {
            this.listener = Listener;
            this.resolvedIncludes = ResolvedIncludes;
            this.resolvedExcludes = ResolvedExcludes;
            this.resolvedTargetLocation = ResolvedTargetLocation;
            this.flattenFiles = flattenFiles;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                FilePath[] resolvedFiles = fpWS.list(resolvedIncludes, resolvedExcludes);
                if (resolvedFiles.length == 0) {
                    listener.getLogger().println("0 files found for include pattern '" + resolvedIncludes + "' and exclude pattern '" + resolvedExcludes + "'");
                } else {
                    for (FilePath item : resolvedFiles) {
                        listener.getLogger().println(item.getRemote());
                    }
                }
                if (flattenFiles) {
                    for (FilePath item : resolvedFiles) {
                        item.copyTo(new FilePath(fpTL, item.getName()));
                    }
                    result = true;
                } else {
                    fpWS.copyRecursiveTo(resolvedIncludes, resolvedExcludes, fpTL);
                    result = true;
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
        public void checkRoles(RoleChecker checker) throws SecurityException {

        }
    }

    @Extension
    @Symbol("fileCopyOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "File Copy";
        }

    }
}
