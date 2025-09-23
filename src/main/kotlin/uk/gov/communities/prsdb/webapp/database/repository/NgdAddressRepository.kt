package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.NgdAddress

interface NgdAddressRepository : JpaRepository<NgdAddress, Int> {
    @Query(
        "SELECT obj_description('ngd_address'::regclass, 'pg_class') AS comment;",
        nativeQuery = true,
    )
    fun findComment(): String?

    @Query(
        "COMMENT ON TABLE ngd_address IS :comment;",
        nativeQuery = true,
    )
    fun saveComment(comment: String)
}
