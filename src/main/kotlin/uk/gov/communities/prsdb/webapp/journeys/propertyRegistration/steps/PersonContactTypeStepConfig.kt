package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PersonContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PersonContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1965, PDJB-1974: replace this placeholder with the real person contact type page.
// Implement the routing logic
@JourneyFrameworkComponent
class PersonContactTypeStepConfig :
    AbstractRequestableStepConfig<PersonContactType, PersonContactTypeFormModel, JourneyState>() {
    override val formModelClass = PersonContactTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "contactType",
            "fieldSetHeading" to "registerProperty.person.contactType.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = PersonContactType.EMAIL,
                        labelMsgKey = "registerProperty.person.contactType.radios.option.email",
                    ),
                    RadiosButtonViewModel(
                        value = PersonContactType.POSTAL_ADDRESS,
                        labelMsgKey = "registerProperty.person.contactType.radios.option.postalAddress",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.contactType
}

@JourneyFrameworkComponent
final class PersonContactTypeStep(
    stepConfig: PersonContactTypeStepConfig,
) : RequestableStep<PersonContactType, PersonContactTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "contact-type"
    }
}
