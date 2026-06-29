package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgCompanyNumberFormModel

@JourneyFrameworkComponent
class OrgCompanyNumberStepConfig : AbstractRequestableStepConfig<Complete, OrgCompanyNumberFormModel, JourneyState>() {
    override val formModelClass = OrgCompanyNumberFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.orgCompanyNumber.fieldSetHeading",
            "hint" to "forms.orgCompanyNumber.hint",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgCompanyNumberForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgCompanyNumberStep(
    stepConfig: OrgCompanyNumberStepConfig,
) : RequestableStep<Complete, OrgCompanyNumberFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-company-number"
    }
}
