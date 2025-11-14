package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterPropertyController
import uk.gov.communities.prsdb.webapp.forms.steps.RegisterPropertyStepId
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.AddressState
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NoInputFormModel

@Scope("prototype")
@PrsdbWebComponent
class AlreadyRegisteredStepConfig : AbstractGenericStepConfig<Nothing, NoInputFormModel, AddressState>() {
    override val formModelClass = NoInputFormModel::class

    override fun getStepSpecificContent(state: AddressState): Map<String, Any?> =
        mapOf(
            "title" to "registerProperty.title",
            "searchAgainUrl" to
                "${RegisterPropertyController.PROPERTY_REGISTRATION_ROUTE}/" +
                RegisterPropertyStepId.LookupAddress.urlPathSegment,
            "singleLineAddress" to state.selectAddressStep.formModel.address,
        )

    override fun chooseTemplate(state: AddressState): String = "alreadyRegisteredPropertyPage"

    override fun mode(state: AddressState): Nothing? = null
}

@Scope("prototype")
@PrsdbWebComponent
final class AlreadyRegisteredStep(
    stepConfig: AlreadyRegisteredStepConfig,
) : RequestableStep<Nothing, NoInputFormModel, AddressState>(stepConfig)
