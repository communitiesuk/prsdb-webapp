package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.EpcExemptionReason
import uk.gov.communities.prsdb.webapp.constants.enums.MeesExemptionReason
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcExemptionFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.EpcInDateAtStartOfTenancyCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.IsEpcRequiredFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionCheckFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.MeesExemptionReasonFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EpcRegistrationCyaSummaryRowsFactoryTests {
    private val mockEpcCertificateUrlProvider: EpcCertificateUrlProvider = mock()

    private val mockHasEpcStep: HasEpcStep = mock()
    private val mockEpcAgeCheckStep: EpcAgeCheckStep = mock()
    private val mockEpcEnergyRatingCheckStep: EpcEnergyRatingCheckStep = mock()
    private val mockEpcInDateAtStartOfTenancyCheckStep: EpcInDateAtStartOfTenancyCheckStep = mock()
    private val mockHasMeesExemptionStep: HasMeesExemptionStep = mock()
    private val mockMeesExemptionStep: MeesExemptionStep = mock()
    private val mockLowEnergyRatingStep: LowEnergyRatingStep = mock()
    private val mockIsEpcRequiredStep: IsEpcRequiredStep = mock()
    private val mockEpcExemptionStep: EpcExemptionStep = mock()
    private val mockState: EpcState = mock()

    private val epcUrl = "https://find-energy-certificate.service.gov.uk/energy-certificate/0000-0000-0000-0892-1563"
    private val validEpc = MockEpcData.createEpcDataModel(energyRating = "C")
    private val lowRatingEpc = MockEpcData.createEpcDataModel(energyRating = "F")

    @BeforeEach
    fun setupMocks() {
        whenever(mockState.hasEpcStep).thenReturn(mockHasEpcStep)
        whenever(mockState.epcAgeCheckStep).thenReturn(mockEpcAgeCheckStep)
        whenever(mockState.epcEnergyRatingCheckStep).thenReturn(mockEpcEnergyRatingCheckStep)
        whenever(mockState.epcInDateAtStartOfTenancyCheckStep).thenReturn(mockEpcInDateAtStartOfTenancyCheckStep)
        whenever(mockState.hasMeesExemptionStep).thenReturn(mockHasMeesExemptionStep)
        whenever(mockState.meesExemptionStep).thenReturn(mockMeesExemptionStep)
        whenever(mockState.lowEnergyRatingStep).thenReturn(mockLowEnergyRatingStep)
        whenever(mockState.isEpcRequiredStep).thenReturn(mockIsEpcRequiredStep)
        whenever(mockState.epcExemptionStep).thenReturn(mockEpcExemptionStep)

        whenever(mockHasEpcStep.routeSegment).thenReturn(HasEpcStep.ROUTE_SEGMENT)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.routeSegment).thenReturn(EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT)
        whenever(mockHasMeesExemptionStep.routeSegment).thenReturn(HasMeesExemptionStep.ROUTE_SEGMENT)
        whenever(mockMeesExemptionStep.routeSegment).thenReturn(MeesExemptionStep.ROUTE_SEGMENT)
        whenever(mockIsEpcRequiredStep.routeSegment).thenReturn(IsEpcRequiredStep.ROUTE_SEGMENT)
        whenever(mockEpcExemptionStep.routeSegment).thenReturn(EpcExemptionStep.ROUTE_SEGMENT)
        whenever(mockState.journeyId).thenReturn("")

        whenever(mockHasEpcStep.currentJourneyId).thenReturn("")
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.currentJourneyId).thenReturn("")
        whenever(mockHasMeesExemptionStep.currentJourneyId).thenReturn("")
        whenever(mockMeesExemptionStep.currentJourneyId).thenReturn("")
        whenever(mockIsEpcRequiredStep.currentJourneyId).thenReturn("")
        whenever(mockEpcExemptionStep.currentJourneyId).thenReturn("")

        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.HAS_EPC)
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER)
        whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(null)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(null)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(false)
        whenever(mockMeesExemptionStep.isStepReachable).thenReturn(false)
        whenever(mockLowEnergyRatingStep.isStepReachable).thenReturn(false)
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(false)
        whenever(mockEpcExemptionStep.isStepReachable).thenReturn(false)
        whenever(mockState.isOccupied).thenReturn(true)
        whenever(mockState.acceptedEpc).thenReturn(null)
    }

    @Test
    fun `createEpcCardTitle returns heading key when user has an accepted EPC`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.yourEpc", title)
    }

    @Test
    fun `createEpcCardTitle returns null when there is no accepted EPC`() {
        // Arrange -- acceptedEpc is null (default)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when hasEpcStep outcome is PROVIDE_LATER`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when hasEpcStep outcome is NO_EPC`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when EPC is expired and property is unoccupied`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardTitle returns null when EPC has low rating, no MEES exemption, and property is unoccupied`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        whenever(mockMeesExemptionStep.isStepReachable).thenReturn(false)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val title = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardTitle()

        // Assert
        assertNull(title)
    }

    @Test
    fun `createEpcCardRows returns rows for unexpired compliant EPC`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(validEpc.certificateNumber)).thenReturn(epcUrl)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.address",
                    validEpc.singleLineAddress,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.energyRating",
                    validEpc.energyRatingUppercase,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.expiryDate",
                    validEpc.expiryDateAsJavaLocalDate,
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.certificateNumber",
                    validEpc.certificateNumber,
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createEpcCardRows returns null when there is no accepted EPC`() {
        // Arrange -- acceptedEpc is null (default)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardRows()

        // Assert
        assertNull(rows)
    }

    @Test
    fun `createEpcCardActions returns view action when card is visible`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcCertificateUrlProvider.getEpcCertificateUrl(validEpc.certificateNumber)).thenReturn(epcUrl)

        // Act
        val actions = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardActions()

        // Assert
        assertEquals("propertyCompliance.epcTask.checkEpcAnswers.epc.viewFullEpc", actions?.first()?.text)
        assertEquals(epcUrl, actions?.first()?.url)
        assertTrue(actions?.first()?.opensInNewTab ?: false)
    }

    @Test
    fun `createEpcCardActions returns null when card is hidden`() {
        // Arrange -- acceptedEpc is null (default)

        // Act
        val actions = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createEpcCardActions()

        // Assert
        assertNull(actions)
    }

    @Test
    fun `showEpcExpiredText returns true when EPC is expired and property is occupied`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showEpcExpiredText()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showEpcExpiredText returns false when EPC is compliant`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showEpcExpiredText()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showEpcExpiredText returns false when EPC is expired but card is hidden (unoccupied)`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showEpcExpiredText()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createTenancyCheckRows returns tenancy row when EPC was in date at start of tenancy`() {
        // Arrange
        val formModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = true }
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(formModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.tenancyStartCheck",
                    formModel.tenancyStartedBeforeExpiry,
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createTenancyCheckRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createTenancyCheckRows returns tenancy row when EPC was not in date at start of tenancy`() {
        // Arrange
        val formModel = EpcInDateAtStartOfTenancyCheckFormModel().apply { tenancyStartedBeforeExpiry = false }
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.formModelIfReachableOrNull).thenReturn(formModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.tenancyStartCheck",
                    formModel.tenancyStartedBeforeExpiry,
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createTenancyCheckRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createTenancyCheckRows returns empty list when form model is null`() {
        // Arrange -- formModelIfReachableOrNull is null (default)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createTenancyCheckRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `showMeetsRequirementsInset returns true when EPC is compliant`() {
        // Arrange -- EPC_COMPLIANT is set in @BeforeEach

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showMeetsRequirementsInset()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showMeetsRequirementsInset returns true when EPC was in date at tenancy start`() {
        // Arrange
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.IN_DATE)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showMeetsRequirementsInset()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showMeetsRequirementsInset returns false when EPC is expired and was not in date at tenancy start`() {
        // Arrange
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showMeetsRequirementsInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showMeetsRequirementsInset returns false when EPC has low energy rating`() {
        // Arrange
        whenever(mockEpcEnergyRatingCheckStep.outcome).thenReturn(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showMeetsRequirementsInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showLowRatingText returns true when hasMeesExemptionStep is reachable and card is visible`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingText()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showLowRatingText returns false when hasMeesExemptionStep is not reachable`() {
        // Arrange -- hasMeesExemptionStep.isStepReachable = false (default)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingText()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showLowRatingText returns false when hasMeesExemptionStep is reachable but card is hidden (unoccupied, no MEES exemption)`() {
        // Arrange
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        whenever(mockHasMeesExemptionStep.formModelIfReachableOrNull).thenReturn(
            MeesExemptionCheckFormModel().apply { propertyHasExemption = false },
        )
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingText()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createAdditionalRows returns empty list when hasMeesExemptionStep is not reachable`() {
        // Arrange -- hasMeesExemptionStep.isStepReachable = false (default)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createAdditionalRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `createAdditionalRows returns MEES exemption check row when hasMeesExemptionStep is reachable and no exemption`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        val hasMeesExemptionFormModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = false }
        whenever(mockHasMeesExemptionStep.formModelIfReachableOrNull).thenReturn(hasMeesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                    false,
                    Destination.VisitableStep(mockHasMeesExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createAdditionalRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createAdditionalRows returns MEES exemption check and exemption type rows when exemption is registered`() {
        // Arrange
        val exemptionReason = MeesExemptionReason.THIRD_PARTY_CONSENT
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        val hasMeesExemptionFormModel = MeesExemptionCheckFormModel().apply { propertyHasExemption = true }
        whenever(mockHasMeesExemptionStep.formModelIfReachableOrNull).thenReturn(hasMeesExemptionFormModel)
        whenever(mockMeesExemptionStep.isStepReachable).thenReturn(true)
        val meesExemptionFormModel = MeesExemptionReasonFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockMeesExemptionStep.formModelIfReachableOrNull).thenReturn(meesExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemptionCheck",
                    true,
                    Destination.VisitableStep(mockHasMeesExemptionStep, ""),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epc.meesExemption",
                    exemptionReason,
                    Destination.VisitableStep(mockMeesExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createAdditionalRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `showLowRatingOccupiedInset returns true when lowEnergyRatingStep is reachable and property is occupied`() {
        // Arrange
        whenever(mockLowEnergyRatingStep.isStepReachable).thenReturn(true)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingOccupiedInset()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showLowRatingOccupiedInset returns false when lowEnergyRatingStep is reachable but property is unoccupied`() {
        // Arrange
        whenever(mockLowEnergyRatingStep.isStepReachable).thenReturn(true)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingOccupiedInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showLowRatingOccupiedInset returns true when EPC is expired, not in date at tenancy start, and property is occupied`() {
        // Arrange
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingOccupiedInset()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showLowRatingOccupiedInset returns false when EPC is expired, not in date at tenancy start, and property is unoccupied`() {
        // Arrange
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockEpcInDateAtStartOfTenancyCheckStep.outcome).thenReturn(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingOccupiedInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showLowRatingOccupiedInset returns false when EPC is compliant`() {
        // Arrange -- EPC_COMPLIANT and tenancyCheck null (defaults)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showLowRatingOccupiedInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterOccupied when outcome is PROVIDE_LATER and property is occupied`() {
        // Arrange
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterOccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when outcome is PROVIDE_LATER and property is unoccupied`() {
        // Arrange
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.PROVIDE_LATER)
        whenever(mockState.isOccupied).thenReturn(false)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when EPC is expired and property is unoccupied`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)
        whenever(mockEpcAgeCheckStep.outcome).thenReturn(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
        whenever(mockState.isOccupied).thenReturn(false)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when EPC has low rating, no MEES exemption, and property is unoccupied`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(lowRatingEpc)
        whenever(mockHasMeesExemptionStep.isStepReachable).thenReturn(true)
        whenever(mockMeesExemptionStep.isStepReachable).thenReturn(false)
        whenever(mockState.isOccupied).thenReturn(false)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc and isEpcRequired rows when property has no EPC, is occupied, and EPC is required`() {
        // Arrange
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
        val isEpcRequiredFormModel = IsEpcRequiredFormModel().apply { epcRequired = true }
        whenever(mockIsEpcRequiredStep.formModelIfReachableOrNull).thenReturn(isEpcRequiredFormModel)
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.YES)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "commonText.no",
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    true,
                    Destination.VisitableStep(mockIsEpcRequiredStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns provideEpcLaterUnoccupied when property has no EPC, is unoccupied, and EPC is required`() {
        // Arrange
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)
        whenever(mockState.isOccupied).thenReturn(false)
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
        val isEpcRequiredFormModel = IsEpcRequiredFormModel().apply { epcRequired = true }
        whenever(mockIsEpcRequiredStep.formModelIfReachableOrNull).thenReturn(isEpcRequiredFormModel)
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.YES)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.provideEpcLaterUnoccupied",
                    null as String?,
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns hasEpc, isEpcRequired, and epcExemption rows when EPC is not required and exemption is registered`() {
        // Arrange
        val exemptionReason = EpcExemptionReason.PROTECTED_ARCHITECTURAL_OR_HISTORICAL_MERIT
        whenever(mockHasEpcStep.outcome).thenReturn(HasEpcMode.NO_EPC)
        whenever(mockIsEpcRequiredStep.isStepReachable).thenReturn(true)
        val isEpcRequiredFormModel = IsEpcRequiredFormModel().apply { epcRequired = false }
        whenever(mockIsEpcRequiredStep.formModelIfReachableOrNull).thenReturn(isEpcRequiredFormModel)
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.NO)
        whenever(mockEpcExemptionStep.isStepReachable).thenReturn(true)
        val epcExemptionFormModel = EpcExemptionFormModel().apply { this.exemptionReason = exemptionReason }
        whenever(mockEpcExemptionStep.formModelIfReachableOrNull).thenReturn(epcExemptionFormModel)

        val expectedRows =
            listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.hasEpc.label",
                    "commonText.no",
                    null as String?,
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.isEpcRequired",
                    false,
                    Destination.VisitableStep(mockIsEpcRequiredStep, ""),
                ),
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "propertyCompliance.epcTask.checkEpcAnswers.epcExemption",
                    exemptionReason,
                    Destination.VisitableStep(mockEpcExemptionStep, ""),
                ),
            )

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(expectedRows, rows)
    }

    @Test
    fun `createNonEpcRows returns empty list when EPC card is visible`() {
        // Arrange
        whenever(mockState.acceptedEpc).thenReturn(validEpc)

        // Act
        val rows = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).createNonEpcRows()

        // Assert
        assertEquals(emptyList(), rows)
    }

    @Test
    fun `showOccupiedNoEpcInset returns true when EPC is required and property is occupied`() {
        // Arrange
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.YES)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showOccupiedNoEpcInset()

        // Assert
        assertTrue(result)
    }

    @Test
    fun `showOccupiedNoEpcInset returns false when EPC is required but property is unoccupied`() {
        // Arrange
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.YES)
        whenever(mockState.isOccupied).thenReturn(false)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showOccupiedNoEpcInset()

        // Assert
        assertFalse(result)
    }

    @Test
    fun `showOccupiedNoEpcInset returns false when EPC is not required`() {
        // Arrange
        whenever(mockIsEpcRequiredStep.outcome).thenReturn(YesOrNo.NO)

        // Act
        val result = EpcRegistrationCyaSummaryRowsFactory(mockEpcCertificateUrlProvider, mockState).showOccupiedNoEpcInset()

        // Assert
        assertFalse(result)
    }
}
