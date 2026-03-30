package uk.gov.communities.prsdb.webapp.services

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor.captor
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.internal.matchers.apachecommons.ReflectionEquals
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.communities.prsdb.webapp.database.entity.PrsdbUser
import uk.gov.communities.prsdb.webapp.database.repository.PrsdbUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.mockObjects.MockPrsdbUserData
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class PrsdbUserServiceTests {
    @Mock
    private lateinit var prsdbUserRepository: PrsdbUserRepository

    @InjectMocks
    private lateinit var prsdbUserService: PrsdbUserService

    @Test
    fun `findOrCreatePrsdbUser returns the corresponding user when given their ID`() {
        val prsdbUser = MockPrsdbUserData.createPrsdbUser()
        whenever(prsdbUserRepository.findById(prsdbUser.id)).thenReturn(Optional.of(prsdbUser))

        val foundPrsdbUser = prsdbUserService.findOrCreatePrsdbUser(prsdbUser.id)

        assertEquals(prsdbUser, foundPrsdbUser)
        verify(prsdbUserRepository, never()).save(any())
    }

    @Test
    fun `findOrCreatePrsdbUser creates and returns a user when given an unsaved ID`() {
        val prsdbUser = MockPrsdbUserData.createPrsdbUser()
        whenever(prsdbUserRepository.findById(prsdbUser.id)).thenReturn(Optional.empty())
        whenever(prsdbUserRepository.save(any())).thenReturn(prsdbUser)

        val createdPrsdbUser = prsdbUserService.findOrCreatePrsdbUser(prsdbUser.id)

        val prsdbUserCaptor = captor<PrsdbUser>()
        verify(prsdbUserRepository).save(prsdbUserCaptor.capture())
        assertTrue(ReflectionEquals(prsdbUser).matches(prsdbUserCaptor.value))
        assertEquals(prsdbUser, createdPrsdbUser)
    }
}
