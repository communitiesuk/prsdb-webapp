package uk.gov.communities.prsdb.webapp.journeys.shared.tasks

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.AddressInUkStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.ManualAddressStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.SelectAddressStep
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.AddressInUkFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.ManualAddressFormModel

class InternationalAddressTaskTests {
    private val ukManualAddressFormModel =
        ManualAddressFormModel().apply {
            addressLineOne = "1 Test Street"
            townOrCity = "London"
            postcode = "EG1 2AA"
        }

    private val internationalManualAddressFormModel =
        ManualAddressFormModel().apply {
            addressLineOne = "1 Rue Test"
            townOrCity = "Paris"
            postcode = "75001"
        }

    @Test
    fun `getAddress returns the inner address task's address when the user lives in the UK`() {
        // Arrange
        val task = taskWithSteps(addressInUk = true)

        // Act
        val result = task.getAddress()

        // Assert
        val expectedResult =
            AddressDataModel.fromManualAddressData(
                ukManualAddressFormModel.addressLineOne!!,
                ukManualAddressFormModel.townOrCity!!,
                ukManualAddressFormModel.postcode!!,
            )
        assertEquals(expectedResult, result)
    }

    @Test
    fun `getAddress returns the non-UK manual address flagged as international when the user does not live in the UK`() {
        // Arrange
        val task = taskWithSteps(addressInUk = false)

        // Act
        val result = task.getAddress()

        // Assert
        val expectedResult =
            AddressDataModel
                .fromManualAddressData(
                    internationalManualAddressFormModel.addressLineOne!!,
                    internationalManualAddressFormModel.townOrCity!!,
                    internationalManualAddressFormModel.postcode!!,
                ).copy(isInternational = true)
        assertEquals(expectedResult, result)
    }

    @Test
    fun `getAddress throws if the user has not said whether they live in the UK`() {
        val task = taskWithSteps(addressInUk = null)
        assertThrows(NotNullFormModelValueIsNullException::class.java) { task.getAddress() }
    }

    // Both manual address steps have answers, so each test also checks the unselected branch's answer is ignored
    private fun taskWithSteps(addressInUk: Boolean?): InternationalAddressTask {
        val addressInUkFormModel = addressInUk?.let { AddressInUkFormModel().apply { this.addressInUk = it } }
        val addressInUkStep = mock<AddressInUkStep>().apply { whenever(this.formModelOrNull).thenReturn(addressInUkFormModel) }
        val selectAddressStep = mock<SelectAddressStep>().apply { whenever(this.formModelOrNull).thenReturn(null) }
        val manualAddressStep =
            mock<ManualAddressStep>().apply { whenever(this.formModelOrNull).thenReturn(ukManualAddressFormModel) }
        val internationalManualAddressStep =
            mock<ManualAddressStep>().apply { whenever(this.formModelOrNull).thenReturn(internationalManualAddressFormModel) }
        return object : InternationalAddressTask(
            mock(),
            addressInUkStep,
            mock(),
            selectAddressStep,
            mock(),
            manualAddressStep,
            internationalManualAddressStep,
        ) {
            override val addressInUkContentProperties = emptyMap<String, Any?>()
            override val addressInUkMissingAnswerErrorKey = "error.key"
        }
    }
}
