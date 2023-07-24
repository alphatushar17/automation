package io.confluent.connector.verification.constants;

import org.apache.commons.lang3.SystemUtils;

public class ApplicationConstants {

    public static final String DOCKER_COMPOSE_EXE = SystemUtils.IS_OS_WINDOWS ? "docker-compose.exe" : "sudo docker-compose";
    public static final String DOCKER_COMPOSE_ENV_FILE = "COMPOSE_FILE";
    public static final String DOCKER_COMPOSE_UP = "up -d --force-recreate";
    public static final String DOCKER_COMPOSE_DOWN = "down";

    public static final String GITHUB_AUTHORIZATION_HEADER = "Basic XXXXXXXXXXXXXXXXXXXXXXXxxxxxxxxxxxxxxxxxxxxxxxxx";
    public static final String POST = "post";
    public static final String GET = "get";
    public static final int API_RESPONSE_MAXLENGTH = 255;
}