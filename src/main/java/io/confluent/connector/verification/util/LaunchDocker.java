package io.confluent.connector.verification.util;

import com.google.common.base.Splitter;
import io.confluent.connector.verification.constants.ApplicationConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zeroturnaround.exec.InvalidExitValueException;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.ProcessResult;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LaunchDocker {

    private static final Logger logger = LoggerFactory.getLogger(LaunchDocker.class);

    @Value("${docker_compose_file_path}")
    private String docker_compose_file_path;

    @Value("${DOCKER_CLUSTER_REBALANCING_DELAY_INTERVAL}")
    private String DOCKER_CLUSTER_REBALANCING_DELAY_INTERVAL;

    public int launchDockerComposeWithDefaultConfig()
            throws InvalidExitValueException, IOException, InterruptedException {

        Map<String, String> dockerEnvInfo = new HashMap<>();
        dockerEnvInfo.put(ApplicationConstants.DOCKER_COMPOSE_ENV_FILE,
                String.valueOf(Paths.get(docker_compose_file_path)));
        System.out.println(docker_compose_file_path);
        File dockerCurWrkDir = new File(Paths.get(docker_compose_file_path).getParent().toString());
        List<String> dockerComposeCommandUp = Splitter.onPattern(" ").omitEmptyStrings()
                .splitToList(ApplicationConstants.DOCKER_COMPOSE_EXE + " " + ApplicationConstants.DOCKER_COMPOSE_UP);
        ProcessResult exitCode = new ProcessExecutor().command(dockerComposeCommandUp)
                .redirectOutput(Slf4jStream.of(logger()).asInfo()).redirectError(Slf4jStream.of(logger()).asInfo())
                .environment(dockerEnvInfo).directory(dockerCurWrkDir).exitValueNormal().executeNoTimeout();
        logger.info("Return Code is : " + exitCode.getExitValue());
        logger.info("Waiting for docker to complete setup... ");
        delay(Long.parseLong(DOCKER_CLUSTER_REBALANCING_DELAY_INTERVAL));
        return exitCode.getExitValue();
    }

    private static Logger logger() {
        return LoggerFactory.getLogger("docker-compose");
    }

    private void delay(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
