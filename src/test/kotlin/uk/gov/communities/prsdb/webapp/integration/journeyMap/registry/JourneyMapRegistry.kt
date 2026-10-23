package uk.gov.communities.prsdb.webapp.integration.journeyMap.registry

import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.integration.journeyMap.JourneyMapHarness
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.GovBodyMembersBackRoutingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.HasAnyGovBodyMembersStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStepConfig
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.RemoveGovBodyMemberStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.AnyLandlordsInvited
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BeforePdjb1022HasGasCertStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.EpcLookupByUprnStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.FindYourEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyInCollectionStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasAnyJointLandlordsInvitedStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasElectricalCertStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasEpcStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasGasSupplyStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeMode
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LicensingTypeStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.AnyMembers
import uk.gov.communities.prsdb.webapp.journeys.shared.IdentityVerificationStatus
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.CheckLandlordIdentityVerifiedStepConfig
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressMode
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStepConfig
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockEpcData
import java.time.LocalDate
import kotlin.reflect.KClass

class ScenarioOption(
    val label: String,
    val arrange: (JourneyMapHarness) -> Unit,
)

// A mode the generator cannot produce for a journey, or for every journey when journey is null
class Exemption(
    val journey: String?,
    val stepConfig: KClass<*>,
    val mode: String,
    val reason: String,
)

// The first option is the default, applied whenever no other option is being explored
class Scenario(
    val name: String,
    val stepConfigs: Set<KClass<*>>,
    val options: List<ScenarioOption>,
) {
    val stepConfigNames: Set<String> get() = stepConfigs.mapNotNull { it.simpleName }.toSet()

    fun appliesTo(stepConfigNames: Set<String>) = stepConfigs.any { it.simpleName in stepConfigNames }
}

object JourneyMapRegistry {
    private val verifiedIdentity = VerifiedIdentityDataModel("Jane Smith", LocalDate.of(1980, 1, 1))

    private fun epcFound(
        energyRating: String = "C",
        expiryDate: kotlinx.datetime.LocalDate? = null,
        latestCertificateNumber: String = MockEpcData.DEFAULT_EPC_CERTIFICATE_NUMBER,
    ) = if (expiryDate == null) {
        MockEpcData.createEpcRegisterClientEpcFoundResponse(
            energyRating = energyRating,
            latestCertificateNumberForThisProperty = latestCertificateNumber,
        )
    } else {
        MockEpcData.createEpcRegisterClientEpcFoundResponse(
            energyRating = energyRating,
            expiryDate = expiryDate,
            latestCertificateNumberForThisProperty = latestCertificateNumber,
        )
    }

    private fun stubEpcByCertificateNumber(
        harness: JourneyMapHarness,
        response: String,
    ) {
        whenever(harness.epcRegisterClient.getByRrn(any())).thenAnswer { invocation ->
            if (invocation.getArgument<String>(0) == MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER) {
                epcFound(latestCertificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER)
            } else {
                response
            }
        }
    }

    val scenarios =
        listOf(
            Scenario(
                name = "One Login identity check",
                stepConfigs = setOf(IdentityVerifyingStepConfig::class),
                options =
                    listOf(
                        ScenarioOption("Identity not verified") {
                            whenever(it.identityService.getVerifiedIdentityData(any())).thenReturn(null)
                        },
                        ScenarioOption("Identity verified") {
                            whenever(it.identityService.getVerifiedIdentityData(any())).thenReturn(verifiedIdentity)
                        },
                    ),
            ),
            Scenario(
                name = "EPC register search by address",
                stepConfigs = setOf(EpcLookupByUprnStepConfig::class),
                options =
                    listOf(
                        ScenarioOption("No EPC found") {
                            whenever(it.epcRegisterClient.getByUprn(any())).thenReturn(MockEpcData.epcRegisterClientEpcNotFoundResponse)
                        },
                        ScenarioOption("EPC found") { whenever(it.epcRegisterClient.getByUprn(any())).thenReturn(epcFound()) },
                        ScenarioOption("Expired EPC found") {
                            whenever(it.epcRegisterClient.getByUprn(any()))
                                .thenReturn(epcFound(expiryDate = MockEpcData.expiryDateInThePast))
                        },
                        ScenarioOption("Low rated EPC found") {
                            whenever(it.epcRegisterClient.getByUprn(any())).thenReturn(epcFound(energyRating = "F"))
                        },
                    ),
            ),
            Scenario(
                name = "EPC register search by certificate number",
                stepConfigs = setOf(FindYourEpcStepConfig::class),
                options =
                    listOf(
                        ScenarioOption("Certificate found") { stubEpcByCertificateNumber(it, epcFound()) },
                        ScenarioOption("Certificate not found") {
                            stubEpcByCertificateNumber(it, MockEpcData.epcRegisterClientEpcNotFoundResponse)
                        },
                        ScenarioOption("Superseded certificate found") {
                            stubEpcByCertificateNumber(it, epcFound(latestCertificateNumber = MockEpcData.SECONDARY_EPC_CERTIFICATE_NUMBER))
                        },
                        ScenarioOption("Expired certificate found") {
                            stubEpcByCertificateNumber(it, epcFound(expiryDate = MockEpcData.expiryDateInThePast))
                        },
                        ScenarioOption("Low rated certificate found") { stubEpcByCertificateNumber(it, epcFound(energyRating = "F")) },
                    ),
            ),
        )

