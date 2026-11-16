package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership
import uk.gov.communities.prsdb.webapp.database.entity.SuperiorLandlord

// The underscore tells JPA to access fields relating to the referenced table
@Suppress("ktlint:standard:function-naming")
interface SuperiorLandlordRepository : JpaRepository<SuperiorLandlord, Long> {
    fun findAllByPropertyOwnership_Id(propertyOwnershipId: Long): List<SuperiorLandlord>

    fun deleteByPropertyOwnership(propertyOwnership: PropertyOwnership)
}
