package uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_START_PAGE_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.Destination
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.IndividualLandlordPlaceholderStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeMode
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.organisationLandlordRegistration.steps.YourDetailsStep
import java.security.Principal

@JourneyFrameworkComponent("organisationLandlordRegistrationJourneyFactory")
class OrganisationLandlordRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<OrganisationLandlordRegistrationJourney>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()
        return mainJourneyMap(state)
    }

    private fun mainJourneyMap(state: OrganisationLandlordRegistrationJourney): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configureFirst { backDestination { Destination.ExternalUrl(LANDLORD_REGISTRATION_START_PAGE_ROUTE) } }
            unreachableStepUrl { LANDLORD_REGISTRATION_START_PAGE_ROUTE }
            configure {
                withAdditionalContentProperty { "title" to "registerAsALandlord.title" }
            }
            step(journey.landlordTypeStep) {
                routeSegment(LandlordTypeStep.ROUTE_SEGMENT)
                initialStep()
                nextDestination { mode ->
                    when (mode) {
                        LandlordTypeMode.INDIVIDUAL -> Destination(journey.individualLandlordPlaceholderStep)
                        LandlordTypeMode.ORGANISATION -> Destination(journey.yourDetailsStep)
                    }
                }
            }
            step(journey.individualLandlordPlaceholderStep) {
                routeSegment(IndividualLandlordPlaceholderStep.ROUTE_SEGMENT)
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.INDIVIDUAL) }
                nextUrl { LANDLORD_REGISTRATION_START_PAGE_ROUTE }
            }
            step(journey.yourDetailsStep) {
                routeSegment(YourDetailsStep.ROUTE_SEGMENT)
                parents { journey.landlordTypeStep.hasOutcome(LandlordTypeMode.ORGANISATION) }
                nextStep { journey.orgNameStep }
            }
            step(journey.orgNameStep) {
                routeSegment(OrgNameStep.ROUTE_SEGMENT)
                parents { journey.yourDetailsStep.isComplete() }
                nextStep { journey.orgAddressStep }
            }
            step(journey.orgAddressStep) {
                routeSegment(OrgAddressStep.ROUTE_SEGMENT)
                parents { journey.orgNameStep.isComplete() }
                nextStep { journey.orgEmailStep }
            }
            step(journey.orgEmailStep) {
                routeSegment(OrgEmailStep.ROUTE_SEGMENT)
                parents { journey.orgAddressStep.isComplete() }
                nextStep { journey.orgPhoneNumberStep }
            }
            step(journey.orgPhoneNumberStep) {
                routeSegment(OrgPhoneNumberStep.ROUTE_SEGMENT)
                parents { journey.orgEmailStep.isComplete() }
                nextStep { journey.orgTypeStep }
            }
            step(journey.orgTypeStep) {
                routeSegment(OrgTypeStep.ROUTE_SEGMENT)
                parents { journey.orgPhoneNumberStep.isComplete() }
                nextStep { journey.orgCompaniesHouseStep }
            }
            step(journey.orgCompaniesHouseStep) {
                routeSegment(OrgCompaniesHouseStep.ROUTE_SEGMENT)
                parents { journey.orgTypeStep.isComplete() }
                nextStep { journey.orgCharityStep }
            }
            step(journey.orgCharityStep) {
                routeSegment(OrgCharityStep.ROUTE_SEGMENT)
                parents { journey.orgCompaniesHouseStep.isComplete() }
                nextStep { journey.orgDirectorsStep }
            }
            step(journey.orgDirectorsStep) {
                routeSegment(OrgDirectorsStep.ROUTE_SEGMENT)
                parents { journey.orgCharityStep.isComplete() }
                nextStep { journey.orgTrusteesStep }
            }
            step(journey.orgTrusteesStep) {
                routeSegment(OrgTrusteesStep.ROUTE_SEGMENT)
                parents { journey.orgDirectorsStep.isComplete() }
                nextStep { journey.orgMainContactStep }
            }
            step(journey.orgMainContactStep) {
                routeSegment(OrgMainContactStep.ROUTE_SEGMENT)
                parents { journey.orgTrusteesStep.isComplete() }
                nextStep { journey.orgLandlordCyaStep }
            }
            step(journey.orgLandlordCyaStep) {
                routeSegment(OrgLandlordCyaStep.ROUTE_SEGMENT)
                parents { journey.orgMainContactStep.isComplete() }
                nextUrl { LANDLORD_REGISTRATION_CONFIRMATION_ROUTE }
            }
        }

    fun initializeJourneyState(user: Principal): String {
        val state = stateFactory.getObject()
        return state.initializeState(user)
    }
}

@JourneyFrameworkComponent("organisationLandlordRegistrationJourney")
class OrganisationLandlordRegistrationJourney(
    override val landlordTypeStep: LandlordTypeStep,
    override val individualLandlordPlaceholderStep: IndividualLandlordPlaceholderStep,
    override val yourDetailsStep: YourDetailsStep,
    override val orgNameStep: OrgNameStep,
    override val orgAddressStep: OrgAddressStep,
    override val orgEmailStep: OrgEmailStep,
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val orgTypeStep: OrgTypeStep,
    override val orgCompaniesHouseStep: OrgCompaniesHouseStep,
    override val orgCharityStep: OrgCharityStep,
    override val orgDirectorsStep: OrgDirectorsStep,
    override val orgTrusteesStep: OrgTrusteesStep,
    override val orgMainContactStep: OrgMainContactStep,
    override val orgLandlordCyaStep: OrgLandlordCyaStep,
    journeyStateService: JourneyStateService,
) : AbstractJourneyState(journeyStateService),
    OrganisationLandlordRegistrationState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Organisation landlord registration journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}
