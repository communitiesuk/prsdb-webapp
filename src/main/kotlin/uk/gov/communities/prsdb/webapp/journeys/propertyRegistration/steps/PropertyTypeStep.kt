package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.springframework.context.annotation.Scope
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.journeys.AbstractGenericStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.example.steps.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel

@Scope("prototype")
@PrsdbWebComponent
class PropertyTypeStepConfig : AbstractGenericStepConfig<Complete, PropertyTypeFormModel, JourneyState>() {
    override val formModelClass = PropertyTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
            "title" to "registerProperty.title",
            "fieldSetHeading" to "forms.propertyType.fieldSetHeading",
            "radioOptions" to
                listOf(
                    RadiosButtonViewModel(
                        value = PropertyType.DETACHED_HOUSE,
                        labelMsgKey = "forms.propertyType.radios.option.detachedHouse.label",
                        hintMsgKey = "forms.propertyType.radios.option.detachedHouse.hint",
                    ),
                    RadiosButtonViewModel(
                        value = PropertyType.SEMI_DETACHED_HOUSE,
                        labelMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.label",
                        hintMsgKey = "forms.propertyType.radios.option.semiDetachedHouse.hint",
                    ),
                    RadiosButtonViewModel(
                        value = PropertyType.TERRACED_HOUSE,
                        labelMsgKey = "forms.propertyType.radios.option.terracedHouse.label",
                        hintMsgKey = "forms.propertyType.radios.option.terracedHouse.hint",
                    ),
                    RadiosButtonViewModel(
                        value = PropertyType.FLAT,
                        labelMsgKey = "forms.propertyType.radios.option.flat.label",
                        hintMsgKey = "forms.propertyType.radios.option.flat.hint",
                    ),
                    RadiosButtonViewModel(
                        value = PropertyType.OTHER,
                        labelMsgKey = "forms.propertyType.radios.option.other.label",
                        hintMsgKey = "forms.propertyType.radios.option.other.hint",
                        conditionalFragment = "customPropertyTypeInput",
                    ),
                ),
        )

    override fun chooseTemplate(state: JourneyState): String = "forms/propertyTypeForm"

    override fun mode(state: JourneyState) = getFormModelFromStateOrNull(state)?.let { Complete.COMPLETE }
}

@Scope("prototype")
@PrsdbWebComponent
final class PropertyTypeStep(
    stepConfig: PropertyTypeStepConfig,
) : RequestableStep<Complete, PropertyTypeFormModel, JourneyState>(stepConfig)
