package io.confluent.connector.verification.repository;

import io.confluent.connector.verification.model.ReleaseInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReleaseInfoRepository extends JpaRepository<ReleaseInfo, Long> {

    @Query(value = "SELECT * FROM release_info WHERE PARTNER_ID in :partnerIds and skip_verification = false", nativeQuery = true)
    List<ReleaseInfo> getReleaseInfoList(List<String> partnerIds);

    @Query(value = "SELECT * FROM release_info WHERE skip_verification = false", nativeQuery = true)
    List<ReleaseInfo> findAllOverride();
}
