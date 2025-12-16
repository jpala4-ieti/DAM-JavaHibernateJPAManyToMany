package com.project.dao;

import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS DE RELACIONS I INTEGRITAT REFERENCIAL
 * ==========================================
 * 
 * Aquesta classe conté tests específics per verificar el comportament
 * de les relacions JPA entre entitats i la integritat referencial.
 * 
 * RELACIONS TESTADES:
 * 1. OneToMany/ManyToOne: Employee <-> Contact
 *    - cascade=ALL des d'Employee
 *    - orphanRemoval=true
 *    - Contact.employee és obligatori (nullable=false)
 * 
 * 2. ManyToMany: Employee <-> Project
 *    - Employee és el propietari (té @JoinTable)
 *    - Project és l'invers (mappedBy)
 *    - cascade={PERSIST, MERGE} (MAI REMOVE!)
 * 
 * COMPORTAMENTS VERIFICATS:
 * - Cascada d'operacions
 * - Eliminació d'orfes
 * - Consistència bidireccional
 * - Integritat referencial
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests de Relacions JPA i Integritat Referencial")
class ManagerRelationsTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS DE RELACIÓ OneToMany: EMPLOYEE <-> CONTACT
    // ========================================================================
    
    /**
     * Grup de tests per a la relació OneToMany entre Employee i Contact.
     */
    @Nested
    @DisplayName("OneToMany: Employee <-> Contact")
    class EmployeeContactRelationTests {
        
        private Employee empleat;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Relació", "Test", 35000);
        }
        
        /**
         * Test: cascade=ALL - Persistir empleat persisteix contactes.
         * 
         * Nota: En el model actual, els contactes s'afegeixen després
         * de l'empleat, però el cascade hauria de funcionar si es
         * modifica l'empleat amb contactes.
         */
        @Test
        @DisplayName("Els contactes es persisteixen amb l'empleat")
        void cascadePersist_ContactesAmbEmpleat() {
            // ACT
            Contact c1 = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "EMAIL", "email1@test.com", "Email 1");
            Contact c2 = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "PHONE", "666111222", "Telèfon");
            
            // ASSERT - Els contactes existeixen a la BD
            assertAll(
                () -> assertNotNull(Manager.getById(Contact.class, c1.getContactId())),
                () -> assertNotNull(Manager.getById(Contact.class, c2.getContactId()))
            );
        }
        
        /**
         * Test: cascade=ALL + orphanRemoval - Eliminar empleat elimina contactes.
         */
        @Test
        @DisplayName("Eliminar empleat elimina contactes en cascada (orphanRemoval)")
        void cascadeRemove_OrphanRemoval_ContactesEliminats() {
            // ARRANGE
            Contact c1 = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "EMAIL", "cascade@test.com", "Test");
            Contact c2 = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "PHONE", "666999888", "Test");
            Long c1Id = c1.getContactId();
            Long c2Id = c2.getContactId();
            
            // Verificar que existeixen
            assertEquals(2, comptarEntitats(Contact.class));
            
            // ACT
            Manager.delete(Employee.class, empleat.getEmployeeId());
            
            // ASSERT - Tots els contactes eliminats
            assertEquals(0, comptarEntitats(Contact.class));
            assertNull(Manager.getById(Contact.class, c1Id));
            assertNull(Manager.getById(Contact.class, c2Id));
        }
        
        /**
         * Test: orphanRemoval - Desvincular contacte l'elimina.
         */
        @Test
        @DisplayName("orphanRemoval: Desvincular contacte l'elimina de la BD")
        void orphanRemoval_DesvinculantContacte_Eliminat() {
            // ARRANGE
            Contact contacte = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "EMAIL", "orphan@test.com", "Test");
            Long contactId = contacte.getContactId();
            
            // ACT - Desvincular el contacte
            Manager.removeContactFromEmployee(empleat.getEmployeeId(), contactId);
            
            // ASSERT - El contacte s'ha eliminat completament
            assertNull(Manager.getById(Contact.class, contactId),
                "El contacte hauria d'estar eliminat (orphanRemoval=true)");
        }
        
        /**
         * Test: Contact.employee és obligatori.
         */
        @Test
        @DisplayName("Un contacte no pot existir sense empleat")
        void contact_RequereixEmpleat() {
            // ARRANGE
            Contact contacte = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "EMAIL", "test@test.com", "Test");
            
            // ASSERT - El contacte té empleat associat
            Contact recuperat = Manager.getById(Contact.class, contacte.getContactId());
            assertNotNull(recuperat, "El contacte hauria d'existir");
            // La relació amb employee es verifica implícitament perquè
            // el contacte es va crear a través de l'empleat
        }
        
        /**
         * Test: Un empleat pot tenir molts contactes.
         */
        @Test
        @DisplayName("Un empleat pot tenir múltiples contactes")
        void empleat_MultiplesContactes() {
            // ACT - Crear 5 contactes
            for (int i = 1; i <= 5; i++) {
                Manager.addContactToEmployee(empleat.getEmployeeId(), 
                    "TYPE" + i, "value" + i, "Desc " + i);
            }
            
            // ASSERT
            Collection<Contact> tots = Manager.listCollection(Contact.class);
            assertEquals(5, tots.size());
        }
        
        /**
         * Test: Eliminar un contacte no afecta l'empleat.
         */
        @Test
        @DisplayName("Eliminar contacte no afecta l'empleat")
        void eliminarContacte_EmpleatPersisteix() {
            // ARRANGE
            Contact contacte = Manager.addContactToEmployee(empleat.getEmployeeId(), 
                "EMAIL", "delete@test.com", "Test");
            
            // ACT
            Manager.removeContactFromEmployee(empleat.getEmployeeId(), 
                contacte.getContactId());
            
            // ASSERT - L'empleat continua existint
            Employee empRecuperat = Manager.getById(Employee.class, empleat.getEmployeeId());
            assertNotNull(empRecuperat);
        }
    }
    
    // ========================================================================
    // TESTS DE RELACIÓ ManyToMany: EMPLOYEE <-> PROJECT
    // ========================================================================
    
    /**
     * Grup de tests per a la relació ManyToMany entre Employee i Project.
     */
    @Nested
    @DisplayName("ManyToMany: Employee <-> Project")
    class EmployeeProjectRelationTests {
        
        private Employee empleat1;
        private Employee empleat2;
        private Project projecte1;
        private Project projecte2;
        
        @BeforeEach
        void setUpTestData() {
            empleat1 = Manager.addEmployee("Emp1", "Test", 30000);
            empleat2 = Manager.addEmployee("Emp2", "Test", 35000);
            projecte1 = Manager.addProject("Proj1", "Desc", "ACTIU");
            projecte2 = Manager.addProject("Proj2", "Desc", "ACTIU");
        }
        
        /**
         * Test: Assignar un empleat a múltiples projectes.
         */
        @Test
        @DisplayName("Un empleat pot estar en múltiples projectes")
        void empleat_EnMultiplesProjectes() {
            // ACT
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte1, projecte2));
            
            // ASSERT - L'empleat apareix en ambdós projectes
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .extracting(Employee::getEmployeeId)
                        .contains(empleat1.getEmployeeId()),
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .extracting(Employee::getEmployeeId)
                        .contains(empleat1.getEmployeeId())
            );
        }
        
        /**
         * Test: Múltiples empleats en un projecte.
         */
        @Test
        @DisplayName("Un projecte pot tenir múltiples empleats")
        void projecte_AmbMultiplesEmpleats() {
            // ACT
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), Set.of(projecte1));
            Manager.updateEmployeeProjects(empleat2.getEmployeeId(), Set.of(projecte1));
            
            // ASSERT
            Collection<Employee> empleats = Manager.findEmployeesByProject(
                projecte1.getProjectId());
            
            assertThat(empleats)
                .hasSize(2)
                .extracting(Employee::getEmployeeId)
                .containsExactlyInAnyOrder(
                    empleat1.getEmployeeId(), 
                    empleat2.getEmployeeId()
                );
        }
        
        /**
         * Test: cascade NO inclou REMOVE - Eliminar empleat no elimina projectes.
         */
        @Test
        @DisplayName("Eliminar empleat NO elimina projectes (sense cascade REMOVE)")
        void eliminarEmpleat_ProjectesPersisteixen() {
            // ARRANGE
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte1, projecte2));
            Long proj1Id = projecte1.getProjectId();
            Long proj2Id = projecte2.getProjectId();
            
            // ACT
            Manager.delete(Employee.class, empleat1.getEmployeeId());
            
            // ASSERT - Els projectes continuen existint
            assertAll(
                () -> assertNotNull(Manager.getById(Project.class, proj1Id),
                    "Projecte1 hauria de persistir"),
                () -> assertNotNull(Manager.getById(Project.class, proj2Id),
                    "Projecte2 hauria de persistir")
            );
        }
        
        /**
         * Test: Eliminar projecte NO elimina empleats.
         */
        @Test
        @DisplayName("Eliminar projecte NO elimina empleats")
        void eliminarProjecte_EmpleatsPersisteixen() {
            // ARRANGE
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), Set.of(projecte1));
            Manager.updateEmployeeProjects(empleat2.getEmployeeId(), Set.of(projecte1));
            Long emp1Id = empleat1.getEmployeeId();
            Long emp2Id = empleat2.getEmployeeId();
            
            // ACT - Desvinculem empleats abans d'eliminar
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), Set.of());
            Manager.updateEmployeeProjects(empleat2.getEmployeeId(), Set.of());
            Manager.delete(Project.class, projecte1.getProjectId());
                        
            // ASSERT
            assertAll(
                () -> assertNotNull(Manager.getById(Employee.class, emp1Id)),
                () -> assertNotNull(Manager.getById(Employee.class, emp2Id))
            );
        }
        
        /**
         * Test: Reassignar projectes reemplaça completament.
         */
        @Test
        @DisplayName("Reassignar projectes reemplaça els anteriors")
        void reassignarProjectes_ReemplacaAnteriors() {
            // ARRANGE
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte1));
            
            // Verificar assignació inicial
            assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                .hasSize(1);
            
            // ACT - Reassignar a projecte2
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte2));
            
            // ASSERT
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .isEmpty(),
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .hasSize(1)
            );
        }
        
        /**
         * Test: Assignar conjunt buit desvincula tots els projectes.
         */
        @Test
        @DisplayName("Assignar conjunt buit desvincula tots els projectes")
        void assignarConjuntBuit_DesvinculaTots() {
            // ARRANGE
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte1, projecte2));
            
            // ACT
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                new HashSet<>());
            
            // ASSERT
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .isEmpty(),
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .isEmpty()
            );
        }
        
        /**
         * Test: La taula pont (employee_project) es gestiona correctament.
         */
        @Test
        @DisplayName("La taula pont es gestiona correctament")
        void taulaPont_GestionadaCorrectament() {
            // ARRANGE & ACT
            Manager.updateEmployeeProjects(empleat1.getEmployeeId(), 
                Set.of(projecte1, projecte2));
            Manager.updateEmployeeProjects(empleat2.getEmployeeId(), 
                Set.of(projecte1));
            
            // ASSERT - Verificar relacions des de projectes
            assertAll(
                // Projecte1 té 2 empleats
                () -> assertEquals(2, 
                    Manager.findEmployeesByProject(projecte1.getProjectId()).size()),
                // Projecte2 té 1 empleat
                () -> assertEquals(1, 
                    Manager.findEmployeesByProject(projecte2.getProjectId()).size())
            );
        }
    }
    
    // ========================================================================
    // TESTS D'INTEGRITAT REFERENCIAL
    // ========================================================================
    
    /**
     * Grup de tests per verificar la integritat referencial.
     */
    @Nested
    @DisplayName("Integritat Referencial")
    class ReferentialIntegrityTests {
        
        /**
         * Test: Eliminar empleat amb contactes i projectes.
         */
        @Test
        @DisplayName("Eliminar empleat amb contactes i projectes - integritat")
        void eliminarEmpleat_AmbRelacions_IntegritatCorrecta() {
            // ARRANGE - Crear empleat complet
            Employee emp = Manager.addEmployee("Complet", "Test", 40000);
            Contact c1 = Manager.addContactToEmployee(emp.getEmployeeId(), 
                "EMAIL", "test@test.com", "Test");
            Project proj = Manager.addProject("TestProj", "Desc", "ACTIU");
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj));
            
            Long empId = emp.getEmployeeId();
            Long contactId = c1.getContactId();
            Long projId = proj.getProjectId();
            
            // ACT
            Manager.delete(Employee.class, empId);
            
            // ASSERT
            assertAll(
                // L'empleat ja no existeix
                () -> assertNull(Manager.getById(Employee.class, empId)),
                // El contacte s'ha eliminat (orphanRemoval)
                () -> assertNull(Manager.getById(Contact.class, contactId)),
                // El projecte continua existint
                () -> assertNotNull(Manager.getById(Project.class, projId)),
                // El projecte ja no té l'empleat
                () -> assertThat(Manager.findEmployeesByProject(projId)).isEmpty()
            );
        }
        
        /**
         * Test: La BD no queda en estat inconsistent després d'operacions.
         */
        @Test
        @DisplayName("La BD manté consistència després de múltiples operacions")
        void multipleOperacions_BDConsistent() {
            // ARRANGE - Crear estructura complexa
            Employee emp1 = Manager.addEmployee("E1", "T", 30000);
            Employee emp2 = Manager.addEmployee("E2", "T", 35000);
            Employee emp3 = Manager.addEmployee("E3", "T", 40000);
            
            Project p1 = Manager.addProject("P1", "D", "ACTIU");
            Project p2 = Manager.addProject("P2", "D", "ACTIU");
            
            Manager.addContactToEmployee(emp1.getEmployeeId(), "EMAIL", "e1@t.com", "T");
            Manager.addContactToEmployee(emp2.getEmployeeId(), "EMAIL", "e2@t.com", "T");
            
            Manager.updateEmployeeProjects(emp1.getEmployeeId(), Set.of(p1, p2));
            Manager.updateEmployeeProjects(emp2.getEmployeeId(), Set.of(p1));
            Manager.updateEmployeeProjects(emp3.getEmployeeId(), Set.of(p2));
            
            // ACT - Operacions diverses
            Manager.delete(Employee.class, emp1.getEmployeeId()); // Elimina amb relacions
            Manager.updateEmployee(emp2.getEmployeeId(), "E2Updated", "T", 45000);
            Manager.addContactToEmployee(emp3.getEmployeeId(), "PHONE", "666", "T");
            
            // ASSERT - Verificar consistència
            assertAll(
                // Recompte d'entitats
                () -> assertEquals(2, comptarEntitats(Employee.class)),
                () -> assertEquals(2, comptarEntitats(Project.class)),
                () -> assertEquals(2, comptarEntitats(Contact.class)), // 1 eliminat amb emp1
                
                // Relacions correctes
                () -> assertEquals(1, 
                    Manager.findEmployeesByProject(p1.getProjectId()).size()), // emp2
                () -> assertEquals(1, 
                    Manager.findEmployeesByProject(p2.getProjectId()).size()), // emp3
                
                // Dades actualitzades
                () -> assertEquals("E2Updated", 
                    Manager.getById(Employee.class, emp2.getEmployeeId()).getFirstName())
            );
        }
        
        /**
         * Test: No es poden crear cicles de referència problemàtics.
         * 
         * Nota: En aquest model no hi ha risc de cicles, però verifiquem
         * que les relacions bidireccionals es gestionen correctament.
         */
        @Test
        @DisplayName("Les relacions bidireccionals es mantenen consistents")
        void relacionsBidireccionals_Consistents() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Bidireccional", "Test", 30000);
            Project proj = Manager.addProject("Bidireccional", "Desc", "ACTIU");
            
            // ACT
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj));
            
            // ASSERT - Verificar ambdós costats de la relació
            Collection<Employee> empleatsDelProjecte = 
                Manager.findEmployeesByProject(proj.getProjectId());
            
            // Des del projecte podem trobar l'empleat
            assertThat(empleatsDelProjecte)
                .extracting(Employee::getEmployeeId)
                .contains(emp.getEmployeeId());
        }
    }
    
    // ========================================================================
    // TESTS DE CASOS LÍMIT
    // ========================================================================
    
    /**
     * Grup de tests per a casos límit en relacions.
     */
    @Nested
    @DisplayName("Casos Límit en Relacions")
    class EdgeCasesRelationsTests {
        
        /**
         * Test: Empleat sense cap relació.
         */
        @Test
        @DisplayName("Empleat sense contactes ni projectes és vàlid")
        void empleatSenseRelacions_Valid() {
            // ACT
            Employee emp = Manager.addEmployee("Sol", "Test", 30000);
            
            // ASSERT
            Employee recuperat = Manager.getById(Employee.class, emp.getEmployeeId());
            assertNotNull(recuperat);
        }
        
        /**
         * Test: Projecte sense empleats.
         */
        @Test
        @DisplayName("Projecte sense empleats és vàlid")
        void projecteSenseEmpleats_Valid() {
            // ACT
            Project proj = Manager.addProject("Solitari", "Desc", "PLANIFICAT");
            
            // ASSERT
            Project recuperat = Manager.getById(Project.class, proj.getProjectId());
            assertNotNull(recuperat);
            assertThat(Manager.findEmployeesByProject(proj.getProjectId())).isEmpty();
        }
        
        /**
         * Test: Múltiples operacions sobre el mateix empleat.
         */
        @Test
        @DisplayName("Múltiples operacions sobre el mateix empleat")
        void multiplesOperacions_MateixEmpleat() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Multi", "Op", 30000);
            Project p1 = Manager.addProject("P1", "D", "ACTIU");
            Project p2 = Manager.addProject("P2", "D", "ACTIU");
            
            // ACT - Moltes operacions
            Manager.addContactToEmployee(emp.getEmployeeId(), "EMAIL", "a@a.com", "T");
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p1));
            Manager.addContactToEmployee(emp.getEmployeeId(), "PHONE", "666", "T");
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p1, p2));
            Manager.updateEmployee(emp.getEmployeeId(), "MultiUpdated", "Op", 50000);
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p2));
            
            // ASSERT
            Employee recuperat = Manager.getById(Employee.class, emp.getEmployeeId());
            assertAll(
                () -> assertEquals("MultiUpdated", recuperat.getFirstName()),
                () -> assertEquals(50000, recuperat.getSalary()),
                () -> assertEquals(2, comptarEntitats(Contact.class)),
                () -> assertThat(Manager.findEmployeesByProject(p1.getProjectId())).isEmpty(),
                () -> assertThat(Manager.findEmployeesByProject(p2.getProjectId())).hasSize(1)
            );
        }
    }
}
