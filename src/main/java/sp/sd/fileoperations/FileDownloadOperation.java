package sp.sd.fileoperations;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import hudson.util.Secret;
import org.apache.http.impl.client.*;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.FileOutputStream;
import java.io.File;
import java.io.OutputStream;

import hudson.FilePath.FileCallable;
import hudson.remoting.VirtualChannel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.jenkinsci.remoting.RoleChecker;

import java.io.Serializable;
import java.net.URI;


public class FileDownloadOperation extends FileOperation implements Serializable {
    private final String url;
    private final String userName;
    private final String targetLocation;
    private final String targetFileName;
    private final String password;
    private final String proxyHost;
    private final String proxyPort;

    @DataBoundConstructor
    public FileDownloadOperation(String url, String userName, String password, String targetLocation, String targetFileName, String proxyHost, String proxyPort) {
        this.url = url;
        this.userName = userName;
        this.targetLocation = targetLocation;
        this.targetFileName = targetFileName;
        this.password = Secret.fromString(password).getEncryptedValue();
        this.proxyHost=proxyHost;
        this.proxyPort=proxyPort;
    }

    public String getUrl() {
        return url;
    }

    public String getUserName() {
        return userName;
    }

    public String getTargetLocation() {
        return targetLocation;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public String getPassword() {
        return Secret.decrypt(password).getPlainText();
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Download Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(listener,
                        envVars.expand(url),
                        envVars.expand(userName),
                        envVars.expand(Secret.decrypt(password).getPlainText()),
                        envVars.expand(targetLocation),
                        envVars.expand(targetFileName),
                        envVars.expand(proxyHost),
                        envVars.expand(proxyPort)));
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                return false;
            }

        } catch (Exception e) {
            listener.fatalError(e.getMessage());
        }
        return result;
    }

    private static final class TargetFileCallable implements FileCallable<Boolean> {
        private static final long serialVersionUID = 1;
        private final TaskListener listener;
        private final String resolvedUrl;
        private final String resolvedUserName;
        private final String resolvedTargetLocation;
        private final String resolvedTargetFileName;
        private final String resolvedPassword;
        private final String proxyHost;
        private final String proxyPort;

        public TargetFileCallable(TaskListener Listener, String ResolvedUrl, String ResolvedUserName, String ResolvedPassword, String ResolvedTargetLocation, String ResolvedTargetFileName, String proxyHost, String proxyPort) {
            this.listener = Listener;
            this.resolvedUrl = ResolvedUrl;
            this.resolvedUserName = ResolvedUserName;
            this.resolvedTargetLocation = ResolvedTargetLocation;
            this.resolvedTargetFileName = ResolvedTargetFileName;
            this.resolvedPassword = ResolvedPassword;
            this.proxyHost = proxyHost;
            this.proxyPort = proxyPort;
        }

        @Override
        public Boolean invoke(File ws, VirtualChannel channel) {
            boolean result;
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                FilePath fpTLF = new FilePath(fpTL, resolvedTargetFileName);
                File fTarget = new File(fpTLF.toURI());
                URI Url = new URI(resolvedUrl);
                listener.getLogger().println("Started downloading file from " + resolvedUrl);
                HttpHost host = new HttpHost(Url.getHost(), Url.getPort(), Url.getScheme());
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(Url.getHost(), Url.getPort()), new UsernamePasswordCredentials(resolvedUserName, resolvedPassword));
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(host, basicAuth);

                HttpClientBuilder httpClientBuilder = HttpClients.custom()
                        .setDefaultCredentialsProvider(credsProvider);

                if (proxyHost != null && !proxyHost.isEmpty()
                        && proxyPort != null && proxyPort.matches("[0-9]+")) {
                    HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    httpClientBuilder.setProxy(proxy);
                }
                CloseableHttpClient httpClient = httpClientBuilder.setRedirectStrategy(new LaxRedirectStrategy()).build();
                HttpGet httpGet = new HttpGet(Url);
                HttpClientContext localContext = HttpClientContext.create();
                if (!resolvedUserName.isEmpty() && !resolvedPassword.isEmpty()) {
                    localContext.setAuthCache(authCache);
                }
                HttpResponse response = httpClient.execute(host, httpGet, localContext);
                HttpEntity entity = response.getEntity();
                try (OutputStream fosTarget = new FileOutputStream(fTarget)) {
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        result = false;
                    } else {
                        entity.writeTo(fosTarget);
                        result = true;
                        listener.getLogger().println("Completed downloading file.");
                    }
                }
            } catch (RuntimeException e) {
                listener.fatalError(e.getMessage());
                throw e;
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                result = false;
            }
            return result;
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {

        }
    }

    @Extension
    @Symbol("fileDownloadOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        public String getDisplayName() {
            return "File Download";
        }

    }

}
