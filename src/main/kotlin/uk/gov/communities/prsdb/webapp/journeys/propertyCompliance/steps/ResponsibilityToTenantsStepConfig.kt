package uk.gov.communities.prsdb.webapp.journeys.propertyCompliance.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL
import uk.gov.communities.prsdb.webapp.constants.HOW_TO_RENT_GUIDE_URL
import uk.gov.communities.prsdb.webapp.constants.LANDLORD_RESPONSIBILITIES_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ResponsibilityToTenantsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.CheckboxViewModel

@JourneyFrameworkComponent
class ResponsibilityToTenantsStepConfig : AbstractRequestableStepConfig<Complete, ResponsibilityToTenantsFormModel, JourneyState>() {
    override val formModelClass = ResponsibilityToTenantsFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "title" to "propertyCompliance.title",
            "landlordResponsibilitiesUrl" to LANDLORD_RESPONSIBILITIES_URL,
            "governmentApprovedDepositProtectionSchemeUrl" to GOVERNMENT_APPROVED_DEPOSIT_PROTECTION_SCHEME_URL,
            "howToRentGuideUrl" to HOW_TO_RENT_GUIDE_URL,
            "options" to
                listOf(
                    CheckboxViewModel(
                        value = "true",
                        labelMsgKey = "forms.landlordResponsibilities.responsibilityToTenants.checkbox.label",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/responsibilityToTenantsForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.agreesToResponsibility?.let { Complete.COMPLETE }
}

@JourneyFrameworkComponent
final class ResponsibilityToTenantsStep(
    stepConfig: ResponsibilityToTenantsStepConfig,
) : RequestableStep<Complete, ResponsibilityToTenantsFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "responsibility-to-tenants"
    }
}
