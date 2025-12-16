package com.project.domain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS UNITARIS DE L'ENTITAT EMPLOYEE
 * ====================================
 * 
 * Aquests tests verifiquen el comportament de la classe Employee
 * de forma aïllada, sense accés a la base de dades.
 * 
 * ASPECTES TESTATS:
 * - Constructors i inicialització
 * - Getters i setters
 * - Mètodes helper per relacions (addContact, removeContact, addProject, removeProject)
 * - Consistència bidireccional de les relacions
 * - equals() i hashCode()
 * - toString()
 * 
 * NOTA: Aquests tests són ràpids perquè no necessiten Hibernate ni BD.
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests Unitaris de l'Entitat Employee")
class EmployeeEntityTest {
    
    // ========================================================================
    // TESTS DE CONSTRUCTORS
    // ========================================================================
    
    /**
     * Grup de tests per als constructors de Employee.
     */
    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {
        
        /**
         * Test: Constructor per defecte crea instància vàlida.
         */
        @Test
        @DisplayName("Constructor per defecte crea instància amb col·leccions buides")
        void constructorPerDefecte_ColeccionsBuides() {
            // ACT
            Employee emp = new Employee();
            
            // ASSERT
            assertAll("Verificació constructor per defecte",
                () -> assertNull(emp.getEmployeeId(), "L'ID hauria de ser null"),
                () -> assertNull(emp.getFirstName(), "El nom hauria de ser null"),
                () -> assertNull(emp.getLastName(), "El cognom hauria de ser null"),
                () -> assertEquals(0, emp.getSalary(), "El salari hauria de ser 0"),
                () -> assertNotNull(emp.getContacts(), "Contacts hauria d'estar inicialitzat"),
                () -> assertTrue(emp.getContacts().isEmpty(), "Contacts hauria d'estar buit"),
                () -> assertNotNull(emp.getProjects(), "Projects hauria d'estar inicialitzat"),
                () -> assertTrue(emp.getProjects().isEmpty(), "Projects hauria d'estar buit")
            );
        }
        
        /**
         * Test: Constructor amb paràmetres inicialitza correctament.
         */
        @Test
        @DisplayName("Constructor amb paràmetres inicialitza tots els camps")
        void constructorAmbParametres_TotsElsCamps() {
            // ACT
            Employee emp = new Employee("Joan", "Garcia", 35000);
            
            // ASSERT
            assertAll("Verificació constructor amb paràmetres",
                () -> assertNull(emp.getEmployeeId(), "L'ID hauria de ser null (no persistit)"),
                () -> assertEquals("Joan", emp.getFirstName()),
                () -> assertEquals("Garcia", emp.getLastName()),
                () -> assertEquals(35000, emp.getSalary()),
                () -> assertNotNull(emp.getContacts()),
                () -> assertNotNull(emp.getProjects())
            );
        }
        
        /**
         * Test parametritzat: Constructor amb diverses dades.
         */
        @ParameterizedTest(name = "Nom: {0}, Cognom: {1}, Salari: {2}")
        @CsvSource({
            "Anna, Martí, 28000",
            "Pere, López, 0",
            "Maria, O'Brien, 100000",
            "'', '', 50000"
        })
        @DisplayName("Constructor accepta diverses combinacions de dades")
        void constructorAmbParametres_DiversesDades(String nom, String cognom, int salari) {
            // ACT
            Employee emp = new Employee(nom, cognom, salari);
            
            // ASSERT
            assertAll(
                () -> assertEquals(nom, emp.getFirstName()),
                () -> assertEquals(cognom, emp.getLastName()),
                () -> assertEquals(salari, emp.getSalary())
            );
        }
    }
    
    // ========================================================================
    // TESTS DE GETTERS I SETTERS
    // ========================================================================
    
    /**
     * Grup de tests per a getters i setters.
     */
    @Nested
    @DisplayName("Getters i Setters")
    class GetterSetterTests {
        
        private Employee employee;
        
        @BeforeEach
        void setUp() {
            employee = new Employee("Original", "Name", 30000);
        }
        
        /**
         * Test: setFirstName i getFirstName.
         */
        @Test
        @DisplayName("setFirstName i getFirstName funcionen correctament")
        void firstName_SetGet() {
            // ACT
            employee.setFirstName("NouNom");
            
            // ASSERT
            assertEquals("NouNom", employee.getFirstName());
        }
        
        /**
         * Test: setLastName i getLastName.
         */
        @Test
        @DisplayName("setLastName i getLastName funcionen correctament")
        void lastName_SetGet() {
            // ACT
            employee.setLastName("NouCognom");
            
            // ASSERT
            assertEquals("NouCognom", employee.getLastName());
        }
        
