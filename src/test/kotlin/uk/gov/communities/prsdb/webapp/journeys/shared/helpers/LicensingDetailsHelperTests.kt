package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsViewModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import kotlin.test.assertEquals

class LicensingDetailsHelperTests {
    private val licensingDetailsHelper = LicensingDetailsHelper()

    private val childJourneyId = "childJourneyId"

    @Test
    fun `When there is no licensing, getCheckYourAnswersSummaryList returns only a no licence required row`() {
        // Arrange
        val state = createMockLicensingState(LicensingType.NO_LICENSING, null)

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, state)

        // Assert
        summaryList.single().let { row ->
            assertEquals("propertyDetails.propertyRecord.licensingInformation.licensingType", row.fieldHeading)
            assertEquals(LicensingType.NO_LICENSING, row.fieldValue)
            assertEquals(
                listOf(SummaryListRowActionsViewModel("forms.links.change", "licensing-type?journeyId=$childJourneyId")),
                row.actions,
            )
        }
    }

    @Test
    fun `When there is a licence provided, getCheckYourAnswersSummaryList returns licence type and number rows`() {
        // Arrange
        val licenceNumber = "LIC123456"
        val state = createMockLicensingState(LicensingType.HMO_MANDATORY_LICENCE, licenceNumber)

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, state)

        // Assert
        assertEquals(2, summaryList.size)

        summaryList[0].let { row ->
            assertEquals("propertyDetails.propertyRecord.licensingInformation.licensingType", row.fieldHeading)
            assertEquals(LicensingType.HMO_MANDATORY_LICENCE, row.fieldValue)
            assertEquals(
                listOf(SummaryListRowActionsViewModel("forms.links.change", "licensing-type?journeyId=$childJourneyId")),
                row.actions,
            )
        }

        summaryList[1].let { row ->
            assertEquals("propertyDetails.propertyRecord.licensingInformation.licensingNumber", row.fieldHeading)
            assertEquals(licenceNumber, row.fieldValue)
            assertEquals(
                listOf(SummaryListRowActionsViewModel("forms.links.change", "licence-number?journeyId=$childJourneyId")),
                row.actions,
            )
        }
    }

    @Test
    fun `When an unoccupied property will provide licensing later, getCheckYourAnswersSummaryList uses the unoccupied wording`() {
        // Arrange
        val state = createMockLicensingStateWithSkip()
        whenever(state.isOccupied).thenReturn(false)

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, state)

        // Assert
        assertEquals("licensing.checkAnswers.licensingProvideLaterUnoccupied", summaryList.single().fieldValue)
    }

    @Test
    fun `When an occupied property will provide licensing later, getCheckYourAnswersSummaryList uses the provide later type`() {
        // Arrange
        val state = createMockLicensingStateWithSkip()
        whenever(state.isOccupied).thenReturn(true)

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, state)

        // Assert
        assertEquals(LicensingType.PROVIDE_LATER, summaryList.single().fieldValue)
    }

    @Test
    fun `getLicensingCyaContent returns the check your answers rows as licensingRows`() {
        // Arrange
        val state = createMockLicensingState(LicensingType.NO_LICENSING, null)

        // Act
        val content = licensingDetailsHelper.getLicensingCyaContent(state, state)

        // Assert
        assertEquals(setOf("licensingRows"), content.keys)
        @Suppress("UNCHECKED_CAST")
        val rows = content["licensingRows"] as List<SummaryListRowViewModel>
        assertEquals(LicensingType.NO_LICENSING, rows.single().fieldValue)
    }

    interface TestableLicensingState :
        CheckYourAnswersJourneyState,
        LicensingState,
        JourneyState

    fun createMockLicensingState(
        licenseType: LicensingType,
        licenceNumber: String?,
    ): TestableLicensingState {
        val stateMock = mock<TestableLicensingState>()

        val typeStepMock =
            mock<LicensingTypeStep>().apply {
                whenever(this.urlPath).thenReturn("licensing-type")
                whenever(this.isStepReachable).thenReturn(true)
            }
        whenever(stateMock.licensingTypeStep).thenReturn(typeStepMock)
        whenever(stateMock.getLicensingType()).thenReturn(licenseType)
        whenever(stateMock.getLicenceNumber()).thenReturn(licenceNumber)
        whenever(stateMock.getCyaJourneyId(anyOrNull())).thenReturn(childJourneyId)

        val licenceNumberStepMock =
            when (licenseType) {
                LicensingType.HMO_MANDATORY_LICENCE -> {
                    val stepMock = mock<HmoMandatoryLicenceStep>()
                    whenever(stateMock.hmoMandatoryLicenceStep).thenReturn(stepMock)
                    stepMock
                }

                LicensingType.HMO_ADDITIONAL_LICENCE -> {
                    val stepMock = mock<HmoAdditionalLicenceStep>()
                    whenever(stateMock.hmoAdditionalLicenceStep).thenReturn(stepMock)
                    stepMock
                }

                LicensingType.SELECTIVE_LICENCE -> {
                    val stepMock = mock<SelectiveLicenceStep>()
                    whenever(stateMock.selectiveLicenceStep).thenReturn(stepMock)
                    stepMock
                }

                else -> {
                    return stateMock
                }
            }

        whenever(licenceNumberStepMock.isStepReachable).thenReturn(true)
        whenever(licenceNumberStepMock.urlPath).thenReturn("licence-number")

        return stateMock
    }

    fun createMockLicensingStateWithSkip(): TestableLicensingState {
        val stateMock = mock<TestableLicensingState>()

        val typeStepMock =
            mock<LicensingTypeStep>().apply {
                whenever(this.urlPath).thenReturn("licensing-type")
                whenever(this.isStepReachable).thenReturn(true)
            }
        whenever(stateMock.licensingTypeStep).thenReturn(typeStepMock)
        whenever(stateMock.getLicensingType()).thenReturn(LicensingType.PROVIDE_LATER)
        whenever(stateMock.getCyaJourneyId(anyOrNull())).thenReturn(childJourneyId)

        return stateMock
    }
}
