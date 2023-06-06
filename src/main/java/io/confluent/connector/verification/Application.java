package io.confluent.connector.verification;

import io.confluent.connector.verification.executor.GithubIntegration;
import io.confluent.connector.verification.executor.TestScriptExecutor;
import io.confluent.connector.verification.model.ReleaseInfo;
import io.confluent.connector.verification.util.LaunchDocker;
import io.confluent.connector.verification.util.StopDocker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@SpringBootApplication
public class Application implements ApplicationRunner {

	private static final Logger LOGGER = LogManager.getLogger(ApplicationRunner.class);

	@Autowired
	private final GithubIntegration githubIntegration;

	@Autowired
	private final LaunchDocker launchDocker;

	@Autowired
	private final TestScriptExecutor testScriptExecutor;

	@Autowired
	private final StopDocker stopDocker;

	public Application(GithubIntegration githubIntegration, LaunchDocker launchDocker, TestScriptExecutor testScriptExecutor, StopDocker stopDocker) {
		this.githubIntegration = githubIntegration;
		this.launchDocker = launchDocker;
		this.testScriptExecutor = testScriptExecutor;
		this.stopDocker = stopDocker;
	}

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		System.out.println(args);
	}

	@Override
	public void run(ApplicationArguments arguments) throws IOException, InterruptedException {
		System.out.println(arguments);
		try {
			List<String> args = arguments.getNonOptionArgs();
			System.out.println(args);
			if (args.get(0).equals("Start")) {
				Set<ReleaseInfo> releaseInfos = githubIntegration.getPartnerRepoDetails(args);
				if (!releaseInfos.isEmpty()) {

					int exitCode = launchDocker.launchDockerComposeWithDefaultConfig();
					if (exitCode != 0) {
						LOGGER.error("Unexpected error occurred which configuring docker... Please check the log");
						exit(exitCode);
					}
					LOGGER.info("Docker is Ready");
				}

				// Run Test scripts
				for (ReleaseInfo releaseInfo : releaseInfos) {
					testScriptExecutor.runTests(releaseInfo);
				}

			} else {
				int exitCode = stopDocker.stopDockerComposeWithDefaultConfig();
				if (exitCode != 0) {
					LOGGER.error("Unexpected error occurred which stopping docker... Please check the log");
					exit(exitCode);
				}
			}
			System.exit(0);
		} catch(Exception e) {
			exit(1);
		}
	}

	private void exit(int exitCode) throws IOException, InterruptedException {
		stopDocker.stopDockerComposeWithDefaultConfig();
		System.exit(exitCode);
	}
}
