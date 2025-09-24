package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import uk.gov.communities.prsdb.webapp.database.entity.Address

interface AddressRepository : JpaRepository<Address, Long> {
    fun findByUprn(uprn: Long): Address?

    @Query(
        "SELECT obj_description('address'::regclass, 'pg_class') AS comment;",
        nativeQuery = true,
    )
    fun findComment(): String?

    @Query(
        "COMMENT ON TABLE address IS :comment;",
        nativeQuery = true,
    )
    fun saveComment(comment: String)
}
