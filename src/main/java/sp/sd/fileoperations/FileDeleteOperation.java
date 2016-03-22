package sp.sd.fileoperations;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.File;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import org.jenkinsci.remoting.RoleSensitive;
import java.io.Serializable;

public class FileDeleteOperation extends FileOperation implements Serializable { 
	private final String FileName;
	@DataBoundConstructor 
	 public FileDeleteOperation(String FileName) { 
		this.FileName = FileName;
	 }

	 public String getFileName()
	 {
		 return FileName;
	 }
	 public boolean RunOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
				FilePath targetFilePath = new FilePath(build.getWorkspace(), build.getEnvironment(listener).expand(FileName)); 
				try{
					if(!targetFilePath.exists())
					{
						listener.getLogger().println(targetFilePath.getName() + " file doesn't exists");
						return false;
					}
				}
				catch(RuntimeException e)
				{
					listener.getLogger().println(targetFilePath.getName() + " file doesn't exists");
					throw e;
				}
				catch(Exception e)
				{
					listener.getLogger().println(targetFilePath.getName() + " file doesn't exists");
					return false;
				}	
				
				try {	
					result = targetFilePath.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(FileName)));				
				}
				catch (Exception e) {
					e.printStackTrace(listener.getLogger());
					return false;
				}
				
			}
			catch(Exception e)
			{
				e.printStackTrace(listener.getLogger());
			}	
			return result;
		} 
 
	private static final class TargetFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;
		private final BuildListener listener;
		private final String resolvedFileName;
		public TargetFileCallable(BuildListener Listener, String ResolvedFileName) {
			this.listener = Listener;
			this.resolvedFileName = ResolvedFileName;			
		}
		@Override public Boolean invoke(File targetFile, VirtualChannel channel) {
			String fileName = targetFile.getName();
			boolean result = targetFile.delete();		
			listener.getLogger().println(fileName + " deleted successfully.");
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                
		}		
	}
 @Extension public static class DescriptorImpl extends FileOperationDescriptor {
 public String getDisplayName() { return "File Delete"; }

 }
}