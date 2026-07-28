package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks

import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.journeys.DuplicableTask
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IndividualLandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.LandlordAddressTask

@JourneyFrameworkComponent
class IndividualLandlordRegistrationTask(
    journeyStateService: JourneyStateService,
    override val emailStep: EmailStep,
    override val phoneNumberStep: PhoneNumberStep,
    override val countryOfResidenceStep: CountryOfResidenceStep,
    override val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep,
    override val addressTask: LandlordAddressTask,
) : DuplicableTask<IndividualLandlordRegistrationState>(journeyStateService),
    IndividualLandlordRegistrationState {
    override val taskState get() = this

    override fun makeSubJourney(state: IndividualLandlordRegistrationState) =
        subJourney(state) {
            step(journey.countryOfResidenceStep) {
                routeSegment(CountryOfResidenceStep.ROUTE_SEGMENT)
                nextStep { mode ->
                    when (mode) {
                        CountryOfResidenceMode.ENGLAND_OR_WALES -> journey.addressTask.firstStep
                        CountryOfResidenceMode.NON_ENGLAND_OR_WALES -> journey.nonEnglandOrWalesAddressStep
                    }
                }
            }
            step(journey.nonEnglandOrWalesAddressStep) {
                routeSegment(NonEnglandOrWalesAddressStep.ROUTE_SEGMENT)
                parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.NON_ENGLAND_OR_WALES) }
                noNextDestination()
            }
            duplicableTask(journey.addressTask) {
                parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.ENGLAND_OR_WALES) }
                nextStep { exitStep }
            }
            exitStep {
                parents { journey.addressTask.isComplete() }
            }
        }
}
