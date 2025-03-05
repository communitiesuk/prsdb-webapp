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
import uk.gov.communities.prsdb.webapp.database.entity.OneLoginUser
import uk.gov.communities.prsdb.webapp.database.repository.OneLoginUserRepository
import uk.gov.communities.prsdb.webapp.testHelpers.MockOneLoginUserData
import java.util.Optional
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class OneLoginUserServiceTests {
    @Mock
    private lateinit var oneLoginUserRepository: OneLoginUserRepository

    @InjectMocks
    private lateinit var oneLoginUserService: OneLoginUserService

    @Test
    fun `findOrCreate1LUser returns the corresponding 1L user when given their ID`() {
        val oneLoginUser = MockOneLoginUserData.createOneLoginUser()
        whenever(oneLoginUserRepository.findById(oneLoginUser.id)).thenReturn(Optional.of(oneLoginUser))

        val foundOneLoginUser = oneLoginUserService.findOrCreate1LUser(oneLoginUser.id)

        assertEquals(oneLoginUser, foundOneLoginUser)
        verify(oneLoginUserRepository, never()).save(any())
    }

    @Test
    fun `findOrCreate1LUser creates and returns a 1L user when given an unsaved ID`() {
        val oneLoginUser = MockOneLoginUserData.createOneLoginUser()
        whenever(oneLoginUserRepository.findById(oneLoginUser.id)).thenReturn(Optional.empty())
        whenever(oneLoginUserRepository.save(any())).thenReturn(oneLoginUser)

        val createdOneLoginUser = oneLoginUserService.findOrCreate1LUser(oneLoginUser.id)

        val oneLoginUserCaptor = captor<OneLoginUser>()
        verify(oneLoginUserRepository).save(oneLoginUserCaptor.capture())
        assertTrue(ReflectionEquals(oneLoginUser).matches(oneLoginUserCaptor.value))
        assertEquals(oneLoginUser, createdOneLoginUser)
    }
}
