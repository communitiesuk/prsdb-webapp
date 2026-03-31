package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep.Companion.ROUTE_SEGMENT
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractConfirmEpcDetailsStepConfig() {
    init {
        usingEpc { epcRetrievedByCertificateNumber }
    }

    // TODO PDJB-746 - update content as required
    override fun getStepSpecificContent(state: EpcState) =
        getRelevantEpc(state)?.let { epcDetails ->
            mapOf(
                "epcDetails" to epcDetails,
                "epcCertificateUrl" to epcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber),
                "radioOptions" to RadiosViewModel.yesOrNoRadios(),
            )
        } ?: throw NotNullFormModelValueIsNullException(
            "Attempting to access relevantEpc for ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig but it was null.",
        )

    // TODO PDJB-746 - switch this to a new template if required
    override fun chooseTemplate(state: EpcState): String = "forms/checkMatchedEpcForm"

    override fun afterStepIsReached(state: EpcState) {
        if (state.epcRetrievedByCertificateNumberUpdatedSinceUserReview == true) {
            state.clearStepData(ROUTE_SEGMENT)
            state.epcRetrievedByCertificateNumberUpdatedSinceUserReview = false
        }
    }
}

@JourneyFrameworkComponent
final class ConfirmEpcDetailsRetrievedByCertificateNumberStep(
    stepConfig: ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig,
) : JourneyStep.RequestableStep<YesOrNo, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "confirm-epc-details"
    }
}
