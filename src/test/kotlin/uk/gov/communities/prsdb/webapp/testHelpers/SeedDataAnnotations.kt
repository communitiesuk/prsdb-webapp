package uk.gov.communities.prsdb.webapp.testHelpers

import org.springframework.core.annotation.AliasFor
import org.springframework.test.context.jdbc.Sql

@Target(AnnotationTarget.CLASS)
annotation class SqlBeforeAll(
    vararg val scripts: String,
)

@Target(AnnotationTarget.CLASS)
@Sql
annotation class SqlBeforeEach(
    @get:AliasFor(annotation = Sql::class, attribute = "scripts")
    vararg val scripts: String,
)
