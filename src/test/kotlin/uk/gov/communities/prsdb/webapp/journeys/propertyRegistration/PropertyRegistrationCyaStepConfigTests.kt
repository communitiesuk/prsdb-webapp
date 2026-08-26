package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.lenient
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.context.MessageSource
import uk.gov.communities.prsdb.webapp.config.managers.FeatureFlagManager
import uk.gov.communities.prsdb.webapp.constants.DELEGATE_TO_LETTING_AGENT
import uk.gov.communities.prsdb.webapp.constants.PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING
import uk.gov.communities.prsdb.webapp.constants.enums.WhoProvidesRentalDetails
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.BedroomsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HasJointLandlordsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LettingAgentEmailStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.LocalCouncilStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.OwnershipTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyRegistrationCyaStepConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.PropertyTypeStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.ProvideTenancyDetailsLaterStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.WhoProvidesRentalDetailsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.HouseholdsAndTenantsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.JointLandlordsPropertyRegistrationTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.OwnershipAndLandlordsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.PropertyRegistrationAddressTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.TenancyDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.tasks.WhoProvidesDetailsTask
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.ComplianceDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.LicensingDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AllowLettingAgentEmailFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.HasJointLandlordsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfBedroomsFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OwnershipTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.PropertyTypeFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.WhoProvidesRentalDetailsFormModel
import uk.gov.communities.prsdb.webapp.models.viewModels.summaryModels.SummaryListRowViewModel
import uk.gov.communities.prsdb.webapp.services.LocalCouncilService

@ExtendWith(MockitoExtension::class)
class PropertyRegistrationCyaStepConfigTests {
    @Mock
    private lateinit var mockLocalCouncilService: LocalCouncilService

    @Mock
    private lateinit var mockLicensingDetailsHelper: LicensingDetailsHelper

    @Mock
    private lateinit var mockOccupancyDetailsHelper: OccupancyDetailsHelper

    @Mock
    private lateinit var mockComplianceDetailsHelper: ComplianceDetailsHelper

    @Mock
    private lateinit var mockMessageSource: MessageSource

    @Mock
    private lateinit var mockFeatureFlagManager: FeatureFlagManager

    @Mock
    private lateinit var mockState: PropertyRegistrationJourneyState

    @Mock
    private lateinit var mockPropertyDetailsTask: PropertyDetailsTask

    @Mock
    private lateinit var mockAddressTask: PropertyRegistrationAddressTask

    @Mock
    private lateinit var mockLookupAddressStep: LookupAddressStep

    @Mock
    private lateinit var mockLocalCouncilStep: LocalCouncilStep

    @Mock
    private lateinit var mockPropertyTypeStep: PropertyTypeStep

    @Mock
    private lateinit var mockPropertyTypeFormModel: PropertyTypeFormModel

    @Mock
    private lateinit var mockOwnershipAndLandlordsTask: OwnershipAndLandlordsTask

    @Mock
    private lateinit var mockOwnershipTypeStep: OwnershipTypeStep

    @Mock
    private lateinit var mockOwnershipTypeFormModel: OwnershipTypeFormModel

    @Mock
    private lateinit var mockJointLandlordsTask: JointLandlordsPropertyRegistrationTask

    @Mock
    private lateinit var mockHasJointLandlordsStep: HasJointLandlordsStep

    @Mock
    private lateinit var mockHasJointLandlordsFormModel: HasJointLandlordsFormModel

    @Mock
    private lateinit var mockBedroomsStep: BedroomsStep

    @Mock
    private lateinit var mockBedroomsFormModel: NumberOfBedroomsFormModel

    @Mock
    private lateinit var mockTenancyDetailsTask: TenancyDetailsTask

    @Mock
    private lateinit var mockHouseholdsAndTenantsTask: HouseholdsAndTenantsTask

    @Mock
    private lateinit var mockHouseholdStep: HouseholdStep

    @Mock
    private lateinit var mockProvideTenancyDetailsLaterStep: ProvideTenancyDetailsLaterStep

    @Mock
    private lateinit var mockWhoProvidesDetailsTask: WhoProvidesDetailsTask

    @Mock
    private lateinit var mockWhoProvidesRentalDetailsStep: WhoProvidesRentalDetailsStep

    @Mock
    private lateinit var mockWhoProvidesRentalDetailsFormModel: WhoProvidesRentalDetailsFormModel

    @Mock
    private lateinit var mockLettingAgentEmailFormModel: AllowLettingAgentEmailFormModel

    @Mock
    private lateinit var mockLettingAgentEmailStep: LettingAgentEmailStep

    private lateinit var stepConfig: PropertyRegistrationCyaStepConfig

