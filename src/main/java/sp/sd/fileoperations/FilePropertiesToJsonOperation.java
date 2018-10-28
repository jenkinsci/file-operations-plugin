package sp.sd.fileoperations;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.File;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Serializable;
import java.util.Properties;
import java.util.regex.Pattern;

public class FilePropertiesToJsonOperation extends FileOperation implements Serializable {
    private final String sourceFile;
    private final String targetFile;

    @DataBoundConstructor
    public FilePropertiesToJsonOperation(String sourceFile, String targetFile) {
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
            listener.getLogger().println("File Properties To Json Operation:");
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
                } else {
                    String fileContent = "";
                    Properties sourceProperties = new Properties();
                    sourceProperties.load(fpSL.read());
                    fpTL.write(convertToJson(sourceProperties), "UTF-8");
                    result = true;
                    listener.getLogger().println("Creating Json: from source " + fpSL.getRemote() + " to target " + fpTL.getRemote());
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

        private static final Pattern floatPattern = Pattern.compile("^[0-9]+(\\.[0-9]+)?$");
        private static final Pattern intPattern = Pattern.compile("^[0-9]+$");

        private static String convertToJson(Properties properties) {
            JsonObject json = new JsonObject();
            for (Object key : properties.keySet()) {
                String value = properties.getProperty(key.toString());
                if (value.equals("true") || value.equals("false")) {
                    json.addProperty(key.toString(), Boolean.parseBoolean(value));
                } else if (intPattern.matcher(value).matches()) {
                    json.addProperty(key.toString(), Integer.parseInt(value));
                } else if (floatPattern.matcher(value).matches()) {
                    json.addProperty(key.toString(), Float.parseFloat(value));
                } else {
                    json.addProperty(key.toString(), value);
                }
            }
            return new Gson().toJson(json);
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {

        }
    }

    @Extension
    @Symbol("filePropertiesToJsonOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "File Properties to Json";
        }

    }
}
