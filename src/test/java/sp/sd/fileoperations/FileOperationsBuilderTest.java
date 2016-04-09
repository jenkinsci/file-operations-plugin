package sp.sd.fileoperations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.FreeStyleBuild;
import hudson.model.Result;
import hudson.model.TopLevelItem;
import hudson.model.AbstractBuild;
import hudson.model.Api;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.model.User;
import hudson.model.Cause.UserCause;
import hudson.security.ACL;
import hudson.security.Permission;
import hudson.tasks.BuildTrigger;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.json.JSONObject;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.junit.ComparisonFailure;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.WithoutJenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;


public class FileOperationsBuilderTest {
	@Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private final static String NONE = null;
    
    @Test
    @WithoutJenkins
    public void testDefaults() {    	
    	FileOperationsBuilder fob = new FileOperationsBuilder(null);
    	assertEquals(0, fob.getFileOperations().size());
    }
    
    @Test
    @WithoutJenkins
    public void testSettersAndGetters() {
    	List<FileOperation> fo = new ArrayList<FileOperation>();
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
        assertEquals(Result.FAILURE, build.getResult());
    }
}