package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel

@Scope("prototype")
@PrsdbWebComponent
class SelectiveLicenceStepConfig : AbstractGenericStepConfig<Complete, SelectiveLicenceFormModel, JourneyState>() {
    override val formModelClass = SelectiveLicenceFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.selectiveLicence.fieldSetHeading",
            "label" to "forms.selectiveLicence.label",
            "detailSummary" to "forms.selectiveLicence.detail.summary",
            "detailMainText" to "forms.selectiveLicence.detail.text",
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/licenceNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class SelectiveLicenceStep(
    stepConfig: SelectiveLicenceStepConfig,
) : RequestableStep<Complete, SelectiveLicenceFormModel, JourneyState>(stepConfig)
