package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeMode
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class LicensingDetailsHelper(
    private val featureFlagManager: FeatureFlagManager,
) {
    fun getCheckYourAnswersSummaryList(
        state: CheckYourAnswersJourneyState,
        licensingState: LicensingState,
    ): List<SummaryListRowViewModel> {
        if (featureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING) &&
            licensingState.licensingTypeStep.outcome == LicensingTypeMode.PROVIDE_LATER
        ) {
            return listOf(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    "forms.checkPropertyAnswers.propertyDetails.licensingProvideLater",
                    Destination.VisitableStep(licensingState.licensingTypeStep, state.getCyaJourneyId(licensingState.licensingTypeStep)),
                ),
            )
        }

        return licensingState.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType).let { licensingType ->
            listOfNotNull(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    licensingType,
                    Destination.VisitableStep(licensingState.licensingTypeStep, state.getCyaJourneyId(licensingState.licensingTypeStep)),
                ),
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
    }
}
