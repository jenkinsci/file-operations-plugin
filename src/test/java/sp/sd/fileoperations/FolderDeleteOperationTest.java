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
import static org.junit.Assert.assertFalse;

public class FolderDeleteOperationTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testDefaults() {
        String folderPath = "folderToDelete";
        FolderDeleteOperation operation = new FolderDeleteOperation(folderPath);

        assertEquals(folderPath, operation.getFolderPath());
    }

    @Test
    public void testRunFolderDeleteOperation() throws Exception {
        FreeStyleProject project = jenkins.createFreeStyleProject("folderDeleteTest");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath folderToDelete = new FilePath(workspace, "folderToDelete");

        folderToDelete.mkdirs();
        FilePath fileInFolder = new FilePath(folderToDelete, "file.txt");
        fileInFolder.write("Sample content", "UTF-8");

        FolderDeleteOperation operation = new FolderDeleteOperation("folderToDelete");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse("The folder should have been deleted", folderToDelete.exists());
    }

    @Test
    public void testRunFolderDeleteOperationWithTokens() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("FOLDER_TO_DELETE", "folderToDelete");
        jenkins.jenkins.getGlobalNodeProperties().add(prop);

        FreeStyleProject project = jenkins.createFreeStyleProject("folderDeleteTestWithTokens");

        FilePath workspace = jenkins.jenkins.getWorkspaceFor(project);
        FilePath folderToDelete = new FilePath(workspace, "folderToDelete");

        folderToDelete.mkdirs();
        FilePath fileInFolder = new FilePath(folderToDelete, "file.txt");
        fileInFolder.write("Sample content", "UTF-8");

        FolderDeleteOperation operation = new FolderDeleteOperation("$FOLDER_TO_DELETE");
        project.getBuildersList().add(new FileOperationsBuilder(List.of(operation)));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());

        assertFalse("The folder should have been deleted", folderToDelete.exists());
    }
}
