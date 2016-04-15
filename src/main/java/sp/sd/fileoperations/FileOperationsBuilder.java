package sp.sd.fileoperations;

import hudson.FilePath;
import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.List;
import java.util.ArrayList;

import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;

import java.util.Collections;

import hudson.model.Descriptor;
import hudson.model.Run;

public class FileOperationsBuilder extends Builder {

    private final List<FileOperation> fileOperations;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public FileOperationsBuilder(List<FileOperation> fileOperations) {
        this.fileOperations = fileOperations == null ? new ArrayList<FileOperation>() : new ArrayList<FileOperation>(fileOperations);
    }

    public List<FileOperation> getFileOperations() {
        return Collections.unmodifiableList(fileOperations);
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        boolean result = false;
        if (fileOperations.size() > 0) {
            for (FileOperation item : fileOperations) {
                result = item.runOperation(build, build.getWorkspace(), launcher, listener);
                if (!result) break;
            }
        } else {
            listener.getLogger().println("No File Operation added.");
            result = true;
        }
        return result;
    }


    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }


    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types 
            return true;
        }

        public String getDisplayName() {
            return "File Operations";
        }


        @SuppressWarnings("unused")
        public List<FileOperationDescriptor> getFileOperationDescriptors() {
            List<FileOperationDescriptor> result = new ArrayList<FileOperationDescriptor>();
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

