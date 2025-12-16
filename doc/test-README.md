# Suite Completa de Tests per al Manager JPA/Hibernate

## Descripció

Aquesta suite de tests proporciona una cobertura exhaustiva de les funcionalitats del `Manager` DAO i les entitats JPA (`Employee`, `Contact`, `Project`) del projecte Hibernate Many-to-Many.

## Estructura de Fitxers

```
src/test/
├── java/com/project/
│   ├── test/
│   │   └── HibernateTestBase.java          # Classe base per tots els tests
│   ├── dao/
│   │   ├── ManagerEmployeeTest.java        # Tests CRUD Employee
│   │   ├── ManagerContactTest.java         # Tests CRUD Contact
│   │   ├── ManagerProjectTest.java         # Tests CRUD Project
│   │   ├── ManagerRelationsTest.java       # Tests de relacions JPA
│   │   ├── ManagerQueryTest.java           # Tests de queries i utilitats
│   │   └── ManagerTransactionTest.java     # Tests de transaccions
│   ├── domain/
│   │   ├── EmployeeEntityTest.java         # Tests unitaris Employee
│   │   ├── ContactEntityTest.java          # Tests unitaris Contact
│   │   └── ProjectEntityTest.java          # Tests unitaris Project
│   └── integration/
│       └── FullIntegrationTest.java        # Tests d'integració complets
└── resources/
    └── hibernate-test.properties           # Configuració H2 per tests
```

## Fitxers de Test

### 1. HibernateTestBase.java
**Classe base abstracta** que proporciona:
- Inicialització de la SessionFactory amb H2 en memòria
- Neteja de la base de dades abans de cada test
- Mètodes helper per crear dades de prova
- Gestió del cicle de vida (`@BeforeAll`, `@AfterAll`, `@BeforeEach`)

### 2. ManagerEmployeeTest.java
**Tests CRUD d'Employee** organitzats en grups `@Nested`:
- `CreateEmployeeTests`: Creació amb diverses dades, IDs únics
- `ReadEmployeeTests`: getById, listCollection
- `UpdateEmployeeTests`: Modificació de camps, preservació de relacions
- `DeleteEmployeeTests`: Eliminació, cascade amb contactes
- `SearchEmployeeTests`: findEmployeesByContactType, findEmployeesByProject
- `ProjectAssignmentTests`: updateEmployeeProjects

### 3. ManagerContactTest.java
**Tests CRUD de Contact**:
- Creació vinculada a Employee
- Cerca per tipus i empleat
- Actualització de dades
- Eliminació i orphanRemoval
- Validacions i casos límit

### 4. ManagerProjectTest.java
**Tests CRUD de Project**:
- Creació amb diferents estats
- Lectura i filtrat
- Actualització preservant relacions
- Eliminació sense afectar empleats
- Relacions ManyToMany

### 5. ManagerRelationsTest.java
**Tests de relacions JPA**:
- OneToMany: Employee <-> Contact
  - cascade=ALL
  - orphanRemoval=true
- ManyToMany: Employee <-> Project
  - Consistència bidireccional
  - Taula pont employee_project
- Integritat referencial

### 6. ManagerQueryTest.java
**Tests de queries i utilitats**:
- queryUpdate(): SQL natiu UPDATE/DELETE
- queryTable(): SQL natiu SELECT
- tableToString(): Formatació de resultats
- collectionToString(): Formatació de col·leccions
- listCollection(): Llistat genèric

### 7. ManagerTransactionTest.java
**Tests de transaccions**:
- Atomicitat de les operacions
- Aïllament entre operacions
- Durabilitat (persistència)
- Consistència de dades
- Gestió d'errors

### 8. EmployeeEntityTest.java
**Tests unitaris de l'entitat Employee**:
- Constructors
- Getters i setters
- Mètodes helper (addContact, addProject, etc.)
- equals() i hashCode()
- toString()

### 9. ContactEntityTest.java
**Tests unitaris de l'entitat Contact**:
- Constructors amb diversos tipus de contacte
- Relació ManyToOne amb Employee
- equals() i hashCode()
- toString()
- Casos límit

### 10. ProjectEntityTest.java
**Tests unitaris de l'entitat Project**:
- Constructors amb diversos estats
- Mètodes helper (addEmployee, removeEmployee, hasEmployee)
- Relació ManyToMany inversa
- equals() i hashCode()
- toString()

### 11. FullIntegrationTest.java
**Tests d'integració complets**:
- Escenaris de negoci realistes
- Operacions massives
- Consistència de dades
- Cerques complexes

## Dependències Maven

Afegiu aquestes dependències al vostre `pom.xml`:

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.3</version>
        <scope>test</scope>
    </dependency>
    
    <!-- JUnit 5 Params (tests parametritzats) -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-params</artifactId>
        <version>5.11.3</version>
        <scope>test</scope>
    </dependency>
    
    <!-- AssertJ (assertions fluides) -->
    <dependency>
        <groupId>org.assertj</groupId>
        <artifactId>assertj-core</artifactId>
        <version>3.26.3</version>
        <scope>test</scope>
    </dependency>
    
    <!-- H2 Database (BD en memòria per tests) -->
    <dependency>
        <groupId>com.h2database</groupId>
        <artifactId>h2</artifactId>
        <version>2.2.224</version>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.2.5</version>
        </plugin>
    </plugins>
