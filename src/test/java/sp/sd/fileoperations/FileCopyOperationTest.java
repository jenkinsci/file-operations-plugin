package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
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

public class FileCopyOperationTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @WithoutJenkins
    public void testDefaults() {
        FileCopyOperation fco = new FileCopyOperation("**/*.config", "**/*.xml", "target", true, false, null, null);
        assertEquals("**/*.config", fco.getIncludes());
        assertEquals("**/*.xml", fco.getExcludes());
        assertEquals("target", fco.getTargetLocation());
        assertEquals(true, fco.getFlattenFiles());
        assertEquals(false, fco.getRenameFiles());
        assertEquals(null, fco.getSourceCaptureExpression());
        assertEquals(null, fco.getTargetNameExpression());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStepNoFlatten() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));
        fop.add(new FileCopyOperation("test-results-xml/**/*.xml", "", "test-results", false, false, null, null));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-0/classA/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-0/classB/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-1/classB/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/test-results-xml/pod-1/classC/TestC.xml").exists());
    }

    
    @Test
    public void testRunFileOperationWithFileOperationBuildStepWithFlatten() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));
        fop.add(new FileCopyOperation("test-results-xml/**/*.xml", "", "test-results", true, false, null, null));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestC.xml").exists());
    }

    @Test
    public void testRunFileOperationWithFileOperationBuildStepWithFlattenAndRename() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classC/TestC.xml", ""));

        // Required to handle test being run on either Windows or Unix systems
        String dirSep = "(?:\\\\|/)";

        fop.add(new FileCopyOperation(
                "test-results-xml/**/*.xml",
                "",
                "test-results",
                true,
                true,
                ".*" + dirSep + "test-results-xml" + dirSep + ".*-([\\d]+)" +
                        dirSep + ".*" + dirSep + "([^" + dirSep + "]+)$",
                "$1-$2"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/0-TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/0-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestC.xml").exists());
    }

    /**
     * @see "help-sourceCaptureExpression.html"
     */
    @Test
    public void testFileCopyOperationForSourceCaptureExpressionExample() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("dir1/info-app.txt", ""));
        fop.add(new FileCreateOperation("dir1/error-app.txt", ""));

        // Required to handle test being run on either Windows or Unix systems
        String dirSep = "(?:\\\\|/)";

        fop.add(new FileCopyOperation(
                "**/dir1/*.txt",
                "",
                "logs",
                true,
                true,
                "dir1" + dirSep + "(.*)-app\\.txt$",
                "$1.log"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("logs/info.log").exists());
        assertTrue(build.getWorkspace().child("logs/error.log").exists());
    }

    /**
     * Files will not be flatten because includes path is not included in sourceCaptureExpression.
     *
     * @see "https://github.com/jenkinsci/file-operations-plugin/issues/101"
     */
    @Test
    public void testFileCopyOperationWithFlattenAndRenameFileWithoutMatchingRegex() throws Exception {
        FreeStyleProject p1 = jenkins.createFreeStyleProject("build1");
        List<FileOperation> fop = new ArrayList<>();
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/TestA.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-0/classA/Test-rename-A.xml", ""));
        fop.add(new FileCreateOperation("test-results-xml/pod-1/classB/TestB.xml", ""));

        fop.add(new FileCopyOperation(
                "test-results-xml/**/*.xml",
                "",
                "test-results",
                true,
                true,
                ".*Test-rename-(.*)\\.xml$",
                "Test$1.log"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/TestA.log").exists());
        assertTrue(build.getWorkspace().child("test-results/TestB.xml").exists());
    }
}
