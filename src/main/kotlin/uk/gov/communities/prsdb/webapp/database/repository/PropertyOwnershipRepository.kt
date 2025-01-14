package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.PropertyOwnership

interface PropertyOwnershipRepository : JpaRepository<PropertyOwnership, Long> {
    // The underscore tells JPA to access fields relating to the referenced table
    @Suppress("ktlint:standard:function-naming")
    fun existsByIsActiveTrueAndProperty_Id(id: Long): Boolean

    @Suppress("ktlint:standard:function-naming")
    fun findAllByPrimaryLandlord_BaseUser_IdAndIsActiveTrueAndProperty_Status_Registered(userId: String): List<PropertyOwnership>
}
