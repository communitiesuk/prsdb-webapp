package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

@Suppress("ktlint:standard:function-naming")
interface PropertyComplianceRepository : JpaRepository<PropertyCompliance, Long> {
    fun findByPropertyOwnership_Id(propertyOwnershipId: Long): PropertyCompliance?

    fun findAllByPropertyOwnership_PrimaryLandlord_BaseUser_Id(landlordBaseUserId: String): List<PropertyCompliance>

    fun deleteByPropertyOwnership_IdIn(propertyOwnershipIds: List<Long>)

    fun deleteByPropertyOwnership_Id(propertyOwnershipId: Long)
}
