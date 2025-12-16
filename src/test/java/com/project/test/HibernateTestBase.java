package com.project.test;

import com.project.dao.Manager;
import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.domain.Project;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * CLASSE BASE PER A TOTS ELS TESTS D'HIBERNATE
 * ============================================
 * 
 * Aquesta classe abstracta proporciona la infraestructura comuna per a tots
 * els tests que necessiten accés a la base de dades a través d'Hibernate.
 * 
 * RESPONSABILITATS:
 * - Inicialitzar la SessionFactory abans de tots els tests
 * - Netejar la base de dades abans de cada test individual
 * - Tancar la SessionFactory després de tots els tests
 * - Proporcionar mètodes d'utilitat per crear dades de prova
 * 
 * PATRÓ UTILITZAT: Template Method
 * Les subclasses hereten el comportament de setUp/tearDown i poden
 * sobreescriure mètodes específics si cal.
 * 
 * CICLE DE VIDA DELS TESTS:
 * 1. @BeforeAll: initHibernate() - Crea SessionFactory (una vegada)
 * 2. @BeforeEach: cleanDatabase() - Neteja totes les taules (per cada test)
 * 3. Execució del test
 * 4. @AfterEach: (opcional, no implementat per defecte)
 * 5. @AfterAll: closeHibernate() - Tanca SessionFactory (una vegada)
 * 
 * @author Test Suite Generator
 * @version 1.0
 */

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class HibernateTestBase {
    
    // ========================================================================
    // CONSTANTS
    // ========================================================================
    
    /**
     * Nom del fitxer de propietats per a la configuració de test.
     * Aquest fitxer ha d'estar a src/test/resources/
     */
    protected static final String TEST_PROPERTIES_FILE = "hibernate-test.properties";
    
    // ========================================================================
    // CONFIGURACIÓ DEL CICLE DE VIDA
    // ========================================================================
    
    /**
     * Inicialitza Hibernate abans d'executar cap test.
     * 
     * S'executa UNA SOLA VEGADA abans de tots els tests de la classe.
     * Crear la SessionFactory és una operació costosa, per això només
     * es fa una vegada i es reutilitza per a tots els tests.
     * 
     * IMPORTANT: Si falla aquesta inicialització, cap test s'executarà.
     */
    @BeforeAll
    static void initHibernate() {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  INICIALITZANT HIBERNATE AMB CONFIGURACIÓ DE TEST (H2)");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        try {
            Manager.createSessionFactory(TEST_PROPERTIES_FILE);
            System.out.println("✓ SessionFactory creada correctament");
        } catch (Exception e) {
            System.err.println("✗ ERROR creant SessionFactory: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Tanca Hibernate després d'executar tots els tests.
     * 
     * S'executa UNA SOLA VEGADA després de tots els tests de la classe.
     * Allibera tots els recursos (connexions, caches, etc.).
     * 
     * IMPORTANT: Si no es tanca, poden quedar recursos sense alliberar.
     */
    @AfterAll
    static void closeHibernate() {
        System.out.println("═══════════════════════════════════════════════════════════");
        System.out.println("  TANCANT HIBERNATE");
        System.out.println("═══════════════════════════════════════════════════════════");
        
        Manager.close();
        System.out.println("✓ SessionFactory tancada correctament");
    }
    
    /**
     * Neteja la base de dades abans de cada test.
     * 
     * S'executa ABANS DE CADA TEST individual.
     * Garanteix que cada test comença amb una base de dades buida,
     * proporcionant aïllament total entre tests.
     * 
     * ORDRE D'ELIMINACIÓ:
     * És CRÍTIC eliminar les taules en l'ordre correcte per respectar
     * les restriccions de clau forana (FK):
     * 1. employee_project (taula pont - sense FK cap a ella)
     * 2. contacts (FK a employees)
     * 3. projects (independent)
     * 4. employees (independent)
     * 
     * ALTERNATIVA: Es podria utilitzar TRUNCATE amb CASCADE, però no
     * totes les BD ho suporten igual.
     */
    @BeforeEach
    void cleanDatabase() {
        System.out.println("───────────────────────────────────────────────────────────");
        System.out.println("  Netejant base de dades per al proper test...");
        System.out.println("───────────────────────────────────────────────────────────");
        
        // Utilitzem SQL natiu per màxima eficiència en la neteja
        // L'ordre és important per respectar les FK
        try {
            // 1. Primer eliminem la taula pont (ManyToMany)
            Manager.queryUpdate("DELETE FROM employee_project");
            
            // 2. Després les taules amb FK
            Manager.queryUpdate("DELETE FROM contacts");
            
            // 3. Finalment les taules principals
            Manager.queryUpdate("DELETE FROM projects");
            Manager.queryUpdate("DELETE FROM employees");
            
            System.out.println("✓ Base de dades netejada");
        } catch (Exception e) {
            System.err.println("⚠ Avís durant la neteja: " + e.getMessage());
            // No llancem l'excepció perquè en tests inicials les taules
            // podrien no existir encara
        }
    }
    
    // ========================================================================
    // MÈTODES D'UTILITAT PER CREAR DADES DE PROVA
    // ========================================================================
    
    /**
     * Crea un empleat de prova amb dades per defecte.
     * 
     * Útil per tests que necessiten un empleat però no els importa
     * les dades específiques.
     * 
     * @return Employee persistit amb ID assignat
     */
    protected Employee crearEmpleatProva() {
        return Manager.addEmployee("Test", "Employee", 30000);
    }
    
    /**
     * Crea un empleat de prova amb nom personalitzat.
     * 
     * Útil per tests que necessiten identificar empleats específics
     * pel seu nom.
     * 
     * @param nom Nom de l'empleat
     * @param cognom Cognom de l'empleat
     * @return Employee persistit amb ID assignat
     */
    protected Employee crearEmpleatProva(String nom, String cognom) {
        return Manager.addEmployee(nom, cognom, 35000);
    }
    
    /**
     * Crea un empleat de prova amb tots els paràmetres.
     * 
     * @param nom Nom de l'empleat
     * @param cognom Cognom de l'empleat
     * @param salari Salari de l'empleat
     * @return Employee persistit amb ID assignat
     */
    protected Employee crearEmpleatProva(String nom, String cognom, int salari) {
        return Manager.addEmployee(nom, cognom, salari);
    }
    
    /**
     * Crea un projecte de prova amb dades per defecte.
     * 
     * @return Project persistit amb ID assignat
     */
    protected Project crearProjecteProva() {
        return Manager.addProject("Projecte Test", "Descripció de prova", "ACTIU");
    }
    
    /**
     * Crea un projecte de prova amb nom personalitzat.
     * 
     * @param nom Nom del projecte
     * @return Project persistit amb ID assignat
     */
    protected Project crearProjecteProva(String nom) {
        return Manager.addProject(nom, "Descripció de " + nom, "ACTIU");
    }
    
    /**
     * Crea un projecte de prova amb tots els paràmetres.
     * 
     * @param nom Nom del projecte
     * @param descripcio Descripció del projecte
     * @param estat Estat del projecte (ACTIU, COMPLETAT, PLANIFICAT)
     * @return Project persistit amb ID assignat
     */
    protected Project crearProjecteProva(String nom, String descripcio, String estat) {
        return Manager.addProject(nom, descripcio, estat);
    }
    
    /**
     * Crea un contacte de prova associat a un empleat.
     * 
     * @param employeeId ID de l'empleat propietari del contacte
     * @return Contact persistit amb ID assignat
     */
    protected Contact crearContacteProva(Long employeeId) {
        return Manager.addContactToEmployee(
            employeeId, 
            "EMAIL", 
            "test@example.com", 
            "Contacte de prova"
        );
    }
    
    /**
     * Crea un contacte de prova amb tipus específic.
     * 
     * @param employeeId ID de l'empleat propietari
     * @param tipus Tipus de contacte (EMAIL, PHONE, ADDRESS)
     * @param valor Valor del contacte
     * @return Contact persistit amb ID assignat
     */
    protected Contact crearContacteProva(Long employeeId, String tipus, String valor) {
        return Manager.addContactToEmployee(
            employeeId, 
            tipus, 
            valor, 
            "Contacte " + tipus
        );
    }
    
    /**
     * Crea un conjunt d'empleats de prova.
     * 
     * Útil per tests que necessiten múltiples empleats.
     * 
     * @param quantitat Nombre d'empleats a crear
     * @return Set amb els empleats creats
     */
    protected Set<Employee> crearMultiplesEmpleats(int quantitat) {
        Set<Employee> empleats = new HashSet<>();
        for (int i = 1; i <= quantitat; i++) {
            empleats.add(Manager.addEmployee(
                "Empleat" + i, 
                "Cognom" + i, 
                25000 + (i * 1000)
            ));
        }
        return empleats;
    }
    
    /**
     * Crea un conjunt de projectes de prova.
     * 
     * @param quantitat Nombre de projectes a crear
     * @return Set amb els projectes creats
     */
    protected Set<Project> crearMultiplesProjectes(int quantitat) {
        Set<Project> projectes = new HashSet<>();
        String[] estats = {"ACTIU", "PLANIFICAT", "COMPLETAT"};
        for (int i = 1; i <= quantitat; i++) {
            projectes.add(Manager.addProject(
                "Projecte" + i, 
                "Descripció del projecte " + i, 
                estats[i % 3]
            ));
        }
        return projectes;
    }
    
    /**
     * Crea un empleat amb contactes i projectes assignats.
     * 
     * Mètode de conveniència per crear un empleat complet amb relacions.
     * 
     * @param nom Nom de l'empleat
     * @param numContactes Nombre de contactes a crear
     * @param projectes Set de projectes a assignar
     * @return Employee amb totes les relacions establertes
     */
    protected Employee crearEmpleatComplet(String nom, int numContactes, Set<Project> projectes) {
        // Crear empleat
        Employee emp = Manager.addEmployee(nom, "Cognom", 40000);
        
        // Afegir contactes
        String[] tipusContacte = {"EMAIL", "PHONE", "ADDRESS"};
        String[] valors = {nom.toLowerCase() + "@test.com", "666000000", "Carrer Test 1"};
        for (int i = 0; i < numContactes && i < 3; i++) {
            Manager.addContactToEmployee(
                emp.getEmployeeId(), 
                tipusContacte[i], 
                valors[i], 
                "Contacte " + (i + 1)
            );
        }
        
        // Assignar projectes
        if (projectes != null && !projectes.isEmpty()) {
            Manager.updateEmployeeProjects(emp.getEmployeeId(), projectes);
        }
        
        return emp;
    }
    
    // ========================================================================
    // MÈTODES D'UTILITAT PER VERIFICACIONS
    // ========================================================================
    
    /**
     * Verifica que una col·lecció conté un element amb l'ID especificat.
     * 
     * @param <T> Tipus de l'entitat
     * @param collection Col·lecció a verificar
     * @param id ID a buscar
     * @param idExtractor Funció per extreure l'ID de l'entitat
     * @return true si es troba l'ID, false altrament
     */
    protected <T> boolean conteId(Collection<T> collection, Long id, 
                                   java.util.function.Function<T, Long> idExtractor) {
        return collection.stream()
                        .map(idExtractor)
                        .anyMatch(itemId -> itemId.equals(id));
    }
    
    /**
     * Compta el nombre d'elements a la base de dades per una entitat.
     * 
     * @param <T> Tipus de l'entitat
     * @param clazz Classe de l'entitat
     * @return Nombre d'elements
     */
    protected <T> int comptarEntitats(Class<T> clazz) {
        Collection<T> elements = Manager.listCollection(clazz);
        return elements != null ? elements.size() : 0;
    }
}
