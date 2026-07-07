package uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.REMOVE_ID_VERIFICATION
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.IdentityVerificationStatus
import uk.gov.communities.prsdb.webapp.services.LandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class CheckLandlordIdentityVerifiedStepConfigTests {
    @Mock
    lateinit var mockLandlordService: LandlordService

    @Mock
    lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    lateinit var mockState: JourneyState

    private val baseUserId = "test-user-id"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns NOT_VERIFIED for a verified landlord when ID verification is removed`() {
        whenever(mockFeatureFlagManager.checkFeature(REMOVE_ID_VERIFICATION)).thenReturn(true)

        val stepConfig = CheckLandlordIdentityVerifiedStepConfig(mockLandlordService, mockFeatureFlagManager)

        assertEquals(IdentityVerificationStatus.NOT_VERIFIED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns VERIFIED for a verified landlord when ID verification is not removed`() {
        whenever(mockFeatureFlagManager.checkFeature(REMOVE_ID_VERIFICATION)).thenReturn(false)
        JourneyTestHelper.setMockUser(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId))
            .thenReturn(MockLandlordData.createLandlord(isVerified = true))

        val stepConfig = CheckLandlordIdentityVerifiedStepConfig(mockLandlordService, mockFeatureFlagManager)

        assertEquals(IdentityVerificationStatus.VERIFIED, stepConfig.mode(mockState))
    }

    @Test
    fun `mode returns NOT_VERIFIED for an unverified landlord when ID verification is not removed`() {
        whenever(mockFeatureFlagManager.checkFeature(REMOVE_ID_VERIFICATION)).thenReturn(false)
        JourneyTestHelper.setMockUser(baseUserId)
        whenever(mockLandlordService.retrieveLandlordByBaseUserId(baseUserId))
            .thenReturn(MockLandlordData.createLandlord(isVerified = false))

        val stepConfig = CheckLandlordIdentityVerifiedStepConfig(mockLandlordService, mockFeatureFlagManager)

        assertEquals(IdentityVerificationStatus.NOT_VERIFIED, stepConfig.mode(mockState))
    }
}
