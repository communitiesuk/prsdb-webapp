package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.ArgumentMatchers.any
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.constants.enums.JourneyType
import uk.gov.communities.prsdb.webapp.database.entity.FormContext
import uk.gov.communities.prsdb.webapp.database.repository.FormContextRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockLandlordData
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class FormContextServiceTests {
    @Mock
    private lateinit var mockFormContextRepository: FormContextRepository

    @InjectMocks
    private lateinit var formContextService: FormContextService

    @Test
    fun `createEmptyFormContext creates and returns an empty FormContext`() {
        val expectedFormContext =
            FormContext(
                journeyType = JourneyType.PROPERTY_COMPLIANCE,
                context = "",
                user = MockLandlordData.createOneLoginUser(),
            )

        whenever(mockFormContextRepository.save(any())).thenReturn(expectedFormContext)

        val returnedFormContext = formContextService.createEmptyFormContext(expectedFormContext.journeyType, expectedFormContext.user)

        val formContextCaptor = captor<FormContext>()
        verify(mockFormContextRepository).save(formContextCaptor.capture())
        assertTrue(ReflectionEquals(expectedFormContext, "id").matches(formContextCaptor.value))

        assertEquals(expectedFormContext, returnedFormContext)
    }
}
