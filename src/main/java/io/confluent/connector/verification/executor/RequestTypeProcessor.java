package io.confluent.connector.verification.executor;

import io.confluent.connector.verification.constants.ApplicationConstants;
import io.confluent.connector.verification.model.TestResults;
import io.confluent.connector.verification.model.TestScripts;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class RequestTypeProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RequestTypeProcessor.class);
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final HttpHeaders headers = new HttpHeaders();

    public static void processGetRequest(TestScripts testcase, TestResults testResults) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> entity = new HttpEntity<>(headers);
        try {
            logger.info("Processing GET request: {}", testcase.getTestDescription());
            testResults.setStartTime(new Timestamp(System.currentTimeMillis()));

            ResponseEntity<String> resp = restTemplate.exchange(testcase.getUrl(), HttpMethod.GET, entity, String.class);
            assert resp.getStatusCodeValue() != testcase.getSuccessResponseCode() : "API response not valid";
            logger.info("GET request response is : " + resp);
            saveResponse(testResults, testcase.getTestDescription(), "success",  String.valueOf(resp.getStatusCodeValue()));
        } catch (Exception ex) {
            logger.error("Assert error:", ex);
            saveResponse(testResults, testcase.getTestDescription(), "failed", ex.toString());
        }
    }

    public static void processPostRequest(TestScripts testcase, TestResults testResults) {
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            logger.info("Processing POST request: {}", testcase.getTestDescription());
            testResults.setStartTime(new Timestamp(System.currentTimeMillis()));
            HttpEntity<Object> entity = new HttpEntity<>(getAPIBody(testcase.getInputPayloadFilepath()), headers);
            ResponseEntity<String> resp = restTemplate.exchange(testcase.getUrl(), HttpMethod.POST, entity, String.class);
            assert resp.getStatusCodeValue() != testcase.getSuccessResponseCode() : "API response not valid";

            logger.info("POST request response is : " + resp);
            saveResponse(testResults, testcase.getTestDescription(), "success",  String.valueOf(resp.getStatusCodeValue()));
        } catch (Exception ex) {
            logger.error("Assert error: ",ex);
            saveResponse(testResults, testcase.getTestDescription(), "failed", ex.toString());
        }
    }

    private static void saveResponse(TestResults testResults, String testcaseId, String status, String response) {

        testResults.setResponseText(StringUtils.truncate(response, ApplicationConstants.API_RESPONSE_MAXLENGTH));
        testResults.setRunStatus(status);
        testResults.setTestcase(testcaseId);
        testResults.setEndTime(new Timestamp(System.currentTimeMillis()));
    }

    private static JSONObject getAPIBody(String filePath) {
        try (FileReader reader = new FileReader(filePath))
        {
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(reader);
            return (JSONObject) obj;

        } catch (IOException | ParseException e) {
            logger.error("Failed to read config file: ", e);
        }
        return null;
    }
}
