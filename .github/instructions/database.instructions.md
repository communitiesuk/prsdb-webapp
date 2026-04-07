---
applyTo: "**/database/**,**/db/migrations/**"
---

# Database Instructions

## Entity Conventions

### Base Classes
- `AuditableEntity`: Adds `createdAt`, `createdBy` (immutable)
- `ModifiableAuditableEntity`: Adds `modifiedAt`, `modifiedBy` (mutable)

### Entity Structure
```kotlin
@Entity
@Table(name = "example_table")
class ExampleEntity : ModifiableAuditableEntity() {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
    
    lateinit var name: String
    
    @ManyToOne
    @JoinColumn(name = "parent_id")
    lateinit var parent: ParentEntity
    
    @OneToMany(mappedBy = "example", cascade = [CascadeType.ALL])
    val children: MutableList<ChildEntity> = mutableListOf()
}
```

## Repository Patterns
```kotlin
@Repository
interface ExampleRepository : JpaRepository<ExampleEntity, Long> {
    
    fun findByName(name: String): ExampleEntity?
    
    @Query("SELECT e FROM ExampleEntity e WHERE e.parent.id = :parentId")
    fun findByParentId(parentId: Long): List<ExampleEntity>
}
```

### Underscore Navigation
Use underscores to traverse relationships in derived query method names:
```kotlin
fun findByPrimaryLandlord_BaseUser_Id(userId: String): List<PropertyOwnership>
fun existsByIsActiveTrueAndAddress_Uprn(uprn: Long): Boolean
```

### Custom Search Repositories
For text search, use the interface + implementation pattern:
```kotlin
interface PropertyOwnershipSearchRepository {
    fun searchMatching(searchTerm: String, localCouncilId: Int): Page<PropertyOwnership>
}

class PropertyOwnershipSearchRepositoryImpl(
    private val entityManager: EntityManager,
) : PropertyOwnershipSearchRepository {
    // Uses native SQL with pg_trgm for text search
    // Dual index strategy (GIN for small results, GIST for large)
}
```

The main repository extends both `JpaRepository` and the custom search interface:
```kotlin
@Repository
interface PropertyOwnershipRepository :
    JpaRepository<PropertyOwnership, Long>,
    PropertyOwnershipSearchRepository { }
```

## Flyway Migrations

### Naming Convention
`V<major>_<minor>_<fix>__<descriptive_name>.sql`

Examples:
- `V1_0_0__create_landlord_table.sql`
- `V1_1_0__add_property_compliance.sql`
- `V1_1_1__fix_column_type.sql`

### Migration Location
`src/main/resources/db/migrations/`

### Migration Content
```sql
-- V1_2_0__add_example_table.sql
CREATE TABLE example_table (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    modified_at TIMESTAMP,
    modified_by VARCHAR(255)
);

CREATE INDEX idx_example_name ON example_table(name);
```

## Search with Trigrams
- Use `pg_trgm` extension for fuzzy text search
- See existing search implementations for patterns
