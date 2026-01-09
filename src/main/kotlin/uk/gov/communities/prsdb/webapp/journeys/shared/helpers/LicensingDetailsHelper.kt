package uk.gov.communities.prsdb.webapp.journeys.shared.helpers

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.constants.enums.LicensingType
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException.Companion.notNullValue
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.LicensingState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LicensingTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel

@PrsdbWebService
class LicensingDetailsHelper {
    fun getCheckYourAnswersSummaryList(
        state: LicensingState,
        childJourneyId: String,
    ): List<SummaryListRowViewModel> =
        state.licensingTypeStep.formModel.notNullValue(LicensingTypeFormModel::licensingType).let { licensingType ->
            listOfNotNull(
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "forms.checkPropertyAnswers.propertyDetails.licensingType",
                    licensingType,
                    Destination.VisitableStep(state.licensingTypeStep, childJourneyId),
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
                        Destination(step),
                    )
                },
            )
        }
}
