package io.confluent.connector.verification.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "release_info")
@Getter @Setter
public class ReleaseInfo {

	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@Id
	@Column(name = "partner_id", nullable = false)
	private String partnerId;

	@Column(name = "partner_name")
	private String partnerName;

	@Column(name = "git_release_url", nullable = false)
	private String gitReleaseURL;

	@Column(name = "current_release_version")
	private String currentReleaseVersion;

	@Column(name = "last_release_version")
	private String lastReleaseVersion;

	@Column(name = "release_submittedDate", nullable = false)
	private Timestamp releaseSubmittedDate;

	@Column(name = "last_runDate", nullable = false)
	private Timestamp lastRunDate;

	@Column(name = "final_jarName", nullable = false)
	private String finalJarName;

	@Column(name = "buildPath", nullable = false)
	private String buildPath;

	@Column(name = "buildCommand", nullable = false)
	private String buildCommand;

	@Column(name = "skip_verification", nullable = false)
	private Boolean skipVerification;

	@Column(name = "repo_tagName", nullable = false)
	private String repoTagName;

	public ReleaseInfo() {
		
	}

	public ReleaseInfo(long id, String partnerId, String partnerName, String gitReleaseURL, String currentReleaseVersion, String lastReleaseVersion, Timestamp releaseSubmittedDate, Timestamp lastRunDate, String finalJarName, String buildPath, String buildCommand, Boolean skipVerification, String repoTagName) {
		this.id = id;
		this.partnerId = partnerId;
		this.partnerName = partnerName;
		this.gitReleaseURL = gitReleaseURL;
		this.currentReleaseVersion = currentReleaseVersion;
		this.lastReleaseVersion = lastReleaseVersion;
		this.releaseSubmittedDate = releaseSubmittedDate;
		this.lastRunDate = lastRunDate;
		this.finalJarName = finalJarName;
		this.buildPath = buildPath;
		this.buildCommand = buildCommand;
		this.skipVerification = skipVerification;
		this.repoTagName = repoTagName;
	}

}
