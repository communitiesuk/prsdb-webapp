package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoAdditionalLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HmoMandatoryLicenceStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SelectiveLicenceStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionViewModel
import kotlin.test.assertEquals

class LicensingDetailsHelperTests {
    private val licensingDetailsHelper = LicensingDetailsHelper()

    @Test
    fun `When there is no licensing, getCheckYourAnswersSummaryList returns only a licence type row`() {
        // Arrange
        val state = createMockLicensingState(LicensingType.NO_LICENSING, null)
        val childJourneyId = "childJourneyId"

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, childJourneyId)

        // Assert
        summaryList.single().let { row ->
            assertEquals("forms.checkPropertyAnswers.propertyDetails.licensingType", row.fieldHeading)
            assertEquals(LicensingType.NO_LICENSING, row.fieldValue)
            assertEquals(SummaryListRowActionViewModel("forms.links.change", "licensing-type?journeyId=$childJourneyId"), row.action)
        }
    }

    @Test
    fun `When there is a licence provided, getCheckYourAnswersSummaryList returns both licence type and licence number rows`() {
        // Arrange
        val licenceNumber = "LIC123456"
        val state = createMockLicensingState(LicensingType.HMO_MANDATORY_LICENCE, licenceNumber)
        val childJourneyId = "childJourneyId"

        // Act
        val summaryList = licensingDetailsHelper.getCheckYourAnswersSummaryList(state, childJourneyId)

        // Assert
        assertEquals(2, summaryList.size)

        summaryList[0].let { row ->
            assertEquals("forms.checkPropertyAnswers.propertyDetails.licensingType", row.fieldHeading)
            assertEquals(LicensingType.HMO_MANDATORY_LICENCE, row.fieldValue)
            assertEquals(SummaryListRowActionViewModel("forms.links.change", "licensing-type?journeyId=$childJourneyId"), row.action)
        }

        summaryList[1].let { row ->
            assertEquals("propertyDetails.propertyRecord.licensingInformation.licensingNumber", row.fieldHeading)
            assertEquals(licenceNumber, row.fieldValue)
            assertEquals(SummaryListRowActionViewModel("forms.links.change", "licence-number?journeyId=$childJourneyId"), row.action)
        }
    }

    fun createMockLicensingState(
        licenseType: LicensingType,
        licenceNumber: String?,
    ): LicensingState {
        val stateMock = mock<LicensingState>()

        val typeStepMock =
            mock<LicensingTypeStep>().apply {
                whenever(this.formModel).thenReturn(
                    LicensingTypeFormModel().apply {
                        licensingType = licenseType
                    },
                )
                whenever(this.routeSegment).thenReturn("licensing-type")
                whenever(this.isStepReachable).thenReturn(true)
            }
        whenever(stateMock.licensingTypeStep).thenReturn(typeStepMock)
        whenever(stateMock.getLicenceNumber()).thenReturn(licenceNumber)

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
        whenever(licenceNumberStepMock.routeSegment).thenReturn("licence-number")

        return stateMock
    }
}
