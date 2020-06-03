package fr.gouv.stopc.robert.config.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import javax.annotation.PostConstruct;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RobertConfigRepoWrapper {

	@Value("${spring.cloud.config.server.git.uri}")
	private String gitUri;

	/**
	 * Git Wrapper to manipulate the configuration repo
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	private Git gitWrapper;

	/**
	 * Initializes the GIT connection. The GIT wrapper must not be null
	 * 
	 * @since 0.0.1-SNAPSHOT
	 */
	@PostConstruct
	private void initGitConnection() {
		try {
			gitWrapper = Git.open(Paths.get(new URL(gitUri).toURI()).toFile());
			log.info("Connected to the Git repository");
		} catch (IOException | URISyntaxException e) {
			log.error("Failed to connect to the Git repository : ", e);
		}
		Assert.notNull(gitWrapper, "Git connection must be established");
	}

	/**
	 * 
	 * @param branchName
	 * @return
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public Iterable<RevCommit> getHistory(String branchName) throws IOException, GitAPIException {
		switchTo(branchName);
		return gitWrapper.log().call();
	}

	/**
	 * 
	 * @param branchName
	 * @throws IOException
	 * @throws GitAPIException
	 */
	public void switchTo(String branchName) throws IOException, GitAPIException {
		if (!gitWrapper.getRepository().getBranch().equals(branchName)) {
			gitWrapper.checkout().setName(branchName).call();
		}
	}

	public File[] listConfigurationFiles(String branchName) throws IOException, GitAPIException {
		switchTo(branchName);
		return this.gitWrapper.getRepository().getWorkTree().listFiles(x -> x.isFile());
	}

	/**
	 * 
	 * @param branchName
	 * @param message
	 * @throws GitAPIException
	 * @throws IOException
	 */
	public void saveConfiguration(String branchName, String message) throws GitAPIException, IOException {
		switchTo(branchName);
		gitWrapper.add().addFilepattern(".").call();
		gitWrapper.commit().setMessage(message).call();
	}
}
