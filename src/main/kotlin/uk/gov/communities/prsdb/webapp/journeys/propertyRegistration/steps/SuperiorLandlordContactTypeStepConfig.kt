package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SuperiorLandlordContactTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

// TODO PDJB-1974: replace this placeholder with the real superior landlord contact type page.
// Implement the routing logic
// TODO PDJB-1974: when changing a superior landlord, prefill this page with the contact type from
//  state.editingSuperiorLandlord by overriding resolvePageContent, as in FreeholderContactTypeStepConfig.
//  Check when adding a second superior landlord that the page starts empty, as SaveSuperiorLandlordStepConfig clears the
//  form data after each save.
@JourneyFrameworkComponent
class SuperiorLandlordContactTypeStepConfig :
    AbstractRequestableStepConfig<SuperiorLandlordContactType, SuperiorLandlordContactTypeFormModel, JourneyState>() {
    override val formModelClass = SuperiorLandlordContactTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> =
        mapOf(
            "fieldName" to "contactType",
            "fieldSetHeading" to "registerProperty.superiorLandlords.contactType.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = SuperiorLandlordContactType.EMAIL,
                        labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.email",
                    ),
                    RadiosButtonViewModel(
                        value = SuperiorLandlordContactType.POSTAL_ADDRESS,
                        labelMsgKey = "registerProperty.superiorLandlords.contactType.radios.option.postalAddress",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState) = "forms/todoWithRadios"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.contactType
}

@JourneyFrameworkComponent
final class SuperiorLandlordContactTypeStep(
    stepConfig: SuperiorLandlordContactTypeStepConfig,
) : RequestableStep<SuperiorLandlordContactType, SuperiorLandlordContactTypeFormModel, JourneyState>(stepConfig) {
    companion object {
        const val ROUTE_SEGMENT = "superior-landlord-contact-type"
    }
}
