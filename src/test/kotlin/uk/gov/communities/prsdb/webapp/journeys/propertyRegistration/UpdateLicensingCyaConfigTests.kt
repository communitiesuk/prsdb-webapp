package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class UpdateLicensingCyaConfigTests {
    @Mock
    private lateinit var mockLicensingDetailsHelper: LicensingDetailsHelper

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateLicensingJourneyState

    @Mock
    private lateinit var mockLicensingTypeStep: LicensingTypeStep

    @Mock
    private lateinit var mockLicensingTypeFormModel: LicensingTypeFormModel

    private val propertyId = 123L

    @Mock
    private lateinit var stepConfig: UpdateLicensingCyaConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateLicensingCyaConfig(
                licensingDetailsHelper = mockLicensingDetailsHelper,
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        stepConfig.routeSegment = UpdateLicensingCyaStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn("2024-01-01T00:00:00Z")
        whenever(mockState.licensingTypeStep).thenReturn(mockLicensingTypeStep)
        whenever(mockLicensingTypeStep.formModel).thenReturn(mockLicensingTypeFormModel)
        whenever(mockLicensingTypeFormModel.licensingType).thenReturn(LicensingType.NO_LICENSING)
        whenever(mockState.getLicenceNumberOrNull()).thenReturn(null)
    }

    @Test
    fun `afterStepDataIsAdded sends update emails with the correct updated items`() {
        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(eq(propertyId), eq(listOf("The licensing information")))
    }

    @Test
    fun `afterStepDataIsAdded deletes the journey and rethrows when an UpdateConflictException is thrown`() {
        whenever(
            mockPropertyOwnershipService.updateLicensing(any(), any(), anyOrNull(), any()),
        ).thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
