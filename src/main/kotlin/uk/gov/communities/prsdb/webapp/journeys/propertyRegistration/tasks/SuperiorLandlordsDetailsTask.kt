package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.constants.enums.SuperiorLandlordContactType
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.SingleParent
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsDependencies
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.SuperiorLandlordsDetailsState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmRemoveSuperiorLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnySuperiorLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideSuperiorLandlordDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SaveSuperiorLandlordStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SetStateForSuperiorLandlordEditStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordContactTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordNameStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.SuperiorLandlordsListStep
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStepConfig
import uk.gov.communities.prsdb.webapp.models.dataModels.SuperiorLandlordDataModel

@JourneyFrameworkComponent
class SuperiorLandlordsDetailsTask(
    journeyStateService: JourneyStateService,
    override val superiorLandlordProvideDetailsStep: ProvideSuperiorLandlordDetailsStep,
    override val superiorLandlordNameStep: SuperiorLandlordNameStep,
    override val superiorLandlordContactTypeStep: SuperiorLandlordContactTypeStep,
    override val superiorLandlordEmailStep: SuperiorLandlordEmailStep,
    override val superiorLandlordAddressTask: SuperiorLandlordAddressTask,
    override val superiorLandlordsListStep: SuperiorLandlordsListStep,
    override val hasAnySuperiorLandlordsStep: HasAnySuperiorLandlordsStep,
    override val saveSuperiorLandlordStep: SaveSuperiorLandlordStep,
    override val setStateForSuperiorLandlordEditStep: SetStateForSuperiorLandlordEditStep,
    override val confirmRemoveSuperiorLandlordStep: ConfirmRemoveSuperiorLandlordStep,
) : Task<SuperiorLandlordsDetailsState, SuperiorLandlordsDetailsDependencies>(journeyStateService),
    SuperiorLandlordsDetailsState {
    override val taskState get() = this

    override var superiorLandlordsMap: Map<Int, SuperiorLandlordDataModel>?
        get() = dependencies.listState.superiorLandlordsMap
        set(value) {
            dependencies.listState.superiorLandlordsMap = value
        }
    override var nextSuperiorLandlordId: Int?
        get() = dependencies.listState.nextSuperiorLandlordId
        set(value) {
            dependencies.listState.nextSuperiorLandlordId = value
        }
    override var editingSuperiorLandlordId: Int?
        get() = dependencies.listState.editingSuperiorLandlordId
        set(value) {
            dependencies.listState.editingSuperiorLandlordId = value
        }

    override fun makeSubJourney(state: SuperiorLandlordsDetailsState) =
        subJourney(state) {
            step(journey.hasAnySuperiorLandlordsStep) {
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.NO_MEMBERS -> journey.superiorLandlordProvideDetailsStep
                        AnyMembers.SOME_MEMBERS -> journey.superiorLandlordsListStep
                    }
                }
            }
            step(journey.setStateForSuperiorLandlordEditStep) {
                routeSegment(SetStateForSuperiorLandlordEditStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextStep { journey.superiorLandlordProvideDetailsStep }
            }
            step(journey.confirmRemoveSuperiorLandlordStep) {
                routeSegment(ConfirmRemoveSuperiorLandlordStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                backStep { journey.superiorLandlordsListStep }
                nextStep { mode ->
                    when (mode) {
                        AnyMembers.SOME_MEMBERS -> journey.superiorLandlordsListStep
                        AnyMembers.NO_MEMBERS -> exitStep
                    }
                }
                savable()
            }
            step(journey.superiorLandlordsListStep) {
                routeSegment(SuperiorLandlordsListStep.ROUTE_SEGMENT)
                parents { journey.hasAnySuperiorLandlordsStep.hasOutcome(AnyMembers.SOME_MEMBERS) }
                nextStep { exitStep }
                savable()
            }
            step(journey.superiorLandlordProvideDetailsStep) {
                routeSegment(ProvideSuperiorLandlordDetailsStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasAnySuperiorLandlordsStep.hasOutcome(AnyMembers.NO_MEMBERS),
                        SingleParent(journey.superiorLandlordsListStep) {
                            !journey.superiorLandlordsMap.isNullOrEmpty()
                        },
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.superiorLandlordNameStep
                        YesOrNo.NO -> journey.saveSuperiorLandlordStep
                    }
                }
                savable()
            }
            step(journey.superiorLandlordNameStep) {
                routeSegment(SuperiorLandlordNameStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordProvideDetailsStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.superiorLandlordContactTypeStep }
                savable()
            }
            step(journey.superiorLandlordContactTypeStep) {
                routeSegment(SuperiorLandlordContactTypeStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordNameStep.isComplete() }
                nextStep { mode ->
                    when (mode) {
                        SuperiorLandlordContactType.EMAIL -> journey.superiorLandlordEmailStep
                        SuperiorLandlordContactType.POSTAL_ADDRESS -> journey.superiorLandlordAddressTask.firstStep
                    }
                }
                savable()
            }
            step(journey.superiorLandlordEmailStep) {
                routeSegment(SuperiorLandlordEmailStep.ROUTE_SEGMENT)
                parents { journey.superiorLandlordContactTypeStep.hasOutcome(SuperiorLandlordContactType.EMAIL) }
                nextStep { journey.saveSuperiorLandlordStep }
                savable()
            }
            task(journey.superiorLandlordAddressTask, SuperiorLandlordAddressTask.ROUTE_SEGMENT) {
                parents { journey.superiorLandlordContactTypeStep.hasOutcome(SuperiorLandlordContactType.POSTAL_ADDRESS) }
                nextStep { journey.saveSuperiorLandlordStep }
                savable()
                configureStep(journey.superiorLandlordAddressTask.lookupAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.superiorLandlordNameStep.formModelOrNull?.name
                    }
                    withAdditionalContentProperties {
                        val editingSuperiorLandlord = journey.editingSuperiorLandlord
                        if (editingSuperiorLandlord != null) {
                            mapOf(
                                LookupAddressStepConfig.PREFILL_POSTCODE to editingSuperiorLandlord.addressSearchPostcode,
                                LookupAddressStepConfig.PREFILL_HOUSE_NAME_OR_NUMBER to
                                    editingSuperiorLandlord.addressSearchHouseNameOrNumber,
                            )
                        } else {
                            emptyMap()
                        }
                    }
                }
                configureStep(journey.superiorLandlordAddressTask.selectAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.superiorLandlordNameStep.formModelOrNull?.name
                    }
                    withAdditionalContentProperties {
                        mapOf(
                            SelectAddressStepConfig.PREFILL_SELECTED_ADDRESS to journey.editingSuperiorLandlord?.selectedAddress,
                        )
                    }
                }
                configureStep(journey.superiorLandlordAddressTask.manualAddressStep) {
                    withAdditionalContentProperty {
                        "fieldSetHeadingParam" to journey.superiorLandlordNameStep.formModelOrNull?.name
                    }
                    withAdditionalContentProperties {
                        val editingSuperiorLandlord = journey.editingSuperiorLandlord
                        if (editingSuperiorLandlord?.manualAddressLineOne != null) {
                            mapOf(
                                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_ONE to editingSuperiorLandlord.manualAddressLineOne,
                                ManualAddressStepConfig.PREFILL_ADDRESS_LINE_TWO to editingSuperiorLandlord.manualAddressLineTwo,
                                ManualAddressStepConfig.PREFILL_TOWN_OR_CITY to editingSuperiorLandlord.manualTownOrCity,
                                ManualAddressStepConfig.PREFILL_COUNTY to editingSuperiorLandlord.manualCounty,
                                ManualAddressStepConfig.PREFILL_POSTCODE to editingSuperiorLandlord.manualPostcode,
                            )
                        } else {
                            emptyMap()
                        }
                    }
                }
            }
            step(journey.saveSuperiorLandlordStep) {
                parents {
                    OrParents(
                        journey.superiorLandlordProvideDetailsStep.hasOutcome(YesOrNo.NO),
                        journey.superiorLandlordEmailStep.isComplete(),
                        journey.superiorLandlordAddressTask.isComplete(),
                    )
                }
                nextStep { journey.superiorLandlordsListStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.superiorLandlordsListStep.isComplete(),
                        journey.confirmRemoveSuperiorLandlordStep.hasOutcome(AnyMembers.NO_MEMBERS),
                    )
                }
            }
        }
}
