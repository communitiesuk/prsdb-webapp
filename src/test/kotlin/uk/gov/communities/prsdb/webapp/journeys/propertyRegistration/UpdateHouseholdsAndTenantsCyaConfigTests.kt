package uk.gov.communities.prsdb.webapp.journeys.propertyRegistration

import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.exceptions.UpdateConflictException
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.HouseholdStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.steps.TenantsStep
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsCyaConfig
import uk.gov.communities.prsdb.webapp.journeys.propertyRegistration.update.householdsAndTenants.UpdateHouseholdsAndTenantsJourneyState
import uk.gov.communities.prsdb.webapp.journeys.shared.helpers.OccupancyDetailsHelper
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NewNumberOfPeopleFormModel
import uk.gov.communities.prsdb.webapp.models.requestModels.formModels.NumberOfHouseholdsFormModel
import uk.gov.communities.prsdb.webapp.services.PropertyOwnershipService
import uk.gov.communities.prsdb.webapp.services.PropertyUpdateEmailService

@ExtendWith(MockitoExtension::class)
class UpdateHouseholdsAndTenantsCyaConfigTests {
    @Mock
    private lateinit var mockPropertyOwnershipService: PropertyOwnershipService

    @Mock
    private lateinit var mockPropertyUpdateEmailService: PropertyUpdateEmailService

    @Mock
    private lateinit var mockState: UpdateHouseholdsAndTenantsJourneyState

    @Mock
    private lateinit var mockHouseholdStep: HouseholdStep

    @Mock
    private lateinit var mockTenantsStep: TenantsStep

    @Mock
    private lateinit var stepConfig: UpdateHouseholdsAndTenantsCyaConfig

    @Mock
    private lateinit var mockNumberOfHouseholdsFormModel: NumberOfHouseholdsFormModel

    @Mock
    private lateinit var mockNumberOfTenantsFormModel: NewNumberOfPeopleFormModel

    private val propertyId = 123L
    private val childJourneyId = "child-journey-123"
    private val numberOfHouseholds = 2
    private val numberOfTenants = 5
    private val initialLastModifiedDate = Clock.System.now().toJavaInstant()

    @BeforeEach
    fun setUp() {
        stepConfig =
            UpdateHouseholdsAndTenantsCyaConfig(
                occupancyDetailsHelper = OccupancyDetailsHelper(),
                propertyOwnershipService = mockPropertyOwnershipService,
                propertyUpdateEmailService = mockPropertyUpdateEmailService,
            )
        stepConfig.afterStepIsReached(mockState) // This initializes the childJourneyId
        whenever(mockState.propertyId).thenReturn(propertyId)
        whenever(mockState.households).thenReturn(mockHouseholdStep)
        whenever(mockState.tenants).thenReturn(mockTenantsStep)
        whenever(mockState.lastModifiedDate).thenReturn(initialLastModifiedDate.toString())
        whenever(mockHouseholdStep.formModel).thenReturn(mockNumberOfHouseholdsFormModel)
        whenever(mockTenantsStep.formModel).thenReturn(mockNumberOfTenantsFormModel)
        whenever(mockNumberOfHouseholdsFormModel.numberOfHouseholds).thenReturn(numberOfHouseholds.toString())
        whenever(mockNumberOfTenantsFormModel.numberOfPeople).thenReturn(numberOfTenants.toString())
    }

    @Test
    fun `afterStepDataIsAdded calls updateHouseholdsAndTenants on propertyOwnershipService`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyOwnershipService).updateHouseholdsAndTenants(
            id = propertyId,
            numberOfHouseholds = numberOfHouseholds,
            numberOfPeople = numberOfTenants,
            initialLastModifiedDate = initialLastModifiedDate,
        )
    }

    @Test
    fun `afterStepDataIsAdded sends update emails with the correct updated items`() {
        // Act
        stepConfig.afterStepDataIsAdded(mockState)

        // Assert
        verify(mockPropertyUpdateEmailService).sendUpdateEmails(
            eq(propertyId),
            eq(
                listOf(
                    "The number of households living in this property",
                    "The number of people living in this property",
                ),
            ),
        )
    }

    @Test
    fun `afterStepDataIsAdded deletes the journey then rethrows when it gets an UpdateConflictException`() {
        // Arrange
        whenever(
            mockPropertyOwnershipService.updateHouseholdsAndTenants(
                id = propertyId,
                numberOfHouseholds = numberOfHouseholds,
                numberOfPeople = numberOfTenants,
                initialLastModifiedDate = initialLastModifiedDate,
            ),
        ).thenThrow(UpdateConflictException::class.java)

        // Act, assert
        assertThrows<UpdateConflictException> { stepConfig.afterStepDataIsAdded(mockState) }

        verify(mockState).deleteJourney()
    }
}
