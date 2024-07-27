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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FolderCreateOperationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDefaults() {
        String defaultFolderPath = "defaultFolder";
        FolderCreateOperation operation = new FolderCreateOperation(defaultFolderPath);

        assertEquals(defaultFolderPath, operation.getFolderPath());
    }

    @Test
    public void testRunFolderCreateOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("folderCreateTest");

        String folderPath = "newFolder";
        FolderCreateOperation operation = new FolderCreateOperation(folderPath);
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        FilePath folder = new FilePath(jenkins.jenkins.getWorkspaceFor(project), folderPath);
        assertTrue("The folder should have been created", folder.exists() && folder.isDirectory());
    }

    @Test
    public void testRunFolderCreateOperationWithTokens() throws Exception {
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
        assertTrue("The folder should have been created", folder.exists() && folder.isDirectory());
    }
}