    @BeforeEach
    fun setUp() {
        stepConfig =
            PropertyRegistrationCyaStepConfig(
                mockLocalCouncilService,
                mockLicensingDetailsHelper,
                mockOccupancyDetailsHelper,
                mockComplianceDetailsHelper,
                mockMessageSource,
                mockFeatureFlagManager,
            )
        lenient().`when`(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
        lenient().`when`(mockState.propertyDetailsTask).thenReturn(mockPropertyDetailsTask)
        lenient().`when`(mockPropertyDetailsTask.addressTask).thenReturn(mockAddressTask)
        lenient().`when`(mockAddressTask.getAddress()).thenReturn(AddressDataModel("1 Test Street", localCouncilId = 1))
        lenient().`when`(mockAddressTask.lookupAddressStep).thenReturn(mockLookupAddressStep)
        lenient().`when`(mockAddressTask.localCouncilStep).thenReturn(mockLocalCouncilStep)
        lenient().`when`(mockLocalCouncilService.retrieveLocalCouncilById(1)).thenReturn(mock())
        lenient().`when`(mockPropertyDetailsTask.propertyTypeStep).thenReturn(mockPropertyTypeStep)
        lenient().`when`(mockPropertyTypeStep.formModel).thenReturn(mockPropertyTypeFormModel)
        lenient().`when`(mockPropertyDetailsTask.bedrooms).thenReturn(mockBedroomsStep)
        lenient().`when`(mockState.bedrooms).thenReturn(mockBedroomsStep)
        lenient().`when`(mockBedroomsStep.formModel).thenReturn(mockBedroomsFormModel)
        lenient().`when`(mockState.ownershipAndLandlordsTask).thenReturn(mockOwnershipAndLandlordsTask)
        lenient().`when`(mockOwnershipAndLandlordsTask.ownershipTypeStep).thenReturn(mockOwnershipTypeStep)
        lenient().`when`(mockOwnershipTypeStep.formModel).thenReturn(mockOwnershipTypeFormModel)
        lenient().`when`(mockOwnershipAndLandlordsTask.jointLandlordsTask).thenReturn(mockJointLandlordsTask)
        lenient().`when`(mockJointLandlordsTask.hasJointLandlordsStep).thenReturn(mockHasJointLandlordsStep)
        lenient().`when`(mockHasJointLandlordsStep.formModel).thenReturn(mockHasJointLandlordsFormModel)
        lenient().`when`(mockHasJointLandlordsFormModel.hasJointLandlords).thenReturn(false)
        lenient().`when`(mockState.tenancyDetailsTask).thenReturn(mockTenancyDetailsTask)
        lenient().`when`(mockTenancyDetailsTask.householdsAndTenantsTask).thenReturn(mockHouseholdsAndTenantsTask)
        lenient().`when`(mockHouseholdsAndTenantsTask.households).thenReturn(mockHouseholdStep)
        lenient().`when`(mockHouseholdsAndTenantsTask.provideTenancyDetailsLaterStep).thenReturn(mockProvideTenancyDetailsLaterStep)
        lenient().`when`(mockState.getCyaJourneyId(any())).thenReturn("test-journey-id")
        lenient().`when`(mockComplianceDetailsHelper.getGasSafetyCyaContent(any(), any())).thenReturn(emptyMap())
        lenient().`when`(mockComplianceDetailsHelper.getElectricalSafetyCyaContent(any(), any())).thenReturn(emptyMap())
        lenient().`when`(mockComplianceDetailsHelper.getEpcCyaContent(any(), any())).thenReturn(emptyMap())
        lenient().`when`(mockState.whoProvidesDetailsTask).thenReturn(mockWhoProvidesDetailsTask)
        lenient().`when`(mockWhoProvidesDetailsTask.whoProvidesRentalDetailsStep).thenReturn(mockWhoProvidesRentalDetailsStep)
        lenient().`when`(mockWhoProvidesRentalDetailsStep.formModel).thenReturn(mockWhoProvidesRentalDetailsFormModel)
        lenient().`when`(mockWhoProvidesRentalDetailsStep.formModelIfReachableOrNull).thenReturn(mockWhoProvidesRentalDetailsFormModel)
        lenient().`when`(mockWhoProvidesDetailsTask.lettingAgentEmailStep).thenReturn(mockLettingAgentEmailStep)
        lenient().`when`(mockLettingAgentEmailStep.formModel).thenReturn(mockLettingAgentEmailFormModel)
    }

    @Nested
    inner class RestructureAndSkippingEnabled {
        @BeforeEach
        fun enableRestructureAndSkippingFlag() {
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
        }

        @Test
        fun `chooseTemplate returns restructured CYA template`() {
            assertEquals(
                "forms/restructureAndSkipping/propertyRegistrationCheckAnswersForm",
                stepConfig.chooseTemplate(mockState),
            )
        }

        @Test
        fun `getStepSpecificContent puts occupancyDetails from getRestructuredOccupancySummaryList`() {
            val expectedOccupancyDetails = listOf<SummaryListRowViewModel>()
            whenever(mockOccupancyDetailsHelper.getRestructuredOccupancySummaryList(mockState)).thenReturn(expectedOccupancyDetails)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals(expectedOccupancyDetails, content["occupancyDetails"])
        }

        @Test
        fun `getStepSpecificContent puts tenancyDetails from getRestructuredCheckYourAnswersSummaryList`() {
            val expectedTenancyDetails = listOf<SummaryListRowViewModel>()
            whenever(
                mockOccupancyDetailsHelper.getRestructuredCheckYourAnswersSummaryList(any(), any(), any()),
            ).thenReturn(expectedTenancyDetails)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals(expectedTenancyDetails, content["tenancyDetails"])
        }
    }

    @Nested
    inner class RestructureAndSkippingDisabled {
        @BeforeEach
        fun disableRestructureAndSkippingFlag() {
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(false)
        }

        @Test
        fun `chooseTemplate returns legacy CYA template`() {
            assertEquals(
                "forms/restructureAndSkipping/propertyRegistrationCheckAnswersFormLegacy",
                stepConfig.chooseTemplate(mockState),
            )
        }

        @Test
        fun `getStepSpecificContent puts null for occupancyDetails`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            assertNull(content["occupancyDetails"])
        }

        @Test
        fun `getStepSpecificContent puts tenancyDetails from getCheckYourAnswersSummaryList`() {
            val expectedTenancyDetails = listOf<SummaryListRowViewModel>()
            whenever(mockOccupancyDetailsHelper.getCheckYourAnswersSummaryList(any(), any())).thenReturn(expectedTenancyDetails)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertEquals(expectedTenancyDetails, content["tenancyDetails"])
        }
    }

