package com.project.dao;

import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS PER A QUERIES I MÈTODES D'UTILITAT DEL MANAGER
 * ====================================================
 * 
 * Aquesta classe conté tests per als mètodes de consulta SQL nativa
 * i els mètodes d'utilitat per formatar resultats.
 * 
 * MÈTODES TESTATS:
 * - queryUpdate(): Execució de sentències SQL UPDATE/INSERT/DELETE
 * - queryTable(): Execució de sentències SQL SELECT
 * - tableToString(): Formatació de resultats de queries
 * - collectionToString(): Formatació de col·leccions d'entitats
 * - listCollection(): Llistat genèric d'entitats
 * 
 * NOTA: Els mètodes de query nativa (queryUpdate, queryTable) s'utilitzen
 * principalment per operacions que no es poden fer eficientment amb HQL
 * o per a tasques d'administració.
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests de Queries i Utilitats del Manager")
class ManagerQueryTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS DE QUERY UPDATE (SQL NATIU)
    // ========================================================================
    
    /**
     * Grup de tests per a queryUpdate().
     */
    @Nested
    @DisplayName("queryUpdate() - SQL UPDATE/INSERT/DELETE")
    class QueryUpdateTests {
        
        /**
         * Test: queryUpdate pot executar DELETE.
         */
        @Test
        @DisplayName("queryUpdate executa DELETE correctament")
        void queryUpdate_Delete_Executat() {
            // ARRANGE - Crear dades
            Manager.addEmployee("Delete1", "Test", 30000);
            Manager.addEmployee("Delete2", "Test", 35000);
            assertEquals(2, comptarEntitats(Employee.class));
            
            // ACT
            Manager.queryUpdate("DELETE FROM employees WHERE salary < 33000");
            
            // ASSERT
            assertEquals(1, comptarEntitats(Employee.class));
        }
        
        /**
         * Test: queryUpdate pot executar UPDATE.
         */
        @Test
        @DisplayName("queryUpdate executa UPDATE correctament")
        void queryUpdate_Update_Executat() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Update", "Test", 30000);
            
            // ACT - Actualitzar salari via SQL natiu
            Manager.queryUpdate(
                "UPDATE employees SET salary = 50000 WHERE id = " + emp.getEmployeeId()
            );
            
            // ASSERT - Verificar el canvi
            Employee actualitzat = Manager.getById(Employee.class, emp.getEmployeeId());
            assertEquals(50000, actualitzat.getSalary());
        }
        
        /**
         * Test: queryUpdate amb taula buida no falla.
         */
        @Test
        @DisplayName("queryUpdate amb taula buida no falla")
        void queryUpdate_TaulaBuida_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.queryUpdate("DELETE FROM employees");
            });
        }
        
        /**
         * Test: queryUpdate pot netejar la taula pont.
         */
        @Test
        @DisplayName("queryUpdate pot netejar la taula pont employee_project")
        void queryUpdate_NetejaTaulaPont() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Project proj = Manager.addProject("Test", "Desc", "ACTIU");
            Manager.updateEmployeeProjects(emp.getEmployeeId(), 
                java.util.Set.of(proj));
            
            // Verificar que hi ha relació
            assertEquals(1, Manager.findEmployeesByProject(proj.getProjectId()).size());
            
            // ACT
            Manager.queryUpdate("DELETE FROM employee_project");
            
            // ASSERT - La relació s'ha eliminat
            assertEquals(0, Manager.findEmployeesByProject(proj.getProjectId()).size());
            // Però les entitats continuen existint
            assertNotNull(Manager.getById(Employee.class, emp.getEmployeeId()));
            assertNotNull(Manager.getById(Project.class, proj.getProjectId()));
        }
    }
    
    // ========================================================================
    // TESTS DE QUERY TABLE (SQL SELECT NATIU)
    // ========================================================================
    
    /**
     * Grup de tests per a queryTable().
     */
    @Nested
    @DisplayName("queryTable() - SQL SELECT")
    class QueryTableTests {
        
        @BeforeEach
        void setUpTestData() {
            Manager.addEmployee("Anna", "Garcia", 35000);
            Manager.addEmployee("Pere", "López", 42000);
            Manager.addEmployee("Maria", "Ferrer", 38000);
        }
        
        /**
         * Test: queryTable retorna resultats de SELECT.
         */
        @Test
        @DisplayName("queryTable retorna resultats de SELECT")
        void queryTable_SelectAll_RetornaResultats() {
            // ACT
            List<Object[]> resultats = Manager.queryTable(
                "SELECT * FROM employees"
            );
            
            // ASSERT
            assertEquals(3, resultats.size(), 
                "Hauria de retornar 3 files");
        }
        
        /**
         * Test: queryTable amb filtre WHERE.
         */
        @Test
        @DisplayName("queryTable amb WHERE filtra correctament")
        void queryTable_AmbWhere_Filtrat() {
            // ACT
            List<Object[]> resultats = Manager.queryTable(
                "SELECT * FROM employees WHERE salary > 37000"
            );
            
            // ASSERT
            assertEquals(2, resultats.size()); // Pere i Maria
        }
        
        /**
         * Test: queryTable seleccionant columnes específiques.
         */
        @Test
        @DisplayName("queryTable pot seleccionar columnes específiques")
        void queryTable_ColumnesEspecifiques() {
            // ACT
            List<Object[]> resultats = Manager.queryTable(
                "SELECT firstName, salary FROM employees ORDER BY salary DESC"
            );
            
            // ASSERT
            assertEquals(3, resultats.size());
            // Cada fila hauria de tenir 2 columnes
            assertEquals(2, resultats.get(0).length);
            // La primera fila hauria de ser Pere (salari més alt)
            assertEquals("Pere", resultats.get(0)[0]);
        }
        
        /**
         * Test: queryTable amb COUNT.
         */
        @Test
        @DisplayName("queryTable pot executar funcions agregades (COUNT)")
        void queryTable_Count_Funciona() {
            // ARRANGE
            // El @BeforeEach ja crea 3 empleats (Anna, Pere, Maria)
            
            // ACT
            List<?> resultats = Manager.queryTable(
                "SELECT COUNT(*) FROM employees"
            );
            
            // ASSERT
            assertEquals(1, resultats.size());
            // COUNT pot retornar Long directament o Object[]
            Object firstResult = resultats.get(0);
            Number count;
            if (firstResult instanceof Object[]) {
                count = (Number) ((Object[]) firstResult)[0];
            } else {
                count = (Number) firstResult;
            }
            assertEquals(3, count.intValue());
        }        

        /**
         * Test: queryTable sense resultats retorna llista buida.
         */
        @Test
        @DisplayName("queryTable sense resultats retorna llista buida")
        void queryTable_SenseResultats_LlistaBuida() {
            // ACT
            List<Object[]> resultats = Manager.queryTable(
                "SELECT * FROM employees WHERE salary > 100000"
            );
            
            // ASSERT
            assertNotNull(resultats);
            assertTrue(resultats.isEmpty());
        }
        
        /**
         * Test: queryTable amb JOIN.
         */
        @Test
        @DisplayName("queryTable pot executar JOINs")
        void queryTable_AmbJoin() {
            // ARRANGE - Afegir contactes
            Employee emp = Manager.getById(Employee.class, 
                Manager.listCollection(Employee.class).iterator().next().getEmployeeId());
            Manager.addContactToEmployee(emp.getEmployeeId(), 
                "EMAIL", "test@test.com", "Test");
            
            // ACT
            List<Object[]> resultats = Manager.queryTable(
                "SELECT e.firstName, c.contact_value " +
                "FROM employees e " +
                "JOIN contacts c ON e.id = c.employee_id"
            );
            
            // ASSERT
            assertFalse(resultats.isEmpty());
            assertEquals(2, resultats.get(0).length); // 2 columnes
        }
    }
    
    // ========================================================================
    // TESTS DE TABLE TO STRING
    // ========================================================================
    
    /**
     * Grup de tests per a tableToString().
     */
    @Nested
    @DisplayName("tableToString() - Formatació de resultats de queries")
    class TableToStringTests {
        
        /**
         * Test: tableToString formata correctament una fila.
         */
        @Test
        @DisplayName("tableToString formata una fila correctament")
        void tableToString_UnaFila_Formatada() {
            // ARRANGE
            Manager.addEmployee("Test", "User", 30000);
            List<Object[]> resultats = Manager.queryTable(
                "SELECT firstName, lastName FROM employees"
            );
            
            // ACT
            String resultat = Manager.tableToString(resultats);
            
            // ASSERT
            assertNotNull(resultat);
            assertFalse(resultat.isEmpty());
            assertTrue(resultat.contains("Test"));
            assertTrue(resultat.contains("User"));
        }
        
        /**
         * Test: tableToString formata múltiples files.
         */
        @Test
        @DisplayName("tableToString formata múltiples files amb separadors")
        void tableToString_MultiplesFiles_AmbSeparadors() {
            // ARRANGE
            Manager.addEmployee("Anna", "Garcia", 30000);
            Manager.addEmployee("Pere", "López", 35000);
            List<Object[]> resultats = Manager.queryTable(
                "SELECT firstName, lastName FROM employees ORDER BY firstName"
            );
            
            // ACT
            String resultat = Manager.tableToString(resultats);
            
            // ASSERT
            assertTrue(resultat.contains("Anna"));
            assertTrue(resultat.contains("Pere"));
            assertTrue(resultat.contains("\n"), 
                "Hauria de tenir separadors de línia entre files");
        }
        
        /**
         * Test: tableToString amb llista buida retorna string buit.
         */
        @Test
        @DisplayName("tableToString amb llista buida retorna string buit")
        void tableToString_LlistaBuida_StringBuit() {
            // ARRANGE
            List<Object[]> resultats = Manager.queryTable(
                "SELECT * FROM employees WHERE 1=0"
            );
            
            // ACT
            String resultat = Manager.tableToString(resultats);
            
            // ASSERT
            assertEquals("", resultat);
        }
        
        /**
         * Test: tableToString amb null retorna string buit.
         */
        @Test
        @DisplayName("tableToString amb null retorna string buit")
        void tableToString_Null_StringBuit() {
            // ACT
            String resultat = Manager.tableToString(null);
            
            // ASSERT
            assertEquals("", resultat);
        }
        
        /**
         * Test: tableToString gestiona valors null a les cel·les.
         */
        @Test
        @DisplayName("tableToString gestiona valors null a les cel·les")
        void tableToString_AmbNulls_GestionatCorrectament() {
            // ARRANGE
            Manager.addEmployee("Test", "User", 30000);
            // La descripció del contacte és nullable
            Manager.addContactToEmployee(
                Manager.listCollection(Employee.class).iterator().next().getEmployeeId(),
                "EMAIL", "test@test.com", null
            );
            
            List<Object[]> resultats = Manager.queryTable(
                "SELECT contactType, contact_value, description FROM contacts"
            );
            
            // ACT
            String resultat = Manager.tableToString(resultats);
            
            // ASSERT - No hauria de fallar amb nulls
            assertNotNull(resultat);
            assertTrue(resultat.contains("EMAIL"));
        }
    }
    
    // ========================================================================
    // TESTS DE COLLECTION TO STRING
    // ========================================================================
    
    /**
     * Grup de tests per a collectionToString().
     */
    @Nested
    @DisplayName("collectionToString() - Formatació de col·leccions")
    class CollectionToStringTests {
        
        /**
         * Test: collectionToString formata col·lecció d'empleats.
         */
        @Test
        @DisplayName("collectionToString formata col·lecció d'empleats")
        void collectionToString_Empleats_Formatada() {
            // ARRANGE
            Manager.addEmployee("Anna", "Garcia", 30000);
            Manager.addEmployee("Pere", "López", 35000);
            Collection<Employee> empleats = Manager.listCollection(Employee.class);
            
            // ACT
            String resultat = Manager.collectionToString(Employee.class, empleats);
            
            // ASSERT
            assertNotNull(resultat);
            assertTrue(resultat.contains("Anna"));
            assertTrue(resultat.contains("Pere"));
        }
        
        /**
         * Test: collectionToString formata col·lecció de projectes.
         */
        @Test
        @DisplayName("collectionToString formata col·lecció de projectes")
        void collectionToString_Projectes_Formatada() {
            // ARRANGE
            Manager.addProject("Web", "Desc", "ACTIU");
            Manager.addProject("App", "Desc", "PLANIFICAT");
            Collection<Project> projectes = Manager.listCollection(Project.class);
            
            // ACT
            String resultat = Manager.collectionToString(Project.class, projectes);
            
            // ASSERT
            assertNotNull(resultat);
            assertTrue(resultat.contains("Web"));
            assertTrue(resultat.contains("App"));
        }
        
        /**
         * Test: collectionToString amb col·lecció buida.
         */
        @Test
        @DisplayName("collectionToString amb col·lecció buida retorna buit")
        void collectionToString_ColeccioBuida_StringBuit() {
            // ACT
            String resultat = Manager.collectionToString(Employee.class, 
                java.util.Collections.emptyList());
            
            // ASSERT
            assertEquals("", resultat);
        }
        
        /**
         * Test: collectionToString amb null.
         */
        @Test
        @DisplayName("collectionToString amb null retorna buit")
        void collectionToString_Null_StringBuit() {
            // ACT
            String resultat = Manager.collectionToString(Employee.class, null);
            
            // ASSERT
            assertEquals("", resultat);
        }
    }
    
    // ========================================================================
    // TESTS DE LIST COLLECTION (GENÈRIC)
    // ========================================================================
    
    /**
     * Grup de tests per a listCollection() genèric.
     */
    @Nested
    @DisplayName("listCollection() - Llistat genèric d'entitats")
    class ListCollectionTests {
        
        /**
         * Test: listCollection amb Employee.
         */
        @Test
        @DisplayName("listCollection retorna tots els Employee")
        void listCollection_Employee_RetornaTots() {
            // ARRANGE
            Manager.addEmployee("E1", "T", 30000);
            Manager.addEmployee("E2", "T", 35000);
            Manager.addEmployee("E3", "T", 40000);
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT
            assertEquals(3, resultat.size());
        }
        
        /**
         * Test: listCollection amb Project.
         */
        @Test
        @DisplayName("listCollection retorna tots els Project")
        void listCollection_Project_RetornaTots() {
            // ARRANGE
            Manager.addProject("P1", "D", "ACTIU");
            Manager.addProject("P2", "D", "COMPLETAT");
            
            // ACT
            Collection<Project> resultat = Manager.listCollection(Project.class);
            
            // ASSERT
            assertEquals(2, resultat.size());
        }
        
        /**
         * Test: listCollection amb Contact.
         */
        @Test
        @DisplayName("listCollection retorna tots els Contact")
        void listCollection_Contact_RetornaTots() {
            // ARRANGE
            Employee emp = Manager.addEmployee("Test", "Test", 30000);
            Manager.addContactToEmployee(emp.getEmployeeId(), "EMAIL", "a@a.com", "T");
            Manager.addContactToEmployee(emp.getEmployeeId(), "PHONE", "666", "T");
            
            // ACT
            Collection<com.project.domain.Contact> resultat = 
                Manager.listCollection(com.project.domain.Contact.class);
            
            // ASSERT
            assertEquals(2, resultat.size());
        }
        
        /**
         * Test: listCollection amb taula buida.
         */
        @Test
        @DisplayName("listCollection amb taula buida retorna col·lecció buida")
        void listCollection_TaulaBuida_ColeccioBuida() {
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT
            assertNotNull(resultat);
            assertTrue(resultat.isEmpty());
        }
        
        /**
         * Test: listCollection retorna objectes correctament tipats.
         */
        @Test
        @DisplayName("listCollection retorna objectes del tipus correcte")
        void listCollection_TipusCorrecte() {
            // ARRANGE
            Manager.addEmployee("Test", "Test", 30000);
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT
            for (Object obj : resultat) {
                assertInstanceOf(Employee.class, obj);
            }
        }
        
        /**
         * Test: listCollection amb AssertJ per verificar contingut.
         */
        @Test
        @DisplayName("listCollection amb verificació detallada de contingut")
        void listCollection_VerificacioDetallada() {
            // ARRANGE
            Manager.addEmployee("Anna", "Garcia", 35000);
            Manager.addEmployee("Pere", "López", 45000);
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT amb AssertJ
            assertThat(resultat)
                .hasSize(2)
                .extracting(Employee::getFirstName, Employee::getSalary)
                .containsExactlyInAnyOrder(
                    tuple("Anna", 35000),
                    tuple("Pere", 45000)
                );
        }
    }
}
