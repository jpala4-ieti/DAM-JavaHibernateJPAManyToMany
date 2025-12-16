package com.project.dao;

import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS CRUD PER A LA GESTIÓ DE PROJECTES
 * =======================================
 * 
 * Aquesta classe conté tots els tests relacionats amb les operacions
 * CRUD de projectes a través del Manager.
 * 
 * PARTICULARITATS DELS PROJECTES:
 * - Relació ManyToMany amb Employee (costat invers, mappedBy)
 * - No té cascade des d'Employee (els projectes sobreviuen a l'eliminació d'empleats)
 * - Pot existir sense empleats assignats
 * - Té un camp d'estat (status) per gestionar el cicle de vida
 * 
 * COBERTURA:
 * - addProject(): Creació de projectes
 * - getById(): Lectura de projectes
 * - updateProject(): Actualització de projectes
 * - delete(): Eliminació de projectes
 * - listCollection(): Llistat de projectes
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests CRUD del Manager per a Project")
class ManagerProjectTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS DE CREACIÓ (CREATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació addProject().
     */
    @Nested
    @DisplayName("CREATE - Tests d'addProject()")
    class CreateProjectTests {
        
        /**
         * Test bàsic: Crear un projecte amb dades vàlides.
         */
        @Test
        @DisplayName("Crear projecte amb dades vàlides retorna entitat amb ID")
        void addProject_DadesValides_RetornaProjecteAmbId() {
            // ARRANGE
            String nom = "Web Corporativa";
            String descripcio = "Desenvolupament del portal web de l'empresa";
            String estat = "ACTIU";
            
            // ACT
            Project resultat = Manager.addProject(nom, descripcio, estat);
            
            // ASSERT
            assertAll("Verificació completa del projecte creat",
                () -> assertNotNull(resultat, 
                    "El projecte no hauria de ser null"),
                () -> assertNotNull(resultat.getProjectId(), 
                    "L'ID hauria d'estar assignat"),
                () -> assertTrue(resultat.getProjectId() > 0, 
                    "L'ID ha de ser positiu"),
                () -> assertEquals(nom, resultat.getName(), 
                    "El nom no coincideix"),
                () -> assertEquals(descripcio, resultat.getDescription(), 
                    "La descripció no coincideix"),
                () -> assertEquals(estat, resultat.getStatus(), 
                    "L'estat no coincideix"),
                () -> assertNotNull(resultat.getEmployees(), 
                    "La col·lecció d'empleats hauria d'estar inicialitzada"),
                () -> assertTrue(resultat.getEmployees().isEmpty(), 
                    "La col·lecció d'empleats hauria d'estar buida")
            );
        }
        
        /**
         * Test: Crear projectes amb diferents estats.
         */
        @ParameterizedTest(name = "Estat: {0}")
        @ValueSource(strings = {"ACTIU", "COMPLETAT", "PLANIFICAT", "PAUSAT", "CANCEL·LAT"})
        @DisplayName("Crear projecte amb diversos estats")
        void addProject_DiversosEstats_TotsAcceptats(String estat) {
            // ACT
            Project resultat = Manager.addProject("Test", "Descripció", estat);
            
            // ASSERT
            assertNotNull(resultat.getProjectId());
            assertEquals(estat, resultat.getStatus());
        }
        
        /**
         * Test parametritzat: Crear projectes amb dades diverses.
         */
        @ParameterizedTest(name = "Projecte: {0}")
        @CsvSource({
            "App Mòbil, Aplicació per iOS i Android, ACTIU",
            "Intranet, Portal intern per empleats, PLANIFICAT",
            "API REST, Backend per serveis, COMPLETAT",
            "Machine Learning, Model predictiu de vendes, PAUSAT"
        })
        @DisplayName("Crear projectes amb diferents dades")
        void addProject_DadesVariades_TotsCreats(String nom, String desc, String estat) {
            // ACT
            Project resultat = Manager.addProject(nom, desc, estat);
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat.getProjectId()),
                () -> assertEquals(nom, resultat.getName()),
                () -> assertEquals(desc, resultat.getDescription()),
                () -> assertEquals(estat, resultat.getStatus())
            );
        }
        
        /**
         * Test: Els IDs generats són únics.
         */
        @Test
        @DisplayName("IDs generats són únics per cada projecte")
        void addProject_MultiplesProjectes_IDsUnics() {
            // ACT
            Project p1 = Manager.addProject("P1", "D1", "ACTIU");
            Project p2 = Manager.addProject("P2", "D2", "ACTIU");
            Project p3 = Manager.addProject("P3", "D3", "PLANIFICAT");
            
            // ASSERT
            Set<Long> ids = Set.of(
                p1.getProjectId(), 
                p2.getProjectId(), 
                p3.getProjectId()
            );
            
            assertEquals(3, ids.size(), "Tots els IDs haurien de ser únics");
        }
        
        /**
         * Test: El projecte es persisteix a la BD.
         */
        @Test
        @DisplayName("El projecte creat existeix a la base de dades")
        void addProject_DesprésDeCrear_ExisteixABD() {
            // ACT
            Project creat = Manager.addProject("Persistent", "Test", "ACTIU");
            Long id = creat.getProjectId();
            
            // ASSERT
            Project recuperat = Manager.getById(Project.class, id);
            
            assertNotNull(recuperat);
            assertEquals(creat.getName(), recuperat.getName());
        }
        
        /**
         * Test: Crear projecte amb descripció llarga.
         */
        @Test
        @DisplayName("Crear projecte amb descripció llarga")
        void addProject_DescripcioLlarga_Acceptat() {
            // ARRANGE
            String descripcioLlarga = "Aquest és un projecte molt important que ".repeat(10);
            
            // ACT
            Project resultat = Manager.addProject("Test", descripcioLlarga, "ACTIU");
            
            // ASSERT
            assertNotNull(resultat.getProjectId());
            assertEquals(descripcioLlarga, resultat.getDescription());
        }
        
        /**
         * Test: Crear projecte amb caràcters especials al nom.
         */
        @ParameterizedTest(name = "Nom: {0}")
        @ValueSource(strings = {
            "Projecte v2.0",
            "App-Mobile (iOS)",
            "Portal_Intern",
            "Proyecto español",
            "项目名称"
        })
        @DisplayName("Crear projecte amb caràcters especials al nom")
        void addProject_CaractersEspecials_Acceptats(String nom) {
            // ACT
            Project resultat = Manager.addProject(nom, "Test", "ACTIU");
            
            // ASSERT
            assertNotNull(resultat.getProjectId());
            assertEquals(nom, resultat.getName());
        }
    }
    
    // ========================================================================
    // TESTS DE LECTURA (READ)
    // ========================================================================
    
    /**
     * Grup de tests per a operacions de lectura de projectes.
     */
    @Nested
    @DisplayName("READ - Tests de lectura de projectes")
    class ReadProjectTests {
        
        private Project projecteProva;
        
        @BeforeEach
        void setUpTestData() {
            projecteProva = Manager.addProject("Lectura Test", "Descripció test", "ACTIU");
        }
        
        /**
         * Test: getById retorna el projecte correcte.
         */
        @Test
        @DisplayName("getById retorna el projecte quan existeix")
        void getById_ProjecteExistent_RetornaProjecte() {
            // ACT
            Project resultat = Manager.getById(Project.class, projecteProva.getProjectId());
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat),
                () -> assertEquals(projecteProva.getProjectId(), resultat.getProjectId()),
                () -> assertEquals("Lectura Test", resultat.getName()),
                () -> assertEquals("Descripció test", resultat.getDescription()),
                () -> assertEquals("ACTIU", resultat.getStatus())
            );
        }
        
        /**
         * Test: getById retorna null per ID inexistent.
         */
        @Test
        @DisplayName("getById retorna null per ID inexistent")
        void getById_IdInexistent_RetornaNull() {
            // ACT
            Project resultat = Manager.getById(Project.class, 99999L);
            
            // ASSERT
            assertNull(resultat);
        }
        
        /**
         * Test: listCollection retorna tots els projectes.
         */
        @Test
        @DisplayName("listCollection retorna tots els projectes")
        void listCollection_ProjectesExistents_RetornaTots() {
            // ARRANGE
            Manager.addProject("Segon", "Desc", "ACTIU");
            Manager.addProject("Tercer", "Desc", "PLANIFICAT");
            
            // ACT
            Collection<Project> resultat = Manager.listCollection(Project.class);
            
            // ASSERT
            assertEquals(3, resultat.size());
        }
        
        /**
         * Test: listCollection retorna buit si no hi ha projectes.
         */
        @Test
        @DisplayName("listCollection retorna buit si no hi ha projectes")
        void listCollection_SenseProjectes_RetornaBuit() {
            // ARRANGE
            Manager.delete(Project.class, projecteProva.getProjectId());
            
            // ACT
            Collection<Project> resultat = Manager.listCollection(Project.class);
            
            // ASSERT
            assertTrue(resultat.isEmpty());
        }
        
        /**
         * Test amb AssertJ: Verificar contingut de la llista.
         */
        @Test
        @DisplayName("listCollection amb AssertJ - verificar noms")
        void listCollection_VerificarContingut() {
            // ARRANGE
            Manager.addProject("Alpha", "Desc", "ACTIU");
            Manager.addProject("Beta", "Desc", "COMPLETAT");
            
            // ACT
            Collection<Project> resultat = Manager.listCollection(Project.class);
            
            // ASSERT
            assertThat(resultat)
                .hasSize(3)
                .extracting(Project::getName)
                .containsExactlyInAnyOrder("Lectura Test", "Alpha", "Beta");
        }
        
        /**
         * Test: Filtrar projectes per estat.
         */
        @Test
        @DisplayName("Filtrar projectes per estat ACTIU")
        void listCollection_FiltrarPerEstat() {
            // ARRANGE
            Manager.addProject("Actiu1", "Desc", "ACTIU");
            Manager.addProject("Completat1", "Desc", "COMPLETAT");
            Manager.addProject("Planificat1", "Desc", "PLANIFICAT");
            
            // ACT
            Collection<Project> tots = Manager.listCollection(Project.class);
            long actius = tots.stream()
                             .filter(p -> "ACTIU".equals(p.getStatus()))
                             .count();
            
            // ASSERT
            assertEquals(2, actius, "Hauria d'haver-hi 2 projectes actius");
        }
    }
    
    // ========================================================================
    // TESTS D'ACTUALITZACIÓ (UPDATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació updateProject().
     */
    @Nested
    @DisplayName("UPDATE - Tests d'actualització de projectes")
    class UpdateProjectTests {
        
        private Project projecteOriginal;
        
        @BeforeEach
        void setUpTestData() {
            projecteOriginal = Manager.addProject("Original", "Descripció original", "PLANIFICAT");
        }
        
        /**
         * Test: updateProject modifica tots els camps.
         */
        @Test
        @DisplayName("updateProject modifica tots els camps")
        void updateProject_TotsElsCamps_Modificats() {
            // ARRANGE
            Long id = projecteOriginal.getProjectId();
            
            // ACT
            Manager.updateProject(id, "NouNom", "Nova descripció", "ACTIU");
            
            // ASSERT
            Project actualitzat = Manager.getById(Project.class, id);
            
            assertAll(
                () -> assertEquals("NouNom", actualitzat.getName()),
                () -> assertEquals("Nova descripció", actualitzat.getDescription()),
                () -> assertEquals("ACTIU", actualitzat.getStatus()),
                () -> assertEquals(id, actualitzat.getProjectId(), 
                    "L'ID no hauria de canviar")
            );
        }
        
        /**
         * Test: updateProject canviant només l'estat.
         */
        @Test
        @DisplayName("updateProject canviant només l'estat")
        void updateProject_NomesEstat_RestaIgual() {
            // ARRANGE
            Long id = projecteOriginal.getProjectId();
            
            // ACT
            Manager.updateProject(id, "Original", "Descripció original", "COMPLETAT");
            
            // ASSERT
            Project actualitzat = Manager.getById(Project.class, id);
            
            assertAll(
                () -> assertEquals("Original", actualitzat.getName(), 
                    "El nom no hauria de canviar"),
                () -> assertEquals("Descripció original", actualitzat.getDescription(), 
                    "La descripció no hauria de canviar"),
                () -> assertEquals("COMPLETAT", actualitzat.getStatus(), 
                    "L'estat hauria de canviar")
            );
        }
        
        /**
         * Test: updateProject amb ID inexistent no falla.
         */
        @Test
        @DisplayName("updateProject amb ID inexistent no falla")
        void updateProject_IdInexistent_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.updateProject(99999L, "Nou", "Desc", "ACTIU");
            });
        }
        
        /**
         * Test: updateProject preserva els empleats assignats.
         */
        @Test
        @DisplayName("updateProject preserva els empleats assignats")
        void updateProject_AmbEmpleats_PreservaRelacions() {
            // ARRANGE - Assignar empleats al projecte
            Employee emp = Manager.addEmployee("Test", "Employee", 30000);
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(projecteOriginal));
            
            // ACT
            Manager.updateProject(
                projecteOriginal.getProjectId(), 
                "NouNom", 
                "Nova desc", 
                "ACTIU"
            );
            
            // ASSERT
            Collection<Employee> empleatsDelProjecte = Manager.findEmployeesByProject(
                projecteOriginal.getProjectId()
            );
            
            assertThat(empleatsDelProjecte).hasSize(1);
        }
        
        /**
         * Test: Cicle de vida complet d'un projecte.
         */
        @Test
        @DisplayName("Cicle de vida: PLANIFICAT -> ACTIU -> COMPLETAT")
        void updateProject_CicleVida_Complet() {
            // ARRANGE
            Long id = projecteOriginal.getProjectId();
            assertEquals("PLANIFICAT", projecteOriginal.getStatus());
            
            // ACT & ASSERT - Passar a ACTIU
            Manager.updateProject(id, "Test", "Desc", "ACTIU");
            assertEquals("ACTIU", Manager.getById(Project.class, id).getStatus());
            
            // ACT & ASSERT - Passar a COMPLETAT
            Manager.updateProject(id, "Test", "Desc", "COMPLETAT");
            assertEquals("COMPLETAT", Manager.getById(Project.class, id).getStatus());
        }
    }
    
    // ========================================================================
    // TESTS D'ELIMINACIÓ (DELETE)
    // ========================================================================
    
    /**
     * Grup de tests per a operacions d'eliminació de projectes.
     */
    @Nested
    @DisplayName("DELETE - Tests d'eliminació de projectes")
    class DeleteProjectTests {
        
        private Project projecteAEliminar;
        
        @BeforeEach
        void setUpTestData() {
            projecteAEliminar = Manager.addProject("Eliminar", "Test", "ACTIU");
        }
        
        /**
         * Test: delete elimina el projecte.
         */
        @Test
        @DisplayName("delete elimina el projecte correctament")
        void delete_ProjecteExistent_Eliminat() {
            // ARRANGE
            Long id = projecteAEliminar.getProjectId();
            assertNotNull(Manager.getById(Project.class, id));
            
            // ACT
            Manager.delete(Project.class, id);
            
            // ASSERT
            assertNull(Manager.getById(Project.class, id));
        }
        
        /**
         * Test: delete projecte NO elimina empleats associats.
         */
        @Test
        @DisplayName("delete projecte NO elimina empleats associats")
        void delete_AmbEmpleats_EmpleatsPersisteixen() {
            // ARRANGE - Assignar empleat
            Employee emp = Manager.addEmployee("Sobreviu", "Test", 30000);
            Long empId = emp.getEmployeeId();
            Manager.updateEmployeeProjects(empId, Set.of(projecteAEliminar));
            
            // ACT - Primer desvinculem l'empleat, després eliminem
            Manager.updateEmployeeProjects(empId, Set.of()); // Desvincula tots els projectes
            Manager.delete(Project.class, projecteAEliminar.getProjectId());
            
            // ASSERT
            Employee empRecuperat = Manager.getById(Employee.class, empId);
            assertNotNull(empRecuperat, 
                "L'empleat NO hauria d'eliminar-se quan s'elimina el projecte");
        }
        
        /**
         * Test: delete amb ID inexistent no falla.
         */
        @Test
        @DisplayName("delete amb ID inexistent no falla")
        void delete_IdInexistent_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.delete(Project.class, 99999L);
            });
        }
        
        /**
         * Test: Després de delete, el recompte és correcte.
         */
        @Test
        @DisplayName("delete decrementa el nombre de projectes")
        void delete_DecrementaComptador() {
            // ARRANGE
            Manager.addProject("Extra", "Test", "PLANIFICAT");
            int comptadorInicial = comptarEntitats(Project.class);
            
            // ACT
            Manager.delete(Project.class, projecteAEliminar.getProjectId());
            
            // ASSERT
            assertEquals(comptadorInicial - 1, comptarEntitats(Project.class));
        }
        
        /**
         * Test: Eliminar projecte desvincula empleats però no els elimina.
         */
        @Test
        @DisplayName("Eliminar projecte desvincula empleats de la relació")
        void delete_DesvinculaEmpleats() {
            // ARRANGE
            Employee emp1 = Manager.addEmployee("Emp1", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Emp2", "Test", 35000);
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(projecteAEliminar));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projecteAEliminar));
            
            // Verificar que el projecte té empleats
            assertEquals(2, Manager.findEmployeesByProject(
                projecteAEliminar.getProjectId()).size());
            
            // ACT - Desvinculem tots els empleats abans d'eliminar
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of());
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of());
            Manager.delete(Project.class, projecteAEliminar.getProjectId());
            
            // ASSERT - Els empleats continuen existint
            assertAll(
                () -> assertNotNull(Manager.getById(Employee.class, emp1.getEmployeeId())),
                () -> assertNotNull(Manager.getById(Employee.class, emp2.getEmployeeId()))
            );
        }
    }
    
    // ========================================================================
    // TESTS DE RELACIÓ AMB EMPLEATS
    // ========================================================================
    
    /**
     * Grup de tests per a la relació ManyToMany amb Employee.
     */
    @Nested
    @DisplayName("RELACIONS - Tests de relació Project-Employee")
    class ProjectEmployeeRelationTests {
        
        private Project projecte;
        
        @BeforeEach
        void setUpTestData() {
            projecte = Manager.addProject("Relacions", "Test", "ACTIU");
        }
        
        /**
         * Test: Un projecte pot tenir múltiples empleats.
         */
        @Test
        @DisplayName("Un projecte pot tenir múltiples empleats")
        void projecte_MultiplesEmpleats() {
            // ARRANGE
            Employee emp1 = Manager.addEmployee("Emp1", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Emp2", "Test", 35000);
            Employee emp3 = Manager.addEmployee("Emp3", "Test", 40000);
            
            // ACT
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(projecte));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projecte));
            Manager.updateEmployeeProjects(emp3.getEmployeeId(), Set.of(projecte));
            
            // ASSERT
            Collection<Employee> empleats = Manager.findEmployeesByProject(
                projecte.getProjectId()
            );
            
            assertEquals(3, empleats.size());
        }
        
        /**
         * Test: Un projecte pot no tenir empleats.
         */
        @Test
        @DisplayName("Un projecte pot existir sense empleats")
        void projecte_SenseEmpleats_Valid() {
            // ACT
            Collection<Employee> empleats = Manager.findEmployeesByProject(
                projecte.getProjectId()
            );
            
            // ASSERT
            assertThat(empleats).isEmpty();
            // El projecte continua existint
            assertNotNull(Manager.getById(Project.class, projecte.getProjectId()));
        }
        
        /**
         * Test: Desassignar tots els empleats d'un projecte.
         */
        @Test
        @DisplayName("Desassignar tots els empleats d'un projecte")
        void projecte_DesassignarTotsEmpleats() {
            // ARRANGE
            Employee emp1 = Manager.addEmployee("Emp1", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Emp2", "Test", 35000);
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(projecte));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projecte));
            
            assertEquals(2, Manager.findEmployeesByProject(projecte.getProjectId()).size());
            
            // ACT - Desassignar empleats
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of());
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of());
            
            // ASSERT
            assertThat(Manager.findEmployeesByProject(projecte.getProjectId())).isEmpty();
            // El projecte continua existint
            assertNotNull(Manager.getById(Project.class, projecte.getProjectId()));
        }
        
        /**
         * Test: Un empleat pot estar en múltiples projectes.
         */
        @Test
        @DisplayName("Un empleat pot estar en múltiples projectes")
        void empleat_EnMultiplesProjectes() {
            // ARRANGE
            Project p2 = Manager.addProject("Projecte2", "Desc", "ACTIU");
            Project p3 = Manager.addProject("Projecte3", "Desc", "PLANIFICAT");
            Employee emp = Manager.addEmployee("Multi", "Projecte", 45000);
            
            // ACT
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(projecte, p2, p3));
            
            // ASSERT - L'empleat apareix a cada projecte
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte.getProjectId()))
                        .hasSize(1),
                () -> assertThat(Manager.findEmployeesByProject(p2.getProjectId()))
                        .hasSize(1),
                () -> assertThat(Manager.findEmployeesByProject(p3.getProjectId()))
                        .hasSize(1)
            );
        }
    }
}
