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

public class FileCreateOperation extends FileOperation implements Serializable { 
	private final String fileName;
	private final String fileContent;
	
	@DataBoundConstructor 
	 public FileCreateOperation(String fileName, String fileContent) { 
		this.fileName = fileName;
		this.fileContent = fileContent;		
	 }

	 public String getFileName()
	 {
		 return fileName;
	 }
	 public String getFileContent()
	 {
		 return fileContent;
	 }
	 
	 public boolean runOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
			 	listener.getLogger().println("File Create Operation:");				
				try {	
					FilePath ws = new FilePath(build.getWorkspace(),"."); 
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(fileName), build.getEnvironment(listener).expand(fileContent),build.getEnvironment(listener)));				
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
		private final String resolvedFileName;
		private final String resolvedFileContent;
		public TargetFileCallable(BuildListener Listener, String ResolvedFileName, String ResolvedFileContent, EnvVars environment) {
			this.listener = Listener;
			this.resolvedFileName = ResolvedFileName;	
			this.resolvedFileContent = ResolvedFileContent;
			this.environment = environment;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try 
			{				
				FilePath fpWS = new FilePath(ws);
				FilePath fpTL = new FilePath(fpWS, resolvedFileName);
				if(fpTL.exists())
				{
					listener.getLogger().println(resolvedFileName + " file already exists, replacing the content with the provided content.");
				}
				listener.getLogger().println("Creating file: " + fpTL.getRemote());
				fpTL.write(resolvedFileContent, "UTF-8");
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
 public String getDisplayName() { return "File Create"; }

 }
}