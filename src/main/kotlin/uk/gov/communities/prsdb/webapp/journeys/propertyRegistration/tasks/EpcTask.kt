package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmedEpcRoutingMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSupersededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.IsEpcRequiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep
import uk.gov.communities.prsdb.webapp.journeys.shared.YesOrNo

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            // TODO (no ticket yet): Implement EPC lookup by UPRN navigational step
            step(journey.epcLookupByUprnStep) {
                nextStep { mode ->
                    when (mode) {
                        EpcLookupMode.EPC_FOUND -> journey.checkUprnMatchedEpcStep
                        EpcLookupMode.NOT_FOUND -> journey.hasEpcStep
                    }
                }
            }
            // TODO PDJB-661: Implement Check Automatched EPC step logic
            step(journey.checkUprnMatchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.AUTOMATCHED_ROUTE_SEGMENT)
                parents { journey.epcLookupByUprnStep.hasOutcome(EpcLookupMode.EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.confirmedEpcRoutingStep
                        YesOrNo.NO -> journey.hasEpcStep
                    }
                }
                savable()
            }
            // TODO PDJB-656: Implement Has EPC step logic
            step(journey.hasEpcStep) {
                routeSegment(HasEpcStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.epcLookupByUprnStep.hasOutcome(EpcLookupMode.NOT_FOUND),
                        journey.checkUprnMatchedEpcStep.hasOutcome((YesOrNo.NO)),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        HasEpcMode.HAS_EPC -> journey.epcSearchStep
                        HasEpcMode.NO_EPC -> journey.isEpcRequiredStep
                        HasEpcMode.PROVIDE_LATER -> journey.provideEpcLaterStep
                    }
                }
                savable()
            }
            // TODO PDJB-662: Implement EPC Search step logic
            // TODO PDJB-725 - add "Search again" link
            step(journey.epcSearchStep) {
                routeSegment(EpcSearchStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.hasOutcome(HasEpcMode.HAS_EPC) }
                nextStep { mode ->
                    when (mode) {
                        EpcSearchMode.CURRENT_EPC_FOUND -> journey.checkSearchedEpcStep
                        EpcSearchMode.SUPERSEDED_EPC_FOUND -> journey.epcSupersededStep
                        EpcSearchMode.NOT_FOUND -> journey.epcNotFoundStep
                    }
                }
                savable()
            }
            // TODO PDJB-661: Implement Check Matched EPC step logic
            step(journey.checkSearchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.CURRENT_EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.YES -> journey.confirmedEpcRoutingStep
                        YesOrNo.NO -> journey.epcSearchStep
                    }
                }
                savable()
            }
            // TODO PDJB-664: Implement EPC Superseded step logic
            step(journey.epcSupersededStep) {
                routeSegment(EpcSupersededStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.SUPERSEDED_EPC_FOUND) }
                nextStep { journey.confirmedEpcRoutingStep }
                savable()
            }
            // TODO PDJB-663: Implement EPC Not Found step logic
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.NOT_FOUND) }
                nextStep { journey.isEpcRequiredStep }
                savable()
            }
            // Routes after EPC details are confirmed: checks expiry, energy rating, and occupancy
            step(journey.confirmedEpcRoutingStep) {
                parents {
                    OrParents(
                        journey.checkUprnMatchedEpcStep.hasOutcome(YesOrNo.YES),
                        journey.checkSearchedEpcStep.hasOutcome(YesOrNo.YES),
                        journey.epcSupersededStep.isComplete(),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        ConfirmedEpcRoutingMode.LOW_ENERGY_RATING -> journey.hasMeesExemptionStep
                        ConfirmedEpcRoutingMode.OCCUPIED -> journey.epcExpiryCheckStep
                        ConfirmedEpcRoutingMode.UNOCCUPIED -> journey.epcExpiredStep
                    }
                }
            }
            // TODO PDJB-667: Implement Has MEES Exemption step logic
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.confirmedEpcRoutingStep.hasOutcome(ConfirmedEpcRoutingMode.LOW_ENERGY_RATING) }
                nextStep { mode ->
                    when (mode) {
                        HasMeesExemptionMode.HAS_EXEMPTION -> journey.meesExemptionStep
                        HasMeesExemptionMode.NO_EXEMPTION -> journey.lowEnergyRatingStep
                    }
                }
                savable()
            }
            // TODO PDJB-668: Implement MEES Exemption step logic
            step(journey.meesExemptionStep) {
                routeSegment(MeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.HAS_EXEMPTION) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-669: Implement Low Energy Rating step logic
            step(journey.lowEnergyRatingStep) {
                routeSegment(LowEnergyRatingStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.NO_EXEMPTION) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-665: Implement EPC Expiry Check step logic
            step(journey.epcExpiryCheckStep) {
                routeSegment(EpcExpiryCheckStep.ROUTE_SEGMENT)
                parents { journey.confirmedEpcRoutingStep.hasOutcome(ConfirmedEpcRoutingMode.OCCUPIED) }
                nextStep { mode ->
                    when (mode) {
                        EpcExpiryCheckMode.IN_DATE -> journey.checkEpcAnswersStep
                        EpcExpiryCheckMode.NOT_IN_DATE -> journey.epcExpiredStep
                    }
                }
                savable()
            }
            // TODO PDJB-666: Implement EPC Expired step logic
            step(journey.epcExpiredStep) {
                routeSegment(EpcExpiredStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.confirmedEpcRoutingStep.hasOutcome(ConfirmedEpcRoutingMode.UNOCCUPIED),
                        journey.epcExpiryCheckStep.hasOutcome(EpcExpiryCheckMode.NOT_IN_DATE),
                    )
                }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-657: Implement Is EPC required step logic
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
            // TODO PDJB-658: Implement EPC Exemption step logic
            step(journey.epcExemptionStep) {
                routeSegment(EpcExemptionStep.ROUTE_SEGMENT)
                parents {
                    journey.isEpcRequiredStep.hasOutcome(YesOrNo.NO)
                }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-659: Implement EPC Missing step logic
            step(journey.epcMissingStep) {
                routeSegment(EpcMissingStep.ROUTE_SEGMENT)
                parents { journey.isEpcRequiredStep.hasOutcome(YesOrNo.YES) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-660: Implement Provide EPC Later step logic
            step(journey.provideEpcLaterStep) {
                routeSegment(ProvideEpcLaterStep.ROUTE_SEGMENT)
                parents { journey.hasEpcStep.hasOutcome(HasEpcMode.PROVIDE_LATER) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-670: Implement Check EPC Answers step logic
            step(journey.checkEpcAnswersStep) {
                routeSegment(CheckEpcAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.epcExpiryCheckStep.hasOutcome(EpcExpiryCheckMode.IN_DATE),
                        journey.epcExpiredStep.isComplete(),
                        journey.meesExemptionStep.isComplete(),
                        journey.lowEnergyRatingStep.isComplete(),
                        journey.epcExemptionStep.isComplete(),
                        journey.epcMissingStep.isComplete(),
                        journey.provideEpcLaterStep.isComplete(),
                    )
                }
                nextStep { exitStep }
                savable()
            }
            exitStep {
                parents { journey.checkEpcAnswersStep.isComplete() }
            }
        }
}
