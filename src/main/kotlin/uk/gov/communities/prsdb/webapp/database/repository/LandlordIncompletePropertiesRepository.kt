package uk.gov.communities.prsdb.webapp.database.repository

import org.springframework.data.jpa.repository.JpaRepository
import uk.gov.communities.prsdb.webapp.database.entity.LandlordIncompleteProperties
import java.time.Instant

interface LandlordIncompletePropertiesRepository : JpaRepository<LandlordIncompleteProperties, Long> {
    @Suppress("ktlint:standard:function-naming")
    fun findBySavedJourneyState_CreatedDateBefore(cutoffDate: Instant): List<LandlordIncompleteProperties>
}
