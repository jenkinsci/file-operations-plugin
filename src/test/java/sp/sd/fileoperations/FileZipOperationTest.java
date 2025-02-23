package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;

public class FileZipOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileZipOperation fzo = new FileZipOperation("source", null);
        assertEquals("source", fzo.getFolderPath());
        assertNull(fzo.getOutputFolderPath());
    }

    @Test
    public void testRunFileOperationZipDirectoryToDefaultOutput() throws Exception {
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
    public void testRunFileOperationZipFileToDefaultOutput() throws Exception {
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
    public void testRunFileOperationZipDirectoryToCustomOutput() throws Exception {
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
    public void testRunFileOperationZipFileToCustomOutput() throws Exception {
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
