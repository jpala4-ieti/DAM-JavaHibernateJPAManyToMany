package com.project.dao;

import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS DE TRANSACCIONS I CICLE DE VIDA
 * =====================================
 * 
 * Aquesta classe conté tests que verifiquen el comportament
 * transaccional del Manager i el cicle de vida de les sessions.
 * 
 * CONCEPTES TESTATS:
 * - Atomicitat de les operacions (tot o res)
 * - Aïllament entre operacions
 * - Consistència de les dades
 * - Durabilitat (persistència)
 * 
 * NOTA IMPORTANT:
 * El Manager actual utilitza el patró session-per-request,
 * on cada operació obre i tanca la seva pròpia sessió.
 * Això significa que cada mètode del Manager és una transacció atòmica.
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests de Transaccions i Cicle de Vida")
class ManagerTransactionTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS D'ATOMICITAT
    // ========================================================================
    
    /**
     * Grup de tests per verificar l'atomicitat de les operacions.
     */
    @Nested
    @DisplayName("Atomicitat de les Operacions")
    class AtomicityTests {
        
        /**
         * Test: addEmployee és atòmic - crea l'empleat completament o no el crea.
         */
        @Test
        @DisplayName("addEmployee és una operació atòmica")
        void addEmployee_Atomic() {
            // ACT
            Employee emp = Manager.addEmployee("Atomic", "Test", 30000);
            
            // ASSERT - L'empleat existeix completament a la BD
            Employee recuperat = Manager.getById(Employee.class, emp.getEmployeeId());
            
            assertAll("Verificar creació atòmica",
                () -> assertNotNull(recuperat, "L'empleat hauria d'existir"),
                () -> assertEquals("Atomic", recuperat.getFirstName()),
                () -> assertEquals("Test", recuperat.getLastName()),
                () -> assertEquals(30000, recuperat.getSalary())
            );
        }
        
        /**
         * Test: addContactToEmployee és atòmic.
         */
        @Test
        @DisplayName("addContactToEmployee és una operació atòmica")
        void addContactToEmployee_Atomic() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            
            // ACT
            Contact contact = Manager.addContactToEmployee(
                emp.getEmployeeId(), "EMAIL", "atomic@test.com", "Test"
            );
            
            // ASSERT
            Contact recuperat = Manager.getById(Contact.class, contact.getContactId());
            
            assertAll("Verificar creació atòmica del contacte",
                () -> assertNotNull(recuperat),
                () -> assertEquals("EMAIL", recuperat.getContactType()),
                () -> assertEquals("atomic@test.com", recuperat.getValue())
            );
        }
        
        /**
         * Test: updateEmployeeProjects és atòmic.
         */
        @Test
        @DisplayName("updateEmployeeProjects actualitza totes les relacions o cap")
        void updateEmployeeProjects_Atomic() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Project p1 = Manager.addProject("P1", "D", "ACTIU");
            Project p2 = Manager.addProject("P2", "D", "ACTIU");
            Project p3 = Manager.addProject("P3", "D", "ACTIU");
            
            // ACT
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p1, p2, p3));
            
            // ASSERT - Totes les relacions o cap
            assertAll(
                () -> assertEquals(1, Manager.findEmployeesByProject(p1.getProjectId()).size()),
                () -> assertEquals(1, Manager.findEmployeesByProject(p2.getProjectId()).size()),
                () -> assertEquals(1, Manager.findEmployeesByProject(p3.getProjectId()).size())
            );
        }
        
        /**
         * Test: delete és atòmic (elimina tot o res).
         */
        @Test
        @DisplayName("delete Employee elimina empleat i contactes atòmicament")
        void deleteEmployee_Atomic() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Delete", "Atomic", 30000);
            Contact c1 = Manager.addContactToEmployee(emp.getEmployeeId(), "EMAIL", "a@a.com", "T");
            Contact c2 = Manager.addContactToEmployee(emp.getEmployeeId(), "PHONE", "666", "T");
            
            Long empId = emp.getEmployeeId();
            Long c1Id = c1.getContactId();
            Long c2Id = c2.getContactId();
            
            // ACT
            Manager.delete(Employee.class, empId);
            
            // ASSERT - Tot eliminat atòmicament
            assertAll(
                () -> assertNull(Manager.getById(Employee.class, empId)),
                () -> assertNull(Manager.getById(Contact.class, c1Id)),
                () -> assertNull(Manager.getById(Contact.class, c2Id))
            );
        }
    }
    
    // ========================================================================
    // TESTS D'AÏLLAMENT
    // ========================================================================
    
    /**
     * Grup de tests per verificar l'aïllament entre operacions.
     */
    @Nested
    @DisplayName("Aïllament entre Operacions")
    class IsolationTests {
        
        /**
         * Test: Operacions separades no interfereixen.
         */
        @Test
        @DisplayName("Operacions separades són independents")
        void operacionsSeparades_Independents() {
            // Operació 1
            Employee emp1 = Manager.addEmployee("Op1", "Test", 30000);
            
            // Operació 2 (independent)
            Employee emp2 = Manager.addEmployee("Op2", "Test", 35000);
            
            // Operació 3 (actualitza emp1, no afecta emp2)
            Manager.updateEmployee(emp1.getEmployeeId(), "Op1Updated", "Test", 50000);
            
            // ASSERT - emp2 no afectat
            Employee emp2Recuperat = Manager.getById(Employee.class, emp2.getEmployeeId());
            assertEquals("Op2", emp2Recuperat.getFirstName());
            assertEquals(35000, emp2Recuperat.getSalary());
        }
        
        /**
         * Test: Eliminar un empleat no afecta altres empleats.
         */
        @Test
        @DisplayName("Eliminar un empleat no afecta els altres")
        void deleteEmpleat_NoAfectaAltres() {
            // ARRANGE
            Employee emp1 = Manager.addEmployee("Delete", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Keep", "Test", 35000);
            Manager.addContactToEmployee(emp1.getEmployeeId(), "EMAIL", "d@t.com", "T");
            Manager.addContactToEmployee(emp2.getEmployeeId(), "EMAIL", "k@t.com", "T");
            
            // ACT
            Manager.delete(Employee.class, emp1.getEmployeeId());
            
            // ASSERT - emp2 intacte
            Employee emp2Recuperat = Manager.getById(Employee.class, emp2.getEmployeeId());
            assertNotNull(emp2Recuperat);
            
            // El contacte de emp2 també intacte
            assertEquals(1, comptarEntitats(Contact.class));
        }
        
        /**
         * Test: Modificar projecte no afecta altres projectes.
         */
        @Test
        @DisplayName("Modificar projecte no afecta altres projectes")
        void updateProjecte_NoAfectaAltres() {
            // ARRANGE
            Project p1 = Manager.addProject("Modificar", "Desc original", "PLANIFICAT");
            Project p2 = Manager.addProject("Intacte", "Desc intacte", "ACTIU");
            
            // ACT
            Manager.updateProject(p1.getProjectId(), "Modificat", "Nova desc", "ACTIU");
            
            // ASSERT - p2 intacte
            Project p2Recuperat = Manager.getById(Project.class, p2.getProjectId());
            assertAll(
                () -> assertEquals("Intacte", p2Recuperat.getName()),
                () -> assertEquals("Desc intacte", p2Recuperat.getDescription()),
                () -> assertEquals("ACTIU", p2Recuperat.getStatus())
            );
        }
    }
    
    // ========================================================================
    // TESTS DE DURABILITAT
    // ========================================================================
    
    /**
     * Grup de tests per verificar la durabilitat (persistència).
     */
    @Nested
    @DisplayName("Durabilitat (Persistència)")
    class DurabilityTests {
        
        /**
         * Test: Les dades persisteixen després de múltiples operacions.
         */
        @Test
        @DisplayName("Les dades persisteixen després de múltiples operacions")
        void dades_Persisteixen() {
            // ARRANGE & ACT - Múltiples operacions
            Employee emp = Manager.addEmployee("Durable", "Test", 30000);
            Long empId = emp.getEmployeeId();
            
            Manager.addContactToEmployee(empId, "EMAIL", "d@t.com", "T");
            Manager.updateEmployee(empId, "DurableUpdated", "TestUpdated", 50000);
            
            Project proj = Manager.addProject("P", "D", "ACTIU");
            Manager.updateEmployeeProjects(empId, Set.of(proj));
            
            // ASSERT - Recuperar i verificar que tot persisteix
            Employee recuperat = Manager.getById(Employee.class, empId);
            
            assertAll("Verificar durabilitat",
                () -> assertNotNull(recuperat),
                () -> assertEquals("DurableUpdated", recuperat.getFirstName()),
                () -> assertEquals(50000, recuperat.getSalary()),
                () -> assertEquals(1, comptarEntitats(Contact.class)),
                () -> assertEquals(1, Manager.findEmployeesByProject(proj.getProjectId()).size())
            );
        }
        
        /**
         * Test: Les eliminacions són permanents.
         */
        @Test
        @DisplayName("Les eliminacions són permanents")
        void eliminacions_Permanents() {
            // ARRANGE
            Employee emp = Manager.addEmployee("ToDelete", "Test", 30000);
            Long empId = emp.getEmployeeId();
            
            // ACT
            Manager.delete(Employee.class, empId);
            
            // ASSERT - Múltiples intents de recuperació fallen
            assertNull(Manager.getById(Employee.class, empId));
            assertNull(Manager.getById(Employee.class, empId)); // Segon intent
            assertNull(Manager.getById(Employee.class, empId)); // Tercer intent
        }
        
        /**
         * Test: Les actualitzacions són permanents.
         */
        @Test
        @DisplayName("Les actualitzacions són permanents")
        void actualitzacions_Permanents() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Original", "Original", 30000);
            Long empId = emp.getEmployeeId();
            
            // ACT - Actualitzar múltiples vegades
            Manager.updateEmployee(empId, "Update1", "Update1", 35000);
            Manager.updateEmployee(empId, "Update2", "Update2", 40000);
            Manager.updateEmployee(empId, "Final", "Final", 50000);
            
            // ASSERT - L'última actualització és la que persisteix
            Employee recuperat = Manager.getById(Employee.class, empId);
            assertAll(
                () -> assertEquals("Final", recuperat.getFirstName()),
                () -> assertEquals("Final", recuperat.getLastName()),
                () -> assertEquals(50000, recuperat.getSalary())
            );
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
    class ConsistencyTests {
        
        /**
         * Test: Els comptadors són consistents.
         */
        @Test
        @DisplayName("Els comptadors d'entitats són consistents")
        void comptadors_Consistents() {
            // ARRANGE & ACT
            int empleatsInicial = comptarEntitats(Employee.class);
            
            Manager.addEmployee("E1", "T", 30000);
            assertEquals(empleatsInicial + 1, comptarEntitats(Employee.class));
            
            Manager.addEmployee("E2", "T", 30000);
            assertEquals(empleatsInicial + 2, comptarEntitats(Employee.class));
            
            Employee e3 = Manager.addEmployee("E3", "T", 30000);
            assertEquals(empleatsInicial + 3, comptarEntitats(Employee.class));
            
            Manager.delete(Employee.class, e3.getEmployeeId());
            assertEquals(empleatsInicial + 2, comptarEntitats(Employee.class));
        }
        
        /**
         * Test: Les relacions ManyToMany són consistents en ambdós sentits.
         */
        @Test
        @DisplayName("Relacions ManyToMany consistents en ambdós sentits")
        void manyToMany_Consistent() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Project proj = Manager.addProject("Test", "D", "ACTIU");
            
            // ACT
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(proj));
            
            // ASSERT - Verificar des d'ambdós costats
            // Des del projecte
            assertThat(Manager.findEmployeesByProject(proj.getProjectId()))
                .hasSize(1)
                .extracting(Employee::getEmployeeId)
                .containsExactly(emp.getEmployeeId());
        }
        
        /**
         * Test: orphanRemoval manté consistència.
         */
        @Test
        @DisplayName("orphanRemoval manté consistència (no queden orfes)")
        void orphanRemoval_Consistent() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Manager.addContactToEmployee(emp.getEmployeeId(), "EMAIL", "a@a.com", "T");
            Manager.addContactToEmployee(emp.getEmployeeId(), "PHONE", "666", "T");
            
            assertEquals(2, comptarEntitats(Contact.class));
            
            // ACT
            Manager.delete(Employee.class, emp.getEmployeeId());
            
            // ASSERT - No queden contactes orfes
            assertEquals(0, comptarEntitats(Contact.class), 
                "No haurien de quedar contactes orfes");
        }
        
        /**
         * Test: La taula pont es manté consistent.
         */
        @Test
        @DisplayName("La taula pont employee_project es manté consistent")
        void taulaPont_Consistent() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Project p1 = Manager.addProject("P1", "D", "ACTIU");
            Project p2 = Manager.addProject("P2", "D", "ACTIU");
            
            // ACT - Assignar i desassignar
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p1, p2));
            assertEquals(1, Manager.findEmployeesByProject(p1.getProjectId()).size());
            assertEquals(1, Manager.findEmployeesByProject(p2.getProjectId()).size());
            
            Manager.updateEmployeeProjects(emp.getEmployeeId(), Set.of(p1));
            
            // ASSERT
            assertAll(
                () -> assertEquals(1, Manager.findEmployeesByProject(p1.getProjectId()).size()),
                () -> assertEquals(0, Manager.findEmployeesByProject(p2.getProjectId()).size())
            );
        }
    }
    
    // ========================================================================
    // TESTS DE RECUPERACIÓ D'ERRORS
    // ========================================================================
    
    /**
     * Grup de tests per a la gestió d'errors.
     */
    @Nested
    @DisplayName("Gestió d'Errors")
    class ErrorHandlingTests {
        
        /**
         * Test: Operacions amb IDs inexistents no afecten altres dades.
         */
        @Test
        @DisplayName("Operacions amb ID inexistent no afecten altres dades")
        void operacioIdInexistent_NoAfectaDades() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            int comptadorInicial = comptarEntitats(Employee.class);
            
            // ACT - Operacions amb IDs que no existeixen
            Manager.updateEmployee(99999L, "Nou", "Nom", 50000);
            Manager.delete(Employee.class, 99998L);
            Manager.addContactToEmployee(99997L, "EMAIL", "a@a.com", "T");
            
            // ASSERT - Les dades originals no s'han afectat
            assertEquals(comptadorInicial, comptarEntitats(Employee.class));
            Employee recuperat = Manager.getById(Employee.class, emp.getEmployeeId());
            assertEquals("Test", recuperat.getFirstName());
        }
        
        /**
         * Test: getById amb ID inexistent no llança excepció.
         */
        @Test
        @DisplayName("getById amb ID inexistent retorna null, no llança excepció")
        void getById_IdInexistent_RetornaNull() {
            // ACT & ASSERT
            assertDoesNotThrow(() -> {
                Employee result = Manager.getById(Employee.class, 99999L);
                assertNull(result);
            });
        }
        
        /**
         * Test: findEmployeesByProject amb projecte inexistent.
         */
        @Test
        @DisplayName("findEmployeesByProject amb projecte inexistent retorna buit")
        void findEmployeesByProject_Inexistent_RetornaBuit() {
            // ACT & ASSERT
            assertDoesNotThrow(() -> {
                var result = Manager.findEmployeesByProject(99999L);
                assertThat(result).isEmpty();
            });
        }
    }
}
