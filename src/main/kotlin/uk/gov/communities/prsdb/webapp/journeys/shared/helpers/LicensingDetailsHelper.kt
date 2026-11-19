package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class LicensingDetailsHelper {
    fun getCheckYourAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): List<SummaryListRowViewModel> {
        val licensingType = licensingState.getLicensingType()
        val licensingTypeValue: Any =
            if (licensingType == LicensingType.PROVIDE_LATER && licensingState.isOccupied == false) {
                "licensing.checkAnswers.licensingProvideLaterUnoccupied"
            } else {
                licensingType
            }

        return listOfNotNull(
            getLicensingTypeRow(state, licensingState, licensingTypeValue),
            getLicenceNumberRow(state, licensingState, licensingType),
        )
    }

    fun getLicensingCyaContent(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): Map<String, Any?> = mapOf("licensingRows" to getCheckYourAnswersSummaryList(state, licensingState))

    private fun getLicensingTypeRow(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
        licensingTypeValue: Any,
    ) = SummaryListRowViewModel.forCheckYourAnswersPage(
        "propertyDetails.propertyRecord.licensingInformation.licensingType",
        licensingTypeValue,
        Destination.VisitableStep(licensingState.licensingTypeStep, state.getCyaJourneyId(licensingState.licensingTypeStep)),
    )

    private fun getLicenceNumberRow(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
        licensingType: LicensingType,
    ): SummaryListRowViewModel? =
        when (licensingType) {
            LicensingType.HMO_MANDATORY_LICENCE -> licensingState.hmoMandatoryLicenceStep
            LicensingType.HMO_ADDITIONAL_LICENCE -> licensingState.hmoAdditionalLicenceStep
            LicensingType.SELECTIVE_LICENCE -> licensingState.selectiveLicenceStep
            else -> null
        }?.let { step ->
            SummaryListRowViewModel.forCheckYourAnswersPage(
                "propertyDetails.propertyRecord.licensingInformation.licensingNumber",
                licensingState.getLicenceNumber(),
                Destination.VisitableStep(step, state.getCyaJourneyId(step)),
            )
        }
}
