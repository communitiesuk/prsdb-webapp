package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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

class AddressTaskTests {
    private val session = mutableMapOf<String, Any?>()
    private lateinit var journeyStateService: JourneyStateService

    @BeforeEach
    fun setUp() {
        // Back the cached-variable storage (journeyStateService) with a real map so the cachedAddresses delegate
        // used by the address-resolution tests reads and writes correctly.
        journeyStateService = mock()
        whenever(journeyStateService.getValue(any())).thenAnswer { session[it.getArgument<String>(0)] }
        doAnswer { session[it.getArgument<String>(0)] = it.getArgument(1) }
            .whenever(journeyStateService)
            .setValue(any(), anyOrNull())
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
            override val selectAddressContentProperties = emptyMap<String, Any?>()
            override val manualAddressContentProperties = emptyMap<String, Any?>()
        }.apply {
            bindRoute(null)
            this.cachedAddresses = cachedAddresses
        }
    }
}
