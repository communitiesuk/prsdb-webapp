package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.ACTIVE_INACTIVE
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LandlordActiveStatusServiceTests {
    @Mock
    private lateinit var userToLandlordService: UserToLandlordService

    // TODO PDJB-2149: Remove this mock when the ACTIVE_INACTIVE flag is removed
    @Mock
    private lateinit var featureFlagManager: FeatureFlagManager

    @InjectMocks
    private lateinit var landlordActiveStatusService: LandlordActiveStatusService

    @Test
    fun `isLandlordInactive returns true for an inactive landlord`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        val landlord = MockLandlordData.createIndividualLandlord().apply { deactivate(ORIGINAL_DATE) }

        assertTrue(landlordActiveStatusService.isLandlordInactive(landlord))
    }

    @Test
    fun `isLandlordInactive returns false for an active landlord`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)

        assertFalse(landlordActiveStatusService.isLandlordInactive(MockLandlordData.createIndividualLandlord()))
    }

    // TODO PDJB-2149: Delete this test when the ACTIVE_INACTIVE feature flag is removed
    @Test
    fun `isLandlordInactive returns false for an inactive landlord when ACTIVE_INACTIVE is disabled`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(false)
        val landlord = MockLandlordData.createIndividualLandlord().apply { deactivate(ORIGINAL_DATE) }

        assertFalse(landlordActiveStatusService.isLandlordInactive(landlord))
    }

    @Test
    fun `isCurrentLandlordInactive returns true when the current landlord is inactive`() {
        whenever(featureFlagManager.checkFeature(ACTIVE_INACTIVE)).thenReturn(true)
        val landlord = MockLandlordData.createIndividualLandlord().apply { deactivate(ORIGINAL_DATE) }
        whenever(userToLandlordService.getCurrentLandlordForUserOrNull()).thenReturn(landlord)

        assertTrue(landlordActiveStatusService.isCurrentLandlordInactive())
    }

    @Test
    fun `isCurrentLandlordInactive returns false when the user has no landlord`() {
        whenever(userToLandlordService.getCurrentLandlordForUserOrNull()).thenReturn(null)

        assertFalse(landlordActiveStatusService.isCurrentLandlordInactive())
    }

    companion object {
        private val ORIGINAL_DATE = LocalDate.of(2026, 1, 1)
    }
}
