package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.util.List;

import static org.junit.Assert.*;

public class FileRenameOperationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDefaults() {
        String source = "source.txt";
        String destination = "destination.txt";
        FileRenameOperation operation = new FileRenameOperation(source, destination);

        assertEquals(source, operation.getSource());
        assertEquals(destination, operation.getDestination());
    }

    @Test
    public void testRunFileRenameOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("fileRenameTest");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFile = new FilePath(workspace, "source.txt");
        FilePath destinationFile = new FilePath(workspace, "destination.txt");

        sourceFile.write("Sample content", "UTF-8");

        FileRenameOperation operation = new FileRenameOperation("source.txt", "destination.txt");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse("The source file should have been renamed", sourceFile.exists());
        assertTrue("The destination file should exist", destinationFile.exists());
    }

    @Test
    public void testRunFileRenameOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("SOURCE_FILE", "source.txt");
        envVars.put("DESTINATION_FILE", "destination.txt");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("fileRenameTestWithTokens");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFile = new FilePath(workspace, "source.txt");
        FilePath destinationFile = new FilePath(workspace, "destination.txt");

        sourceFile.write("Sample content", "UTF-8");

        FileRenameOperation operation = new FileRenameOperation("$SOURCE_FILE", "$DESTINATION_FILE");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse("The source file should have been renamed", sourceFile.exists());
        assertTrue("The destination file should exist", destinationFile.exists());
    }
}
