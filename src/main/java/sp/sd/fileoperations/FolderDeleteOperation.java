package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import java.io.Serializable;

public class FolderDeleteOperation extends FileOperation implements Serializable { 
	private final String folderPath;	
	
	@DataBoundConstructor 
	 public FolderDeleteOperation(String folderPath) { 
		this.folderPath = folderPath;			
	 }

	 public String getFolderPath()
	 {
		 return folderPath;
	 }
	 	 
	 public boolean runOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
			 	listener.getLogger().println("Folder Delete Operation:");				
				try {	
					FilePath ws = new FilePath(build.getWorkspace(),"."); 
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(folderPath),build.getEnvironment(listener)));				
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
		private final BuildListener listener;
		private final EnvVars environment;
		private final String resolvedFolderPath;
		
		public TargetFileCallable(BuildListener Listener, String ResolvedFolderPath, EnvVars environment) {
			this.listener = Listener;
			this.resolvedFolderPath = ResolvedFolderPath;	
			this.environment = environment;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try 
			{				
				FilePath fpWS = new FilePath(ws);
				FilePath fpTL = new FilePath(fpWS, resolvedFolderPath);				
				listener.getLogger().println("Deleting folder: " + fpTL.getRemote());
				fpTL.deleteRecursive();
				result = true;
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
 public String getDisplayName() { return "Folder Delete"; }

 }
}