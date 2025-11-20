package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.FIND_LOCAL_AUTHORITY_URL
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectLocalAuthorityFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.SelectViewModel
import uk.gov.communities.prsdb.webapp.services.LocalAuthorityService

@Scope("prototype")
@PrsdbWebComponent
class LocalAuthorityStepConfig(
    private val localAuthorityService: LocalAuthorityService,
) : AbstractGenericStepConfig<Complete, SelectLocalAuthorityFormModel, JourneyState>() {
    override val formModelClass = SelectLocalAuthorityFormModel::class

    override fun getStepSpecificContent(state: JourneyState): Map<String, Any?> {
        val localAuthoritiesSelectOptions =
            localAuthorityService.retrieveAllLocalAuthorities().map {
                SelectViewModel(
                    value = it.id,
                    label = it.name,
                )
            }

        return mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.selectLocalAuthority.fieldSetHeading",
            "fieldSetHint" to "forms.selectLocalAuthority.fieldSetHint",
            "selectLabel" to "forms.selectLocalAuthority.select.label",
            "findLocalAuthorityUrl" to FIND_LOCAL_AUTHORITY_URL,
            "selectOptions" to localAuthoritiesSelectOptions,
        )
    }

    override fun chooseTemplate(state: JourneyState): String = "forms/selectLocalAuthorityForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class LocalAuthorityStep(
    stepConfig: LocalAuthorityStepConfig,
) : RequestableStep<Complete, SelectLocalAuthorityFormModel, JourneyState>(stepConfig)
