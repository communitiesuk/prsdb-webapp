package uk.gov.communities.prsdb.webapp.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.communities.prsdb.webapp.constants.PROVIDE_LATER_DEADLINE_DAYS
import uk.gov.communities.prsdb.webapp.database.repository.PropertyOwnershipRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import java.time.Instant
import java.time.LocalDate

class PropertyWithProvideLaterTasksPersistenceTests :
    IntegrationTestWithMutableData(
        listOf("data-mockuser-landlord-with-many-compliance-actions.sql", "data-overlay-provide-later-properties.sql"),
    ) {
    @Autowired
    private lateinit var propertyOwnershipRepository: PropertyOwnershipRepository

    @Test
    fun `returns active properties created before the cutoff where any provide later status is true`() {
        assertThat(propertyOwnershipRepository.findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff()))
            .containsExactlyInAnyOrderElementsOf(ELIGIBLE_PROPERTY_IDS)
    }

    @Test
    fun `does not return properties created exactly at the cutoff`() {
        assertEquals(
            emptyList<Long>(),
            propertyOwnershipRepository.findActivePropertyIdsWithAnyProvideLaterTaskCreatedBefore(cutoff(OLDEST_PROPERTY_AGE_IN_DAYS)),
        )
    }

    private fun cutoff(daysAgo: Long = PROVIDE_LATER_DEADLINE_DAYS.toLong()): Instant =
        LocalDate
            .now(DateTimeHelper.UK_ZONE)
            .minusDays(daysAgo)
            .atStartOfDay(DateTimeHelper.UK_ZONE)
            .toInstant()

    companion object {
        private val ELIGIBLE_PROPERTY_IDS = (1L..5L).toList()
        private const val OLDEST_PROPERTY_AGE_IN_DAYS = 30L
    }
}
