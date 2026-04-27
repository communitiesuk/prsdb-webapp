package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcRetrievedByUprnStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcEnergyRatingCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcInDateAtStartOfTenancyCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent("propertyRegistrationEpcDetailsTask")
class EpcDetailsTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            step(journey.epcLookupByUprnStep) {
                nextStep { mode ->
                    when (mode) {
                        EpcLookupByUprnMode.EPC_FOUND -> journey.checkUprnMatchedEpcStep
                        EpcLookupByUprnMode.NOT_FOUND -> journey.hasEpcStep
                    }
                }
            }
            step<YesOrNo, ConfirmEpcRetrievedByUprnStepConfig>(journey.checkUprnMatchedEpcStep) {
                routeSegment(ConfirmEpcRetrievedByUprnStep.ROUTE_SEGMENT)
                parents { journey.epcLookupByUprnStep.hasOutcome(EpcLookupByUprnMode.EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.NO -> journey.hasEpcStep
                        YesOrNo.YES -> journey.epcAgeCheckStep
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { epcRetrievedByUprn }
                }
                savable()
            }
            step(journey.hasEpcStep) {
                routeSegment(HasEpcStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.epcLookupByUprnStep.hasOutcome(EpcLookupByUprnMode.NOT_FOUND),
                        journey.checkUprnMatchedEpcStep.hasOutcome(YesOrNo.NO),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        HasEpcMode.HAS_EPC -> journey.findYourEpcStep
                        HasEpcMode.NO_EPC -> journey.isEpcRequiredStep
                        HasEpcMode.PROVIDE_LATER -> journey.provideEpcLaterStep
                    }
                }
                savable()
            }
            step(journey.findYourEpcStep) {
                routeSegment(FindYourEpcStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.hasOutcome(HasEpcMode.HAS_EPC) }
                nextStep { mode ->
                    when (mode) {
                        FindYourEpcMode.LATEST_EPC_FOUND -> journey.confirmEpcDetailsRetrievedByCertificateNumberStep
                        FindYourEpcMode.SUPERSEDED_EPC_FOUND -> journey.checkSupersededEpcStep
                        FindYourEpcMode.NOT_FOUND -> journey.epcNotFoundStep
                    }
                }
                savable()
            }
            step<YesOrNo, ConfirmEpcDetailsRetrievedByCertificateNumberStepConfig>(
                journey.confirmEpcDetailsRetrievedByCertificateNumberStep,
            ) {
                routeSegment(ConfirmEpcDetailsRetrievedByCertificateNumberStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.LATEST_EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.NO -> journey.findYourEpcStep
                        YesOrNo.YES -> journey.epcAgeCheckStep
                    }
                }
                stepSpecificInitialisation {
                    usingEpc { epcRetrievedByCertificateNumber }
                }
                savable()
            }
            step(journey.checkSupersededEpcStep) {
                routeSegment(EpcSuperseededStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.SUPERSEDED_EPC_FOUND) }
                nextStep { journey.epcAgeCheckStep }
                savable()
            }
            step(journey.epcAgeCheckStep) {
                parents {
                    OrParents(
                        journey.confirmEpcDetailsRetrievedByCertificateNumberStep.hasOutcome(YesOrNo.YES),
                        journey.checkUprnMatchedEpcStep.hasOutcome(YesOrNo.YES),
                        journey.checkSupersededEpcStep.isComplete(),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER -> journey.epcEnergyRatingCheckStep
                        EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS -> journey.isPropertyOccupiedCheckStep
                    }
                }
            }
            step(journey.isPropertyOccupiedCheckStep) {
                parents {
                    journey.epcAgeCheckStep.hasOutcome(EpcAgeCheckMode.EPC_OLDER_THAN_10_YEARS)
                }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.epcInDateAtStartOfTenancyCheckStep
                        YesOrNo.NO -> journey.epcExpiredStep
                    }
                }
            }
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.NOT_FOUND) }
                nextStep { journey.isEpcRequiredStep }
                savable()
            }
            step(journey.epcInDateAtStartOfTenancyCheckStep) {
                routeSegment(EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT)
                parents {
                    journey.isPropertyOccupiedCheckStep.hasOutcome(YesOrNo.YES)
                }
                nextStep { mode ->
                    when (mode) {
                        EpcInDateAtStartOfTenancyCheckMode.IN_DATE -> journey.epcEnergyRatingCheckStep
                        EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE -> journey.epcExpiredStep
                    }
                }
                savable()
            }
            step(journey.epcExpiredStep) {
                routeSegment(EpcExpiredStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.isPropertyOccupiedCheckStep.hasOutcome(YesOrNo.NO),
                        journey.epcInDateAtStartOfTenancyCheckStep.hasOutcome(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE),
                    )
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.isEpcRequiredStep) {
                routeSegment(IsEpcRequiredStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasEpcStep.hasOutcome(HasEpcMode.NO_EPC),
                        journey.epcNotFoundStep.isComplete(),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.epcMissingStep
                        YesOrNo.NO -> journey.epcExemptionStep
                    }
                }
                savable()
            }
            step(journey.epcExemptionStep) {
                routeSegment(EpcExemptionStep.ROUTE_SEGMENT)
                parents {
                    journey.isEpcRequiredStep.hasOutcome(YesOrNo.NO)
                }
                nextStep { exitStep }
                savable()
            }
            step(journey.epcMissingStep) {
                routeSegment(EpcMissingStep.ROUTE_SEGMENT)
                parents { journey.isEpcRequiredStep.hasOutcome(YesOrNo.YES) }
                nextStep { exitStep }
                savable()
            }
            step(journey.epcEnergyRatingCheckStep) {
                parents {
                    OrParents(
                        journey.epcAgeCheckStep.hasOutcome(EpcAgeCheckMode.EPC_10_YEARS_OR_NEWER),
                        journey.epcInDateAtStartOfTenancyCheckStep.hasOutcome(EpcInDateAtStartOfTenancyCheckMode.IN_DATE),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS -> exitStep
                        EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING -> journey.hasMeesExemptionStep
                    }
                }
            }
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents {
                    journey.epcEnergyRatingCheckStep.hasOutcome(EpcEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING)
                }
                nextStep { mode ->
                    when (mode) {
                        HasMeesExemptionMode.HAS_EXEMPTION -> journey.meesExemptionStep
                        HasMeesExemptionMode.NO_EXEMPTION -> journey.lowEnergyRatingStep
                    }
                }
                savable()
            }
            step(journey.meesExemptionStep) {
                routeSegment(MeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.HAS_EXEMPTION) }
                nextStep { exitStep }
                savable()
            }
            step(journey.lowEnergyRatingStep) {
                routeSegment(LowEnergyRatingStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.NO_EXEMPTION) }
                nextStep { exitStep }
                savable()
            }
            step(journey.provideEpcLaterStep) {
                routeSegment(ProvideEpcLaterStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.hasOutcome(HasEpcMode.PROVIDE_LATER) }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents {
                    OrParents(
                        journey.epcEnergyRatingCheckStep.hasOutcome(EpcEnergyRatingCheckMode.EPC_MEETS_ENERGY_REQUIREMENTS),
                        journey.epcExpiredStep.isComplete(),
                        journey.meesExemptionStep.isComplete(),
                        journey.lowEnergyRatingStep.isComplete(),
                        journey.epcExemptionStep.isComplete(),
                        journey.epcMissingStep.isComplete(),
                        journey.provideEpcLaterStep.isComplete(),
                    )
                }
            }
        }
}
