package com.project.integration;

import com.project.dao.Manager;
import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS D'INTEGRACIÓ COMPLETS
 * ==========================
 * 
 * Aquesta classe conté tests d'integració que verifiquen escenaris
 * complets que involucren múltiples entitats i operacions.
 * 
 * DIFERÈNCIA AMB TESTS UNITARIS:
 * - Els tests unitaris verifiquen components aïllats
 * - Els tests d'integració verifiquen la interacció entre components
 * 
 * ESCENARIS TESTATS:
 * - Flux complet de creació d'una empresa amb empleats i projectes
 * - Operacions massives
 * - Consistència de dades després de múltiples operacions
 * - Escenaris de negoci realistes
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests d'Integració Complets")
class FullIntegrationTest extends HibernateTestBase {
    
    // ========================================================================
    // ESCENARIS DE NEGOCI COMPLETS
    // ========================================================================
    
    /**
     * Grup de tests per a escenaris de negoci realistes.
     */
    @Nested
    @DisplayName("Escenaris de Negoci")
    class BusinessScenariosTests {
        
        /**
         * Test: Escenari complet de creació d'un departament.
         * 
         * Simula la creació d'un departament amb:
         * - 5 empleats
         * - Cada empleat amb contactes
         * - 3 projectes
         * - Assignacions empleat-projecte
         */
        @Test
        @DisplayName("Escenari complet: Crear departament amb empleats i projectes")
        void escenariComplet_CrearDepartament() {
            // ============ ARRANGE: Crear estructura ============
            
            // Crear empleats
            Employee joan = Manager.addEmployee("Joan", "Garcia", 35000);
            Employee marta = Manager.addEmployee("Marta", "Ferrer", 42000);
            Employee pere = Manager.addEmployee("Pere", "Soler", 38000);
            Employee anna = Manager.addEmployee("Anna", "Puig", 45000);
            Employee marc = Manager.addEmployee("Marc", "Vidal", 32000);
            
            // Afegir contactes a cada empleat
            Manager.addContactToEmployee(joan.getEmployeeId(), "EMAIL", "joan@empresa.cat", "Email");
            Manager.addContactToEmployee(joan.getEmployeeId(), "PHONE", "666111111", "Mòbil");
            
            Manager.addContactToEmployee(marta.getEmployeeId(), "EMAIL", "marta@empresa.cat", "Email");
            Manager.addContactToEmployee(marta.getEmployeeId(), "EMAIL", "marta.personal@gmail.com", "Personal");
            
            Manager.addContactToEmployee(pere.getEmployeeId(), "EMAIL", "pere@empresa.cat", "Email");
            Manager.addContactToEmployee(pere.getEmployeeId(), "PHONE", "666333333", "Mòbil");
            Manager.addContactToEmployee(pere.getEmployeeId(), "ADDRESS", "Carrer Major 1", "Adreça");
            
            Manager.addContactToEmployee(anna.getEmployeeId(), "EMAIL", "anna@empresa.cat", "Email");
            
            Manager.addContactToEmployee(marc.getEmployeeId(), "EMAIL", "marc@empresa.cat", "Email");
            Manager.addContactToEmployee(marc.getEmployeeId(), "PHONE", "666555555", "Mòbil");
            
            // Crear projectes
            Project webApp = Manager.addProject("Web Corporativa", "Portal web", "ACTIU");
            Project mobileApp = Manager.addProject("App Mòbil", "iOS i Android", "ACTIU");
            Project intranet = Manager.addProject("Intranet", "Portal intern", "PLANIFICAT");
            
            // Assignar empleats a projectes
            Manager.updateEmployeeProjects(joan.getEmployeeId(), Set.of(webApp, intranet));
            Manager.updateEmployeeProjects(marta.getEmployeeId(), Set.of(webApp, mobileApp));
            Manager.updateEmployeeProjects(pere.getEmployeeId(), Set.of(mobileApp));
            Manager.updateEmployeeProjects(anna.getEmployeeId(), Set.of(mobileApp, intranet));
            Manager.updateEmployeeProjects(marc.getEmployeeId(), Set.of(intranet));
            
            // ============ ACT & ASSERT: Verificar estructura ============
            
            // Verificar recomptes
            assertEquals(5, comptarEntitats(Employee.class), "Haurien d'haver 5 empleats");
            assertEquals(3, comptarEntitats(Project.class), "Haurien d'haver 3 projectes");
            assertEquals(10, comptarEntitats(Contact.class), "Haurien d'haver 10 contactes");
            
            // Verificar empleats per projecte
            assertAll("Verificar assignacions a projectes",
                () -> assertEquals(2, Manager.findEmployeesByProject(webApp.getProjectId()).size(),
                    "Web hauria de tenir 2 empleats"),
                () -> assertEquals(3, Manager.findEmployeesByProject(mobileApp.getProjectId()).size(),
                    "App Mòbil hauria de tenir 3 empleats"),
                () -> assertEquals(3, Manager.findEmployeesByProject(intranet.getProjectId()).size(),
                    "Intranet hauria de tenir 3 empleats")
            );
            
            // Verificar cerques per tipus de contacte
            assertEquals(5, Manager.findEmployeesByContactType("EMAIL").size(),
                "Tots els empleats tenen email");
            assertEquals(3, Manager.findEmployeesByContactType("PHONE").size(),
                "3 empleats tenen telèfon");
        }
        
