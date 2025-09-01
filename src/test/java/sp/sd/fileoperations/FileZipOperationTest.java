package sp.sd.fileoperations;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class FileZipOperationTest {

    private JenkinsRule jenkins;

    @BeforeEach
    void setUp(JenkinsRule rule) {
        jenkins = rule;
    }

    @Test
    @WithoutJenkins
    void testDefaults() {
        FileZipOperation fzo = new FileZipOperation("source", null);
        assertEquals("source", fzo.getFolderPath());
        assertNull(fzo.getOutputFolderPath());
    }

    @Test
    void testRunFileOperationZipDirectoryToDefaultOutput() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("source-directory/nested-folder1/TestFile1-1", ""));
        fop.add(new FileCreateOperation("source-directory/nested-folder2/TestFile2-1", ""));
        fop.add(new FileCreateOperation("source-directory/nested-folder2/TestFile2-2", ""));
        fop.add(new FileZipOperation("source-directory", null));
        fop.add(new FileUnZipOperation("source-directory/nested-folder1/TestFile1-1.zip", "unzipped-directory"));
        fop.add(new FileUnZipOperation("source-directory/nested-folder2/TestFile2-1.zip", "unzipped-directory"));
        fop.add(new FileUnZipOperation("source-directory/nested-folder2/TestFile2-2.zip", "unzipped-directory"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));

        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("unzipped-directory").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder1/TestFile1-1").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder2/TestFile2-1").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder2/TestFile2-2").exists());
    }

    @Test
    void testRunFileOperationZipFileToDefaultOutput() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("source-directory/nested-folder1/TestFile1-1", ""));
        fop.add(new FileZipOperation("source-directory/nested-folder1/TestFile1-1", null));
        fop.add(new FileUnZipOperation("nested-folder1/TestFile1-1.zip", "unzipped-directory"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));

        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("unzipped-directory").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/TestFile1-1").exists());
    }

    @Test
    void testRunFileOperationZipDirectoryToCustomOutput() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("source-directory/nested-folder1/TestFile1-1", ""));
        fop.add(new FileCreateOperation("source-directory/nested-folder2/TestFile2-1", ""));
        fop.add(new FileCreateOperation("source-directory/nested-folder2/TestFile2-2", ""));
        fop.add(new FileZipOperation("source-directory", "output-directory/nested-output-folder1"));
        fop.add(new FileUnZipOperation("output-directory/nested-output-folder1/source-directory/nested-folder1/TestFile1-1.zip", "unzipped-directory"));
        fop.add(new FileUnZipOperation("output-directory/nested-output-folder1/source-directory/nested-folder2/TestFile2-1.zip", "unzipped-directory"));
        fop.add(new FileUnZipOperation("output-directory/nested-output-folder1/source-directory/nested-folder2/TestFile2-2.zip", "unzipped-directory"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));

        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("unzipped-directory").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder1/TestFile1-1").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder2/TestFile2-1").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/source-directory/nested-folder2/TestFile2-2").exists());
    }

    @Test
    void testRunFileOperationZipFileToCustomOutput() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("source-directory/nested-folder1/TestFile1-1", ""));
        fop.add(new FileZipOperation(
            "source-directory/nested-folder1/TestFile1-1",
            "output-directory/nested-output-folder1"));
        fop.add(new FileUnZipOperation(
                "output-directory/nested-output-folder1/nested-folder1/TestFile1-1.zip",
                "unzipped-directory"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));

        FreeStyleBuild build = p1.scheduleBuild2(0).get();

        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("unzipped-directory").exists());
        assertTrue(build.getWorkspace().child("unzipped-directory/TestFile1-1").exists());
    }
}
