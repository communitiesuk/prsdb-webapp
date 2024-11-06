package uk.gov.communities.prsdb.webapp.validation

/**
 * Used to delegate the constraint validation to a function on the class that is annotated with @IsValidPrioritised.
 * The @ValidatedBy annotation that includes this constraint validator must specify a targetMethod that takes no args
 * and returns a Boolean
 */
interface DelegatedPropertyConstraintValidator : PrioritisedConstraintValidator
