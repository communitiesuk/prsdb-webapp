package uk.gov.communities.prsdb.webapp.services

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaInstant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.constants.enums.NonStepJourneyDataKey
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.helpers.DateTimeHelper
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class LegacyIncompletePropertyFormContextServiceTests {
    @Mock
    private lateinit var mockFormContextRepository: FormContextRepository

    @InjectMocks
    private lateinit var legacyIncompletePropertyFormContextService: LegacyIncompletePropertyFormContextService

    private lateinit var currentDate: LocalDate
    private lateinit var currentInstant: Instant

    @BeforeEach
    fun setup() {
        currentDate = DateTimeHelper().getCurrentDateInUK()
        currentInstant =
            LocalDateTime(
                currentDate.year,
                currentDate.monthNumber,
                currentDate.dayOfMonth,
                11,
                30,
            ).toInstant(TimeZone.of("Europe/London"))
    }

    @Test
    fun `getIncompletePropertyFormContextForLandlordIfNotExpired returns the form context for a valid incomplete property`() {
        val createdTodayDate = currentInstant.toJavaInstant()

        val expectedFormContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = createdTodayDate)
        val principalName = "user"

        whenever(
            mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                expectedFormContext.id,
                principalName,
                JourneyType.PROPERTY_REGISTRATION,
            ),
        ).thenReturn(expectedFormContext)

        val formContext =
            legacyIncompletePropertyFormContextService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                expectedFormContext.id,
                principalName,
            )

        assertEquals(expectedFormContext, formContext)
    }

    @Test
    fun `getIncompletePropertyFormContextForLandlordIfNotExpired throws NOT_FOUND error when no FormContext is found`() {
        val formContextId: Long = 123
        val principalName = "user"

        val expectedErrorMessage =
            "404 NOT_FOUND \"Form context with ID: $formContextId and journey type: " +
                "${JourneyType.PROPERTY_REGISTRATION.name} not found for base user: $principalName\""

        whenever(
            mockFormContextRepository.findByIdAndUser_IdAndJourneyType(formContextId, principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(null)

        // Act and Assert
        val exception =
            assertThrows<ResponseStatusException> {
                legacyIncompletePropertyFormContextService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                    formContextId,
                    principalName,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun `getIncompletePropertyFormContextForLandlordIfNotExpired throws NOT_FOUND error for an out of date incomplete property`() {
        val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

        val principalName = "user"
        val formContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

        val expectedErrorMessage = "404 NOT_FOUND \"Complete by date for form context with ID: ${formContext.id} is in the past\""

        whenever(
            mockFormContextRepository.findByIdAndUser_IdAndJourneyType(
                formContext.id,
                principalName,
                JourneyType.PROPERTY_REGISTRATION,
            ),
        ).thenReturn(formContext)

        // Act and Assert
        val exception =
            assertThrows<ResponseStatusException> {
                legacyIncompletePropertyFormContextService.getIncompletePropertyFormContextForLandlordIfNotExpired(
                    formContext.id,
                    principalName,
                )
            }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals(expectedErrorMessage, exception.message)
    }

    @Test
    fun `getAllInDateIncompletePropertiesForLandlord returns a list of valid incomplete properties`() {
        val address = "2, Example Road, EG"
        val context =
            "{\"lookup-address\":{\"houseNameOrNumber\":\"73\",\"postcode\":\"WC2R 1LA\"}," +
                "\"${NonStepJourneyDataKey.LookedUpAddresses.key}\":\"[{\\\"singleLineAddress\\\":\\\"2, Example Road, EG\\\"," +
                "\\\"localCouncilId\\\":241,\\\"uprn\\\":2123456,\\\"buildingNumber\\\":\\\"2\\\"," +
                "\\\"postcode\\\":\\\"EG\\\"}]\",\"select-address\":{\"address\":\"$address\"}}"

        val createdTodayDate = currentInstant.toJavaInstant()
        val formContextCreatedToday =
            MockLandlordData.createPropertyRegistrationFormContext(
                id = 1,
                createdDate = createdTodayDate,
                context = context,
            )

        val createdYesterdayDate = currentInstant.minus(1, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()
        val formContextCreatedYesterday =
            MockLandlordData.createPropertyRegistrationFormContext(
                id = 2,
                createdDate = createdYesterdayDate,
                context = context,
            )

        val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()
        val formContextCreated29DaysAgo = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

        val principalName = "principalName"
        val incompleteProperties = listOf(formContextCreatedToday, formContextCreatedYesterday, formContextCreated29DaysAgo)

        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(incompleteProperties)

        val inDateIncompleteProperties =
            legacyIncompletePropertyFormContextService.getAllInDateIncompletePropertiesForLandlord(
                principalName,
            )

        assertEquals(2, inDateIncompleteProperties.size)
        assertTrue(inDateIncompleteProperties.contains(formContextCreatedToday))
        assertTrue(inDateIncompleteProperties.contains(formContextCreatedYesterday))
        assertFalse(inDateIncompleteProperties.contains(formContextCreated29DaysAgo))
    }

    @Test
    fun `getAllInDateIncompletePropertiesForLandlord returns an emptyList if there are no valid incomplete properties`() {
        val principalName = "principalName"
        val outOfDateCreatedDate = currentInstant.minus(29, DateTimeUnit.DAY, TimeZone.of("Europe/London")).toJavaInstant()

        val formContext = MockLandlordData.createPropertyRegistrationFormContext(createdDate = outOfDateCreatedDate)

        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(listOf(formContext))

        val incompleteProperties =
            legacyIncompletePropertyFormContextService.getAllInDateIncompletePropertiesForLandlord(principalName)

        assertTrue(incompleteProperties.isEmpty())
    }

    @Test
    fun `getAllInDateIncompletePropertiesForLandlord returns an emptyList if there are no incomplete properties`() {
        val principalName = "principalName"
        whenever(
            mockFormContextRepository.findAllByUser_IdAndJourneyType(principalName, JourneyType.PROPERTY_REGISTRATION),
        ).thenReturn(emptyList())

        val incompleteProperties =
            legacyIncompletePropertyFormContextService.getAllInDateIncompletePropertiesForLandlord(principalName)

        assertTrue(incompleteProperties.isEmpty())
    }

    @Test
    fun `getFormContext returns the form context when it exists`() {
        // Arrange
        val formContextId = 1L
        val expectedFormContext = MockLandlordData.createPropertyRegistrationFormContext(id = formContextId)
        whenever(mockFormContextRepository.findById(formContextId))
            .thenReturn(Optional.of(expectedFormContext))

        // Act
        val formContext = legacyIncompletePropertyFormContextService.getFormContext(formContextId)

        // Assert
        assertEquals(expectedFormContext, formContext)
    }

    @Test
    fun `getFormContext returns null when the form context does not exist`() {
        // Arrange
        val formContextId = 1L
        whenever(mockFormContextRepository.findById(formContextId)).thenReturn(Optional.empty())

        // Act
        val formContext = legacyIncompletePropertyFormContextService.getFormContext(formContextId)

        // Assert
        assertEquals(null, formContext)
    }
}