        /**
         * Test: setSalary i getSalary.
         */
        @Test
        @DisplayName("setSalary i getSalary funcionen correctament")
        void salary_SetGet() {
            // ACT
            employee.setSalary(50000);
            
            // ASSERT
            assertEquals(50000, employee.getSalary());
        }
        
        /**
         * Test: setEmployeeId i getEmployeeId.
         */
        @Test
        @DisplayName("setEmployeeId i getEmployeeId funcionen correctament")
        void employeeId_SetGet() {
            // ACT
            employee.setEmployeeId(123L);
            
            // ASSERT
            assertEquals(123L, employee.getEmployeeId());
        }
        
        /**
         * Test: Salaris extrems.
         */
        @ParameterizedTest(name = "Salari: {0}")
        @ValueSource(ints = {0, -1000, Integer.MAX_VALUE, Integer.MIN_VALUE})
        @DisplayName("setSalary accepta valors extrems")
        void salary_ValorsExtrems(int salari) {
            // ACT
            employee.setSalary(salari);
            
            // ASSERT
            assertEquals(salari, employee.getSalary());
        }
        
        /**
         * Test: setContacts i getContacts.
         */
        @Test
        @DisplayName("setContacts i getContacts funcionen correctament")
        void contacts_SetGet() {
            // ARRANGE
            Set<Contact> nouSet = new HashSet<>();
            nouSet.add(new Contact("EMAIL", "test@test.com", "Test"));
            
            // ACT
            employee.setContacts(nouSet);
            
            // ASSERT
            assertEquals(nouSet, employee.getContacts());
            assertEquals(1, employee.getContacts().size());
        }
        
        /**
         * Test: setProjects i getProjects.
         */
        @Test
        @DisplayName("setProjects i getProjects funcionen correctament")
        void projects_SetGet() {
            // ARRANGE
            Set<Project> nouSet = new HashSet<>();
            nouSet.add(new Project("Test", "Desc", "ACTIU"));
            
            // ACT
            employee.setProjects(nouSet);
            
            // ASSERT
            assertEquals(nouSet, employee.getProjects());
            assertEquals(1, employee.getProjects().size());
        }
    }
    
    // ========================================================================
    // TESTS DE MÈTODES HELPER PER RELACIONS
    // ========================================================================
    
    /**
     * Grup de tests per als mètodes helper de relacions.
     */
    @Nested
    @DisplayName("Mètodes Helper de Relacions")
    class RelationHelperTests {
        
        private Employee employee;
        
        @BeforeEach
        void setUp() {
            employee = new Employee("Test", "Employee", 30000);
        }
        
        // -------- Tests per Contact --------
        
        /**
         * Test: addContact afegeix el contacte al set.
         */
        @Test
        @DisplayName("addContact afegeix el contacte al set d'empleat")
        void addContact_AfegeixAlSet() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            // ACT
            employee.addContact(contact);
            
            // ASSERT
            assertTrue(employee.getContacts().contains(contact));
            assertEquals(1, employee.getContacts().size());
        }
        
        /**
         * Test: addContact estableix la relació bidireccional.
         */
        @Test
        @DisplayName("addContact estableix la relació bidireccional (employee del contact)")
        void addContact_EtableixRelacioBidireccional() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            assertNull(contact.getEmployee(), "Precondició: contact sense employee");
            
            // ACT
            employee.addContact(contact);
            
