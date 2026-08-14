package uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.GoverningBodyMemberType
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.OrgGovBodyMembersState
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.LookupAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.tasks.AddressTask
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.dataModels.GoverningBodyMemberDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.GoverningBodyMemberNameFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.LookupAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyMemberDobFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.OrgGovBodyWhoToProvideFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.AlwaysTrueValidator

@ExtendWith(MockitoExtension::class)
class SaveGovBodyMemberStepConfigTests {
    @Mock
    lateinit var mockState: OrgGovBodyMembersState

    @Mock
    lateinit var orgGovBodyMemberNameStep: OrgGovBodyMemberNameStep

    @Mock
    lateinit var orgGovBodyWhoToProvideStep: OrgGovBodyWhoToProvideStep

    @Mock
    lateinit var orgGovBodyMemberDobStep: OrgGovBodyMemberDobStep

    @Mock
    lateinit var govBodyMemberAddressTask: AddressTask

    @Mock
    lateinit var lookupAddressStep: LookupAddressStep

    @Mock
    lateinit var selectAddressStep: SelectAddressStep

    @Mock
    lateinit var manualAddressStep: ManualAddressStep

    @Test
    fun `afterStepIsReached replaces existing member when editing`() {
        val existingMember = createMember(name = "Existing member")
        val stepConfig = setupStepConfig()
        whenever(mockState.editingGovBodyMemberId).thenReturn(1)

        whenever(mockState.governingBodyMembersMap).thenReturn(mapOf(1 to existingMember))

        stepConfig.afterStepIsReached(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, GoverningBodyMemberDataModel>>()
        verify(mockState).governingBodyMembersMap = updatedMapCaptor.capture()
        assertEquals(1, updatedMapCaptor.firstValue.size)
        verify(mockState).editingGovBodyMemberId = null
    }

    @Test
    fun `afterStepIsReached adds new member when not editing`() {
        val stepConfig = setupStepConfig()
        whenever(mockState.editingGovBodyMemberId).thenReturn(null)
        whenever(mockState.governingBodyMembersMap).thenReturn(emptyMap())
        whenever(mockState.nextGoverningBodyMemberId).thenReturn(3)

        stepConfig.afterStepIsReached(mockState)

        val updatedMapCaptor = argumentCaptor<Map<Int, GoverningBodyMemberDataModel>>()
        verify(mockState).governingBodyMembersMap = updatedMapCaptor.capture()
        assertEquals(1, updatedMapCaptor.firstValue.size)
        assert(3 in updatedMapCaptor.firstValue)
        verify(mockState).nextGoverningBodyMemberId = 4
        verify(mockState).editingGovBodyMemberId = null
    }

    private fun setupStepConfig(): SaveGovBodyMemberStepConfig {
        val stepConfig = SaveGovBodyMemberStepConfig()
        stepConfig.urlPath = "save-governing-body-member"
        stepConfig.validator = AlwaysTrueValidator()

        val nameFormModel = GoverningBodyMemberNameFormModel().apply { name = "Alex Example" }
        val whoToProvideFormModel = OrgGovBodyWhoToProvideFormModel().apply { whoToProvide = GoverningBodyMemberType.DIRECTOR }
        val dobFormModel =
            OrgGovBodyMemberDobFormModel().apply {
                day = "2"
                month = "1"
                year = "1980"
            }
        val address = AddressDataModel(singleLineAddress = "1 Test Street, Test Town, TT1 1TT", postcode = "TT1 1TT")
        val lookupFormModel =
            LookupAddressFormModel().apply {
                postcode = "TT1 1TT"
                houseNameOrNumber = "1"
            }
        val selectFormModel = SelectAddressFormModel().apply { this.address = "1 Test Street, Test Town, TT1 1TT" }
        val manualFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "1 Test Street"
                townOrCity = "Test Town"
                postcode = "TT1 1TT"
            }

        whenever(mockState.orgGovBodyMemberNameStep).thenReturn(orgGovBodyMemberNameStep)
        whenever(orgGovBodyMemberNameStep.formModelOrNull).thenReturn(nameFormModel)
        whenever(mockState.orgGovBodyWhoToProvideStep).thenReturn(orgGovBodyWhoToProvideStep)
        whenever(orgGovBodyWhoToProvideStep.formModelOrNull).thenReturn(whoToProvideFormModel)
        whenever(mockState.orgGovBodyMemberDobStep).thenReturn(orgGovBodyMemberDobStep)
        whenever(orgGovBodyMemberDobStep.formModelOrNull).thenReturn(dobFormModel)
        whenever(mockState.govBodyMemberAddressTask).thenReturn(govBodyMemberAddressTask)
        whenever(govBodyMemberAddressTask.getAddress()).thenReturn(address)
        whenever(govBodyMemberAddressTask.lookupAddressStep).thenReturn(lookupAddressStep)
        whenever(lookupAddressStep.formModelOrNull).thenReturn(lookupFormModel)
        whenever(govBodyMemberAddressTask.selectAddressStep).thenReturn(selectAddressStep)
        whenever(selectAddressStep.formModelOrNull).thenReturn(selectFormModel)
        whenever(govBodyMemberAddressTask.manualAddressStep).thenReturn(manualAddressStep)
        whenever(manualAddressStep.formModelOrNull).thenReturn(manualFormModel)

        return stepConfig
    }

    private fun createMember(name: String) =
        GoverningBodyMemberDataModel(
            name = name,
            type = GoverningBodyMemberType.DIRECTOR,
            dateOfBirth = LocalDate(1980, 1, 2),
            address = AddressDataModel(singleLineAddress = "1 Test Street, Test Town, TT1 1TT", postcode = "TT1 1TT"),
        )
}
