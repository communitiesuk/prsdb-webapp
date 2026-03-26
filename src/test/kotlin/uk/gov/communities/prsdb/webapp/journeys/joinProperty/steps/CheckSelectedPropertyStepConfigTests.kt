package uk.gov.communities.prsdb.webapp.journeys.joinProperty.steps

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.communities.prsdb.webapp.exceptions.PrsdbWebException
import uk.gov.communities.prsdb.webapp.journeys.joinProperty.states.JoinPropertyAddressSearchState
import uk.gov.communities.prsdb.webapp.models.dataModels.AddressDataModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.SelectPropertyFormModel
import uk.gov.communities.prsdb.webapp.services.AddressAvailabilityService
import uk.gov.communities.prsdb.webapp.testHelpers.JourneyTestHelper.Companion.setMockUser

@ExtendWith(MockitoExtension::class)
class CheckSelectedPropertyStepConfigTests {
    @Mock
    lateinit var mockAddressAvailabilityService: AddressAvailabilityService

    @Mock
    lateinit var mockState: JoinPropertyAddressSearchState

    @Mock
    lateinit var mockSelectPropertyStep: SelectPropertyStep

    private val testUserId = "test-user-id"
    private val testUprn = 123456L
    private val testAddress = "1 Example Road, London, EG1 2AB"

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `mode returns PROPERTY_NOT_REGISTERED when address is not owned`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        setupStateWithSelectedProperty()
        whenever(mockAddressAvailabilityService.isAddressOwned(testUprn)).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(SelectedPropertyCheckResult.PROPERTY_NOT_REGISTERED, result)
    }

    @Test
    fun `mode returns ALREADY_LANDLORD when address is owned by the current user`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        setupStateWithSelectedProperty()
        setMockUser(testUserId)
        whenever(mockAddressAvailabilityService.isAddressOwned(testUprn)).thenReturn(true)
        whenever(mockAddressAvailabilityService.isAddressOwnedByUser(testUprn, testUserId)).thenReturn(true)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(SelectedPropertyCheckResult.ALREADY_LANDLORD, result)
    }

    @Test
    fun `mode returns ELIGIBLE_TO_JOIN when address is registered but not owned by the current user`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        setupStateWithSelectedProperty()
        setMockUser(testUserId)
        whenever(mockAddressAvailabilityService.isAddressOwned(testUprn)).thenReturn(true)
        whenever(mockAddressAvailabilityService.isAddressOwnedByUser(testUprn, testUserId)).thenReturn(false)

        // Act
        val result = stepConfig.mode(mockState)

        // Assert
        assertEquals(SelectedPropertyCheckResult.ELIGIBLE_TO_JOIN, result)
    }

    @Test
    fun `mode throws when no property is selected`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        val formModel = SelectPropertyFormModel().apply { selectedOption = null }
        whenever(mockState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
        whenever(mockSelectPropertyStep.formModel).thenReturn(formModel)

        // Act & Assert
        assertThrows<PrsdbWebException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `mode throws when no matching address found for selected option`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        val formModel = SelectPropertyFormModel().apply { selectedOption = testAddress }
        whenever(mockState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
        whenever(mockSelectPropertyStep.formModel).thenReturn(formModel)
        whenever(mockState.getMatchingAddress(testAddress)).thenReturn(null)

        // Act & Assert
        assertThrows<PrsdbWebException> { stepConfig.mode(mockState) }
    }

    @Test
    fun `mode throws when selected address has no UPRN`() {
        // Arrange
        val stepConfig = CheckSelectedPropertyStepConfig(mockAddressAvailabilityService)
        val formModel = SelectPropertyFormModel().apply { selectedOption = testAddress }
        whenever(mockState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
        whenever(mockSelectPropertyStep.formModel).thenReturn(formModel)
        whenever(mockState.getMatchingAddress(testAddress)).thenReturn(AddressDataModel(testAddress, uprn = null))

        // Act & Assert
        assertThrows<PrsdbWebException> { stepConfig.mode(mockState) }
    }

    private fun setupStateWithSelectedProperty() {
        val formModel = SelectPropertyFormModel().apply { selectedOption = testAddress }
        whenever(mockState.selectPropertyStep).thenReturn(mockSelectPropertyStep)
        whenever(mockSelectPropertyStep.formModel).thenReturn(formModel)
        whenever(mockState.getMatchingAddress(testAddress)).thenReturn(
            AddressDataModel(testAddress, uprn = testUprn),
        )
    }
}
