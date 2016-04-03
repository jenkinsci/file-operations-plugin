package sp.sd.fileoperations;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import org.kohsuke.stapler.DataBoundConstructor;
import java.io.File;
import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.remoting.RoleChecker;
import java.io.Serializable;

public class FileUnTarOperation extends FileOperation implements Serializable { 
	private final String filePath;	
	private final String targetLocation;
	private boolean isGZIP = false;
	
	@DataBoundConstructor 
	 public FileUnTarOperation(String filePath, String targetLocation, boolean isGZIP) { 
		this.filePath = filePath;		
		this.targetLocation = targetLocation;		
		this.isGZIP = isGZIP;
	 }

	 public String getFilePath()
	 {
		 return filePath;
	 }
	 
	 public String getTargetLocation()
	 {
		 return targetLocation;
	 }
	 
	 public boolean getIsGZIP()
	 {
		 return isGZIP;
	 }
	 
	 public boolean runOperation(AbstractBuild build, Launcher launcher, BuildListener listener) {
		 boolean result = false;
		 try
			{
			 listener.getLogger().println("Untar File Operation:");
				try {	
					FilePath ws = new FilePath(build.getWorkspace(),"."); 
					result = ws.act(new TargetFileCallable(listener, build.getEnvironment(listener).expand(filePath), build.getEnvironment(listener).expand(targetLocation), isGZIP));					
				}
				catch(RuntimeException e)
				{
					listener.getLogger().println(e.getMessage());
					throw e;
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
		private final String resolvedFilePath;		
		private final String resolvedTargetLocation;
		private boolean IsGZIP = false;
		
		public TargetFileCallable(BuildListener Listener, String ResolvedFilePath, String ResolvedTargetLocation, boolean IsGZIP) {
			this.listener = Listener;
			this.resolvedFilePath = ResolvedFilePath;
			this.resolvedTargetLocation = ResolvedTargetLocation;
			this.IsGZIP = IsGZIP;
		}
		@Override public Boolean invoke(File ws, VirtualChannel channel) {
			boolean result = false;
			try {
				FilePath fpWS = new FilePath(ws);
				FilePath fpSrcTar = new FilePath(fpWS, resolvedFilePath);
				FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
				listener.getLogger().println("Untarring " + resolvedFilePath + " to " + fpTL.getRemote());
				if(!fpTL.exists())
				{
					fpTL.mkdirs();					
				}
				if(IsGZIP)
				{
					fpSrcTar.untar(fpTL, FilePath.TarCompression.GZIP);
					result = true;
					listener.getLogger().println("Untar completed.");
				}
				else
				{
					fpSrcTar.untar(fpTL, FilePath.TarCompression.NONE);
					result = true;
					listener.getLogger().println("Untar completed.");
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
 public String getDisplayName() { return "Untar"; }

 }
}