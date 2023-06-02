package io.confluent.connector.verification.executor;

import io.confluent.connector.verification.constants.ApplicationConstants;
import io.confluent.connector.verification.model.ReleaseInfo;
import io.confluent.connector.verification.model.TestResults;
import io.confluent.connector.verification.model.TestScripts;
import io.confluent.connector.verification.repository.ReleaseInfoRepository;
import io.confluent.connector.verification.repository.TestResultsRepository;
import io.confluent.connector.verification.repository.TestScriptsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class TestScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestScriptExecutor.class);

    @Autowired
    private TestResultsRepository testResultsRepository;

    @Autowired
    private ReleaseInfoRepository releaseInfoRepository;

    @Autowired
    private TestScriptsRepository testScriptsRepository;

    public void runTests(ReleaseInfo releaseInfo) throws Exception {

        String partnerId = releaseInfo.getPartnerId();
        List<TestScripts> testScripts =testScriptsRepository.findByPartnerId(partnerId);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        for (TestScripts testcase : testScripts) {
            TestResults testResults = new TestResults();
            testResults.setPartnerId(partnerId);
            testResults.setRunDate(timestamp);
            processAPIRequest(testcase, testResults);
            testResultsRepository.save(testResults);
        }
        releaseInfo.setLastRunDate(timestamp);
        releaseInfoRepository.save(releaseInfo);
    }

    private static void processAPIRequest(TestScripts testcase, TestResults testResults) throws InterruptedException {
        logger.info("Executing testId : {} | requestType: {}", testcase.getTestId(), testcase.getRequestType());
        delay(testcase.getDelay());
        switch (testcase.getRequestType().toLowerCase()) {
            case ApplicationConstants.GET:
                RequestTypeProcessor.processGetRequest(testcase, testResults);
                break;
            case ApplicationConstants.POST:
                RequestTypeProcessor.processPostRequest(testcase, testResults);
                break;
            default:
                logger.error("Invalid Request Method :" + testcase.getRequestType());
                break;
        }
    }

    private static void delay(long milliseconds) throws InterruptedException {
        Thread.sleep(milliseconds);
    }
}
