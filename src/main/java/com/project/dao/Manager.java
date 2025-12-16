package com.project.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;


import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.NativeQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToMany;


import com.project.domain.*;


/**
 * Gestor DAO (Data Access Object) per operacions amb Hibernate/JPA.
 * 
 * PATRONS IMPLEMENTATS:
 * - DAO Pattern: Separa la lògica d'accés a dades de la lògica de negoci
 * - Session-per-request: Cada operació obre/tanca la seva sessió
 * - Try-with-resources: Gestió automàtica de recursos (sessions)
 * 
 * CONCEPTES CLAU HIBERNATE:
 * - SessionFactory: Fàbrica de sessions, és cara de crear, una per aplicació
 * - Session: Unitat de treball, curta durada, una per operació/request
 * - Transaction: Agrupa operacions atòmiques (tot o res)
 */
public class Manager {
    
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    
    /**
     * SessionFactory - Thread-safe i compartida per tota l'aplicació.
     * Crear-la és costós, per això només en tenim una (Singleton implícit).
     */
    private static SessionFactory factory;

    // ================================================================
    // MÈTODES DE CONFIGURACIÓ I INICIALITZACIÓ
    // ================================================================

    /**
     * Crea la SessionFactory llegint hibernate.properties del classpath.
     * 
     * IMPORTANT: Les classes anotades s'han de registrar explícitament
     * amb addAnnotatedClass() quan NO utilitzem persistence.xml
     */
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // Registrar totes les entitats JPA
            // IMPORTANT: Si afegeixes una nova entitat, cal registrar-la aquí!
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            // El ServiceRegistry gestiona els serveis d'Hibernate (connexions, etc.)
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
            logger.info("SessionFactory creada amb èxit");
        } catch (Throwable ex) {
            logger.error("No s'ha pogut crear la SessionFactory", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Crea la SessionFactory amb un fitxer de propietats específic.
     * Útil per tenir diferents configuracions (dev, test, prod).
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            // Carregar propietats des del classpath
            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader()
                    .getResourceAsStream(propertiesFileName)) {
                if (input == null) {
                    throw new IOException("No s'ha trobat " + propertiesFileName);
                }
                properties.load(input);
            }

            configuration.addProperties(properties);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
            logger.info("SessionFactory creada amb èxit utilitzant {}", propertiesFileName);
        } catch (Throwable ex) {
            logger.error("Error creant la SessionFactory amb {}", propertiesFileName, ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Tanca la SessionFactory i allibera recursos.
     * IMPORTANT: Cridar sempre al final de l'aplicació!
     */
    public static void close() {
        if (factory != null && !factory.isClosed()) {
            factory.close();
            logger.info("SessionFactory tancada");
        }
    }

    // ================================================================
    // MÈTODES CRUD PER EMPLOYEE
    // ================================================================

    /**
     * Crea un nou empleat a la base de dades.
     * 
     * FLUX:
     * 1. Obrir sessió (try-with-resources la tanca automàticament)
     * 2. Iniciar transacció
     * 3. Crear objecte i persistir
     * 4. Commit si tot OK, rollback si hi ha error
     * 
     * @return L'empleat amb l'ID assignat per la BD
     */
    public static Employee addEmployee(String firstName, String lastName, int salary) {
        Employee result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                result = new Employee(firstName, lastName, salary);
                session.persist(result);  // PERSIST: L'objecte passa a estat "managed"
                tx.commit();
                logger.info("Empleat creat amb ID: {}", result.getEmployeeId());
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error creant empleat", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza les dades d'un empleat existent.
     * 
     * IMPORTANT: session.get() retorna l'objecte en estat "managed"
     * Els canvis es sincronitzen automàticament amb la BD al fer commit.
     * El merge() és opcional en aquest cas, però explícit és més clar.
     */
    public static void updateEmployee(long employeeId, String firstName, String lastName, int salary) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                if (emp != null) {
                    emp.setFirstName(firstName);
                    emp.setLastName(lastName);
                    emp.setSalary(salary);
                    // No cal merge() explícit perquè emp ja és "managed"
                    // Però ho deixem per claredat
                    session.merge(emp);
                    logger.info("Empleat actualitzat: {}", employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error actualitzant empleat: {}", employeeId, e);
                throw e;
            }
        }
    }

    /**
     * Cerca empleats que tinguin un tipus de contacte específic.
     * 
     * HQL (Hibernate Query Language):
     * - Similar a SQL però treballa amb entitats i atributs, no taules i columnes
     * - JOIN automàtic gràcies a les relacions definides a les entitats
     * - DISTINCT evita duplicats quan un empleat té múltiples contactes del mateix tipus
     * 
     * NOTA: No cal Transaction per a consultes SELECT, però és bona pràctica
     * tenir-la per consistència i per si la consulta modifica cache.
     */
    public static Collection<Employee> findEmployeesByContactType(String contactType) {
        Collection<Employee> result = Collections.emptyList();
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = "SELECT DISTINCT e FROM Employee e " +
                            "JOIN e.contacts c " +
                            "WHERE c.contactType = :type";
                result = session.createQuery(hql, Employee.class)
                            .setParameter("type", contactType)
                            .list();
                
                // AFEGIT: Inicialitzar col·leccions LAZY abans de tancar la sessió
                for (Employee emp : result) {
                    initializeLazyCollections(emp);
                }
                
                logger.info("Trobats {} empleats amb tipus de contacte: {}", 
                        result.size(), contactType);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error cercant empleats per tipus de contacte: {}", contactType, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Troba tots els empleats assignats a un projecte.
     * 
     * PROBLEMA POTENCIAL AMB LAZY LOADING:
     * Si Project.employees és LAZY, accedir a getEmployees() fora de la sessió
     * causaria LazyInitializationException.
     * 
     * SOLUCIÓ: Inicialitzar la col·lecció dins la sessió o usar FETCH JOIN.
     */
    public static Collection<Employee> findEmployeesByProject(long projectId) {
        Collection<Employee> result = Collections.emptyList();
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = "SELECT DISTINCT p FROM Project p " +
                            "LEFT JOIN FETCH p.employees " +
                            "WHERE p.projectId = :id";
                Project project = session.createQuery(hql, Project.class)
                                        .setParameter("id", projectId)
                                        .uniqueResult();
                
                if (project != null) {
                    result = project.getEmployees();
                    
                    // AFEGIT: Inicialitzar col·leccions LAZY dels empleats
                    for (Employee emp : result) {
                        initializeLazyCollections(emp);
                    }
                    
                    logger.info("Trobats {} empleats al projecte {}", result.size(), projectId);
                } else {
                    logger.warn("No s'ha trobat el projecte amb ID: {}", projectId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error cercant empleats per projecte: {}", projectId, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza els projectes assignats a un empleat.
     * 
     * IMPORTANT per ManyToMany:
     * - Cal obtenir referències "managed" dels projectes (session.get)
     * - Utilitzar els mètodes helper (addProject) per mantenir consistència bidireccional
     */
    public static void updateEmployeeProjects(long employeeId, Set<Project> newProjects) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                if (emp != null) {
                    // Primer, eliminar totes les assignacions actuals
                    // Fem còpia per evitar ConcurrentModificationException
                    for (Project oldProject : Set.copyOf(emp.getProjects())) {
                        emp.removeProject(oldProject);
                    }
                    
                    // Afegir els nous projectes
                    for (Project project : newProjects) {
                        // CRÍTIC: Obtenir el projecte "managed" de la sessió
                        Project managedProject = session.get(Project.class, project.getProjectId());
                        if (managedProject != null) {
                            emp.addProject(managedProject);
                        }
                    }
                    session.merge(emp);
                    logger.info("Projectes actualitzats per l'empleat: {}", employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error actualitzant projectes de l'empleat: {}", employeeId, e);
                throw e;
            }
        }
    }

    // ================================================================
    // MÈTODES CRUD PER CONTACT
    // ================================================================

    /**
     * Afegeix un contacte a un empleat existent.
     * 
     * NOTA: Com Employee té cascade=ALL amb Contact,
     * podríem només fer emp.addContact() i session.merge(emp)
     * sense session.persist(contact) explícit.
     * Ho deixem per claredat.
     */
    public static Contact addContactToEmployee(long employeeId, String contactType, 
                                                String value, String description) {
        Contact result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                if (emp != null) {
                    Contact contact = new Contact(contactType, value, description);
                    emp.addContact(contact);
                    session.persist(contact);  // AFEGIR AQUESTA LÍNIA - persistir explícitament
                    session.merge(emp);
                    result = contact;  // Ara contact té l'ID assignat
                    logger.info("Contacte afegit a l'empleat {}: {}", 
                            employeeId, contact.getContactId());
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error afegint contacte a l'empleat: {}", employeeId, e);
                throw e;
            }
        }
        return result;
    }
    
    /**
     * Cerca contactes d'un empleat filtrats per tipus.
     */
    public static Collection<Contact> findContactsByEmployeeAndType(long employeeId, 
                                                                     String contactType) {
        Collection<Contact> result = Collections.emptyList();
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = "FROM Contact c " +
                            "WHERE c.employee.employeeId = :empId " +
                            "AND c.contactType = :type";
                result = session.createQuery(hql, Contact.class)
                              .setParameter("empId", employeeId)
                              .setParameter("type", contactType)
                              .list();
                logger.info("Trobats {} contactes per l'empleat {} de tipus {}", 
                          result.size(), employeeId, contactType);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error cercant contactes per empleat {} i tipus {}", 
                           employeeId, contactType, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Elimina un contacte d'un empleat.
     * Gràcies a orphanRemoval=true, el contact s'elimina de la BD.
     */
    public static void removeContactFromEmployee(long employeeId, long contactId) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                Contact contact = session.get(Contact.class, contactId);
                
                if (emp != null && contact != null) {
                    emp.removeContact(contact);  // orphanRemoval s'encarrega d'eliminar-lo
                    session.merge(emp);
                    logger.info("Contacte {} eliminat de l'empleat {}", contactId, employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat {} o el contacte {}", 
                               employeeId, contactId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error eliminant contacte {} de l'empleat {}", 
                            contactId, employeeId, e);
                throw e;
            }
        }
    }

    /**
     * Actualitza la informació d'un contacte.
     */
    public static void updateContact(long contactId, String contactType, 
                                     String value, String description) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Contact contact = session.get(Contact.class, contactId);
                if (contact != null) {
                    contact.setContactType(contactType);
                    contact.setValue(value);
                    contact.setDescription(description);
                    session.merge(contact);
                    logger.info("Contacte actualitzat: {}", contactId);
                } else {
                    logger.warn("No s'ha trobat el contacte amb ID: {}", contactId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error actualitzant contacte: {}", contactId, e);
                throw e;
            }
        }
    }

    // ================================================================
    // MÈTODES CRUD PER PROJECT
    // ================================================================

    /**
     * Crea un nou projecte.
     */
    public static Project addProject(String name, String description, String status) {
        Project result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                result = new Project(name, description, status);
                session.persist(result);
                logger.info("Projecte creat amb ID: {}", result.getProjectId());
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error creant projecte", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza un projecte existent.
     */
    public static void updateProject(long projectId, String name, 
                                     String description, String status) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Project project = session.get(Project.class, projectId);
                if (project != null) {
                    project.setName(name);
                    project.setDescription(description);
                    project.setStatus(status);
                    session.merge(project);
                    logger.info("Projecte actualitzat: {}", projectId);
                } else {
                    logger.warn("No s'ha trobat el projecte amb ID: {}", projectId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error actualitzant projecte: {}", projectId, e);
                throw e;
            }
        }
    }

    // ================================================================
    // MÈTODES GENÈRICS (CRUD per qualsevol entitat)
    // ================================================================

    /**
     * Obté una entitat per ID.
     * 
     * PROBLEMA POTENCIAL: Si l'entitat té col·leccions LAZY,
     * no es podran accedir fora d'aquesta sessió.
     * 
     * SOLUCIÓ: Inicialitzar les col·leccions necessàries dins la sessió
     * o retornar un DTO (Data Transfer Object).
     */
    public static <T> T getById(Class<? extends T> clazz, long id) {
        T obj = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                obj = session.get(clazz, id);
                
                // IMPORTANT: Si volem accedir a col·leccions LAZY fora de la sessió,
                // cal inicialitzar-les aquí. Exemple:
                // if (obj instanceof Employee) {
                //     Hibernate.initialize(((Employee) obj).getContacts());
                //     Hibernate.initialize(((Employee) obj).getProjects());
                // }
                
                if (obj != null) {
                    logger.info("Obtingut {} amb ID: {}", clazz.getSimpleName(), id);
                } else {
                    logger.warn("No s'ha trobat {} amb ID: {}", clazz.getSimpleName(), id);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error obtenint {} amb ID: {}", clazz.getSimpleName(), id, e);
                throw e;
            }
        }
        return obj;
    }

    /**
     * Elimina una entitat per ID.
     */
    public static <T> void delete(Class<? extends T> clazz, Serializable id) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                T obj = session.get(clazz, id);
                if (obj != null) {
                    session.remove(obj);
                    logger.info("Eliminat {} amb ID: {}", clazz.getSimpleName(), id);
                } else {
                    logger.warn("No s'ha trobat {} amb ID: {} per eliminar", 
                              clazz.getSimpleName(), id);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error eliminant {} amb ID: {}", clazz.getSimpleName(), id, e);
                throw e;
            }
        }
    }

    /**
     * Retorna tots els objectes d'una entitat amb les col·leccions LAZY inicialitzades.
     */
    public static <T> Collection<T> listCollection(Class<T> clazz) {
        try (Session session = factory.openSession()) {
            List<T> results = session.createQuery("FROM " + clazz.getSimpleName(), clazz).list();
            
            // Inicialitzar totes les col·leccions LAZY de cada entitat
            for (T entity : results) {
                initializeLazyCollections(entity);
            }
            
            return results;
        }
    }

    /**
     * Inicialitza totes les col·leccions LAZY d'una entitat usant reflexió.
     * Detecta camps anotats amb @OneToMany o @ManyToMany.
     */
    private static void initializeLazyCollections(Object entity) {
        if (entity == null) return;
        
        Class<?> clazz = entity.getClass();
        
        for (Field field : clazz.getDeclaredFields()) {
            // Només processem relacions que poden ser col·leccions LAZY
            if (field.isAnnotationPresent(OneToMany.class) || 
                field.isAnnotationPresent(ManyToMany.class)) {
                
                field.setAccessible(true);  // Permetre accés a camps privats
                try {
                    Object value = field.get(entity);
                    if (value != null) {
                        Hibernate.initialize(value);  // Inicialitzar la col·lecció
                    }
                } catch (IllegalAccessException e) {
                    // Log error si cal, però continuar
                    logger.warn("No s'ha pogut inicialitzar el camp: {}", field.getName());
                }
            }
        }
    }
    
    // ================================================================
    // MÈTODES PER QUERIES NATIVES SQL
    // ================================================================

    /**
     * Executa una consulta SQL nativa d'actualització (INSERT, UPDATE, DELETE).
     * 
     * QUAN USAR SQL NATIU:
     * - Operacions massives (UPDATE on mil registres)
     * - Funcions específiques de la BD
     * - Optimitzacions necessàries
     * 
     * INCONVENIENTS:
     * - Perd la portabilitat entre BDs
     * - Perd els avantatges del cache d'Hibernate
     */
    public static void queryUpdate(String queryString) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.createNativeQuery(queryString, Void.class)
                       .executeUpdate();
                logger.info("Executada consulta d'actualització: {}", queryString);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error executant consulta d'actualització: {}", queryString, e);
                throw e;
            }
        }
    }

    /**
     * Executa una consulta SQL nativa de selecció.
     * Retorna una llista d'arrays d'objectes (un array per fila).
     */
    public static List<Object[]> queryTable(String queryString) {
        List<Object[]> result = Collections.emptyList();
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                NativeQuery<Object[]> query = session.createNativeQuery(queryString, Object[].class);
                result = query.getResultList();
                logger.info("Executada consulta de selecció amb {} resultats: {}", 
                          result.size(), queryString);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null && tx.isActive()) tx.rollback();
                logger.error("Error executant consulta de selecció: {}", queryString, e);
                throw e;
            }
        }
        return result;
    }

    // ================================================================
    // MÈTODES D'UTILITAT PER FORMATEJAR OUTPUT
    // ================================================================

    /**
     * Converteix resultats d'una query nativa a String.
     */
    public static String tableToString(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        
        StringBuilder txt = new StringBuilder();
        for (Object[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                if (i > 0) txt.append(", ");
                txt.append(row[i] != null ? row[i].toString() : "null");
            }
            txt.append("\n");
        }
        // Eliminar últim salt de línia
        if (txt.length() > 0) {
            txt.setLength(txt.length() - 1);
        }
        return txt.toString();
    }

    /**
     * Converteix una col·lecció d'entitats a String.
     */
    public static <T> String collectionToString(Class<? extends T> clazz, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        
        StringBuilder txt = new StringBuilder();
        for (Object obj : collection) {
            if (txt.length() > 0) txt.append("\n");
            txt.append(obj.toString());
        }
        return txt.toString();
    }
}