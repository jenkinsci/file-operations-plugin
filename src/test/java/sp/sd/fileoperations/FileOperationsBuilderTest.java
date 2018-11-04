package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;

import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.FreeStyleProject;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

public class FileOperationsBuilderTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileOperationsBuilder fob = new FileOperationsBuilder(null);
        assertEquals(0, fob.getFileOperations().size());
    }

    @Test
    @WithoutJenkins
    public void testSettersAndGetters() {
        List<FileOperation> fo = new ArrayList<>();
        FileOperationsBuilder fob = new FileOperationsBuilder(fo);
        assertEquals(0, fob.getFileOperations().size());
    }

    @Test
    public void testAddFileOperationToBuildSteps() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(null));
        assertEquals(1, p1.getBuildersList().size());
    }

    @Test
    public void testRunWithOutFileOperationWithFileOperationBuildStep() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(null));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
    }
}