</build>
```

## Configuració de Tests (hibernate-test.properties)

El fitxer `src/test/resources/hibernate-test.properties` configura Hibernate per utilitzar H2 en memòria:

```properties
# H2 Database en memòria
hibernate.connection.driver_class=org.h2.Driver
hibernate.connection.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
hibernate.connection.username=sa
hibernate.connection.password=

# Dialecte H2
hibernate.dialect=org.hibernate.dialect.H2Dialect

# Crea i elimina taules automàticament
hibernate.hbm2ddl.auto=create-drop

# Mostrar SQL per depuració
hibernate.show_sql=true
hibernate.format_sql=true
```

## Execució dels Tests

### Amb Maven

```bash
# Executar tots els tests
mvn test

# Executar tests d'una classe específica
mvn test -Dtest=ManagerEmployeeTest

# Executar tests amb un patró
mvn test -Dtest=Manager*Test

# Executar amb informació detallada
mvn test -X

# Generar informe de cobertura (si teniu JaCoCo configurat)
mvn test jacoco:report
```

### Amb IDE

1. **IntelliJ IDEA**: Clic dret sobre el directori `src/test/java` → "Run All Tests"
2. **Eclipse**: Clic dret sobre el projecte → "Run As" → "JUnit Test"
3. **VS Code**: Utilitzar l'extensió "Test Runner for Java"

## Patrons Utilitzats

### Arrange-Act-Assert (AAA)
Cada test segueix l'estructura:
```java
@Test
void nomTest() {
    // ARRANGE - Preparar dades
    Employee emp = Manager.addEmployee("Test", "Test", 30000);
    
    // ACT - Executar acció
    Manager.updateEmployee(emp.getEmployeeId(), "Nou", "Nom", 50000);
    
    // ASSERT - Verificar resultats
    Employee actualitzat = Manager.getById(Employee.class, emp.getEmployeeId());
    assertEquals("Nou", actualitzat.getFirstName());
}
```

### Given-When-Then (BDD)
Per tests més complexos:
```java
@Nested
@DisplayName("Given un empleat amb contactes")
class EmpleatAmbContactes {
    
    @Nested
    @DisplayName("When s'elimina l'empleat")
    class QuanEliminarEmpleat {
        
        @Test
        @DisplayName("Then els contactes també s'eliminen")
        void contactesEliminats() { ... }
    }
}
```

### Test Data Builder
Per crear dades de prova fàcilment:
```java
protected Employee crearEmpleatComplet(String nom, int numContactes, Set<Project> projectes) {
    Employee emp = Manager.addEmployee(nom, "Cognom", 40000);
    // Afegir contactes i projectes...
    return emp;
}
```

## Cobertura de Tests

La suite cobreix:

| Categoria | Cobertura |
|-----------|-----------|
| Employee CRUD | ✅ Completa |
| Contact CRUD | ✅ Completa |
| Project CRUD | ✅ Completa |
| Relacions JPA | ✅ Completa |
| Queries natius | ✅ Completa |
| Transaccions | ✅ Completa |
| Casos límit | ✅ Completa |
| Integració | ✅ Completa |

## Bones Pràctiques Implementades

1. **Tests independents**: Cada test es pot executar de forma aïllada
2. **Neteja automàtica**: La BD es neteja abans de cada test
3. **Noms descriptius**: Tots els tests tenen `@DisplayName` en català
4. **Agrupació lògica**: Tests organitzats amb `@Nested`
5. **Assertions múltiples**: Ús de `assertAll()` per verificacions completes
6. **Tests parametritzats**: Ús de `@ParameterizedTest` per reduir duplicació
7. **AssertJ**: Assertions fluides i llegibles

## Extensió de la Suite

Per afegir nous tests:

1. Creeu una classe que extengui `HibernateTestBase`
2. Utilitzeu els mètodes helper per crear dades
3. Seguiu el patró AAA
4. Afegiu `@DisplayName` descriptiu
5. Agrupeu tests relacionats amb `@Nested`

## Resolució de Problemes

### Error: "No SessionFactory"
Assegureu-vos que `hibernate-test.properties` està a `src/test/resources/`

### Error: "Table not found"
Verifiqueu que `hibernate.hbm2ddl.auto=create-drop` està configurat

### Tests lents
Considereu:
- Utilitzar `@TestInstance(Lifecycle.PER_CLASS)` per compartir SessionFactory
- Reduir el nombre de tests d'integració
- Utilitzar tests unitaris quan sigui possible

## Autor

Suite de tests generada per Claude - Anthropic

## Llicència

Mateixa llicència que el projecte principal (GNU GPL v3.0)