        /**
         * Test: Escenari de reestructuració - canviar empleats de projecte.
         */
        @Test
        @DisplayName("Escenari: Reestructuració de projectes")
        void escenari_Reestructuracio() {
            // ARRANGE - Crear estructura inicial
            Employee emp1 = Manager.addEmployee("E1", "T", 30000);
            Employee emp2 = Manager.addEmployee("E2", "T", 35000);
            Employee emp3 = Manager.addEmployee("E3", "T", 40000);
            
            Project projA = Manager.addProject("Projecte A", "Desc", "ACTIU");
            Project projB = Manager.addProject("Projecte B", "Desc", "ACTIU");
            
            // Assignació inicial: tots a projecte A
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(projA));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projA));
            Manager.updateEmployeeProjects(emp3.getEmployeeId(), Set.of(projA));
            
            assertEquals(3, Manager.findEmployeesByProject(projA.getProjectId()).size());
            assertEquals(0, Manager.findEmployeesByProject(projB.getProjectId()).size());
            
            // ACT - Reestructurar: moure emp2 i emp3 al projecte B
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projB));
            Manager.updateEmployeeProjects(emp3.getEmployeeId(), Set.of(projB));
            
            // ASSERT
            assertAll("Verificar reestructuració",
                () -> assertEquals(1, Manager.findEmployeesByProject(projA.getProjectId()).size(),
                    "Projecte A hauria de tenir 1 empleat"),
                () -> assertEquals(2, Manager.findEmployeesByProject(projB.getProjectId()).size(),
                    "Projecte B hauria de tenir 2 empleats")
            );
        }
        
        /**
         * Test: Escenari de baixa d'un empleat amb totes les seves dades.
         */
        @Test
        @DisplayName("Escenari: Baixa d'empleat (eliminar amb totes les dades)")
        void escenari_BaixaEmpleat() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Baixa", "Test", 30000);
            Manager.addContactToEmployee(emp.getEmployeeId(), "EMAIL", "baixa@test.com", "Email");
            Manager.addContactToEmployee(emp.getEmployeeId(), "PHONE", "666999888", "Telèfon");
            
            Project proj1 = Manager.addProject("P1", "D", "ACTIU");
            Project proj2 = Manager.addProject("P2", "D", "ACTIU");
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj1, proj2));
            
            Long empId = emp.getEmployeeId();
            
            // Verificar estat inicial
            assertEquals(1, comptarEntitats(Employee.class));
            assertEquals(2, comptarEntitats(Contact.class));
            assertEquals(2, comptarEntitats(Project.class));
            assertEquals(1, Manager.findEmployeesByProject(proj1.getProjectId()).size());
            
            // ACT - Donar de baixa l'empleat
            Manager.deleteEmployee(empId);
            
            // ASSERT
            assertAll("Verificar baixa",
                () -> assertEquals(0, comptarEntitats(Employee.class), 
                    "No haurien de quedar empleats"),
                () -> assertEquals(0, comptarEntitats(Contact.class), 
                    "Els contactes s'haurien d'haver eliminat (orphanRemoval)"),
                () -> assertEquals(2, comptarEntitats(Project.class), 
                    "Els projectes haurien de persistir"),
                () -> assertEquals(0, Manager.findEmployeesByProject(proj1.getProjectId()).size(), 
                    "El projecte ja no hauria de tenir empleats")
            );
        }
        
        /**
         * Test: Escenari de cancel·lació de projecte.
         */
        @Test
        @DisplayName("Escenari: Cancel·lació de projecte")
        void escenari_CancelacioProjecte() {
            // ARRANGE
            Employee emp1 = Manager.addEmployee("E1", "T", 30000);
            Employee emp2 = Manager.addEmployee("E2", "T", 35000);
            Manager.addContactToEmployee(emp1.getEmployeeId(), "EMAIL", "e1@t.com", "T");
            Manager.addContactToEmployee(emp2.getEmployeeId(), "EMAIL", "e2@t.com", "T");
            
            Project projACancelar = Manager.addProject("Cancel·lar", "Desc", "ACTIU");
            Project projActiu = Manager.addProject("Actiu", "Desc", "ACTIU");
            
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(projACancelar, projActiu));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(projACancelar));
            
            Long projCancelId = projACancelar.getProjectId();
            
            // ACT - Cancel·lar el projecte
            Manager.deleteProject(projCancelId);
            
            // ASSERT
            assertAll("Verificar cancel·lació",
                () -> assertNull(Manager.getById(Project.class, projCancelId), 
                    "El projecte cancel·lat no hauria d'existir"),
                () -> assertEquals(2, comptarEntitats(Employee.class), 
                    "Els empleats haurien de persistir"),
                () -> assertEquals(2, comptarEntitats(Contact.class), 
                    "Els contactes haurien de persistir"),
                () -> assertEquals(1, Manager.findEmployeesByProject(projActiu.getProjectId()).size(), 
                    "El projecte actiu hauria de tenir 1 empleat")
            );
        }
    }
    
    // ========================================================================
    // TESTS D'OPERACIONS MASSIVES
    // ========================================================================
    
    /**
     * Grup de tests per a operacions amb grans quantitats de dades.
     */
    @Nested
    @DisplayName("Operacions Massives")
    class MassOperationsTests {
        
        /**
         * Test: Crear molts empleats.
         */
        @Test
        @DisplayName("Crear 50 empleats")
        void crearMoltsEmpleats() {
            // ACT
            for (int i = 1; i <= 50; i++) {
                Manager.addEmployee("Empleat" + i, "Cognom" + i, 25000 + (i * 100));
            }
            
            // ASSERT
            assertEquals(50, comptarEntitats(Employee.class));
        }
        
        /**
         * Test: Crear molts projectes.
         */
        @Test
        @DisplayName("Crear 30 projectes")
        void crearMoltsProjectes() {
            // ACT
            String[] estats = {"ACTIU", "PLANIFICAT", "COMPLETAT"};
            for (int i = 1; i <= 30; i++) {
                Manager.addProject("Projecte" + i, "Descripció " + i, estats[i % 3]);
            }
            
            // ASSERT
            assertEquals(30, comptarEntitats(Project.class));
        }
        
        /**
         * Test: Molts contactes per empleat.
         */
        @Test
        @DisplayName("Un empleat amb 10 contactes")
        void empleatAmbMoltsContactes() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Multi", "Contact", 30000);
            
            // ACT
            String[] tipus = {"EMAIL", "PHONE", "ADDRESS", "FAX", "SKYPE"};
            for (int i = 0; i < 10; i++) {
                Manager.addContactToEmployee(
                    emp.getEmployeeId(), 
                    tipus[i % 5], 
                    "valor" + i, 
                    "Desc " + i
                );
            }
            
            // ASSERT
            assertEquals(10, comptarEntitats(Contact.class));
        }
        
        /**
         * Test: Un projecte amb molts empleats.
         */
        @Test
        @DisplayName("Un projecte amb 20 empleats")
        void projecteAmbMoltsEmpleats() {
            // ARRANGE
            Project proj = Manager.addProject("Gran Projecte", "Desc", "ACTIU");
            Set<Employee> empleats = new HashSet<>();
            
            for (int i = 1; i <= 20; i++) {
                empleats.add(Manager.addEmployee("E" + i, "C" + i, 30000));
            }
            
            // ACT - Assignar tots al projecte
            for (Employee emp : empleats) {
                Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj));
            }
            
            // ASSERT
            assertEquals(20, Manager.findEmployeesByProject(proj.getProjectId()).size());
        }
        
        /**
         * Test: Eliminar molts registres.
         */
        @Test
        @DisplayName("Eliminar 20 empleats seqüencialment")
        void eliminarMoltsEmpleats() {
            // ARRANGE - Crear empleats
            Set<Long> ids = new HashSet<>();
            for (int i = 1; i <= 20; i++) {
                Employee emp = Manager.addEmployee("Delete" + i, "Test", 30000);
                ids.add(emp.getEmployeeId());
            }
            assertEquals(20, comptarEntitats(Employee.class));
            
            // ACT - Eliminar tots
            for (Long id : ids) {
                Manager.deleteEmployee(id);
            }
            
            // ASSERT
            assertEquals(0, comptarEntitats(Employee.class));
        }
    }
    
    // ========================================================================
    // TESTS DE CONSISTÈNCIA
    // ========================================================================
    
    /**
     * Grup de tests per verificar la consistència de les dades.
     */
    @Nested
    @DisplayName("Consistència de Dades")
    class DataConsistencyTests {
        
        /**
         * Test: Consistència després de múltiples operacions.
         */
        @Test
        @DisplayName("Consistència després de crear-modificar-eliminar")
        void consistencia_CRUD_Multiple() {
            // CREATE
            Employee emp1 = Manager.addEmployee("Original1", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Original2", "Test", 35000);
            Employee emp3 = Manager.addEmployee("Original3", "Test", 40000);
            
            Manager.addContactToEmployee(emp1.getEmployeeId(), "EMAIL", "e1@t.com", "T");
            Manager.addContactToEmployee(emp2.getEmployeeId(), "EMAIL", "e2@t.com", "T");
            
            Project proj = Manager.addProject("Test", "Desc", "ACTIU");
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(proj));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(proj));
            
            // UPDATE
            Manager.updateEmployee(emp1.getEmployeeId(), "Modified1", "Test", 50000);
            Manager.updateProject(proj.getProjectId(), "Modified Project", "New Desc", "COMPLETAT");
            
            // DELETE
            Manager.deleteEmployee(emp3.getEmployeeId());
            
            // VERIFY CONSISTENCY
            assertAll("Verificar consistència",
                () -> assertEquals(2, comptarEntitats(Employee.class)),
                () -> assertEquals(2, comptarEntitats(Contact.class)),
                () -> assertEquals(1, comptarEntitats(Project.class)),
                () -> assertEquals("Modified1", 
                    Manager.getById(Employee.class, emp1.getEmployeeId()).getFirstName()),
                () -> assertEquals("COMPLETAT", 
                    Manager.getById(Project.class, proj.getProjectId()).getStatus()),
                () -> assertEquals(2, 
                    Manager.findEmployeesByProject(proj.getProjectId()).size())
            );
        }
        
        /**
         * Test: Les relacions es mantenen consistents.
         */
        @Test
        @DisplayName("Les relacions bidireccionals són consistents")
        void consistencia_RelacionsBidireccionals() {
            // ARRANGE & ACT
            Employee emp = Manager.addEmployee("Rel", "Test", 30000);
            Project proj1 = Manager.addProject("P1", "D", "ACTIU");
            Project proj2 = Manager.addProject("P2", "D", "ACTIU");
            
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj1, proj2));
            
            // VERIFY - Des de l'empleat cap als projectes
            // (hauria de funcionar si les col·leccions LAZY s'inicialitzen)
            Collection<Employee> empsP1 = Manager.findEmployeesByProject(proj1.getProjectId());
            Collection<Employee> empsP2 = Manager.findEmployeesByProject(proj2.getProjectId());
            
            assertAll(
                () -> assertEquals(1, empsP1.size()),
                () -> assertEquals(1, empsP2.size()),
                () -> assertTrue(empsP1.stream()
                    .anyMatch(e -> e.getEmployeeId().equals(emp.getEmployeeId()))),
                () -> assertTrue(empsP2.stream()
                    .anyMatch(e -> e.getEmployeeId().equals(emp.getEmployeeId())))
            );
        }
        
        /**
         * Test: Els IDs són estables.
         */
        @Test
        @DisplayName("Els IDs no canvien després d'actualitzacions")
        void consistencia_IDsEstables() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Long idOriginal = emp.getEmployeeId();
            
            // ACT - Múltiples actualitzacions
            Manager.updateEmployee(idOriginal, "Nom1", "Cognom1", 35000);
            Manager.updateEmployee(idOriginal, "Nom2", "Cognom2", 40000);
            Manager.updateEmployee(idOriginal, "Nom3", "Cognom3", 45000);
            
            // ASSERT
            Employee recuperat = Manager.getById(Employee.class, idOriginal);
            assertEquals(idOriginal, recuperat.getEmployeeId(), "L'ID no hauria de canviar");
        }
    }
    
    // ========================================================================
    // TESTS DE CERCA COMPLEXA
    // ========================================================================
    
    /**
     * Grup de tests per a cerques complexes.
     */
    @Nested
    @DisplayName("Cerques Complexes")
    class ComplexSearchTests {
        
        @BeforeEach
        void setUpComplexData() {
            // Crear estructura complexa per a cerques
            Employee e1 = Manager.addEmployee("Joan", "Garcia", 35000);
            Employee e2 = Manager.addEmployee("Marta", "Ferrer", 45000);
            Employee e3 = Manager.addEmployee("Pere", "López", 30000);
            Employee e4 = Manager.addEmployee("Anna", "Puig", 50000);
            
            // Contactes variats
            Manager.addContactToEmployee(e1.getEmployeeId(), "EMAIL", "joan@empresa.cat", "Corp");
            Manager.addContactToEmployee(e1.getEmployeeId(), "PHONE", "666111111", "Mòbil");
            Manager.addContactToEmployee(e2.getEmployeeId(), "EMAIL", "marta@empresa.cat", "Corp");
            Manager.addContactToEmployee(e3.getEmployeeId(), "PHONE", "666333333", "Mòbil");
            Manager.addContactToEmployee(e4.getEmployeeId(), "EMAIL", "anna@empresa.cat", "Corp");
            Manager.addContactToEmployee(e4.getEmployeeId(), "ADDRESS", "Carrer Test", "Casa");
            
            // Projectes
            Project pActiu = Manager.addProject("Actiu", "D", "ACTIU");
            Project pPlanificat = Manager.addProject("Planificat", "D", "PLANIFICAT");
            Project pCompletat = Manager.addProject("Completat", "D", "COMPLETAT");
            
            // Assignacions
            Manager.updateEmployeeProjects(e1.getEmployeeId(), Set.of(pActiu));
            Manager.updateEmployeeProjects(e2.getEmployeeId(), Set.of(pActiu, pPlanificat));
            Manager.updateEmployeeProjects(e3.getEmployeeId(), Set.of(pCompletat));
            Manager.updateEmployeeProjects(e4.getEmployeeId(), Set.of(pActiu, pPlanificat, pCompletat));
        }
        
        /**
         * Test: Trobar empleats amb email.
         */
        @Test
        @DisplayName("Trobar empleats amb email corporatiu")
        void cercaEmpleatsAmbEmail() {
            Collection<Employee> result = Manager.findEmployeesByContactType("EMAIL");
            
            assertThat(result)
                .hasSize(3)
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Joan", "Marta", "Anna");
        }
        
        /**
         * Test: Trobar empleats amb telèfon.
         */
        @Test
        @DisplayName("Trobar empleats amb telèfon")
        void cercaEmpleatsAmbTelefon() {
            Collection<Employee> result = Manager.findEmployeesByContactType("PHONE");
            
            assertThat(result)
                .hasSize(2)
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Joan", "Pere");
        }
        
        /**
         * Test: Empleats en projecte actiu.
         */
        @Test
        @DisplayName("Trobar empleats en projecte actiu")
        void cercaEmpleatsProjecteActiu() {
            // Trobar el projecte actiu
            Project pActiu = Manager.listCollection(Project.class).stream()
                .filter(p -> "ACTIU".equals(p.getStatus()))
                .findFirst()
                .orElseThrow();
            
            Collection<Employee> result = Manager.findEmployeesByProject(pActiu.getProjectId());
            
            assertThat(result)
                .hasSize(3)
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Joan", "Marta", "Anna");
        }
        
        /**
         * Test: Estadístiques de contactes per tipus.
         */
        @Test
        @DisplayName("Estadístiques de contactes per tipus")
        void estadistiquesContactes() {
            Collection<Contact> totsContactes = Manager.listCollection(Contact.class);
            
            // Agrupar per tipus
            var perTipus = totsContactes.stream()
                .collect(Collectors.groupingBy(Contact::getContactType, Collectors.counting()));
            
            assertAll(
                () -> assertEquals(3L, perTipus.getOrDefault("EMAIL", 0L)),
                () -> assertEquals(2L, perTipus.getOrDefault("PHONE", 0L)),
                () -> assertEquals(1L, perTipus.getOrDefault("ADDRESS", 0L))
            );
        }
    }
}
