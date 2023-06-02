package io.confluent.connector.verification.repository;

import io.confluent.connector.verification.model.TestScripts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestScriptsRepository extends JpaRepository<TestScripts, Long> {

    @Query(value = "SELECT * FROM testcase_info WHERE PARTNER_ID = ?1 order by test_id", nativeQuery = true)
    List<TestScripts> findByPartnerId(String partnerId);
}
