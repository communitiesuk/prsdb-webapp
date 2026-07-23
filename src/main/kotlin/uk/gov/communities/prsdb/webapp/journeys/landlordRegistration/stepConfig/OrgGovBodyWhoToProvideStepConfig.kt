package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyWhoToProvideFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@JourneyFrameworkComponent
class OrgGovBodyWhoToProvideStepConfig : AbstractRequestableStepConfig<Complete, OrgGovBodyWhoToProvideFormModel, JourneyState>() {
    override val formModelClass = OrgGovBodyWhoToProvideFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "fieldSetHeading" to "registerAsALandlord.orgGovBodyWhoToProvide.fieldSetHeading",
            "fieldName" to "whoToProvide",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.DIRECTOR,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.director",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.TRUSTEE,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.trustee",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.PARTNER,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.partner",
                    ),
                    RadiosButtonViewModel(
                        value = GoverningBodyMemberType.OTHER,
                        labelMsgKey = "registerAsALandlord.orgGovBodyWhoToProvide.radios.otherMember",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/orgGovBodyWhoToProvideForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.whoToProvide?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class OrgGovBodyWhoToProvideStep(
    stepConfig: OrgGovBodyWhoToProvideStepConfig,
) : RequestableStep<Complete, OrgGovBodyWhoToProvideFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "organisation-governing-body-who-to-provide"
    }
}
