package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import hudson.FilePath;
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
        fop.add(new FileCopyOperation("test-results-xml/**/*.xml",
                                      "",
                                      "test-results",
                                      true,
                                      true,
                                      ".*/test-results-xml/.*-([\\d]+)/.*/([^/]+)$",
                                      "$1-$2"));
        p1.getBuildersList().add(new FileOperationsBuilder(fop));
        FreeStyleBuild build = p1.scheduleBuild2(0).get();
        assertEquals(Result.SUCCESS, build.getResult());
        assertTrue(build.getWorkspace().child("test-results/0-TestA.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/0-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestB.xml").exists());
        assertTrue(build.getWorkspace().child("test-results/1-TestC.xml").exists());
    }
}
