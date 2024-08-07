package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.File;
import hudson.util.DirScanner;
import java.io.Serializable;

public class FileCopyOperation extends FileOperation implements Serializable {
    private final String includes;
    private final String excludes;
    private final String targetLocation;
    private final boolean flattenFiles;
    private final boolean renameFiles;
    private final String sourceCaptureExpression;
    private final String targetNameExpression;
    private Boolean useDefaultExcludes;

    @DataBoundConstructor
    public FileCopyOperation(String includes,
                             String excludes,
                             String targetLocation,
                             boolean flattenFiles,
                             boolean renameFiles,
                             String sourceCaptureExpression,
                             String targetNameExpression) {
        this.includes = includes;
        this.excludes = excludes;
        this.targetLocation = targetLocation;
        this.flattenFiles = flattenFiles;
        this.renameFiles = renameFiles;
        this.sourceCaptureExpression = sourceCaptureExpression;
        this.targetNameExpression = targetNameExpression;
        this.useDefaultExcludes = true;
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

    public boolean getRenameFiles() {
        return renameFiles;
    }

    public String getSourceCaptureExpression() {
        return sourceCaptureExpression;
    }

    public String getTargetNameExpression() {
        return targetNameExpression;
    }

    public boolean getUseDefaultExcludes() {
        return useDefaultExcludes;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Copy Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener,
                                                       envVars.expand(includes),
                                                       envVars.expand(excludes),
                                                       envVars.expand(targetLocation),
                                                       flattenFiles,
                                                       renameFiles,
                                                       sourceCaptureExpression,
                                                       targetNameExpression,
                                                       useDefaultExcludes));
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
        private final boolean renameFiles;
        private final String sourceCaptureExpression;
        private final String targetNameExpression;
        private final boolean useDefaultExcludes;

        public TargetFileCallable(TaskListener Listener,
                                  String ResolvedIncludes,
                                  String ResolvedExcludes,
                                  String ResolvedTargetLocation,
                                  boolean flattenFiles,
                                  boolean renameFiles,
                                  String sourceCaptureExpression,
                                  String targetNameExpression,
                                  boolean UseDefaultExcludes) {
            this.listener = Listener;
            this.resolvedIncludes = ResolvedIncludes;
            this.resolvedExcludes = ResolvedExcludes;
            this.resolvedTargetLocation = ResolvedTargetLocation;
            this.flattenFiles = flattenFiles;
            this.renameFiles = renameFiles;
            this.sourceCaptureExpression = sourceCaptureExpression;
            this.targetNameExpression = targetNameExpression;
            this.useDefaultExcludes = UseDefaultExcludes;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                FilePath[] resolvedFiles = fpWS.list(resolvedIncludes, resolvedExcludes, useDefaultExcludes);
                if (resolvedFiles.length == 0) {
                    listener.getLogger().println("0 files found for include pattern '" + resolvedIncludes + "' and exclude pattern '" + resolvedExcludes + "'");
                }
                if (flattenFiles) {
                    for (FilePath item : resolvedFiles) {
                        if (renameFiles) {
                            final String relativePath = item.getRemote()
                                    .replace(fpWS.getRemote(), ".");
                            final String relativeTargetFileName = relativePath
                                    .replaceAll(sourceCaptureExpression, targetNameExpression);
                            final String targetFileName = relativePath.equals(relativeTargetFileName) ? item.getName() : relativeTargetFileName;
                            FilePath fpTF = new FilePath(fpTL, targetFileName);
                            listener.getLogger().println("Copy from " + item.getRemote() + " to " + fpTF);
                            item.copyTo(fpTF);
                        } else {
                            listener.getLogger().println(item.getRemote());
                            item.copyTo(new FilePath(fpTL, item.getName()));
                        }
                    }
                    result = true;
                } else {
                    for (FilePath item : resolvedFiles) {
                        listener.getLogger().println(item.getRemote());
                    }
                    fpWS.copyRecursiveTo(new DirScanner.Glob(resolvedIncludes, resolvedExcludes, useDefaultExcludes), fpTL, resolvedIncludes);
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

    @DataBoundSetter
    public void setUseDefaultExcludes(boolean useDefaultExcludes) {
        this.useDefaultExcludes = useDefaultExcludes;
    }

    protected Object readResolve() {
        if (useDefaultExcludes == null) {
            useDefaultExcludes = true;
        }
        return this;
    }
}
