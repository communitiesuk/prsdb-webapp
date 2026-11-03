package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.FreeholderContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.FreeholderContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1965: replace this placeholder with the real freeholder contact type page.
// Implement the routing logic
// TODO PDJB-1965: when changing a freeholder, prefill this page with the contact type from
//  state.editingFreeholder by overriding resolvePageContent, as in OrgGovBodyMemberNameStepConfig.
//  Check when adding a second freeholder that the page starts empty, as SaveFreeholderStepConfig clears the form data after each save.
@JourneyFrameworkComponent
class FreeholderContactTypeStepConfig :
    AbstractRequestableStepConfig<FreeholderContactType, FreeholderContactTypeFormModel, JourneyState>() {
    override val formModelClass = FreeholderContactTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "contactType",
            "fieldSetHeading" to "registerProperty.freeholders.contactType.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = FreeholderContactType.EMAIL,
                        labelMsgKey = "registerProperty.freeholders.contactType.radios.option.email",
                    ),
                    RadiosButtonViewModel(
                        value = FreeholderContactType.POSTAL_ADDRESS,
                        labelMsgKey = "registerProperty.freeholders.contactType.radios.option.postalAddress",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.contactType
}

@JourneyFrameworkComponent
final class FreeholderContactTypeStep(
    stepConfig: FreeholderContactTypeStepConfig,
) : RequestableStep<FreeholderContactType, FreeholderContactTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "freeholder-contact-type"
    }
}
