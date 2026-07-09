package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.JourneyStateService
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectAddressFormModel

// AddressTask owns its own steps and IS its own route-scoped AddressState. These tests prove that two instances,
// distinguished only by the route bound at build time, keep their stored data completely separate (so the same
// task can be added to a journey more than once), that a null route preserves the pre-existing bare keys, and that
// the ported AddressState address-resolution behaviour still works.
class AddressTaskTests {
    private val session = mutableMapOf<String, Any?>()
    private lateinit var journeyStateService: JourneyStateService

    @BeforeEach
    fun setUp() {
        // Back the cached-variable storage (journeyStateService) and the step-data storage (the self-made state
        // delegate) with real maps so we can observe exactly which keys each routed instance reads and writes.
        journeyStateService = mock()
        whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
        doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .setValue(any(), anyOrNull())

        whenever(journeyStateService.getSubmittedStepData()).thenAnswer { session["journeyData"] }
        doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .addSingleStepData(any(), any())
    }

    private fun taskFor(route: String?): AddressTask =
        object : AddressTask(journeyStateService, mock(), mock(), mock(), mock()) {
            override val lookupAddressContentProperties = emptyMap<String, Any?>()
            override val manualAddressContentProperties = emptyMap<String, Any?>()
        }.apply { bindRoute(route) }

    @Test
    fun `two routed instances keep their cached variables separate`() {
        val leadTrusteeAddress = taskFor("lead-trustee-address")
        val ownAddress = taskFor(null)

        leadTrusteeAddress.cachedSelectedAddress = "10 Downing Street"

        assertEquals("10 Downing Street", leadTrusteeAddress.cachedSelectedAddress)
        assertNull(ownAddress.cachedSelectedAddress)
    }

    @Test
    fun `two routed instances keep their step data separate`() {
        val leadTrusteeAddress = taskFor("lead-trustee-address")
        val ownAddress = taskFor(null)

        leadTrusteeAddress.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        assertEquals("SW1A 2AA", leadTrusteeAddress.getStepData("lookup-address")?.get("postcode"))
        assertNull(ownAddress.getStepData("lookup-address"))
    }

    @Test
    fun `a bound route prefixes cached variable and step data keys`() {
        val leadTrusteeAddress = taskFor("lead-trustee-address")

        leadTrusteeAddress.cachedSelectedAddress = "10 Downing Street"
        leadTrusteeAddress.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        assertTrue(session.containsKey("lead-trustee-address/cachedSelectedAddress"))
        assertTrue(session.containsKey("lead-trustee-address/lookup-address"))
    }

    @Test
    fun `a null route leaves cached variable and step data keys bare`() {
        val ownAddress = taskFor(null)

        ownAddress.cachedSelectedAddress = "10 Downing Street"
        ownAddress.addStepData("lookup-address", mapOf("postcode" to "SW1A 2AA"))

        assertTrue(session.containsKey("cachedSelectedAddress"))
        assertTrue(session.containsKey("lookup-address"))
    }

    @Test
    fun `getManualAddressOrNull returns null if manualAddressStep's form model is null`() {
        val task = taskWithSteps(manualAddressFormModel = null)
        assertNull(task.getManualAddressOrNull())
    }

    @Test
    fun `getManualAddressOrNull throws if manualAddressStep's form model is invalid`() {
        val task =
            taskWithSteps(
                manualAddressFormModel =
                    ManualAddressFormModel().apply {
                        addressLineOne = "Flat 1"
                        townOrCity = null
                        postcode = "ZZ1 1ZZ"
                    },
            )
        assertThrows(NotNullFormModelValueIsNullException::class.java) { task.getManualAddressOrNull() }
    }

    @Test
    fun `getManualAddressOrNull returns address if manualAddressStep's form model is valid`() {
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "Flat 1"
                townOrCity = "Town"
                postcode = "ZZ1 1ZZ"
                addressLineTwo = "Building"
                county = "County"
            }
        val task = taskWithSteps(manualAddressFormModel = manualAddressFormModel)

        val expectedResult =
            AddressDataModel.fromManualAddressData(
                manualAddressFormModel.addressLineOne!!,
                manualAddressFormModel.townOrCity!!,
                manualAddressFormModel.postcode!!,
                manualAddressFormModel.addressLineTwo,
                manualAddressFormModel.county,
            )
        assertEquals(expectedResult, task.getManualAddressOrNull())
    }

    @Test
    fun `getAddress returns selected address if present`() {
        val address = AddressDataModel("1 Test St, City, AB1 2CD")
        val selectForm = SelectAddressFormModel().apply { this.address = address.singleLineAddress }
        val task = taskWithSteps(selectAddressFormModel = selectForm, cachedAddresses = listOf(address))

        assertEquals(address, task.getAddress())
    }

    @Test
    fun `getAddress returns manual address if present and there's no selected address`() {
        val manualAddressFormModel =
            ManualAddressFormModel().apply {
                addressLineOne = "Flat 1"
                townOrCity = "Town"
                postcode = "ZZ1 1ZZ"
            }
        val task = taskWithSteps(selectAddressFormModel = null, manualAddressFormModel = manualAddressFormModel)

        val expectedResult =
            AddressDataModel.fromManualAddressData(
                manualAddressFormModel.addressLineOne!!,
                manualAddressFormModel.townOrCity!!,
                manualAddressFormModel.postcode!!,
            )
        assertEquals(expectedResult, task.getAddress())
    }

    @Test
    fun `getAddress throws if neither selected nor manual address present`() {
        val task = taskWithSteps(selectAddressFormModel = null, manualAddressFormModel = null)
        assertThrows(NotNullFormModelValueIsNullException::class.java) { task.getAddress() }
    }

    private fun taskWithSteps(
        selectAddressFormModel: SelectAddressFormModel? = null,
        manualAddressFormModel: ManualAddressFormModel? = null,
        cachedAddresses: List<AddressDataModel>? = null,
    ): AddressTask {
        val selectAddressStep = mock<SelectAddressStep>().apply { whenever(this.formModelOrNull).thenReturn(selectAddressFormModel) }
        val manualAddressStep = mock<ManualAddressStep>().apply { whenever(this.formModelOrNull).thenReturn(manualAddressFormModel) }
        return object : AddressTask(journeyStateService, mock(), selectAddressStep, mock(), manualAddressStep) {
            override val lookupAddressContentProperties = emptyMap<String, Any?>()
            override val manualAddressContentProperties = emptyMap<String, Any?>()
        }.apply {
            bindRoute(null)
            this.cachedAddresses = cachedAddresses
        }
    }
}
