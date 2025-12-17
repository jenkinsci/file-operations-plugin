package sp.sd.fileoperations;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.util.Secret;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.URI;
import org.apache.hc.client5.http.auth.AuthCache;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.DefaultRedirectStrategy;
import org.apache.hc.client5.http.impl.auth.BasicAuthCache;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.auth.BasicScheme;
import org.apache.hc.client5.http.impl.classic.*;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.jenkinsci.Symbol;
import org.jenkinsci.remoting.RoleChecker;
import org.kohsuke.stapler.DataBoundConstructor;

public class FileDownloadOperation extends FileOperation implements Serializable {
    private final String url;
    private final String userName;
    private final String targetLocation;
    private final String targetFileName;
    private final String password;
    private final String proxyHost;
    private final String proxyPort;

    @DataBoundConstructor
    public FileDownloadOperation(
            String url,
            String userName,
            String password,
            String targetLocation,
            String targetFileName,
            String proxyHost,
            String proxyPort) {
        this.url = url;
        this.userName = userName;
        this.targetLocation = targetLocation;
        this.targetFileName = targetFileName;
        this.password = Secret.fromString(password).getEncryptedValue();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
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

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public String getPassword() {
        return Secret.decrypt(password).getPlainText();
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public String getProxyPort() {
        return proxyPort;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public boolean runOperation(Run<?, ?> run, FilePath buildWorkspace, Launcher launcher, TaskListener listener) {
        boolean result = false;
        try {
            listener.getLogger().println("File Download Operation:");
            EnvVars envVars = run.getEnvironment(listener);
            try {
                FilePath ws = new FilePath(buildWorkspace, ".");
                result = ws.act(new TargetFileCallable(
                        listener,
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
        @Serial
        private static final long serialVersionUID = 1;

        private final TaskListener listener;
        private final String resolvedUrl;
        private final String resolvedUserName;
        private final String resolvedTargetLocation;
        private final String resolvedTargetFileName;
        private final String resolvedPassword;
        private final String proxyHost;
        private final String proxyPort;

        public TargetFileCallable(
                TaskListener Listener,
                String ResolvedUrl,
                String ResolvedUserName,
                String ResolvedPassword,
                String ResolvedTargetLocation,
                String ResolvedTargetFileName,
                String proxyHost,
                String proxyPort) {
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
            try {
                FilePath fpWS = new FilePath(ws);
                FilePath fpTL = new FilePath(fpWS, resolvedTargetLocation);
                FilePath fpTLF = new FilePath(fpTL, resolvedTargetFileName);
                File fTarget = new File(fpTLF.toURI());
                URI uri = new URI(resolvedUrl);
                listener.getLogger().println("Started downloading file from " + resolvedUrl);
                HttpHost host = new HttpHost(uri.getScheme(), uri.getHost(), uri.getPort());
                BasicCredentialsProvider credsProvider = new BasicCredentialsProvider();
                UsernamePasswordCredentials credentials =
                        new UsernamePasswordCredentials(resolvedUserName, resolvedPassword.toCharArray());
                credsProvider.setCredentials(new AuthScope(uri.getHost(), uri.getPort()), credentials);
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                basicAuth.initPreemptive(credentials);
                authCache.put(host, basicAuth);

                HttpClientBuilder httpClientBuilder = HttpClients.custom().setDefaultCredentialsProvider(credsProvider);

                if (proxyHost != null && !proxyHost.isEmpty() && proxyPort != null && proxyPort.matches("[0-9]+")) {
                    HttpHost proxy = new HttpHost(proxyHost, Integer.parseInt(proxyPort));
                    httpClientBuilder.setProxy(proxy);
                }

                CloseableHttpClient httpClient = httpClientBuilder
                        .setRedirectStrategy(DefaultRedirectStrategy.INSTANCE)
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                HttpClientContext localContext = HttpClientContext.create();
                if (!resolvedUserName.isEmpty() && !resolvedPassword.isEmpty()) {
                    localContext.setAuthCache(authCache);
                }
                return httpClient.execute(host, httpGet, localContext, response -> {
                    HttpEntity entity = response.getEntity();
                    try (OutputStream fosTarget = new FileOutputStream(fTarget)) {
                        if (response.getCode() != HttpStatus.SC_OK) {
                            return false;
                        } else {
                            entity.writeTo(fosTarget);
                            listener.getLogger().println("Completed downloading file.");
                            return true;
                        }
                    }
                });
            } catch (RuntimeException e) {
                listener.fatalError(e.getMessage());
                throw e;
            } catch (Exception e) {
                listener.fatalError(e.getMessage());
                return false;
            }
        }

        @Override
        public void checkRoles(RoleChecker checker) throws SecurityException {}
    }

    @Extension
    @Symbol("fileDownloadOperation")
    public static class DescriptorImpl extends FileOperationDescriptor {
        @NonNull
        @Override
        public String getDisplayName() {
            return "File Download";
        }
    }
}
