package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration

import kotlinx.datetime.Instant
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
import uk.gov.communities.prsdb.webapp.journeys.isComplete
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.LandlordRegistrationState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.CountryOfResidenceStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DeleteJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.EmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordRegistrationCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LandlordTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeDobStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteeNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.LeadTrusteePhoneStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.NonEnglandOrWalesAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgAddressStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberEnglandAndWalesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberNorthernIrelandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityNumberScotlandStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityRegisteredWithStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCharityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompaniesHouseStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgCompanyNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgDirectorsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgEmailStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgLandlordCyaStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgMainContactStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgNameStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgPhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTrusteesStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.OrgTypeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PhoneNumberStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.PrivacyNoticeStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.YourDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IdentityTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.IndividualLandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.LandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.tasks.OrgLandlordRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FinishCyaJourneyStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NoAddressFoundStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.LandlordAddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import java.security.Principal

@JourneyFrameworkComponent("landlordRegistrationJourneyFactory")
class LandlordRegistrationJourneyFactory(
    private val stateFactory: ObjectFactory<LandlordRegistrationJourneyState>,
) {
    fun createJourneySteps(): Map<String, StepLifecycleOrchestrator> {
        val state = stateFactory.getObject()

        val checkingAnswersFor = state.checkingAnswersFor
        return if (checkingAnswersFor == null) {
            mainJourneyMap(state)
        } else {
            LandlordRegistrationTask.checkYourAnswersJourneyMap(state, checkingAnswersFor)
        }
    }

    private fun mainJourneyMap(state: LandlordRegistrationJourneyState): Map<String, StepLifecycleOrchestrator> =
        journey(state) {
            configureFirst { backDestination { Destination.ExternalUrl(LANDLORD_REGISTRATION_START_PAGE_ROUTE) } }
            unreachableStepStep { journey.privacyNoticeStep }
            configure {
                withAdditionalContentProperty { "title" to "registerAsALandlord.title" }
            }
            section {
                withHeadingMessageKey("registerAsALandlord.caption", shouldUseNumbering = false)
                task(journey.landlordRegistrationTask) {
                    initialStep()
                    nextStep { journey.deleteJourneyStep }
                }
            }
            step(journey.deleteJourneyStep) {
                parents { journey.landlordRegistrationTask.isComplete() }
                nextUrl { LANDLORD_REGISTRATION_CONFIRMATION_ROUTE }
            }
        }

    fun initializeJourneyState(user: Principal) = stateFactory.getObject().initializeState(user)
}

@JourneyFrameworkComponent("landlordRegistrationJourney")
class LandlordRegistrationJourney(
    // Top-level task
    override val landlordRegistrationTask: LandlordRegistrationTask,
    override val individualLandlordRegistrationTask: IndividualLandlordRegistrationTask,
    override val orgLandlordRegistrationTask: OrgLandlordRegistrationTask,
    // Landlord type step
    override val landlordTypeStep: LandlordTypeStep,
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
    override val addressTask: LandlordAddressTask,
    override val lookupAddressStep: LookupAddressStep,
    override val noAddressFoundStep: NoAddressFoundStep,
    override val selectAddressStep: SelectAddressStep,
    override val manualAddressStep: ManualAddressStep,
    // Check your answers step
    override val cyaStep: LandlordRegistrationCyaStep,
    override val finishCyaStep: FinishCyaJourneyStep,
    // Org landlord steps
    override val yourDetailsStep: YourDetailsStep,
    override val orgNameStep: OrgNameStep,
    override val orgAddressStep: OrgAddressStep,
    override val orgEmailStep: OrgEmailStep,
    override val orgPhoneNumberStep: OrgPhoneNumberStep,
    override val orgTypeStep: OrgTypeStep,
    override val orgCompaniesHouseStep: OrgCompaniesHouseStep,
    override val orgCompanyNumberStep: OrgCompanyNumberStep,
    override val orgCharityStep: OrgCharityStep,
    override val orgCharityRegisteredWithStep: OrgCharityRegisteredWithStep,
    override val orgCharityNumberEnglandAndWalesStep: OrgCharityNumberEnglandAndWalesStep,
    override val orgCharityNumberNorthernIrelandStep: OrgCharityNumberNorthernIrelandStep,
    override val orgCharityNumberScotlandStep: OrgCharityNumberScotlandStep,
    override val orgDirectorsStep: OrgDirectorsStep,
    override val orgTrusteesStep: OrgTrusteesStep,
    override val leadTrusteeNameStep: LeadTrusteeNameStep,
    override val leadTrusteeEmailStep: LeadTrusteeEmailStep,
    override val leadTrusteePhoneStep: LeadTrusteePhoneStep,
    override val leadTrusteeDobStep: LeadTrusteeDobStep,
    override val leadTrusteeAddressStep: LeadTrusteeAddressStep,
    override val orgMainContactStep: OrgMainContactStep,
    override val orgLandlordCyaStep: OrgLandlordCyaStep,
    // Infrastructure
    override val deleteJourneyStep: DeleteJourneyStep,
    journeyStateService: JourneyStateService,
    override val stateFactory: ObjectFactory<LandlordRegistrationJourneyState>,
) : AbstractJourneyState(journeyStateService),
    LandlordRegistrationJourneyState {
    private val delegateProvider = JourneyStateDelegateProvider(journeyStateService)
    override var verifiedIdentity: VerifiedIdentityDataModel? by delegateProvider.nullableDelegate("verifiedIdentity")
    override var cachedAddresses: List<AddressDataModel>? by delegateProvider.nullableDelegate("cachedAddresses")
    override var isAddressAlreadyRegistered: Boolean? by delegateProvider.nullableDelegate("isAddressAlreadyRegistered")
    override var cachedSelectedAddress: String? by delegateProvider.nullableDelegate("cachedSelectedAddress")
    override var originalJourneyUpdated: Instant? by delegateProvider.nullableDelegate("originalJourneyUpdated")
    override var cyaJourneys: Map<String, String> = mapOf()
    override var checkingAnswersFor: String? by delegateProvider.nullableDelegate("checkingAnswersFor")

    override var cyaRouteSegment: String? by delegateProvider.nullableDelegate("cyaRouteSegment")

    override fun generateJourneyId(seed: Any?): String {
        val user = seed as? Principal
        return super<AbstractJourneyState>.generateJourneyId(user?.let { generateSeedForUser(user) })
    }

    companion object {
        private fun generateSeedForUser(user: Principal): String =
            "Landlord registration journey for ${user.name} at time ${System.currentTimeMillis()}"
    }
}

interface LandlordRegistrationJourneyState : LandlordRegistrationState {
    val deleteJourneyStep: DeleteJourneyStep
}
