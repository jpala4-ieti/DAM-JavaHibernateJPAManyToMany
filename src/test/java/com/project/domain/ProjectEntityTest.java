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
 * TESTS UNITARIS DE L'ENTITAT PROJECT
 * ===================================
 * 
 * Aquests tests verifiquen el comportament de la classe Project
 * de forma aïllada, sense accés a la base de dades.
 * 
 * ASPECTES TESTATS:
 * - Constructors i inicialització
 * - Getters i setters
 * - Mètodes helper per relacions (addEmployee, removeEmployee, hasEmployee)
 * - Consistència bidireccional de la relació ManyToMany
 * - equals() i hashCode()
 * - toString()
 * 
 * PARTICULARITATS DE PROJECT:
 * - És el costat INVERS de la relació ManyToMany (mappedBy="projects")
 * - Pot existir sense empleats assignats
 * - Té un camp status per gestionar el cicle de vida
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests Unitaris de l'Entitat Project")
class ProjectEntityTest {
    
    // ========================================================================
    // TESTS DE CONSTRUCTORS
    // ========================================================================
    
    /**
     * Grup de tests per als constructors de Project.
     */
    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {
        
        /**
         * Test: Constructor per defecte crea instància vàlida.
         */
        @Test
        @DisplayName("Constructor per defecte crea instància amb col·lecció buida")
        void constructorPerDefecte_ColeccioBuida() {
            // ACT
            Project project = new Project();
            
            // ASSERT
            assertAll("Verificació constructor per defecte",
                () -> assertNull(project.getProjectId(), "L'ID hauria de ser null"),
                () -> assertNull(project.getName(), "El nom hauria de ser null"),
                () -> assertNull(project.getDescription(), "La descripció hauria de ser null"),
                () -> assertNull(project.getStatus(), "L'estat hauria de ser null"),
                () -> assertNotNull(project.getEmployees(), "Employees hauria d'estar inicialitzat"),
                () -> assertTrue(project.getEmployees().isEmpty(), "Employees hauria d'estar buit")
            );
        }
        
        /**
         * Test: Constructor amb paràmetres inicialitza correctament.
         */
        @Test
        @DisplayName("Constructor amb paràmetres inicialitza tots els camps")
        void constructorAmbParametres_TotsElsCamps() {
            // ACT
            Project project = new Project("Web Corporativa", "Portal web de l'empresa", "ACTIU");
            
            // ASSERT
            assertAll("Verificació constructor amb paràmetres",
                () -> assertNull(project.getProjectId(), "L'ID hauria de ser null (no persistit)"),
                () -> assertEquals("Web Corporativa", project.getName()),
                () -> assertEquals("Portal web de l'empresa", project.getDescription()),
                () -> assertEquals("ACTIU", project.getStatus()),
                () -> assertNotNull(project.getEmployees()),
                () -> assertTrue(project.getEmployees().isEmpty())
            );
        }
        
        /**
         * Test parametritzat: Constructor amb diversos estats.
         */
        @ParameterizedTest(name = "Estat: {0}")
        @ValueSource(strings = {"ACTIU", "COMPLETAT", "PLANIFICAT", "PAUSAT", "CANCEL·LAT", "EN_REVISIÓ"})
        @DisplayName("Constructor accepta diversos estats")
        void constructorAmbParametres_DiversosEstats(String estat) {
            // ACT
            Project project = new Project("Test", "Descripció", estat);
            
            // ASSERT
            assertEquals(estat, project.getStatus());
        }
        
        /**
         * Test parametritzat: Constructor amb diverses dades.
         */
        @ParameterizedTest(name = "Projecte: {0}")
        @CsvSource({
            "App Mòbil, Aplicació iOS/Android, ACTIU",
            "Intranet, Portal intern, PLANIFICAT",
            "API REST, Backend serveis, COMPLETAT",
            "ML Pipeline, Model predictiu, PAUSAT"
        })
        @DisplayName("Constructor accepta diverses combinacions de dades")
        void constructorAmbParametres_DiversesDades(String nom, String desc, String estat) {
            // ACT
            Project project = new Project(nom, desc, estat);
            
            // ASSERT
            assertAll(
                () -> assertEquals(nom, project.getName()),
                () -> assertEquals(desc, project.getDescription()),
                () -> assertEquals(estat, project.getStatus())
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
        
        private Project project;
        
        @BeforeEach
        void setUp() {
            project = new Project("Original", "Descripció original", "PLANIFICAT");
        }
        
        /**
         * Test: setProjectId i getProjectId.
         */
        @Test
        @DisplayName("setProjectId i getProjectId funcionen correctament")
        void projectId_SetGet() {
            // ACT
            project.setProjectId(123L);
            
            // ASSERT
            assertEquals(123L, project.getProjectId());
        }
        
        /**
         * Test: setName i getName.
         */
        @Test
        @DisplayName("setName i getName funcionen correctament")
        void name_SetGet() {
            // ACT
            project.setName("Nou Nom");
            
            // ASSERT
            assertEquals("Nou Nom", project.getName());
        }
        
        /**
         * Test: setDescription i getDescription.
         */
        @Test
        @DisplayName("setDescription i getDescription funcionen correctament")
        void description_SetGet() {
            // ACT
            project.setDescription("Nova descripció del projecte");
            
            // ASSERT
            assertEquals("Nova descripció del projecte", project.getDescription());
        }
        
        /**
         * Test: setStatus i getStatus.
         */
        @Test
        @DisplayName("setStatus i getStatus funcionen correctament")
        void status_SetGet() {
            // ACT
            project.setStatus("COMPLETAT");
            
            // ASSERT
            assertEquals("COMPLETAT", project.getStatus());
        }
        
        /**
         * Test: setEmployees i getEmployees.
         */
        @Test
        @DisplayName("setEmployees i getEmployees funcionen correctament")
        void employees_SetGet() {
            // ARRANGE
            Set<Employee> nouSet = new HashSet<>();
            nouSet.add(new Employee("Test", "Employee", 30000));
            
            // ACT
            project.setEmployees(nouSet);
            
            // ASSERT
            assertEquals(nouSet, project.getEmployees());
            assertEquals(1, project.getEmployees().size());
        }
        
        /**
         * Test: Canviar estat seguint cicle de vida.
         */
        @Test
        @DisplayName("Canvi d'estat: PLANIFICAT -> ACTIU -> COMPLETAT")
        void status_CicleVida() {
            // ARRANGE
            assertEquals("PLANIFICAT", project.getStatus());
            
            // ACT & ASSERT - PLANIFICAT -> ACTIU
            project.setStatus("ACTIU");
            assertEquals("ACTIU", project.getStatus());
            
            // ACT & ASSERT - ACTIU -> COMPLETAT
            project.setStatus("COMPLETAT");
            assertEquals("COMPLETAT", project.getStatus());
        }
        
        /**
         * Test: Noms amb caràcters especials.
         */
        @ParameterizedTest(name = "Nom: {0}")
        @ValueSource(strings = {
            "Projecte v2.0",
            "App-Mobile (iOS)",
            "Portal_Intern_2024",
            "Proyecto español",
            "项目名称"
        })
        @DisplayName("setName accepta noms amb caràcters especials")
        void name_CaractersEspecials(String nom) {
            // ACT
            project.setName(nom);
            
            // ASSERT
            assertEquals(nom, project.getName());
        }
    }
    
    // ========================================================================
    // TESTS DE MÈTODES HELPER PER RELACIONS
    // ========================================================================
    
    /**
     * Grup de tests per als mètodes helper de relacions amb Employee.
     */
    @Nested
    @DisplayName("Mètodes Helper de Relacions amb Employee")
    class RelationHelperTests {
        
        private Project project;
        
        @BeforeEach
        void setUp() {
            project = new Project("Test Project", "Descripció", "ACTIU");
        }
        
        /**
         * Test: addEmployee afegeix l'empleat al set.
         */
        @Test
        @DisplayName("addEmployee afegeix l'empleat al set del projecte")
        void addEmployee_AfegeixAlSet() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT
            project.addEmployee(emp);
            
            // ASSERT
            assertTrue(project.getEmployees().contains(emp));
            assertEquals(1, project.getEmployees().size());
        }
        
        /**
         * Test: addEmployee estableix la relació bidireccional.
         */
        @Test
        @DisplayName("addEmployee estableix la relació bidireccional")
        void addEmployee_EstableixRelacioBidireccional() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            assertFalse(emp.getProjects().contains(project));
            
            // ACT
            project.addEmployee(emp);
            
            // ASSERT
            assertTrue(emp.getProjects().contains(project),
                "L'empleat hauria de tenir referència al projecte");
        }
        
        /**
         * Test: removeEmployee elimina del set.
         */
        @Test
        @DisplayName("removeEmployee elimina l'empleat del set")
        void removeEmployee_EliminaDelSet() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            project.addEmployee(emp);
            assertEquals(1, project.getEmployees().size());
            
            // ACT
            project.removeEmployee(emp);
            
            // ASSERT
            assertFalse(project.getEmployees().contains(emp));
            assertEquals(0, project.getEmployees().size());
        }
        
