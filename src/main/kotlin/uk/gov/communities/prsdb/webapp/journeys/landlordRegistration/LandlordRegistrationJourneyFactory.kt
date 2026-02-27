package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration

import org.springframework.beans.factory.ObjectFactory
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.JourneyFrameworkComponent
import uk.gov.communities.prsdb.webapp.annotations.webAnnotations.PrsdbWebService
import uk.gov.communities.prsdb.webapp.controllers.RegisterLandlordController.Companion.LANDLORD_REGISTRATION_CONFIRMATION_ROUTE
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateDelegateProvider
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.StepLifecycleOrchestrator
import uk.gov.communities.prsdb.webapp.journeys.builders.JourneyBuilder.Companion.journey
import uk.gov.communities.prsdb.webapp.journeys.hasOutcome
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceMode
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IdentityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.shared.states.AddressState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkYourAnswersJourney
import uk.gov.communities.prsdb.webapp.journeys.shared.states.CheckYourAnswersJourneyState.Companion.checkable
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AbstractCheckYourAnswersStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import java.security.Principal

@PrsdbWebService
class LandlordRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LandlordRegistrationJourneyState>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        return journey(state) {
            unreachableStepStep { journey.privacyNoticeStep }
            configure {
                withAdditionalContentProperty { "title" to "registerAsALandlord.title" }
            }
            section {
                withHeadingMessageKey("registerAsALandlord.section.privacyNotice.heading")
                step(journey.privacyNoticeStep) {
                    routeSegment(PrivacyNoticeStep.ROUTE_SEGMENT)
                    initialStep()
                    nextStep { journey.identityTask.firstVisitableStep }
                }
            }
            section {
                withHeadingMessageKey("registerAsALandlord.section.yourDetails.heading")
                task(journey.identityTask) {
                    parents { journey.privacyNoticeStep.isComplete() }
                    nextStep { journey.emailStep }
                }
                step(journey.emailStep) {
                    routeSegment(EmailStep.ROUTE_SEGMENT)
                    parents { journey.identityTask.isComplete() }
                    nextStep { journey.phoneNumberStep }
                    checkable()
                }
                step(journey.phoneNumberStep) {
                    routeSegment(PhoneNumberStep.ROUTE_SEGMENT)
                    parents { journey.emailStep.isComplete() }
                    nextStep { journey.countryOfResidenceStep }
                    checkable()
                }
                step(journey.countryOfResidenceStep) {
                    routeSegment(CountryOfResidenceStep.ROUTE_SEGMENT)
                    parents { journey.phoneNumberStep.isComplete() }
                    nextStep { mode ->
                        when (mode) {
                            CountryOfResidenceMode.ENGLAND_OR_WALES -> journey.addressTask.firstVisitableStep
                            CountryOfResidenceMode.NON_ENGLAND_OR_WALES -> journey.nonEnglandOrWalesAddressStep
                        }
                    }
                    // TODO PDJB-550: Fix CYA flow
                    checkable()
                }
                step(journey.nonEnglandOrWalesAddressStep) {
                    routeSegment(NonEnglandOrWalesAddressStep.ROUTE_SEGMENT)
                    parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.NON_ENGLAND_OR_WALES) }
                    noNextDestination()
                }
                task(journey.addressTask) {
                    parents { journey.countryOfResidenceStep.hasOutcome(CountryOfResidenceMode.ENGLAND_OR_WALES) }
                    nextStep { journey.cyaStep }
                    checkable()
                }
            }
            section {
                withHeadingMessageKey("registerAsALandlord.section.checkAndSubmit.heading")
                step(journey.cyaStep) {
                    routeSegment(AbstractCheckYourAnswersStep.ROUTE_SEGMENT)
                    parents { journey.addressTask.isComplete() }
                    nextUrl { LANDLORD_REGISTRATION_CONFIRMATION_ROUTE }
                }
            }
            checkYourAnswersJourney()
        }
    }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent
class LandlordRegistrationJourney(
    // Privacy notice step
    override val privacyNoticeStep: PrivacyNoticeStep,
    // Identity task
    override val identityTask: IdentityTask,
    override val identityVerifyingStep: IdentityVerifyingStep,
    override val confirmIdentityStep: ConfirmIdentityStep,
    override val identityNotVerifiedStep: IdentityNotVerifiedStep,
    override val nameStep: NameStep,
    override val dateOfBirthStep: DateOfBirthStep,
    // Landlord details steps
    override val emailStep: EmailStep,
    override val phoneNumberStep: PhoneNumberStep,
    override val countryOfResidenceStep: CountryOfResidenceStep,
    override val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep,
    // Address task
    override val addressTask: LandlordRegistrationAddressTask,
    override val lookupAddressStep: LookupAddressStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val selectAddressStep: SelectAddressStep,
    override val manualAddressStep: ManualAddressStep,
    // Check your answers step
    override val cyaStep: LandlordRegistrationCyaStep,
    journeyStateService: JourneyStateService,
    delegateProvider: JourneyStateDelegateProvider,
) : AbstractJourneyState(journeyStateService),
    LandlordRegistrationJourneyState {
    override var verifiedIdentity: VerifiedIdentityDataModel? by delegateProvider.nullableDelegate("verifiedIdentity")
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")
    override var cyaChildJourneyIdIfInitialized: String? by delegateProvider.nullableDelegate("cyaChildJourneyIdIfInitialized")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Landlord registration journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface LandlordRegistrationJourneyState :
    IdentityState,
    AddressState,
    CheckYourAnswersJourneyState {
    val privacyNoticeStep: PrivacyNoticeStep
    val identityTask: IdentityTask
    val emailStep: EmailStep
    val phoneNumberStep: PhoneNumberStep
    val countryOfResidenceStep: CountryOfResidenceStep
    val nonEnglandOrWalesAddressStep: NonEnglandOrWalesAddressStep
    val addressTask: LandlordRegistrationAddressTask
    override val cyaStep: LandlordRegistrationCyaStep
}
