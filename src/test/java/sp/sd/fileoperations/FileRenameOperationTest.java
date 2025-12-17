package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileRenameOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testDefaults() {
        String source = "source.txt";
        String destination = "destination.txt";
        FileRenameOperation operation = new FileRenameOperation(source, destination);

        assertEquals(source, operation.getSource());
        assertEquals(destination, operation.getDestination());
    }

    @Test
    void testRunFileRenameOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("fileRenameTest");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath sourceFile = new FilePath(workspace, "source.txt");
        FilePath destinationFile = new FilePath(workspace, "destination.txt");

        sourceFile.write("Sample content", "UTF-8");

        FileRenameOperation operation = new FileRenameOperation("source.txt", "destination.txt");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse(sourceFile.exists(), "The source file should have been renamed");
        assertTrue(destinationFile.exists(), "The destination file should exist");
    }

    @Test
    void testRunFileRenameOperationWithTokens() throws Exception {
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

        assertFalse(sourceFile.exists(), "The source file should have been renamed");
        assertTrue(destinationFile.exists(), "The destination file should exist");
    }
}