        /**
         * Test: removeEmployee trenca la relació bidireccional.
         */
        @Test
        @DisplayName("removeEmployee trenca la relació bidireccional")
        void removeEmployee_TrencaRelacioBidireccional() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            project.addEmployee(emp);
            assertTrue(emp.getProjects().contains(project));
            
            // ACT
            project.removeEmployee(emp);
            
            // ASSERT
            assertFalse(emp.getProjects().contains(project),
                "L'empleat ja no hauria de tenir referència al projecte");
        }
        
        /**
         * Test: hasEmployee retorna true si l'empleat està assignat.
         */
        @Test
        @DisplayName("hasEmployee retorna true si l'empleat està assignat")
        void hasEmployee_Assignat_RetornaTrue() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            project.addEmployee(emp);
            
            // ACT & ASSERT
            assertTrue(project.hasEmployee(emp));
        }
        
        /**
         * Test: hasEmployee retorna false si l'empleat no està assignat.
         */
        @Test
        @DisplayName("hasEmployee retorna false si l'empleat no està assignat")
        void hasEmployee_NoAssignat_RetornaFalse() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT & ASSERT
            assertFalse(project.hasEmployee(emp));
        }
        
        /**
         * Test: addEmployee múltiples empleats.
         */
        @Test
        @DisplayName("addEmployee permet afegir múltiples empleats")
        void addEmployee_MultiplesEmpleats() {
            // ACT
            project.addEmployee(new Employee("E1", "C1", 30000));
            project.addEmployee(new Employee("E2", "C2", 35000));
            project.addEmployee(new Employee("E3", "C3", 40000));
            
            // ASSERT
            assertEquals(3, project.getEmployees().size());
        }
        
        /**
         * Test: Consistència bidireccional amb múltiples empleats.
         */
        @Test
        @DisplayName("Consistència bidireccional amb múltiples empleats")
        void addEmployee_ConsistenciaBidireccional_MultiplesEmpleats() {
            // ARRANGE
            Employee e1 = new Employee("E1", "C1", 30000);
            Employee e2 = new Employee("E2", "C2", 35000);
            
            // ACT
            project.addEmployee(e1);
            project.addEmployee(e2);
            
            // ASSERT - Des del projecte
            assertThat(project.getEmployees()).containsExactlyInAnyOrder(e1, e2);
            
            // ASSERT - Des dels empleats
            assertTrue(e1.getProjects().contains(project));
            assertTrue(e2.getProjects().contains(project));
        }
    }
    
    // ========================================================================
    // TESTS DE EQUALS I HASHCODE
    // ========================================================================
    
    /**
     * Grup de tests per equals() i hashCode().
     */
    @Nested
    @DisplayName("equals() i hashCode()")
    class EqualsHashCodeTests {
        
        /**
         * Test: Reflexivitat.
         */
        @Test
        @DisplayName("equals és reflexiu (p.equals(p) == true)")
        void equals_Reflexiu() {
            Project project = new Project("Test", "Desc", "ACTIU");
            
            assertEquals(project, project);
        }
        
        /**
         * Test: equals amb null retorna false.
         */
        @Test
        @DisplayName("equals amb null retorna false")
        void equals_AmbNull_RetornaFalse() {
            Project project = new Project("Test", "Desc", "ACTIU");
            
            assertNotEquals(null, project);
        }
        
        /**
         * Test: equals amb classe diferent retorna false.
         */
        @Test
        @DisplayName("equals amb classe diferent retorna false")
        void equals_ClasseDiferent_RetornaFalse() {
            Project project = new Project("Test", "Desc", "ACTIU");
            
            assertNotEquals("String", project);
            assertNotEquals(123, project);
        }
        
        /**
         * Test: Simetria.
         */
        @Test
        @DisplayName("equals és simètric")
        void equals_Simetric() {
            Project p1 = new Project("Test", "Desc", "ACTIU");
            Project p2 = new Project("Test", "Desc", "ACTIU");
            
            assertEquals(p1.equals(p2), p2.equals(p1));
        }
        
        /**
         * Test: hashCode consistent.
         */
        @Test
        @DisplayName("hashCode és consistent")
        void hashCode_Consistent() {
            Project project = new Project("Test", "Desc", "ACTIU");
            
            int hash1 = project.hashCode();
            int hash2 = project.hashCode();
            
            assertEquals(hash1, hash2);
        }
        
        /**
         * Test: Si equals és true, hashCode és igual.
         */
        @Test
        @DisplayName("Si equals és true, hashCode és igual")
        void equals_True_HashCodeIgual() {
            Project p1 = new Project("Test", "Desc", "ACTIU");
            Project p2 = new Project("Test", "Desc", "ACTIU");
            
            if (p1.equals(p2)) {
                assertEquals(p1.hashCode(), p2.hashCode());
            }
        }
        
        /**
         * Test: Projectes amb ID diferent no són iguals.
         */
        @Test
        @DisplayName("Projectes amb ID diferent no són iguals")
        void equals_IdDiferent_NoIguals() {
            Project p1 = new Project("Test", "Desc", "ACTIU");
            Project p2 = new Project("Test", "Desc", "ACTIU");
            p1.setProjectId(1L);
            p2.setProjectId(2L);
            
            assertNotEquals(p1, p2);
        }
        
        /**
         * Test: Projectes amb el mateix ID són iguals.
         */
        @Test
        @DisplayName("Projectes amb el mateix ID són iguals")
        void equals_MateixId_Iguals() {
            Project p1 = new Project("Test1", "Desc1", "ACTIU");
            Project p2 = new Project("Test2", "Desc2", "COMPLETAT");
            p1.setProjectId(1L);
            p2.setProjectId(1L);
            
            assertEquals(p1, p2);
        }
        
        /**
         * Test: Projectes amb nom diferent (sense ID) no són iguals.
         */
        @Test
        @DisplayName("Projectes sense ID amb nom diferent no són iguals")
        void equals_SenseId_NomDiferent_NoIguals() {
            Project p1 = new Project("Projecte A", "Desc", "ACTIU");
            Project p2 = new Project("Projecte B", "Desc", "ACTIU");
            
            assertNotEquals(p1, p2);
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
            Project project = new Project("Test", "Desc", "ACTIU");
            
            assertNotNull(project.toString());
        }
        
        /**
         * Test: toString conté les dades bàsiques.
         */
        @Test
        @DisplayName("toString conté nom i estat")
        void toString_ConteDadesBasiques() {
            Project project = new Project("Web App", "Descripció", "ACTIU");
            String result = project.toString();
            
            assertAll(
                () -> assertTrue(result.contains("Web App"), "Hauria de contenir el nom"),
                () -> assertTrue(result.contains("ACTIU"), "Hauria de contenir l'estat")
            );
        }
        
        /**
         * Test: toString amb empleats no causa recursió infinita.
         */
        @Test
        @DisplayName("toString amb empleats no causa recursió infinita")
        void toString_AmbEmpleats_NoRecursio() {
            Project project = new Project("Test", "Desc", "ACTIU");
            Employee emp = new Employee("Test", "Employee", 30000);
            project.addEmployee(emp);
            
            // Si hi ha recursió infinita, això fallarà amb StackOverflowError
            assertDoesNotThrow(() -> project.toString());
        }
        
        /**
         * Test: toString sense empleats.
         */
        @Test
        @DisplayName("toString sense empleats funciona")
        void toString_SenseEmpleats() {
            Project project = new Project("Test", "Desc", "ACTIU");
            
            String result = project.toString();
            
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
        
        /**
         * Test: toString amb múltiples empleats.
         */
        @Test
        @DisplayName("toString amb múltiples empleats")
        void toString_MultiplesEmpleats() {
            Project project = new Project("Test", "Desc", "ACTIU");
            project.addEmployee(new Employee("Anna", "Garcia", 30000));
            project.addEmployee(new Employee("Pere", "López", 35000));
            
            String result = project.toString();
            
            assertNotNull(result);
            // Verificar que no hi ha error i conté algun nom
            assertTrue(result.contains("Anna") || result.contains("Pere") || 
                      result.contains("employees"));
        }
    }
    
    // ========================================================================
    // TESTS DE CASOS LÍMIT
    // ========================================================================
    
    /**
     * Grup de tests per a casos límit.
     */
    @Nested
    @DisplayName("Casos Límit")
    class EdgeCasesTests {
        
        /**
         * Test: Descripció molt llarga.
         */
        @Test
        @DisplayName("Accepta descripció molt llarga")
        void description_MoltLlarga() {
            // ARRANGE
            String descLlarga = "Aquesta és una descripció molt detallada del projecte ".repeat(20);
            
            // ACT
            Project project = new Project("Test", descLlarga, "ACTIU");
            
            // ASSERT
            assertEquals(descLlarga, project.getDescription());
        }
        
        /**
         * Test: Projecte sense descripció.
         */
        @Test
        @DisplayName("Accepta descripció nul·la")
        void description_Nulla() {
            // ACT
            Project project = new Project("Test", null, "ACTIU");
            
            // ASSERT
            assertNull(project.getDescription());
        }
        
        /**
         * Test: Afegir i eliminar el mateix empleat repetidament.
         */
        @Test
        @DisplayName("Afegir i eliminar el mateix empleat repetidament")
        void addRemove_Repetidament() {
            Project project = new Project("Test", "Desc", "ACTIU");
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT - Afegir i eliminar repetidament
            for (int i = 0; i < 5; i++) {
                project.addEmployee(emp);
                assertEquals(1, project.getEmployees().size());
                assertTrue(emp.getProjects().contains(project));
                
                project.removeEmployee(emp);
                assertEquals(0, project.getEmployees().size());
                assertFalse(emp.getProjects().contains(project));
            }
            
            // ASSERT final
            assertEquals(0, project.getEmployees().size());
        }
        
        /**
         * Test: Afegir el mateix empleat dues vegades (Set no permet duplicats).
         */
        @Test
        @DisplayName("Afegir el mateix empleat dues vegades no crea duplicats")
        void addEmployee_Duplicat_NoDuplica() {
            Project project = new Project("Test", "Desc", "ACTIU");
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT
            project.addEmployee(emp);
            project.addEmployee(emp); // Intent de duplicat
            
            // ASSERT - Set no permet duplicats
            assertEquals(1, project.getEmployees().size());
        }
    }
}
