package sp.sd.fileoperations;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import hidden.jth.org.apache.commons.lang3.RandomStringUtils;
import hudson.model.FreeStyleProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class to validate {@link FileDownloadOperation}.
 */
@WithJenkins
class FileDownloadOperationTest {

	// matches the file in __files
	private static final String DUMMY_ZIP = "dummy.zip";
	private static final String TEST_PATH = "/test/" + DUMMY_ZIP;

	@RegisterExtension
	static WireMockExtension wireMock = WireMockExtension.newInstance()
			.options(options().dynamicPort())
			.build();

	@Test
	void shouldDownload(JenkinsRule r) throws Exception {

		// endpoint without authentication
		wireMock.stubFor(get(urlEqualTo(TEST_PATH))
				.willReturn(aResponse()
						.withStatus(HttpStatus.SC_OK)
						.withBodyFile(DUMMY_ZIP) // matches the file in __files
						.withHeader(HttpHeaders.CONTENT_TYPE, "application/zip")));

		// define operation & run
		FreeStyleProject project = r.createFreeStyleProject();
		Run run = project.scheduleBuild2(0).get();
		FileDownloadOperation operation = new FileDownloadOperation(
				wireMock.baseUrl() + TEST_PATH,
				"",
				"",
				run.getRootDir().getAbsolutePath(),
				DUMMY_ZIP,
				null,
				null);
		boolean result = operation.runOperation(run, r.jenkins.getWorkspaceFor(project), null, TaskListener.NULL);
		File download = new File(operation.getTargetLocation(), operation.getTargetFileName());

		// validate
		assertTrue(result);
		assertTrue(download.exists());
		assertTrue(download.length() > 0);
	}

	@Test
	void shouldDownloadWithBasicAuth(JenkinsRule r) throws Exception {
		String username = RandomStringUtils.secure().nextAlphabetic(10);
		String password = RandomStringUtils.secure().nextAlphabetic(10);

		// endpoint with authentication
		wireMock.stubFor(get(urlEqualTo(TEST_PATH))
				.withBasicAuth(username, password)
				.willReturn(aResponse()
						.withStatus(HttpStatus.SC_OK)
						.withBodyFile(DUMMY_ZIP) // matches the file in __files
						.withHeader(HttpHeaders.CONTENT_TYPE, "application/zip")));

		// define operation & run
		FreeStyleProject project = r.createFreeStyleProject();
		Run run = project.scheduleBuild2(0).get();
		FileDownloadOperation operation = new FileDownloadOperation(
				wireMock.baseUrl() + TEST_PATH,
				username,
				password,
				run.getRootDir().getAbsolutePath(),
				DUMMY_ZIP,
				null,
				null);
		boolean result = operation.runOperation(run, r.jenkins.getWorkspaceFor(project), null, TaskListener.NULL);
		File download = new File(operation.getTargetLocation(), operation.getTargetFileName());

		// validate
		assertTrue(result);
		assertTrue(download.exists());
		assertTrue(download.length() > 0);
	}

	@Test
	void shouldFailWithBadCredentials(JenkinsRule r) throws Exception {
		String username = RandomStringUtils.secure().nextAlphabetic(10);
		String password = RandomStringUtils.secure().nextAlphabetic(10);

		// endpoint with authentication
		wireMock.stubFor(get(urlEqualTo(TEST_PATH))
				.withBasicAuth(username, password)
				.willReturn(aResponse()
						.withStatus(HttpStatus.SC_OK)
						.withBodyFile(DUMMY_ZIP) // matches the file in __files
						.withHeader(HttpHeaders.CONTENT_TYPE, "application/zip")));

		// define operation & run
		FreeStyleProject project = r.createFreeStyleProject();
		Run run = project.scheduleBuild2(0).get();
		FileDownloadOperation operation = new FileDownloadOperation(
				wireMock.baseUrl() + TEST_PATH,
				RandomStringUtils.secure().nextAlphabetic(10),
				RandomStringUtils.secure().nextAlphabetic(10),
				run.getRootDir().getAbsolutePath(),
				DUMMY_ZIP,
				null,
				null);
		boolean result = operation.runOperation(run, r.jenkins.getWorkspaceFor(project), null, TaskListener.NULL);
		File download = new File(operation.getTargetLocation(), operation.getTargetFileName());

		// validate
		assertFalse(result);
		assertTrue(download.exists()); // empty file created
		assertEquals(0, download.length()); // empty file created
	}
}