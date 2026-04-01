package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.OrParents
import uk.gov.communities.prsdb.webapp.journeys.Task
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.EpcState
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckEpcAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.CheckMatchedEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByCertificateNumberStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ConfirmEpcDetailsRetrievedByUprnStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcAgeAndEnergyRatingCheckMode
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

@JourneyFrameworkComponent("propertyRegistrationEpcTask")
class EpcTask : Task<EpcState>() {
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
            step(journey.checkUprnMatchedEpcStep) {
                routeSegment(ConfirmEpcDetailsRetrievedByUprnStep.ROUTE_SEGMENT)
                parents { journey.epcLookupByUprnStep.hasOutcome(EpcLookupByUprnMode.EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.NO -> journey.hasEpcStep
                        YesOrNo.YES -> journey.epcAgeAndEnergyRatingCheckStep
                    }
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
            step(journey.confirmEpcDetailsRetrievedByCertificateNumberStep) {
                routeSegment(ConfirmEpcDetailsRetrievedByCertificateNumberStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.LATEST_EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        YesOrNo.NO -> journey.findYourEpcStep
                        YesOrNo.YES -> journey.epcAgeAndEnergyRatingCheckStep
                    }
                }
                savable()
            }
            step(journey.epcAgeAndEnergyRatingCheckStep) {
                parents {
                    OrParents(
                        journey.confirmEpcDetailsRetrievedByCertificateNumberStep.hasOutcome(YesOrNo.YES),
                        journey.checkUprnMatchedEpcStep.hasOutcome(YesOrNo.YES),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT -> {
                            journey.checkEpcAnswersStep
                        }

                        EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS -> {
                            if (journey.isOccupied == true) journey.epcInDateAtStartOfTenancyCheckStep else journey.epcExpiredStep
                        }

                        EpcAgeAndEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING -> {
                            journey.hasMeesExemptionStep
                        }
                    }
                }
            }
            // TODO PDJB-664: Implement EPC Superseded step logic
            step(journey.checkSupersededEpcStep) {
                routeSegment(EpcSuperseededStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.SUPERSEDED_EPC_FOUND) }
                nextStep { mode ->
                    when (mode) {
                        CheckMatchedEpcMode.EPC_INCORRECT -> {
                            journey.findYourEpcStep
                        }

                        CheckMatchedEpcMode.EPC_COMPLIANT -> {
                            journey.checkEpcAnswersStep
                        }

                        CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS -> {
                            if (journey.isOccupied == true) journey.epcInDateAtStartOfTenancyCheckStep else journey.epcExpiredStep
                        }

                        CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING -> {
                            journey.hasMeesExemptionStep
                        }
                    }
                }
                savable()
            }
            // TODO PDJB-663: Implement EPC Not Found step logic
            step(journey.epcNotFoundStep) {
                routeSegment(EpcNotFoundStep.ROUTE_SEGMENT)
                parents { journey.findYourEpcStep.hasOutcome(FindYourEpcMode.NOT_FOUND) }
                nextStep { journey.isEpcRequiredStep }
                savable()
            }
            step(journey.hasMeesExemptionStep) {
                routeSegment(HasMeesExemptionStep.ROUTE_SEGMENT)
                parents {
                    // TODO PDJB-664 - remove checkSupersededEpcStep parent, should go via journey.epcAgeAndEnergyRatingCheckStep instead.
                    OrParents(
                        journey.epcAgeAndEnergyRatingCheckStep.hasOutcome(EpcAgeAndEnergyRatingCheckMode.EPC_LOW_ENERGY_RATING),
                        journey.checkSupersededEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_LOW_ENERGY_RATING),
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
            step(journey.meesExemptionStep) {
                routeSegment(MeesExemptionStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.HAS_EXEMPTION) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            step(journey.lowEnergyRatingStep) {
                routeSegment(LowEnergyRatingStep.ROUTE_SEGMENT)
                parents { journey.hasMeesExemptionStep.hasOutcome(HasMeesExemptionMode.NO_EXEMPTION) }
                nextStep { journey.checkEpcAnswersStep }
                savable()
            }
            step(journey.epcInDateAtStartOfTenancyCheckStep) {
                routeSegment(EpcInDateAtStartOfTenancyCheckStep.ROUTE_SEGMENT)
                // This should only be the parent if the property is occupied
                parents {
                    OrParents(
                        journey.epcAgeAndEnergyRatingCheckStep.hasOutcome(EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS),
                        // TODO PDJB-664 - remove checkSupersededEpcStep parent, should go via journey.epcAgeAndEnergyRatingCheckStep instead.
                        journey.checkSupersededEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS),
                    )
                }
                nextStep { mode ->
                    when (mode) {
                        EpcInDateAtStartOfTenancyCheckMode.IN_DATE -> journey.checkEpcAnswersStep
                        EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE -> journey.epcExpiredStep
                    }
                }
                savable()
            }
            step(journey.epcExpiredStep) {
                routeSegment(EpcExpiredStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        // This should only be a parent if the property is unoccupied
                        journey.epcAgeAndEnergyRatingCheckStep.hasOutcome(EpcAgeAndEnergyRatingCheckMode.EPC_OLDER_THAN_10_YEARS),
                        // TODO PDJB-664 - remove checkSupersededEpcStep parent, should go via journey.epcAgeAndEnergyRatingCheckStep instead.
                        // This should only be a parent if the property is unoccupied
                        journey.checkSupersededEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_OLDER_THAN_10_YEARS),
                        journey.epcInDateAtStartOfTenancyCheckStep.hasOutcome(EpcInDateAtStartOfTenancyCheckMode.NOT_IN_DATE),
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
            step(journey.checkEpcAnswersStep) {
                routeSegment(CheckEpcAnswersStep.ROUTE_SEGMENT)
                parents {
                    OrParents(
                        journey.epcInDateAtStartOfTenancyCheckStep.hasOutcome(EpcInDateAtStartOfTenancyCheckMode.IN_DATE),
                        journey.epcExpiredStep.isComplete(),
                        journey.meesExemptionStep.isComplete(),
                        journey.lowEnergyRatingStep.isComplete(),
                        journey.epcExemptionStep.isComplete(),
                        journey.epcMissingStep.isComplete(),
                        journey.provideEpcLaterStep.isComplete(),
                        journey.epcAgeAndEnergyRatingCheckStep.hasOutcome(EpcAgeAndEnergyRatingCheckMode.EPC_COMPLIANT),
                        journey.checkSupersededEpcStep.hasOutcome(CheckMatchedEpcMode.EPC_COMPLIANT),
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
