package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyCompliance

interface PropertyComplianceRepository : JpaRepository<PropertyCompliance, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findByPropertyOwnership_Id(propertyOwnershipId: Long): PropertyCompliance?
}
