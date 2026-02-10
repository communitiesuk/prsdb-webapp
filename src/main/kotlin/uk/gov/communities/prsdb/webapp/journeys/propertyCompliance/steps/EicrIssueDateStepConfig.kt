package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.states.EicrState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.TodayOrPastDateFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService

@JourneyFrameworkComponent
class EicrIssueDateStepConfig(
    private val propertyOwnershipService: PropertyOwnershipService,
) : AbstractRequestableStepConfig<EicrIssueDateMode, TodayOrPastDateFormModel, EicrState>() {
    override val formModelClass = TodayOrPastDateFormModel::class

    override fun getStepSpecificContent(state: EicrState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "fieldSetHeading" to "forms.todayOrPastDate.eicr.fieldSetHeading",
            "fieldSetHint" to "forms.todayOrPastDate.eicr.fieldSetHint",
            "submitButtonText" to "forms.buttons.saveAndContinue",
        )

    override fun chooseTemplate(state: EicrState): String = "forms/certificateForm"

    // TODO PDJB-467 - update this when we have done state.getEicrCertificateIsOutdated()
    override fun mode(state: EicrState) =
        getFormModelFromStateOrNull(state)?.isValidDate()?.let { EicrIssueDateMode.EICR_CERTIFICATE_IN_DATE }
}

@JourneyFrameworkComponent
final class EicrIssueDateStep(
    stepConfig: EicrIssueDateStepConfig,
) : RequestableStep<EicrIssueDateMode, TodayOrPastDateFormModel, EicrState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "eicr"
    }
}

enum class EicrIssueDateMode {
    EICR_CERTIFICATE_OUTDATED,
    EICR_CERTIFICATE_IN_DATE,
}
