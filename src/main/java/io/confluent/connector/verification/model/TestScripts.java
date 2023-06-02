package io.confluent.connector.verification.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "testcase_info")
@Getter @Setter
public class TestScripts {

	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Id
	@Column(name = "test_id", nullable = false)
	private String testId;

	@Column(name = "partner_id", nullable = false)
	private String partnerId;

	@Column(name = "test_description", nullable = false)
	private String testDescription;

	@Column(name = "request_type")
	private String requestType;

	@Column(name = "url", nullable = false)
	private String url;

	@Column(name = "input_payload_filePath")
	private String inputPayloadFilepath;

	@Column(name = "success_response_code")
	private int successResponseCode;

	@Column(name = "success_response_validation")
	private String successResponseValidationString;

	@Column(name = "delay")
	private int delay;

	@Column(name = "runStatus")
	private boolean runStatus;

	public TestScripts() {}

	public TestScripts(long id, String testId, String partnerId, String testDescription, String requestType, String url, String inputPayloadFilepath, int successResponseCode, String successResponseValidationString, int delay, boolean runStatus) {
		this.id = id;
		this.testId = testId;
		this.partnerId = partnerId;
		this.testDescription = testDescription;
		this.requestType = requestType;
		this.url = url;
		this.inputPayloadFilepath = inputPayloadFilepath;
		this.successResponseCode = successResponseCode;
		this.successResponseValidationString = successResponseValidationString;
		this.delay = delay;
		this.runStatus = runStatus;
	}

}