package uk.gov.communities.prsdb.webapp.journeys.shared.states

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.NotNullFormModelValueIsNullException
import uk.gov.communities.prsdb.webapp.journeys.AbstractJourneyState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.states.IdentityState
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.ConfirmIdentityStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.DateOfBirthStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityNotVerifiedStep
import uk.gov.communities.prsdb.webapp.journeys.landlordRegistration.stepConfig.IdentityVerifyingStep
import uk.gov.communities.prsdb.webapp.journeys.shared.stepConfig.NameStep
import uk.gov.communities.prsdb.webapp.models.dataModels.VerifiedIdentityDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.DateOfBirthFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NameFormModel
import java.time.LocalDate

class IdentityStateTests {
    @Test
    fun `getNotNullVerifiedIdentity throws if identity not present`() {
        val state = buildTestIdentityState(verifiedIdentity = null)
        assertThrows(NotNullFormModelValueIsNullException::class.java) { state.getNotNullVerifiedIdentity() }
    }

    @Test
    fun `getNotNullVerifiedIdentity returns identity if present`() {
        // Arrange
        val identity = VerifiedIdentityDataModel("John Doe", LocalDate.of(1990, 1, 1))
        val state = buildTestIdentityState(verifiedIdentity = identity)

        // Act & Assert
        assertEquals(identity, state.getNotNullVerifiedIdentity())
    }

    @Test
    fun `getIsIdentityVerified returns true if identity present`() {
        val state = buildTestIdentityState(verifiedIdentity = VerifiedIdentityDataModel("John Doe", LocalDate.of(1990, 1, 1)))
        assertTrue(state.getIsIdentityVerified())
    }

    @Test
    fun `getIsIdentityVerified returns false if identity not present`() {
        val state = buildTestIdentityState(verifiedIdentity = null)
        assertFalse(state.getIsIdentityVerified())
    }

    @Test
    fun `getName returns name from verified identity if present`() {
        // Arrange
        val identity = VerifiedIdentityDataModel("John Doe", LocalDate.of(1990, 1, 1))
        val state = buildTestIdentityState(verifiedIdentity = identity)

        // Act & Assert
        assertEquals(identity.name, state.getName())
    }

    @Test
    fun `getName returns name from nameStep if identity not present`() {
        // Arrange
        val nameFormModel = NameFormModel().apply { name = "Jane Smith" }
        val state = buildTestIdentityState(verifiedIdentity = null, nameFormModel = nameFormModel)

        // Act & Assert
        assertEquals(nameFormModel.name, state.getName())
    }

    @Test
    fun `getName throws if neither identity nor nameStep has name`() {
        // Arrange
        val nameFormModel = NameFormModel().apply { name = null }
        val state = buildTestIdentityState(verifiedIdentity = null, nameFormModel = nameFormModel)

        // Act & Assert
        assertThrows(NotNullFormModelValueIsNullException::class.java) { state.getName() }
    }

    @Test
    fun `getDateOfBirth returns dob from verified identity if present`() {
        // Arrange
        val identity = VerifiedIdentityDataModel("John Doe", LocalDate.of(1990, 1, 1))
        val state = buildTestIdentityState(verifiedIdentity = identity)

        // Act & Assert
        assertEquals(identity.birthDate, state.getDateOfBirth())
    }

    @Test
    fun `getDateOfBirth returns dob from dateOfBirthStep if identity not present`() {
        // Arrange
        val dobFormModel =
            DateOfBirthFormModel().apply {
                day = "5"
                month = "5"
                year = "1995"
            }
        val state = buildTestIdentityState(verifiedIdentity = null, dobFormModel = dobFormModel)

        // Act & Assert
        val expectedDob = dobFormModel.toLocalDateOrNull()
        assertEquals(expectedDob, state.getDateOfBirth())
    }

    @Test
    fun `getDateOfBirth throws if neither identity nor dateOfBirthStep has valid dob`() {
        // Arrange
        val dateOfBirthFormModel = DateOfBirthFormModel().apply { day = "invalid" }
        val state = buildTestIdentityState(verifiedIdentity = null, dobFormModel = dateOfBirthFormModel)

        // Act & Assert
        assertThrows(NotNullFormModelValueIsNullException::class.java) { state.getDateOfBirth() }
    }

    private fun buildTestIdentityState(
        nameFormModel: NameFormModel? = null,
        dobFormModel: DateOfBirthFormModel? = null,
        verifiedIdentity: VerifiedIdentityDataModel? = null,
    ): IdentityState =
        object : AbstractJourneyState(journeyStateService = mock()), IdentityState {
            override val identityVerifyingStep = mock<IdentityVerifyingStep>()

            override val confirmIdentityStep = mock<ConfirmIdentityStep>()

            override val identityNotVerifiedStep = mock<IdentityNotVerifiedStep>()

            override val nameStep =
                mock<NameStep>().apply {
                    whenever(this.formModel).thenReturn(nameFormModel)
                }

            override val dateOfBirthStep =
                mock<DateOfBirthStep>().apply {
                    whenever(this.formModel).thenReturn(dobFormModel)
                }

            override var verifiedIdentity: VerifiedIdentityDataModel? = verifiedIdentity
        }
}
