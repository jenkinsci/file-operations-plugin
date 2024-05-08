package sp.sd.fileoperations;

import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Descriptor;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.ArrayList;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collections;

public class FileOperationsBuilder extends Builder implements SimpleBuildStep {

    private final List<FileOperation> fileOperations;

    @DataBoundConstructor
    public FileOperationsBuilder(List<FileOperation> fileOperations) {
        this.fileOperations = fileOperations == null ? new ArrayList<FileOperation>() : new ArrayList<FileOperation>(fileOperations);
    }

    public List<FileOperation> getFileOperations() {
        return Collections.unmodifiableList(fileOperations);
    }

    @Override
    public void perform(@NonNull Run build, @NonNull FilePath workspace, @NonNull Launcher launcher, @NonNull TaskListener listener)
            throws AbortException {
        boolean result = false;
        if (fileOperations.size() > 0) {
            for (FileOperation item : fileOperations) {
                result = item.runOperation(build, workspace, launcher, listener);
                if (!result) break;
            }
        } else {
            listener.getLogger().println("No File Operation added.");
            result = true;
        }
        if (!result) {
            throw new AbortException("File Operations failed.");
        }
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Extension
    @Symbol("fileOperations")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public DescriptorImpl() {
            load();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "File Operations";
        }


        @SuppressWarnings("unused")
        public List<FileOperationDescriptor> getFileOperationDescriptors() {
            List<FileOperationDescriptor> result = new ArrayList<>();
            Jenkins j = Jenkins.getInstance();
            if (j == null) {
                return result;
            }
            for (Descriptor<FileOperation> d : j.getDescriptorList(FileOperation.class)) {
                if (d instanceof FileOperationDescriptor) {
                    FileOperationDescriptor descriptor = (FileOperationDescriptor) d;
                    result.add(descriptor);
                }
            }
            return result;
        }
    }
}

