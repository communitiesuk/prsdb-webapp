package uk.gov.communities.prsdb.webapp.integration

import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito

class ManageLAUsersTests : IntegrationTest() {
    @BeforeEach
    fun setup() {
        Mockito.`when`(principal.name).thenReturn("Test user")
    }

// TODO: Add tests when OneLogin mockking is working

/*    @Test
    fun `manageLAUsers page renders`(page: Page) {
        page.navigate("http://localhost:$port/manage-users")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("Manage")
        assertThat(page.getByRole(AriaRole.HEADING)).containsText("'s users'")
    }*/
}
