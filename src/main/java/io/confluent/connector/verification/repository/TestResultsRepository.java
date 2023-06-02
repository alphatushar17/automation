package io.confluent.connector.verification.repository;

import io.confluent.connector.verification.model.TestResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestResultsRepository extends JpaRepository<TestResults, Long> {

}
