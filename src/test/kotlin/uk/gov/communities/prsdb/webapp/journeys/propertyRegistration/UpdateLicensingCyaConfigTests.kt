package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingCyaStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.updateLicensing.UpdateLicensingJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.emailModels.PropertyUpdateConfirmation
import uk.gov.communities.prsdb.webapp.services.AbsoluteUrlProvider
import uk.gov.communities.prsdb.webapp.services.EmailNotificationService
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.net.URI

@ExtendWith(MockitoExtension::class)
class UpdateLicensingCyaConfigTests {
    @Mock
    private lateinit var mockLicensingDetailsHelper: LicensingDetailsHelper

    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockEmailNotificationService: EmailNotificationService<PropertyUpdateConfirmation>

    @Mock
    private lateinit var mockAbsoluteUrlProvider: AbsoluteUrlProvider

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
                updateConfirmationEmailService = mockEmailNotificationService,
                absoluteUrlProvider = mockAbsoluteUrlProvider,
            )
        stepConfig.routeSegment = UpdateLicensingCyaStep.ROUTE_SEGMENT
        stepConfig.validator = AlwaysTrueValidator()
        stepConfig.afterStepIsReached(mockState)
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.licensingTypeStep).thenReturn(mockLicensingTypeStep)
        whenever(mockLicensingTypeStep.formModel).thenReturn(mockLicensingTypeFormModel)
        whenever(mockLicensingTypeFormModel.licensingType).thenReturn(LicensingType.NO_LICENSING)
        whenever(mockState.getLicenceNumberOrNull()).thenReturn(null)
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email to primary landlord`() {
        val landlordEmail = "landlord@example.com"
        val landlord = MockLandlordData.createLandlord(email = landlordEmail)
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId, primaryLandlord = landlord)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id)).thenReturn(URI("http://example.com"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(eq(landlordEmail), any<PropertyUpdateConfirmation>())
    }

    @Test
    fun `afterStepDataIsAdded sends confirmation email with correct updated items`() {
        val propertyOwnership = MockLandlordData.createPropertyOwnership(id = propertyId)
        whenever(mockPropertyOwnershipService.getPropertyOwnership(propertyId)).thenReturn(propertyOwnership)
        whenever(mockAbsoluteUrlProvider.buildComplianceInformationUri(propertyOwnership.id)).thenReturn(URI("http://example.com"))

        stepConfig.afterStepDataIsAdded(mockState)

        verify(mockEmailNotificationService).sendEmail(
            any(),
            argThat<PropertyUpdateConfirmation> { this.updatedItems == "The licensing information" },
        )
    }
}