    val exemptions =
        listOf(
            Exemption(
                journey = "landlordRegistration",
                stepConfig = SelectAddressStepConfig::class,
                mode = SelectAddressMode.ADDRESS_ALREADY_REGISTERED.name,
                reason = "Only property registration seed data includes addresses that are already registered",
            ),
            Exemption(
                journey = null,
                stepConfig = HasAnyGovBodyMembersStepConfig::class,
                mode = AnyMembers.SOME_MEMBERS.name,
                reason = "Only reached by going back to an earlier page after adding governing body members",
            ),
            Exemption(
                journey = null,
                stepConfig = GovBodyMembersBackRoutingStepConfig::class,
                mode = AnyMembers.NO_MEMBERS.name,
                reason = "Only chooses where the back link goes, and the generator does not follow back links",
            ),
            Exemption(
                journey = null,
                stepConfig = HasAnyJointLandlordsInvitedStepConfig::class,
                mode = AnyLandlordsInvited.SOME_LANDLORDS.name,
                reason = "Only reached by going back into the joint landlords task after inviting someone",
            ),
            Exemption(
                journey = null,
                stepConfig = HasAnyInCollectionStepConfig::class,
                mode = AnyMembers.SOME_MEMBERS.name,
                reason = "Only reached by going back to the certificate date page after uploading a file",
            ),
            Exemption(
                journey = "updateGoverningBody",
                stepConfig = RemoveGovBodyMemberStepConfig::class,
                mode = AnyMembers.NO_MEMBERS.name,
                reason = "The last governing body member cannot be removed when updating the governing body",
            ),
        ) + provideLaterExemptions() + verifiedIdentityExemptions()

    private fun verifiedIdentityExemptions() =
        listOf("updateLandlordName", "updateLandlordDateOfBirth").map { journey ->
            Exemption(
                journey,
                CheckLandlordIdentityVerifiedStepConfig::class,
                IdentityVerificationStatus.VERIFIED.name,
                "Landlords whose identity was verified through One Login are not offered this change",
            )
        }

    // Updating a property's details does not offer providing them later, so the update journeys never produce these outcomes
    private fun provideLaterExemptions(): List<Exemption> {
        val reason = "Providing this later is only offered when registering a property"
        return listOf(
            Triple("updateElectricalSafety", HasElectricalCertStepConfig::class, HasElectricalCertMode.PROVIDE_THIS_LATER.name),
            Triple("updateGasSafety", BeforePdjb1022HasGasCertStepConfig::class, BeforePdjb1022HasGasCertMode.PROVIDE_THIS_LATER.name),
            Triple("updateGasSafety", HasGasSupplyStepConfig::class, HasGasSupplyMode.PROVIDE_LATER.name),
            Triple("updateEpc", HasEpcStepConfig::class, HasEpcMode.PROVIDE_LATER.name),
            Triple("updateLicensing", LicensingTypeStepConfig::class, LicensingTypeMode.PROVIDE_LATER.name),
            Triple("updateHouseholdsAndTenants", HouseholdStepConfig::class, HouseholdMode.PROVIDE_THIS_LATER.name),
            Triple("updateTenancyDetails", HouseholdStepConfig::class, HouseholdMode.PROVIDE_THIS_LATER.name),
        ).map { (journey, stepConfig, mode) -> Exemption(journey, stepConfig, mode, reason) }
    }

    fun applyDefaults(harness: JourneyMapHarness) = scenarios.forEach { it.options.first().arrange(harness) }

    fun scenariosFor(stepConfigNames: Set<String>) = scenarios.filter { it.appliesTo(stepConfigNames) }

    fun exemptionFor(
        journey: String,
        stepConfig: String,
        mode: String,
    ) = exemptions.firstOrNull {
        (it.journey == null || it.journey == journey) && it.stepConfig.simpleName == stepConfig && it.mode == mode
    }
}
