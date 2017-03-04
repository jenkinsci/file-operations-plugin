package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Serializable;

public class FileJoinOperation extends FileOperation implements Serializable {
    private final String sourceFile;
    private final String targetFile;

    @DataBoundConstructor
    public FileJoinOperation(String sourceFile, String targetFile) {
        this.sourceFile = sourceFile;
        this.targetFile = targetFile;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public String getTargetFile() {
        return targetFile;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Join Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener, envVars.expand(sourceFile), envVars.expand(targetFile), envVars));
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
        private final String resolvedSourceFile;
        private final String resolvedTargetFile;

        public TargetFileCallable(TaskListener Listener, String ResolvedSourceFile, String ResolvedTargetFile, EnvVars environment) {
            this.listener = Listener;
            this.resolvedSourceFile = ResolvedSourceFile;
            this.resolvedTargetFile = ResolvedTargetFile;
            this.environment = environment;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result = false;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpSL = new FilePath(fpWS, resolvedSourceFile);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetFile);
                if (!fpTL.exists()) {
                    listener.getLogger().println(resolvedSourceFile + " file doesn't exists, the target file remains as is.");
                }

                String fileContent = "";
                String sourceFileContents = fpSL.readToString();
                String targetFileContents = fpTL.readToString();
                String eol = System.getProperty("line.separator");

                if(targetFileContents.endsWith(eol)){
                    fileContent = targetFileContents.concat(sourceFileContents);
                }else{
                    fileContent = targetFileContents.concat(eol + sourceFileContents);
                }
                fpTL.write(fileContent, "UTF-8");
                result = true;
                listener.getLogger().println("Joining file: from source " + fpSL.getRemote() + " to target " + fpTL.getRemote());
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
    @Symbol("fileJoinOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "File Join";
        }

    }
}