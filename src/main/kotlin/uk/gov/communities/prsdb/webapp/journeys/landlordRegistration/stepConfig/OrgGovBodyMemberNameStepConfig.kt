package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GoverningBodyMemberNameFormModel

@JourneyFrameworkComponent
class OrgGovBodyMemberNameStepConfig : AbstractRequestableStepConfig<Complete, GoverningBodyMemberNameFormModel, JourneyState>() {
    override val formModelClass = GoverningBodyMemberNameFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "forms.orgGovBodyMemberName.fieldSetHeading",
            "submitButtonText" to "forms.buttons.continue",
        )

    override fun chooseTemplate(state: JourneyState) = "forms/nameForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyMemberNameStep(
    stepConfig: OrgGovBodyMemberNameStepConfig,
) : RequestableStep<Complete, GoverningBodyMemberNameFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-member-name"
    }
}
