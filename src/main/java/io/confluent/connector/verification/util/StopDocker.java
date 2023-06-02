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
public class StopDocker {

    private static final Logger logger = LoggerFactory.getLogger(StopDocker.class);

    @Value("${docker_compose_file_path}")
    private String docker_compose_file_path;

    public int stopDockerComposeWithDefaultConfig()
            throws InvalidExitValueException, IOException, InterruptedException {
        Map<String, String> dockerEnvInfo = new HashMap<>();
        dockerEnvInfo.put(ApplicationConstants.DOCKER_COMPOSE_ENV_FILE,
                String.valueOf(Paths.get(docker_compose_file_path)));
        File dockerCurWrkDir = new File(Paths.get(docker_compose_file_path).getParent().toString());
        List<String> dockerComposeCommandDown = Splitter.onPattern(" ").omitEmptyStrings()
                .splitToList(ApplicationConstants.DOCKER_COMPOSE_EXE + " " + ApplicationConstants.DOCKER_COMPOSE_DOWN);
        ProcessResult exitCode = new ProcessExecutor().command(dockerComposeCommandDown)
                .redirectOutput(Slf4jStream.of(logger()).asInfo()).redirectError(Slf4jStream.of(logger()).asInfo())
                .environment(dockerEnvInfo).directory(dockerCurWrkDir).exitValueNormal().executeNoTimeout();
        logger.info("Return Code is :  " + exitCode.getExitValue());
        return exitCode.getExitValue();
    }

    private Logger logger() {
        return LoggerFactory.getLogger("docker-compose");
    }

}