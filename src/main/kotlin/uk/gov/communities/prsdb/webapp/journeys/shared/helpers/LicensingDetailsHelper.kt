package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.LicensingState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class LicensingDetailsHelper(
    private val featureFlagManager: FeatureFlagManager,
) {
    fun <T> getCheckYourAnswersSummaryList(
        state: T,
    ): List<SummaryListRowViewModel> where T : LicensingState, T : CheckYourAnswersJourneyState {
        // TODO(PDJB-990): show 'Provide this later' in the licensing CYA row (property registration only) behind the FF: pdjb-939-property-registration-restructure-and-skipping/PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
        return state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType).let { licensingType ->
            listOfNotNull(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    licensingType,
                    Destination.VisitableStep(state.licensingTypeStep, state.getCyaJourneyId(state.licensingTypeStep)),
                ),
                when (licensingType) {
                    LicensingType.HMO_MANDATORY_LICENCE -> (state.getLicenceNumber() to state.hmoMandatoryLicenceStep)
                    LicensingType.HMO_ADDITIONAL_LICENCE -> (state.getLicenceNumber() to state.hmoAdditionalLicenceStep)
                    LicensingType.SELECTIVE_LICENCE -> (state.getLicenceNumber() to state.selectiveLicenceStep)
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
