package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.GasSafetyState
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowActionsInputWithDestination
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.CollectionKeyParameterService

// TODO PDJB-635: Implement Check Gas Cert Uploads page
@JourneyFrameworkComponent
class CheckGasCertUploadsStepConfig(
    private val memberIdService: CollectionKeyParameterService,
) : AbstractRequestableStepConfig<Complete, NoInputFormModel, GasSafetyState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: GasSafetyState) =
        mapOf(
            "addAnotherTitle" to "uploads.checkUploads.heading",
            "optionalAddAnotherTitleParam" to getJointLandlordsCount(state),
            "summaryText" to "uploads.checkUploads.paragraph",
            "showWarning" to false,
            "submitButtonText" to "forms.buttons.saveAndContinue",
            "addAnotherButtonText" to "uploads.checkUploads.buttons.addAnother",
            "summaryListData" to getEmailRows(state),
            "addAnotherUrl" to Destination(state.uploadGasCertStep).toUrlStringOrNull(),
        )

    private fun getEmailRows(state: GasSafetyState): List<SummaryListRowViewModel> {
        val gasSafetyUploads = state.gasUploadMap ?: emptyMap()
        return gasSafetyUploads
            .toList()
            .sortedBy { it.first }
            .mapIndexed { displayIndex, (internalIndex, upload) ->
                SummaryListRowViewModel.forCheckYourAnswersPage(
                    "jointLandlords.checkJointLandlords.invitedEmailAddress",
                    upload.fileName,
                    actions =
                        listOf(
                            SummaryListRowActionsInputWithDestination(
                                text = "forms.links.remove",
                                destination =
                                    Destination(
                                        state.removeGasCertUploadStep,
                                    ).withUrlParameter(memberIdService.createParameterPair(internalIndex)),
                            ),
                        ),
                    optionalFieldHeadingParam = displayIndex + 1,
                )
            }
    }

    override fun chooseTemplate(state: GasSafetyState): String = "forms/addAnotherForm"

    override fun mode(state: GasSafetyState) = state.gasUploadMap?.let { if (it.isNotEmpty()) Complete.COMPLETE else null }

    private fun getJointLandlordsCount(state: GasSafetyState): Int = getEmailRows(state).size
}

@JourneyFrameworkComponent
final class CheckGasCertUploadsStep(
    stepConfig: CheckGasCertUploadsStepConfig,
) : RequestableStep<Complete, NoInputFormModel, GasSafetyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "check-gas-safety-certificate-uploads"
    }
}
