package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.EPC_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.MEES_EXEMPTION_GUIDE_URL
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.CheckMatchedEpcFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosViewModel
import uk.gov.communities.prsdb.webapp.services.EpcCertificateUrlProvider

@JourneyFrameworkComponent
class ConfirmEpcDetailsRetrievedByUprnStepConfig(
    private val epcCertificateUrlProvider: EpcCertificateUrlProvider,
) : AbstractConfirmEpcDetailsStepConfig() {
    init {
        usingEpc { epcRetrievedByUprn }
    }

    override fun getStepSpecificContent(state: EpcState) =
        getRelevantEpc(state)?.let { epcDetails ->
            mapOf(
                "epcDetails" to epcDetails,
                "epcCertificateUrl" to epcCertificateUrlProvider.getEpcCertificateUrl(epcDetails.certificateNumber),
                "whenYouCanRegisterAnExemptionUrl" to MEES_EXEMPTION_GUIDE_URL,
                "epcGuideUrl" to EPC_GUIDE_URL,
                "radioOptions" to
                    RadiosViewModel.yesOrNoRadios(noLabel = "propertyCompliance.epcTask.confirmEpcDetailsFromUprn.radios.no.label"),
            )
        } ?: throw NotNullFormModelValueIsNullException(
            "Attempting to access relevantEpc for ConfirmEpcDetailsRetrievedByUprnStepConfig but it was null.",
        )

    override fun chooseTemplate(state: EpcState): String = "forms/confirmEpcDetailsByUprnForm"
}

@JourneyFrameworkComponent
final class ConfirmEpcDetailsRetrievedByUprnStep(
    stepConfig: ConfirmEpcDetailsRetrievedByUprnStepConfig,
) : JourneyStep.RequestableStep<YesOrNo, CheckMatchedEpcFormModel, EpcState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-matched-epc"
    }
}
