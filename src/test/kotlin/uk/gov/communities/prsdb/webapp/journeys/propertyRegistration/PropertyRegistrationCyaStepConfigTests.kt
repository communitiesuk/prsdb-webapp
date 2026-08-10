package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationCyaStepConfigTests {
    @Mock
    private lateinit var mockLocalCouncilService: LocalCouncilService

    @Mock
    private lateinit var mockLicensingDetailsHelper: LicensingDetailsHelper

    @Mock
    private lateinit var mockOccupancyDetailsHelper: OccupancyDetailsHelper

    @Mock
    private lateinit var mockComplianceDetailsHelper: ComplianceDetailsHelper

    @Mock
    private lateinit var mockMessageSource: MessageSource

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Test
    fun `chooseTemplate returns restructured CYA template when feature flag is enabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)

        assertEquals(
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersForm",
            stepConfig.chooseTemplate(mockState),
        )
    }

    @Test
    fun `chooseTemplate returns legacy CYA template when feature flag is disabled`() {
        val stepConfig = setupStepConfig()
        whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(false)

        assertEquals(
            "forms/restructureAndSkipping/propertyRegistrationCheckAnswersFormLegacy",
            stepConfig.chooseTemplate(mockState),
        )
    }

    private fun setupStepConfig() =
        PropertyRegistrationCyaStepConfig(
            mockLocalCouncilService,
            mockLicensingDetailsHelper,
            mockOccupancyDetailsHelper,
            mockComplianceDetailsHelper,
            mockMessageSource,
            mockFeatureFlagManager,
        )
}
