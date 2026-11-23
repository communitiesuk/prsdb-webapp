package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.LicensingTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.models.dataModels.LicensingDetailsDataModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class UpdateLicensingCyaConfigTests {
    @Mock
    private lateinit var mockLicensingDetailsHelper: LicensingDetailsHelper

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockLicensingAndTenancyDetailsDataModelFactory: LicensingAndTenancyDetailsDataModelFactory

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateLicensingJourneyState

    @Mock
    private lateinit var mockLicensingTask: LicensingTask

    private val propertyId = 123L
    private val initialLastModifiedDate = Instant.parse("2024-01-01T00:00:00Z")
    private val licensing = LicensingDetailsDataModel(LicensingType.SELECTIVE_LICENCE, "SL-123")

    @Mock
    private lateinit var stepConfig: UpdateLicensingCyaConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateLicensingCyaConfig(
                licensingDetailsHelper = mockLicensingDetailsHelper,
                propertyOwnershipService = mockPropertyOwnershipService,
                licensingAndTenancyDetailsDataModelFactory = mockLicensingAndTenancyDetailsDataModelFactory,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        stepConfig.urlPath = UpdateLicensingCyaStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockState.licensingTask).thenReturn(mockLicensingTask)
        whenever(mockLicensingAndTenancyDetailsDataModelFactory.fromLicensingState(mockLicensingTask)).thenReturn(licensing)
    }

    @Test
    fun `afterStepDataIsAdded calls updateLicensing with the licensing details from the journey`() {
        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyOwnershipService).updateLicensing(propertyId, licensing, initialLastModifiedDate)
    }

    @Test
    fun `afterStepDataIsAdded sends update emails with the correct updated items`() {
        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockPropertyUpdateEmailService).sendUpdateEmails(eq(propertyId), eq(listOf("The licensing information")))
    }

    @Test
    fun `afterStepDataIsAdded deletes the journey and rethrows when an UpdateConflictException is thrown`() {
        whenever(
            mockPropertyOwnershipService.updateLicensing(propertyId, licensing, initialLastModifiedDate),
        ).thenThrow(UpdateConflictException::class.java)

        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