    @Nested
    inner class LettingAgentDelegationEnabled {
        @BeforeEach
        fun enableDelegationFlags() {
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(true)
            whenever(mockState.isDelegatedToLettingAgent(mockFeatureFlagManager)).thenReturn(true)
        }

        @Test
        fun `getStepSpecificContent includes lettingAgentDelegation when delegated to agent`() {
            whenever(mockWhoProvidesRentalDetailsFormModel.whoProvides).thenReturn(WhoProvidesRentalDetails.LETTING_AGENT)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertTrue(content.containsKey("lettingAgentDelegation"))
            val delegationSection = content["lettingAgentDelegation"] as? List<*>
            assertEquals(2, delegationSection?.size, "Should have 2 rows: who will provide and email placeholder")
            assertEquals(true, content["lettingAgentDelegationBodyText"], "Body text should be shown for letting agent path")
        }

        @Test
        fun `getStepSpecificContent includes lettingAgentDelegation when landlord provides details`() {
            whenever(mockState.isDelegatedToLettingAgent(mockFeatureFlagManager)).thenReturn(false)
            whenever(mockWhoProvidesRentalDetailsFormModel.whoProvides).thenReturn(WhoProvidesRentalDetails.LANDLORD)

            val content = stepConfig.getStepSpecificContent(mockState)
            val delegationSection = content["lettingAgentDelegation"] as? List<*>

            assertTrue(content.containsKey("lettingAgentDelegation"))
            assertEquals(1, delegationSection?.size, "Landlord path should only include who-will-provide row")
            assertEquals(false, content["lettingAgentDelegationBodyText"], "Body text should not be shown for landlord path")
        }

        @Test
        fun `getStepSpecificContent does not include lettingAgentDelegation when whoProvides step is unreachable`() {
            whenever(mockWhoProvidesRentalDetailsStep.formModelIfReachableOrNull).thenReturn(null)

            val content = stepConfig.getStepSpecificContent(mockState)

            assertTrue(!content.containsKey("lettingAgentDelegation") || content["lettingAgentDelegation"] == null)
        }
    }

    @Nested
    inner class LettingAgentDelegationDisabled {
        @BeforeEach
        fun disableDelegationFlag() {
            whenever(mockFeatureFlagManager.checkFeature(PROPERTY_REGISTRATION_RESTRUCTURE_AND_SKIPPING)).thenReturn(true)
            whenever(mockFeatureFlagManager.checkFeature(DELEGATE_TO_LETTING_AGENT)).thenReturn(false)
        }

        @Test
        fun `getStepSpecificContent does not include lettingAgentDelegation when delegate feature is disabled`() {
            val content = stepConfig.getStepSpecificContent(mockState)

            assertTrue(!content.containsKey("lettingAgentDelegation") || content["lettingAgentDelegation"] == null)
        }
    }
}
