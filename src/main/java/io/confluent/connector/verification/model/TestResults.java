package io.confluent.connector.verification.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "test_results")
@Getter @Setter
public class TestResults {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Column(name = "partner_id", nullable = false)
	private String partnerId;

	@Column(name = "testcase")
	private String testcase;

	@Column(name = "runStatus")
	private String runStatus;

	@Column(name = "responseText")
	private String responseText;

	@Column(name = "runDate")
	private Timestamp runDate;

	@Column(name = "startTime")
	private Timestamp startTime;

	@Column(name = "endTime")
	private Timestamp endTime;

	public TestResults() {

	}

	public TestResults(long id, String partnerId, String testcase, String runStatus, String responseText,
					   Timestamp runDate, Timestamp startTime, Timestamp endTime) {
		this.id = id;
		this.partnerId = partnerId;
		this.testcase = testcase;
		this.runStatus = runStatus;
		this.responseText = responseText;
		this.runDate = runDate;
		this.startTime = startTime;
		this.endTime = endTime;
	}
}