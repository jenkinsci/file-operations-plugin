package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileOperationsBuilderTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileOperationsBuilder fob = new FileOperationsBuilder(null);
        assertEquals(0, fob.getFileOperations().size());
    }

    @Test
    @WithoutJenkins
    void testSettersAndGetters() {
        List<FileOperation> fo = new ArrayList<>();
        FileOperationsBuilder fob = new FileOperationsBuilder(fo);
        assertEquals(0, fob.getFileOperations().size());
    }

    @Test
    void testAddFileOperationToBuildSteps() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(null));
        assertEquals(1, p1.getBuildersList().size());
    }

    @Test
    void testRunWithOutFileOperationWithFileOperationBuildStep() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        p1.getBuildersList().add(new FileOperationsBuilder(null));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
    }
}
