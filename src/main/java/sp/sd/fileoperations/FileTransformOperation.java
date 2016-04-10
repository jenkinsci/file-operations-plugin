package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.Run;
import hudson.model.TaskListener;

import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import java.io.Serializable;

public class FileTransformOperation extends FileOperation implements Serializable { 
	private final String includes;
	private final String excludes;
	
	@DataBoundConstructor 
	 public FileTransformOperation(String includes, String excludes) { 
		this.includes = includes;
		this.excludes = excludes;		
	 }

	 public String getIncludes()
	 {
		 return includes;
	 }
	 public String getExcludes()
	 {
		 return excludes;
	 }
	 
	 public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
		 boolean result = false;
		 try
			{
			 	listener.getLogger().println("File Transform Operation:");
			 	EnvVars envVars = run.getEnvironment(listener);
				try {	
					FilePath ws = new FilePath(buildWorkspace,"."); 
					result = ws.act(new TargetFileCallable(listener, envVars.expand(includes), envVars.expand(excludes),envVars));				
				}
				catch (Exception e) {
					listener.fatalError(e.getMessage());
					return false;
				}
				
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
			}	
			return result;
		} 
 
	private static final class TargetFileCallable implements FileCallable<Boolean> {
		private static final long serialVersionUID = 1;
		private final TaskListener listener;
		private final EnvVars environment;
		private final String resolvedIncludes;
		private final String resolvedExcludes;
		public TargetFileCallable(TaskListener Listener, String ResolvedIncludes, String ResolvedExcludes, EnvVars environment) {
			this.listener = Listener;
			this.resolvedIncludes = ResolvedIncludes;	
			this.resolvedExcludes = ResolvedExcludes;
			this.environment = environment;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try 
			{				
				FilePath fpWS = new FilePath(ws);
				FilePath[] resolvedFiles = fpWS.list(resolvedIncludes, resolvedExcludes);
				if(resolvedFiles.length == 0)
				{
					listener.getLogger().println("0 files found for include pattern '" + resolvedIncludes + "' and exclude pattern '" + resolvedExcludes +"'");
					result = true;
				}
				else
				{
					for(FilePath item : resolvedFiles)
					{
						listener.getLogger().println("Transforming: " + item.getRemote());
						String fileContent = item.readToString();
						item.deleteContents();						
						item.write(environment.expand(fileContent), "UTF-8");
						result = true;
					}					
				}				
			}
			catch(RuntimeException e)
			{
				listener.fatalError(e.getMessage());
				throw e;
			}
			catch(Exception e)
			{
				listener.fatalError(e.getMessage());
				result = false;
			}
			return result;	
		}
		
		@Override  public void checkRoles(RoleChecker checker) throws SecurityException {
                
		}		
	}
 @Extension public static class DescriptorImpl extends FileOperationDescriptor {
 public String getDisplayName() { return "File Transform"; }

 }
}