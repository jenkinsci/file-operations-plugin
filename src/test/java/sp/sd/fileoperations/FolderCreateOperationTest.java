package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class FolderCreateOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    void testDefaults() {
        String defaultFolderPath = "defaultFolder";
        FolderCreateOperation operation = new FolderCreateOperation(defaultFolderPath);

        assertEquals(defaultFolderPath, operation.getFolderPath());
    }

    @Test
    void testRunFolderCreateOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("folderCreateTest");

        String folderPath = "newFolder";
        FolderCreateOperation operation = new FolderCreateOperation(folderPath);
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        FilePath folder = new FilePath(jenkins.jenkins.getWorkspaceFor(project), folderPath);
        assertTrue(folder.exists() && folder.isDirectory(), "The folder should have been created");
    }

    @Test
    void testRunFolderCreateOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("FOLDER_PATH", "tokenFolder");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("folderCreateTestWithTokens");

        FolderCreateOperation operation = new FolderCreateOperation("$FOLDER_PATH");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        FilePath folder = new FilePath(jenkins.jenkins.getWorkspaceFor(project), "tokenFolder");
        assertTrue(folder.exists() && folder.isDirectory(), "The folder should have been created");
    }
}
