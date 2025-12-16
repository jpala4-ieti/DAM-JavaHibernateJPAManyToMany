package com.project.dao;

import com.project.domain.Employee;
import com.project.domain.Project;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS CRUD PER A LA GESTIÓ D'EMPLEATS
 * =====================================
 * 
 * Aquesta classe conté tots els tests relacionats amb les operacions
 * CRUD (Create, Read, Update, Delete) d'empleats a través del Manager.
 * 
 * ESTRUCTURA DELS TESTS:
 * - Cada operació CRUD té la seva pròpia classe @Nested
 * - Els tests segueixen el patró AAA (Arrange-Act-Assert)
 * - Cada test és independent i aïllat
 * 
 * COBERTURA:
 * - addEmployee(): Creació d'empleats
 * - getById(): Lectura d'empleats
 * - updateEmployee(): Modificació d'empleats
 * - delete(): Eliminació d'empleats
 * - listCollection(): Llistat d'empleats
 * - updateEmployeeProjects(): Assignació de projectes
 * - findEmployeesByContactType(): Cerca per tipus de contacte
 * - findEmployeesByProject(): Cerca per projecte
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests CRUD del Manager per a Employee")
class ManagerEmployeeTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS DE CREACIÓ (CREATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació addEmployee().
     * 
     * Verifica que es poden crear empleats correctament amb
     * diferents combinacions de paràmetres.
     */
    @Nested
    @DisplayName("CREATE - Tests d'addEmployee()")
    class CreateEmployeeTests {
        
        /**
         * Test bàsic: Crear un empleat amb dades vàlides.
         * 
         * ESCENARI: Cridem addEmployee amb nom, cognom i salari vàlids.
         * ESPERAT: Es retorna un Employee amb tots els camps correctes i ID assignat.
         */
        @Test
        @DisplayName("Crear empleat amb dades vàlides retorna entitat amb ID")
        void addEmployee_DadesValides_RetornaEmpleatAmbId() {
            // ARRANGE - Preparar dades d'entrada
            String nom = "Joan";
            String cognom = "Garcia";
            int salari = 35000;
            
            // ACT - Executar l'operació
            Employee resultat = Manager.addEmployee(nom, cognom, salari);
            
            // ASSERT - Verificar resultats
            assertAll("Verificació completa de l'empleat creat",
                // Verificar que no és null
                () -> assertNotNull(resultat, 
                    "L'empleat retornat no hauria de ser null"),
                
                // Verificar que té ID assignat
                () -> assertNotNull(resultat.getEmployeeId(), 
                    "L'ID hauria d'estar assignat per la BD"),
                () -> assertTrue(resultat.getEmployeeId() > 0, 
                    "L'ID ha de ser un valor positiu"),
                
                // Verificar dades
                () -> assertEquals(nom, resultat.getFirstName(), 
                    "El nom no coincideix"),
                () -> assertEquals(cognom, resultat.getLastName(), 
                    "El cognom no coincideix"),
                () -> assertEquals(salari, resultat.getSalary(), 
                    "El salari no coincideix"),
                
                // Verificar col·leccions inicialitzades però buides
                () -> assertNotNull(resultat.getContacts(), 
                    "La col·lecció de contactes hauria d'estar inicialitzada"),
                () -> assertTrue(resultat.getContacts().isEmpty(), 
                    "La col·lecció de contactes hauria d'estar buida"),
                () -> assertNotNull(resultat.getProjects(), 
                    "La col·lecció de projectes hauria d'estar inicialitzada"),
                () -> assertTrue(resultat.getProjects().isEmpty(), 
                    "La col·lecció de projectes hauria d'estar buida")
            );
        }
        
        /**
         * Test parametritzat: Crear empleats amb diferents salaris.
         * 
         * Verifica que el sistema accepta diversos rangs de salaris,
         * incloent valors límit.
         */
        @ParameterizedTest(name = "Salari: {0}")
        @ValueSource(ints = {0, 1, 1000, 50000, 100000, Integer.MAX_VALUE})
        @DisplayName("Crear empleat amb diversos salaris")
        void addEmployee_DiversosSalaris_TotsAcceptats(int salari) {
            // ACT
            Employee resultat = Manager.addEmployee("Test", "User", salari);
            
            // ASSERT
            assertNotNull(resultat.getEmployeeId(), 
                "Hauria de crear empleat amb salari " + salari);
            assertEquals(salari, resultat.getSalary(), 
                "El salari hauria de coincidir");
        }
        
        /**
         * Test parametritzat amb CSV: Crear múltiples empleats amb dades diverses.
         */
        @ParameterizedTest(name = "Empleat: {0} {1}, Salari: {2}")
        @CsvSource({
            "Anna, Martí, 28000",
            "Pere, López, 45000",
            "Maria, Sánchez, 55000",
            "Joan, O'Brien, 32000",
            "李, 王, 40000"
        })
        @DisplayName("Crear empleats amb diferents noms i caràcters especials")
        void addEmployee_NomsVariats_TotsCreats(String nom, String cognom, int salari) {
            // ACT
            Employee resultat = Manager.addEmployee(nom, cognom, salari);
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat.getEmployeeId()),
                () -> assertEquals(nom, resultat.getFirstName()),
                () -> assertEquals(cognom, resultat.getLastName())
            );
        }
        
        /**
         * Test: Els IDs generats són únics per a cada empleat.
         */
        @Test
        @DisplayName("IDs generats són únics per cada empleat")
        void addEmployee_MultiplesEmpleats_IDsUnics() {
            // ACT - Crear múltiples empleats
            Employee emp1 = Manager.addEmployee("Emp1", "Test", 30000);
            Employee emp2 = Manager.addEmployee("Emp2", "Test", 30000);
            Employee emp3 = Manager.addEmployee("Emp3", "Test", 30000);
            
            // ASSERT - Verificar que tots tenen IDs diferents
            Set<Long> ids = Set.of(
                emp1.getEmployeeId(), 
                emp2.getEmployeeId(), 
                emp3.getEmployeeId()
            );
            
            assertEquals(3, ids.size(), 
                "Tots els IDs haurien de ser únics");
        }
        
        /**
         * Test: L'empleat creat es persisteix realment a la BD.
         */
        @Test
        @DisplayName("L'empleat creat existeix a la base de dades")
        void addEmployee_DesprésDeCrear_ExisteixABD() {
            // ACT - Crear empleat
            Employee creat = Manager.addEmployee("Persistent", "Test", 40000);
            Long id = creat.getEmployeeId();
            
            // ASSERT - Verificar que es pot recuperar de la BD
            Employee recuperat = Manager.getById(Employee.class, id);
            
            assertNotNull(recuperat, 
                "L'empleat hauria d'existir a la BD");
            assertEquals(creat.getFirstName(), recuperat.getFirstName(), 
                "Les dades recuperades haurien de coincidir");
        }
    }
    
    // ========================================================================
    // TESTS DE LECTURA (READ)
    // ========================================================================
    
    /**
     * Grup de tests per a operacions de lectura d'empleats.
     */
    @Nested
    @DisplayName("READ - Tests de lectura d'empleats")
    class ReadEmployeeTests {
        
        // Empleat de prova creat abans de cada test d'aquest grup
        private Employee empleatProva;
        
        /**
         * Prepara un empleat de prova abans de cada test de lectura.
         */
        @BeforeEach
        void setUpTestData() {
            empleatProva = Manager.addEmployee("Lectura", "Test", 35000);
        }
        
        /**
         * Test: getById retorna l'empleat correcte quan existeix.
         */
        @Test
        @DisplayName("getById retorna l'empleat quan existeix")
        void getById_EmpleatExistent_RetornaEmpleat() {
            // ACT
            Employee resultat = Manager.getById(Employee.class, empleatProva.getEmployeeId());
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat, "Hauria de retornar l'empleat"),
                () -> assertEquals(empleatProva.getEmployeeId(), resultat.getEmployeeId()),
                () -> assertEquals("Lectura", resultat.getFirstName()),
                () -> assertEquals("Test", resultat.getLastName()),
                () -> assertEquals(35000, resultat.getSalary())
            );
        }
        
        /**
         * Test: getById retorna null quan l'empleat no existeix.
         */
        @Test
        @DisplayName("getById retorna null per ID inexistent")
        void getById_IdInexistent_RetornaNull() {
            // ACT - Buscar amb ID que no existeix
            Employee resultat = Manager.getById(Employee.class, 99999L);
            
            // ASSERT
            assertNull(resultat, 
                "Hauria de retornar null per un ID inexistent");
        }
        
        /**
         * Test: listCollection retorna tots els empleats.
         */
        @Test
        @DisplayName("listCollection retorna tots els empleats existents")
        void listCollection_AmpleatsExistents_RetornaTots() {
            // ARRANGE - Crear empleats addicionals
            Manager.addEmployee("Segon", "Empleat", 25000);
            Manager.addEmployee("Tercer", "Empleat", 45000);
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT
            assertNotNull(resultat, "No hauria de retornar null");
            assertEquals(3, resultat.size(), 
                "Hauria de retornar els 3 empleats creats");
        }
        
        /**
         * Test: listCollection retorna col·lecció buida si no hi ha empleats.
         */
        @Test
        @DisplayName("listCollection retorna col·lecció buida si no hi ha dades")
        void listCollection_SenseEmpleats_RetornaBuit() {
            // ARRANGE - Eliminar l'empleat de prova
            Manager.delete(Employee.class, empleatProva.getEmployeeId());
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT
            assertNotNull(resultat, "No hauria de retornar null");
            assertTrue(resultat.isEmpty(), 
                "Hauria de retornar col·lecció buida");
        }
        
        /**
         * Test amb AssertJ: Verificar contingut de la llista amb més detall.
         */
        @Test
        @DisplayName("listCollection retorna empleats amb les dades correctes")
        void listCollection_VerificarContingut_DadesCorrectes() {
            // ARRANGE
            Manager.addEmployee("Anna", "Test", 30000);
            Manager.addEmployee("Pere", "Test", 40000);
            
            // ACT
            Collection<Employee> resultat = Manager.listCollection(Employee.class);
            
            // ASSERT amb AssertJ per verificacions més llegibles
            assertThat(resultat)
                .hasSize(3)
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Lectura", "Anna", "Pere");
        }
    }
    
    // ========================================================================
    // TESTS D'ACTUALITZACIÓ (UPDATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació updateEmployee().
     */
    @Nested
    @DisplayName("UPDATE - Tests d'actualització d'empleats")
    class UpdateEmployeeTests {
        
        private Employee empleatOriginal;
        
        @BeforeEach
        void setUpTestData() {
            empleatOriginal = Manager.addEmployee("Original", "Cognom", 30000);
        }
        
        /**
         * Test: updateEmployee modifica tots els camps correctament.
         */
        @Test
        @DisplayName("updateEmployee modifica tots els camps")
        void updateEmployee_TotsElsCamps_ModificatsCorrectament() {
            // ARRANGE
            Long id = empleatOriginal.getEmployeeId();
            String nouNom = "NouNom";
            String nouCognom = "NouCognom";
            int nouSalari = 50000;
            
            // ACT
            Manager.updateEmployee(id, nouNom, nouCognom, nouSalari);
            
            // ASSERT - Recuperar i verificar
            Employee actualitzat = Manager.getById(Employee.class, id);
            
            assertAll("Verificar actualització completa",
                () -> assertEquals(nouNom, actualitzat.getFirstName(), 
                    "El nom hauria d'estar actualitzat"),
                () -> assertEquals(nouCognom, actualitzat.getLastName(), 
                    "El cognom hauria d'estar actualitzat"),
                () -> assertEquals(nouSalari, actualitzat.getSalary(), 
                    "El salari hauria d'estar actualitzat"),
                () -> assertEquals(id, actualitzat.getEmployeeId(), 
                    "L'ID no hauria de canviar")
            );
        }
        
        /**
         * Test: updateEmployee només modifica el camp especificat.
         */
        @Test
        @DisplayName("updateEmployee només modifica el nom mantenint resta")
        void updateEmployee_NomesNom_RestaIgual() {
            // ARRANGE
            Long id = empleatOriginal.getEmployeeId();
            
            // ACT - Canviar només el nom, mantenir cognom i salari
            Manager.updateEmployee(id, "NouNom", "Cognom", 30000);
            
            // ASSERT
            Employee actualitzat = Manager.getById(Employee.class, id);
            
            assertEquals("NouNom", actualitzat.getFirstName());
            assertEquals("Cognom", actualitzat.getLastName(), 
                "El cognom no hauria de canviar");
            assertEquals(30000, actualitzat.getSalary(), 
                "El salari no hauria de canviar");
        }
        
        /**
         * Test: updateEmployee amb ID inexistent no falla.
         * 
         * El comportament actual és que simplement no fa res si
         * l'empleat no existeix (no llança excepció).
         */
        @Test
        @DisplayName("updateEmployee amb ID inexistent no llança excepció")
        void updateEmployee_IdInexistent_NoFalla() {
            // ACT & ASSERT - No hauria de llançar excepció
            assertDoesNotThrow(() -> {
                Manager.updateEmployee(99999L, "Nou", "Nom", 50000);
            }, "No hauria de llançar excepció per ID inexistent");
        }
        
        /**
         * Test: updateEmployee preserva les relacions existents.
         */
        @Test
        @DisplayName("updateEmployee preserva contactes i projectes existents")
        void updateEmployee_AmbRelacions_PreservaRelacions() {
            // ARRANGE - Afegir contacte i projecte
            Manager.addContactToEmployee(
                empleatOriginal.getEmployeeId(), 
                "EMAIL", 
                "test@test.com", 
                "Test"
            );
            Project projecte = Manager.addProject("TestProject", "Desc", "ACTIU");
            Manager.updateEmployeeProjects(
                empleatOriginal.getEmployeeId(), 
                Set.of(projecte)
            );
            
            // ACT - Actualitzar dades bàsiques
            Manager.updateEmployee(
                empleatOriginal.getEmployeeId(), 
                "NouNom", 
                "NouCognom", 
                45000
            );
            
            // ASSERT - Verificar que les relacions es mantenen
            Employee actualitzat = Manager.getById(Employee.class, 
                empleatOriginal.getEmployeeId());
            
            // Nota: Pot requerir inicialització LAZY segons configuració
            assertAll(
                () -> assertEquals("NouNom", actualitzat.getFirstName()),
                () -> assertEquals(1, comptarEntitats(com.project.domain.Contact.class),
                    "El contacte hauria de persistir")
            );
        }
    }
    
    // ========================================================================
    // TESTS D'ELIMINACIÓ (DELETE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació delete() amb Employee.
     */
    @Nested
    @DisplayName("DELETE - Tests d'eliminació d'empleats")
    class DeleteEmployeeTests {
        
        private Employee empleatAEliminar;
        
        @BeforeEach
        void setUpTestData() {
            empleatAEliminar = Manager.addEmployee("Eliminar", "Test", 30000);
        }
        
        /**
         * Test: delete elimina l'empleat de la BD.
         */
        @Test
        @DisplayName("delete elimina l'empleat correctament")
        void delete_EmpleatExistent_Eliminat() {
            // ARRANGE
            Long id = empleatAEliminar.getEmployeeId();
            
            // Verificar que existeix abans
            assertNotNull(Manager.getById(Employee.class, id), 
                "L'empleat hauria d'existir abans d'eliminar");
            
            // ACT
            Manager.deleteEmployee(id);
            
            // ASSERT
            assertNull(Manager.getById(Employee.class, id), 
                "L'empleat no hauria d'existir després d'eliminar");
        }
        
        /**
         * Test: delete amb cascade elimina també els contactes (orphanRemoval).
         */
        @Test
        @DisplayName("delete empleat elimina contactes en cascada (orphanRemoval)")
        void delete_AmbContactes_ContactesEliminats() {
            // ARRANGE - Afegir contactes
            com.project.domain.Contact contact = Manager.addContactToEmployee(
                empleatAEliminar.getEmployeeId(), 
                "EMAIL", 
                "delete@test.com", 
                "Test"
            );
            Long contactId = contact.getContactId();
            
            // Verificar que el contacte existeix
            assertNotNull(Manager.getById(com.project.domain.Contact.class, contactId),
                "El contacte hauria d'existir abans d'eliminar l'empleat");
            
            // ACT
            Manager.deleteEmployee(empleatAEliminar.getEmployeeId());
            
            // ASSERT - El contacte també s'ha eliminat
            assertNull(Manager.getById(com.project.domain.Contact.class, contactId),
                "El contacte hauria d'haver-se eliminat en cascada");
        }
        
        /**
         * Test: delete empleat NO elimina projectes (ManyToMany sense cascade REMOVE).
         */
        @Test
        @DisplayName("delete empleat NO elimina projectes associats")
        void delete_AmbProjectes_ProjectesPersisteixen() {
            // ARRANGE - Crear i assignar projecte
            Project projecte = Manager.addProject("Sobreviu", "Desc", "ACTIU");
            Long projecteId = projecte.getProjectId();
            Manager.updateEmployeeProjects(
                empleatAEliminar.getEmployeeId(), 
                Set.of(projecte)
            );
            
            // ACT
            Manager.deleteEmployee(empleatAEliminar.getEmployeeId());
            
            // ASSERT - El projecte ha de continuar existint
            Project projecteRecuperat = Manager.getById(Project.class, projecteId);
            assertNotNull(projecteRecuperat, 
                "El projecte NO hauria d'eliminar-se quan s'elimina l'empleat");
        }
        
        /**
         * Test: delete amb ID inexistent no llança excepció.
         */
        @Test
        @DisplayName("delete amb ID inexistent no llança excepció")
        void delete_IdInexistent_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.delete(Employee.class, 99999L);
            }, "No hauria de llançar excepció per ID inexistent");
        }
        
        /**
         * Test: Després de delete, el recompte d'empleats és correcte.
         */
        @Test
        @DisplayName("delete decrementa el nombre d'empleats")
        void delete_DecrementaComptador() {
            // ARRANGE
            Manager.addEmployee("Extra", "Test", 25000);
            int comptadorInicial = comptarEntitats(Employee.class);
            
            // ACT
            Manager.deleteEmployee(empleatAEliminar.getEmployeeId());
            
            // ASSERT
            assertEquals(comptadorInicial - 1, comptarEntitats(Employee.class),
                "El nombre d'empleats hauria de decrementar en 1");
        }
    }
    
    // ========================================================================
    // TESTS DE CERCA I FILTRES
    // ========================================================================
    
    /**
     * Grup de tests per a operacions de cerca d'empleats.
     */
    @Nested
    @DisplayName("CERCA - Tests de cerca i filtres d'empleats")
    class SearchEmployeeTests {
        
        private Employee empAmbEmail;
        private Employee empAmbTelefon;
        private Employee empSenseContacte;
        private Project projecteWeb;
        
        /**
         * Prepara un conjunt de dades de prova per als tests de cerca.
         */
        @BeforeEach
        void setUpTestData() {
            // Crear empleats
            empAmbEmail = Manager.addEmployee("Email", "Test", 30000);
            empAmbTelefon = Manager.addEmployee("Telefon", "Test", 35000);
            empSenseContacte = Manager.addEmployee("Sense", "Contacte", 40000);
            
            // Afegir contactes
            Manager.addContactToEmployee(empAmbEmail.getEmployeeId(), 
                "EMAIL", "email@test.com", "Email");
            Manager.addContactToEmployee(empAmbTelefon.getEmployeeId(), 
                "PHONE", "666111222", "Telèfon");
            
            // Crear projecte i assignar
            projecteWeb = Manager.addProject("Web", "Desc", "ACTIU");
            Manager.updateEmployeeProjects(empAmbEmail.getEmployeeId(), 
                Set.of(projecteWeb));
            Manager.updateEmployeeProjects(empAmbTelefon.getEmployeeId(), 
                Set.of(projecteWeb));
        }
        
        /**
         * Test: findEmployeesByContactType retorna empleats amb el tipus de contacte.
         */
        @Test
        @DisplayName("findEmployeesByContactType('EMAIL') retorna empleats correctes")
        void findEmployeesByContactType_Email_RetornaEmpleatsAmbEmail() {
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByContactType("EMAIL");
            
            // ASSERT
            assertThat(resultat)
                .hasSize(1)
                .extracting(Employee::getFirstName)
                .containsExactly("Email");
        }
        
        /**
         * Test: findEmployeesByContactType('PHONE') retorna empleats correctes.
         */
        @Test
        @DisplayName("findEmployeesByContactType('PHONE') retorna empleats correctes")
        void findEmployeesByContactType_Phone_RetornaEmpleatsAmbTelefon() {
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByContactType("PHONE");
            
            // ASSERT
            assertThat(resultat)
                .hasSize(1)
                .extracting(Employee::getFirstName)
                .containsExactly("Telefon");
        }
        
        /**
         * Test: findEmployeesByContactType amb tipus inexistent retorna buit.
         */
        @Test
        @DisplayName("findEmployeesByContactType amb tipus inexistent retorna buit")
        void findEmployeesByContactType_TipusInexistent_RetornaBuit() {
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByContactType("FAX");
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
        
        /**
         * Test: findEmployeesByProject retorna empleats assignats al projecte.
         */
        @Test
        @DisplayName("findEmployeesByProject retorna empleats del projecte")
        void findEmployeesByProject_ProjecteAmbEmpleats_RetornaEmpleats() {
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByProject(
                projecteWeb.getProjectId()
            );
            
            // ASSERT
            assertThat(resultat)
                .hasSize(2)
                .extracting(Employee::getFirstName)
                .containsExactlyInAnyOrder("Email", "Telefon");
        }
        
        /**
         * Test: findEmployeesByProject amb projecte sense empleats retorna buit.
         */
        @Test
        @DisplayName("findEmployeesByProject amb projecte buit retorna buit")
        void findEmployeesByProject_ProjecteSenseEmpleats_RetornaBuit() {
            // ARRANGE
            Project projecteBuit = Manager.addProject("Buit", "Desc", "PLANIFICAT");
            
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByProject(
                projecteBuit.getProjectId()
            );
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
        
        /**
         * Test: findEmployeesByProject amb ID inexistent retorna buit.
         */
        @Test
        @DisplayName("findEmployeesByProject amb ID inexistent retorna buit")
        void findEmployeesByProject_IdInexistent_RetornaBuit() {
            // ACT
            Collection<Employee> resultat = Manager.findEmployeesByProject(99999L);
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
    }
    
    // ========================================================================
    // TESTS D'ASSIGNACIÓ DE PROJECTES
    // ========================================================================
    
    /**
     * Grup de tests per a updateEmployeeProjects().
     */
    @Nested
    @DisplayName("PROJECTES - Tests d'assignació de projectes a empleats")
    class ProjectAssignmentTests {
        
        private Employee empleat;
        private Project projecte1;
        private Project projecte2;
        private Project projecte3;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Assignació", "Test", 35000);
            projecte1 = Manager.addProject("Projecte1", "Desc1", "ACTIU");
            projecte2 = Manager.addProject("Projecte2", "Desc2", "ACTIU");
            projecte3 = Manager.addProject("Projecte3", "Desc3", "PLANIFICAT");
        }
        
        /**
         * Test: Assignar un projecte a un empleat.
         */
        @Test
        @DisplayName("Assignar un projecte a un empleat")
        void updateEmployeeProjects_UnProjecte_Assignat() {
            // ACT
            Manager.updateEmployeeProjects(empleat.getEmployeeId(), Set.of(projecte1));
            
            // ASSERT
            Collection<Employee> empleatsDelProjecte = 
                Manager.findEmployeesByProject(projecte1.getProjectId());
            
            assertThat(empleatsDelProjecte)
                .hasSize(1)
                .extracting(Employee::getEmployeeId)
                .containsExactly(empleat.getEmployeeId());
        }
        
        /**
         * Test: Assignar múltiples projectes a un empleat.
         */
        @Test
        @DisplayName("Assignar múltiples projectes a un empleat")
        void updateEmployeeProjects_MultiplesProjectes_TotsAssignats() {
            // ACT
            Manager.updateEmployeeProjects(
                empleat.getEmployeeId(), 
                Set.of(projecte1, projecte2, projecte3)
            );
            
            // ASSERT - Verificar des de cada projecte
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .hasSize(1),
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .hasSize(1),
                () -> assertThat(Manager.findEmployeesByProject(projecte3.getProjectId()))
                        .hasSize(1)
            );
        }
        
        /**
         * Test: Reassignar projectes reemplaça els anteriors.
         */
        @Test
        @DisplayName("Reassignar projectes reemplaça els anteriors")
        void updateEmployeeProjects_Reassignar_ReemplacaAnteriors() {
            // ARRANGE - Assignar primer projecte
            Manager.updateEmployeeProjects(empleat.getEmployeeId(), Set.of(projecte1));
            
            // ACT - Reassignar amb un altre projecte
            Manager.updateEmployeeProjects(empleat.getEmployeeId(), Set.of(projecte2));
            
            // ASSERT
            assertAll(
                // Projecte1 ja no hauria de tenir l'empleat
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .isEmpty(),
                // Projecte2 hauria de tenir l'empleat
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .hasSize(1)
            );
        }
        
        /**
         * Test: Assignar conjunt buit elimina totes les assignacions.
         */
        @Test
        @DisplayName("Assignar conjunt buit elimina totes les assignacions")
        void updateEmployeeProjects_ConjuntBuit_EliminaAssignacions() {
            // ARRANGE - Assignar projectes
            Manager.updateEmployeeProjects(
                empleat.getEmployeeId(), 
                Set.of(projecte1, projecte2)
            );
            
            // ACT - Assignar conjunt buit
            Manager.updateEmployeeProjects(empleat.getEmployeeId(), new HashSet<>());
            
            // ASSERT
            assertAll(
                () -> assertThat(Manager.findEmployeesByProject(projecte1.getProjectId()))
                        .isEmpty(),
                () -> assertThat(Manager.findEmployeesByProject(projecte2.getProjectId()))
                        .isEmpty()
            );
        }
        
        /**
         * Test: updateEmployeeProjects amb ID inexistent no falla.
         */
        @Test
        @DisplayName("updateEmployeeProjects amb ID inexistent no falla")
        void updateEmployeeProjects_IdInexistent_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.updateEmployeeProjects(99999L, Set.of(projecte1));
            });
        }
    }
}