            // ASSERT
            assertSame(employee, contact.getEmployee(), 
                "El contact hauria de tenir referència a l'employee");
        }
        
        /**
         * Test: removeContact elimina del set.
         */
        @Test
        @DisplayName("removeContact elimina el contacte del set")
        void removeContact_EliminaDelSet() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            employee.addContact(contact);
            assertEquals(1, employee.getContacts().size());
            
            // ACT
            employee.removeContact(contact);
            
            // ASSERT
            assertFalse(employee.getContacts().contains(contact));
            assertEquals(0, employee.getContacts().size());
        }
        
        /**
         * Test: removeContact trenca la relació bidireccional.
         */
        @Test
        @DisplayName("removeContact trenca la relació bidireccional")
        void removeContact_TrencaRelacioBidireccional() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            employee.addContact(contact);
            assertNotNull(contact.getEmployee());
            
            // ACT
            employee.removeContact(contact);
            
            // ASSERT
            assertNull(contact.getEmployee(), 
                "El contact ja no hauria de tenir referència a l'employee");
        }
        
        /**
         * Test: addContact múltiples contactes.
         */
        @Test
        @DisplayName("addContact permet afegir múltiples contactes")
        void addContact_MultiplesContactes() {
            // ACT
            employee.addContact(new Contact("EMAIL", "e1@test.com", "E1"));
            employee.addContact(new Contact("PHONE", "666111222", "P1"));
            employee.addContact(new Contact("ADDRESS", "Carrer Test", "A1"));
            
            // ASSERT
            assertEquals(3, employee.getContacts().size());
        }
        
        // -------- Tests per Project --------
        
        /**
         * Test: addProject afegeix el projecte al set.
         */
        @Test
        @DisplayName("addProject afegeix el projecte al set d'empleat")
        void addProject_AfegeixAlSet() {
            // ARRANGE
            Project project = new Project("Test", "Desc", "ACTIU");
            
            // ACT
            employee.addProject(project);
            
            // ASSERT
            assertTrue(employee.getProjects().contains(project));
            assertEquals(1, employee.getProjects().size());
        }
        
        /**
         * Test: addProject estableix la relació bidireccional.
         */
        @Test
        @DisplayName("addProject estableix la relació bidireccional")
        void addProject_EstableixRelacioBidireccional() {
            // ARRANGE
            Project project = new Project("Test", "Desc", "ACTIU");
            assertFalse(project.getEmployees().contains(employee));
            
            // ACT
            employee.addProject(project);
            
            // ASSERT
            assertTrue(project.getEmployees().contains(employee),
                "El projecte hauria de contenir l'empleat");
        }
        
        /**
         * Test: removeProject elimina del set.
         */
        @Test
        @DisplayName("removeProject elimina el projecte del set")
        void removeProject_EliminaDelSet() {
            // ARRANGE
            Project project = new Project("Test", "Desc", "ACTIU");
            employee.addProject(project);
            
            // ACT
            employee.removeProject(project);
            
            // ASSERT
            assertFalse(employee.getProjects().contains(project));
        }
        
        /**
         * Test: removeProject trenca la relació bidireccional.
         */
        @Test
        @DisplayName("removeProject trenca la relació bidireccional")
        void removeProject_TrencaRelacioBidireccional() {
            // ARRANGE
            Project project = new Project("Test", "Desc", "ACTIU");
            employee.addProject(project);
            assertTrue(project.getEmployees().contains(employee));
            
            // ACT
            employee.removeProject(project);
            
            // ASSERT
            assertFalse(project.getEmployees().contains(employee),
                "El projecte ja no hauria de contenir l'empleat");
        }
        
        /**
         * Test: addProject múltiples projectes.
         */
        @Test
        @DisplayName("addProject permet afegir múltiples projectes")
        void addProject_MultiplesProjectes() {
            // ACT
            employee.addProject(new Project("P1", "D1", "ACTIU"));
            employee.addProject(new Project("P2", "D2", "PLANIFICAT"));
            employee.addProject(new Project("P3", "D3", "COMPLETAT"));
            
            // ASSERT
            assertEquals(3, employee.getProjects().size());
        }
    }
    
    // ========================================================================
    // TESTS DE EQUALS I HASHCODE
    // ========================================================================
    
    /**
     * Grup de tests per equals() i hashCode().
     * 
     * El contracte de equals/hashCode requereix:
     * 1. Reflexivitat: a.equals(a) == true
     * 2. Simetria: a.equals(b) == b.equals(a)
     * 3. Transitivitat: si a.equals(b) i b.equals(c), llavors a.equals(c)
     * 4. Consistència: crides repetides donen el mateix resultat
     * 5. null: a.equals(null) == false
     * 6. hashCode: si a.equals(b), llavors a.hashCode() == b.hashCode()
     */
    @Nested
    @DisplayName("equals() i hashCode()")
    class EqualsHashCodeTests {
        
        /**
         * Test: Reflexivitat - un objecte és igual a si mateix.
         */
        @Test
        @DisplayName("equals és reflexiu (a.equals(a) == true)")
        void equals_Reflexiu() {
            Employee emp = new Employee("Test", "Test", 30000);
            
            assertEquals(emp, emp);
        }
        
        /**
         * Test: equals amb null retorna false.
         */
        @Test
        @DisplayName("equals amb null retorna false")
        void equals_AmbNull_RetornaFalse() {
            Employee emp = new Employee("Test", "Test", 30000);
            
            assertNotEquals(null, emp);
        }
        
        /**
         * Test: equals amb classe diferent retorna false.
         */
        @Test
        @DisplayName("equals amb classe diferent retorna false")
        void equals_ClasseDiferent_RetornaFalse() {
            Employee emp = new Employee("Test", "Test", 30000);
            
            assertNotEquals("String", emp);
            assertNotEquals(123, emp);
        }
        
        /**
         * Test: Simetria - a.equals(b) == b.equals(a).
         */
        @Test
        @DisplayName("equals és simètric")
        void equals_Simetric() {
            Employee emp1 = new Employee("Test", "Test", 30000);
            Employee emp2 = new Employee("Test", "Test", 30000);
            
            assertEquals(emp1.equals(emp2), emp2.equals(emp1));
        }
        
        /**
         * Test: hashCode és consistent.
         */
        @Test
        @DisplayName("hashCode és consistent (múltiples crides donen el mateix resultat)")
        void hashCode_Consistent() {
            Employee emp = new Employee("Test", "Test", 30000);
            
            int hash1 = emp.hashCode();
            int hash2 = emp.hashCode();
            int hash3 = emp.hashCode();
            
            assertEquals(hash1, hash2);
            assertEquals(hash2, hash3);
        }
        
        /**
         * Test: Si equals és true, hashCode ha de ser igual.
         */
        @Test
        @DisplayName("Si equals és true, hashCode és igual")
        void equals_True_HashCodeIgual() {
            Employee emp1 = new Employee("Test", "Test", 30000);
            Employee emp2 = new Employee("Test", "Test", 30000);
            
            // Si són iguals segons equals...
            if (emp1.equals(emp2)) {
                // ... el hashCode ha de ser el mateix
                assertEquals(emp1.hashCode(), emp2.hashCode());
            }
        }
        
        /**
         * Test: Empleats amb dades diferents no són iguals.
         */
        @Test
        @DisplayName("Empleats amb nom diferent no són iguals")
        void equals_NomDiferent_NoIguals() {
            Employee emp1 = new Employee("Joan", "Test", 30000);
            Employee emp2 = new Employee("Pere", "Test", 30000);
            
            assertNotEquals(emp1, emp2);
        }
        
        /**
         * Test: Empleats amb ID diferent (quan tenen ID).
         */
        @Test
        @DisplayName("Empleats amb ID diferent no són iguals")
        void equals_IdDiferent_NoIguals() {
            Employee emp1 = new Employee("Test", "Test", 30000);
            Employee emp2 = new Employee("Test", "Test", 30000);
            emp1.setEmployeeId(1L);
            emp2.setEmployeeId(2L);
            
            assertNotEquals(emp1, emp2);
        }
        
        /**
         * Test: Empleats amb el mateix ID són iguals.
         */
        @Test
        @DisplayName("Empleats amb el mateix ID són iguals")
        void equals_MateixId_Iguals() {
            Employee emp1 = new Employee("Test1", "Test1", 30000);
            Employee emp2 = new Employee("Test2", "Test2", 40000);
            emp1.setEmployeeId(1L);
            emp2.setEmployeeId(1L);
            
            assertEquals(emp1, emp2);
        }
    }
    
    // ========================================================================
    // TESTS DE TOSTRING
    // ========================================================================
    
    /**
     * Grup de tests per toString().
     */
    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        /**
         * Test: toString retorna representació no nul·la.
         */
        @Test
        @DisplayName("toString retorna representació no nul·la")
        void toString_NoNull() {
            Employee emp = new Employee("Test", "Employee", 30000);
            
            assertNotNull(emp.toString());
        }
        
        /**
         * Test: toString conté les dades bàsiques.
         */
        @Test
        @DisplayName("toString conté nom, cognom i salari")
        void toString_ConteDadesBasiques() {
            Employee emp = new Employee("Joan", "Garcia", 35000);
            String result = emp.toString();
            
            assertAll(
                () -> assertTrue(result.contains("Joan"), "Hauria de contenir el nom"),
                () -> assertTrue(result.contains("Garcia"), "Hauria de contenir el cognom"),
                () -> assertTrue(result.contains("35000"), "Hauria de contenir el salari")
            );
        }
        
        /**
         * Test: toString amb contactes no causa recursió infinita.
         */
        @Test
        @DisplayName("toString amb contactes no causa recursió infinita")
        void toString_AmbContactes_NoRecursio() {
            Employee emp = new Employee("Test", "Test", 30000);
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            emp.addContact(contact);
            
            // Si hi ha recursió infinita, això fallarà amb StackOverflowError
            assertDoesNotThrow(() -> emp.toString());
        }
        
        /**
         * Test: toString amb projectes no causa recursió infinita.
         */
        @Test
        @DisplayName("toString amb projectes no causa recursió infinita")
        void toString_AmbProjectes_NoRecursio() {
            Employee emp = new Employee("Test", "Test", 30000);
            Project project = new Project("Test", "Desc", "ACTIU");
            emp.addProject(project);
            
            // Si hi ha recursió infinita, això fallarà amb StackOverflowError
            assertDoesNotThrow(() -> emp.toString());
        }
        
        /**
         * Test: toString amb col·leccions buides.
         */
        @Test
        @DisplayName("toString amb col·leccions buides funciona")
        void toString_ColeccionsBuides() {
            Employee emp = new Employee("Test", "Test", 30000);
            
            String result = emp.toString();
            
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
}
