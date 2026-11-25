package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.states.FreeholdersState
import uk.gov.communities.prsdb.webapp.models.dataModels.FreeholderDataModel
import uk.gov.communities.prsdb.webapp.services.UserToLandlordService
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData

@ExtendWith(MockitoExtension::class)
class AddLoggedInLandlordFreeholderStepConfigTests {
    @Mock
    lateinit var mockState: FreeholdersState

    @Mock
    lateinit var mockUserToLandlordService: UserToLandlordService

    private val existingFreeholder = FreeholderDataModel(detailsKnown = false)

    @Test
    fun `adds an individual landlord's name and email as a freeholder when they are a joint freeholder`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(true)
        whenever(mockState.freeholdersMap).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(null)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createIndividualLandlord(name = "Ind Landlord", email = "ind@example.com"))

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockState).freeholdersMap =
            mapOf(1 to FreeholderDataModel(detailsKnown = true, name = "Ind Landlord", email = "ind@example.com"))
        verify(mockState).nextFreeholderId = 2
        verify(mockState).loggedInLandlordFreeholderInitialised = true
    }

    @Test
    fun `adds an organisational landlord's organisation name and email as a freeholder when they are a joint freeholder`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(true)
        whenever(mockState.freeholdersMap).thenReturn(null)
        whenever(mockState.nextFreeholderId).thenReturn(null)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createOrgLandlord(name = "Org Ltd", email = "org@example.com"))

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockState).freeholdersMap =
            mapOf(1 to FreeholderDataModel(detailsKnown = true, name = "Org Ltd", email = "org@example.com"))
        verify(mockState).nextFreeholderId = 2
        verify(mockState).loggedInLandlordFreeholderInitialised = true
    }

    @Test
    fun `appends the landlord using the next freeholder id when the list already has freeholders`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(true)
        whenever(mockState.loggedInLandlordFreeholderInitialised).thenReturn(null)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to existingFreeholder))
        whenever(mockState.nextFreeholderId).thenReturn(4)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createIndividualLandlord(name = "Ind Landlord", email = "ind@example.com"))

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockState).freeholdersMap =
            mapOf(
                1 to existingFreeholder,
                4 to FreeholderDataModel(detailsKnown = true, name = "Ind Landlord", email = "ind@example.com"),
            )
        verify(mockState).nextFreeholderId = 5
    }

    @Test
    fun `does nothing when the landlord is not a joint freeholder`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(false)

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockUserToLandlordService)
        verify(mockState, never()).freeholdersMap = any()
    }

    @Test
    fun `does not add the landlord again once added while the list still has freeholders`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(true)
        whenever(mockState.loggedInLandlordFreeholderInitialised).thenReturn(true)
        whenever(mockState.freeholdersMap).thenReturn(mapOf(1 to existingFreeholder))

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verifyNoInteractions(mockUserToLandlordService)
        verify(mockState, never()).freeholdersMap = any()
    }

    @Test
    fun `adds the landlord again when every freeholder has been removed`() {
        // Arrange
        whenever(mockState.isJointFreeholder).thenReturn(true)
        whenever(mockState.freeholdersMap).thenReturn(emptyMap())
        whenever(mockState.nextFreeholderId).thenReturn(3)
        whenever(mockUserToLandlordService.getCurrentLandlordForUser())
            .thenReturn(MockLandlordData.createIndividualLandlord(name = "Ind Landlord", email = "ind@example.com"))

        // Act
        stepConfig().afterStepIsReached(mockState)

        // Assert
        verify(mockState).freeholdersMap =
            mapOf(3 to FreeholderDataModel(detailsKnown = true, name = "Ind Landlord", email = "ind@example.com"))
        verify(mockState).loggedInLandlordFreeholderInitialised = true
    }

    private fun stepConfig() = AddLoggedInLandlordFreeholderStepConfig(mockUserToLandlordService)
}
