package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectiveLicenceFormModel

@JourneyFrameworkComponent
class SelectiveLicenceStepConfig : AbstractGenericRequestableStepConfig<Complete, SelectiveLicenceFormModel, JourneyState>() {
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

@JourneyFrameworkComponent
final class SelectiveLicenceStep(
    stepConfig: SelectiveLicenceStepConfig,
) : RequestableStep<Complete, SelectiveLicenceFormModel, JourneyState>(stepConfig)
