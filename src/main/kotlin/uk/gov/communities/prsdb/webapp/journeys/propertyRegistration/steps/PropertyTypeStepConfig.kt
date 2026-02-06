package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.PropertyType
import uk.gov.communities.prsdb.webapp.database.entity.SavedJourneyState
import uk.gov.communities.prsdb.webapp.journeys.AbstractRequestableStepConfig
import uk.gov.communities.prsdb.webapp.journeys.JourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStep.RequestableStep
import uk.gov.communities.prsdb.webapp.journeys.shared.Complete
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.formModels.RadiosButtonViewModel
import uk.gov.communities.prsdb.webapp.services.IncompletePropertyForLandlordService

@JourneyFrameworkComponent
class PropertyTypeStepConfig(
    private val incompletePropertyForLandlordService: IncompletePropertyForLandlordService,
) : AbstractRequestableStepConfig<Complete, PropertyTypeFormModel, JourneyState>() {
    override val formModelClass = PropertyTypeFormModel::class

    override fun getStepSpecificContent(state: JourneyState) =
        mapOf(
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

    override fun afterSaveState(
        state: JourneyState,
        saveStateId: SavedJourneyState,
    ) {
        incompletePropertyForLandlordService.addIncompletePropertyToLandlord(saveStateId)
    }
}

@JourneyFrameworkComponent
final class PropertyTypeStep(
    stepConfig: PropertyTypeStepConfig,
) : RequestableStep<Complete, PropertyTypeFormModel, JourneyState>(stepConfig)
