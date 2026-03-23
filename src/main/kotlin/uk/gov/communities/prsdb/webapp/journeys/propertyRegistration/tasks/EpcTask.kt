package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckAutomatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiredStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcExpiryCheckStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcMissingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcNotFoundStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSearchStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcSuperseededStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasMeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LowEnergyRatingStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.MeesExemptionStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideEpcLaterStep

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask : Task<EpcState>() {
    override fun makeSubJourney(state: EpcState) =
        subJourney(state) {
            // TODO (no ticket yet): Implement EPC lookup by UPRN navigational step
            step(journey.epcLookupStep) {
                nextStep { mode ->
                    when (mode) {
                        EpcLookupMode.AUTOMATCHED -> journey.checkAutomatchedEpcStep
                        EpcLookupMode.NOT_FOUND -> journey.hasEpcStep
                    }
                }
            }
            // TODO PDJB-656: Implement Has EPC step logic
            step(journey.hasEpcStep) {
                routeSegment(HasEpcStep.ROUTE_SEGMENT)
                parents { journey.epcLookupStep.hasOutcome(EpcLookupMode.NOT_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        HasEpcMode.HAS_EPC -> journey.epcSearchStep
                        HasEpcMode.NO_EPC -> journey.hasEpcExemptionStep
                        HasEpcMode.PROVIDE_LATER -> journey.provideEpcLaterStep
                    }
                }
                savable()
            }
            // TODO PDJB-661: Implement Check Automatched EPC step logic
            step(journey.checkAutomatchedEpcStep) {
                routeSegment(CheckAutomatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcLookupStep.hasOutcome(EpcLookupMode.AUTOMATCHED) }
                nextStep { mode ->
                    when (mode) {
                        CheckEpcMode.SEARCH_AGAIN -> journey.epcSearchStep
                        CheckEpcMode.OLD_LOW_RATING -> journey.hasMeesExemptionStep
                        CheckEpcMode.OCCUPIED -> journey.epcExpiryCheckStep
                        CheckEpcMode.UNOCCUPIED -> journey.checkEpcAnswersStep
                    }
                }
                savable()
            }
            // TODO PDJB-662: Implement EPC Search step logic
            step(journey.epcSearchStep) {
                routeSegment(EpcSearchStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasEpcStep.hasOutcome(HasEpcMode.HAS_EPC),
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckEpcMode.SEARCH_AGAIN),
                        journey.checkMatchedEpcStep.hasOutcome(CheckEpcMode.SEARCH_AGAIN),
                        journey.epcSuperseededStep.hasOutcome(CheckEpcMode.SEARCH_AGAIN),
                        journey.epcNotFoundStep.hasOutcome(EpcNotFoundMode.SEARCH_AGAIN),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcSearchMode.FOUND -> journey.checkMatchedEpcStep
                        EpcSearchMode.SUPERSEDED -> journey.epcSuperseededStep
                        EpcSearchMode.NOT_FOUND -> journey.epcNotFoundStep
                    }
                }
                savable()
            }
            // TODO PDJB-661: Implement Check Matched EPC step logic
            step(journey.checkMatchedEpcStep) {
                routeSegment(CheckMatchedEpcStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.FOUND) }
                nextStep { mode ->
                    when (mode) {
                        CheckEpcMode.SEARCH_AGAIN -> journey.epcSearchStep
                        CheckEpcMode.OLD_LOW_RATING -> journey.hasMeesExemptionStep
                        CheckEpcMode.OCCUPIED -> journey.epcExpiryCheckStep
                        CheckEpcMode.UNOCCUPIED -> journey.checkEpcAnswersStep
                    }
                }
                savable()
            }
            // TODO PDJB-664: Implement EPC Superseded step logic
            step(journey.epcSuperseededStep) {
                routeSegment(EpcSuperseededStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.SUPERSEDED) }
                nextStep { mode ->
                    when (mode) {
                        CheckEpcMode.SEARCH_AGAIN -> journey.epcSearchStep
                        CheckEpcMode.OLD_LOW_RATING -> journey.hasMeesExemptionStep
                        CheckEpcMode.OCCUPIED -> journey.epcExpiryCheckStep
                        CheckEpcMode.UNOCCUPIED -> journey.checkEpcAnswersStep
                    }
                }
                savable()
            }
            // TODO PDJB-663: Implement EPC Not Found step logic
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.epcSearchStep.hasOutcome(EpcSearchMode.NOT_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        EpcNotFoundMode.SEARCH_AGAIN -> journey.epcSearchStep
                        EpcNotFoundMode.NO_EPC -> journey.hasEpcExemptionStep
                    }
                }
                savable()
            }
            // TODO PDJB-667: Implement Has MEES Exemption step logic
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckEpcMode.OLD_LOW_RATING),
                        journey.checkMatchedEpcStep.hasOutcome(CheckEpcMode.OLD_LOW_RATING),
                        journey.epcSuperseededStep.hasOutcome(CheckEpcMode.OLD_LOW_RATING),
                    )
                }
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
                parents {
                    OrParents(
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckEpcMode.OCCUPIED),
                        journey.checkMatchedEpcStep.hasOutcome(CheckEpcMode.OCCUPIED),
                        journey.epcSuperseededStep.hasOutcome(CheckEpcMode.OCCUPIED),
                    )
                }
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
                parents { journey.epcExpiryCheckStep.hasOutcome(EpcExpiryCheckMode.NOT_IN_DATE) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-657: Implement Has EPC Exemption step logic
            step(journey.hasEpcExemptionStep) {
                routeSegment(HasEpcExemptionStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.hasEpcStep.hasOutcome(HasEpcMode.NO_EPC),
                        journey.epcNotFoundStep.hasOutcome(EpcNotFoundMode.NO_EPC),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        HasEpcExemptionMode.REQUIRED -> journey.epcMissingStep
                        HasEpcExemptionMode.HAS_EXEMPTION -> journey.epcExemptionStep
                    }
                }
                savable()
            }
            // TODO PDJB-658: Implement EPC Exemption step logic
            step(journey.epcExemptionStep) {
                routeSegment(EpcExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasEpcExemptionStep.hasOutcome(HasEpcExemptionMode.HAS_EXEMPTION) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            // TODO PDJB-659: Implement EPC Missing step logic
            step(journey.epcMissingStep) {
                routeSegment(EpcMissingStep.ROUTE_SEGMENT)
                parents { journey.hasEpcExemptionStep.hasOutcome(HasEpcExemptionMode.REQUIRED) }
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
                        journey.checkAutomatchedEpcStep.hasOutcome(CheckEpcMode.UNOCCUPIED),
                        journey.checkMatchedEpcStep.hasOutcome(CheckEpcMode.UNOCCUPIED),
                        journey.epcSuperseededStep.hasOutcome(CheckEpcMode.UNOCCUPIED),
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
