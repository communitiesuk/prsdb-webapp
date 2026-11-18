package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class LicensingDetailsHelper {
    fun getCheckYourAnswersSummaryListWithoutProvideLater(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): List<SummaryListRowViewModel> =
        licensingState.getLicensingType().let { licensingType ->
            listOfNotNull(
                getLicensingTypeRow(state, licensingState, licensingType),
                when (licensingType) {
                    LicensingType.HMO_MANDATORY_LICENCE -> (licensingState.getLicenceNumber() to licensingState.hmoMandatoryLicenceStep)
                    LicensingType.HMO_ADDITIONAL_LICENCE -> (licensingState.getLicenceNumber() to licensingState.hmoAdditionalLicenceStep)
                    LicensingType.SELECTIVE_LICENCE -> (licensingState.getLicenceNumber() to licensingState.selectiveLicenceStep)
                    else -> null
                }?.let { (licenceNumber, step) ->
                    SummaryListRowViewModel.forCheckYourAnswersPage(
                        "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                        licenceNumber,
                        Destination.VisitableStep(step, state.getCyaJourneyId(step)),
                    )
                },
            )
        }

    fun getCheckYourAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): List<SummaryListRowViewModel> {
        val licensingType = licensingState.getLicensingType()

        if (licensingType == LicensingType.NO_LICENSING) {
            return listOf(getLicensingTypeRow(state, licensingState, "licensing.checkAnswers.noLicensingRequired"))
        }

        if (licensingState.isOccupied == false && licensingType == LicensingType.PROVIDE_LATER) {
            return listOf(getLicensingTypeRow(state, licensingState, "licensing.checkAnswers.licensingProvideLaterUnoccupied"))
        }

        return getCheckYourAnswersSummaryListWithoutProvideLater(state, licensingState)
    }

    fun getLicensingCyaContent(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): Map<String, Any?> = mapOf("licensingRows" to getCheckYourAnswersSummaryListWithoutProvideLater(state, licensingState))

    private fun getLicensingTypeRow(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
        licensingTypeValue: Any,
    ) = SummaryListRowViewModel.forCheckYourAnswersPage(
        "licensing.checkAnswers.licensingType",
        licensingTypeValue,
        Destination.VisitableStep(licensingState.licensingTypeStep, state.getCyaJourneyId(licensingState.licensingTypeStep)),
    )
}
