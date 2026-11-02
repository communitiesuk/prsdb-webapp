package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.AdditionalPropertyOwner

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface AdditionalPropertyOwnerRepository : JpaRepository<AdditionalPropertyOwner, Long> {
    fun findAllByPropertyOwnership_Id(propertyOwnershipId: Long): List<AdditionalPropertyOwner>
}
