package com.project.dao;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.Set;

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

import com.project.domain.*;

/**
 * Gestor principal per les operacions amb la base de dades utilitzant Hibernate/JPA.
 * Implementa el patró try-with-resources per una gestió més segura de les sessions.
 */
public class Manager {
    private static final Logger logger = LoggerFactory.getLogger(Manager.class);
    private static SessionFactory factory;

    // ******************************************
    // Mètodes de configuració i inicialització
    // ******************************************

    /**
     * Crea la SessionFactory amb la configuració per defecte.
     */
    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // Registrem les classes amb anotacions JPA
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

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
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            Properties properties = new Properties();
            try (InputStream input = Manager.class.getClassLoader().getResourceAsStream(propertiesFileName)) {
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
     * Tanca la SessionFactory.
     */
    public static void close() {
        if (factory != null) {
            factory.close();
            logger.info("SessionFactory tancada");
        }
    }

    // ******************************************
    // Mètodes relacionats amb Employee
    // ******************************************

    /**
     * Afegeix un nou empleat.
     */
    public static Employee addEmployee(String firstName, String lastName, int salary) {
        Employee result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                result = new Employee(firstName, lastName, salary);
                session.persist(result);
                tx.commit();
                logger.info("Empleat creat amb ID: {}", result.getEmployeeId());
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error creant empleat", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza un empleat existent.
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
                    session.merge(emp);
                    logger.info("Empleat actualitzat: {}", employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error actualitzant empleat: {}", employeeId, e);
                throw e;
            }
        }
    }

    /**
     * Cerca empleats per tipus de contacte.
     */
    public static Collection<Employee> findEmployeesByContactType(String contactType) {
        Collection<Employee> result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = "SELECT DISTINCT e FROM Employee e JOIN e.contacts c WHERE c.contactType = :type";
                result = session.createQuery(hql, Employee.class)
                              .setParameter("type", contactType)
                              .list();
                logger.info("Trobats {} empleats amb tipus de contacte: {}", result.size(), contactType);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error cercant empleats per tipus de contacte: {}", contactType, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Troba tots els empleats assignats a un projecte específic.
     */
    public static Collection<Employee> findEmployeesByProject(long projectId) {
        Collection<Employee> result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Project project = session.get(Project.class, projectId);
                if (project != null) {
                    result = project.getEmployees();
                    logger.info("Trobats {} empleats al projecte {}", result.size(), projectId);
                } else {
                    logger.warn("No s'ha trobat el projecte amb ID: {}", projectId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error cercant empleats per projecte: {}", projectId, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza els projectes d'un empleat.
     */
    public static void updateEmployeeProjects(long employeeId, Set<Project> projects) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                if (emp != null) {
                    emp.getProjects().clear();
                    for (Project project : projects) {
                        Project managedProject = session.get(Project.class, project.getProjectId());
                        emp.addProject(managedProject);
                    }
                    session.merge(emp);
                    logger.info("Projectes actualitzats per l'empleat: {}", employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error actualitzant projectes de l'empleat: {}", employeeId, e);
                throw e;
            }
        }
    }

    // ******************************************
    // Mètodes relacionats amb Contact
    // ******************************************

    /**
     * Afegeix un contacte a un empleat existent.
     */
    public static Contact addContactToEmployee(long employeeId, String contactType, String value, String description) {
        Contact result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                if (emp != null) {
                    Contact contact = new Contact(contactType, value, description);
                    emp.addContact(contact);
                    session.persist(contact);
                    session.merge(emp);
                    result = contact;
                    logger.info("Contacte afegit a l'empleat {}: {}", employeeId, contact.getContactId());
                } else {
                    logger.warn("No s'ha trobat l'empleat amb ID: {}", employeeId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error afegint contacte a l'empleat: {}", employeeId, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Cerca contactes per empleat i tipus.
     */
    public static Collection<Contact> findContactsByEmployeeAndType(long employeeId, String contactType) {
        Collection<Contact> result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = "FROM Contact c WHERE c.employee.id = :empId AND c.contactType = :type";
                result = session.createQuery(hql, Contact.class)
                              .setParameter("empId", employeeId)
                              .setParameter("type", contactType)
                              .list();
                logger.info("Trobats {} contactes per l'empleat {} de tipus {}", 
                          result.size(), employeeId, contactType);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error cercant contactes per empleat {} i tipus {}", 
                           employeeId, contactType, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Elimina un contacte d'un empleat.
     */
    public static void removeContactFromEmployee(long employeeId, long contactId) {
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Employee emp = session.get(Employee.class, employeeId);
                Contact contact = session.get(Contact.class, contactId);
                
                if (emp != null && contact != null) {
                    emp.removeContact(contact);
                    session.remove(contact);
                    session.merge(emp);
                    logger.info("Contacte {} eliminat de l'empleat {}", contactId, employeeId);
                } else {
                    logger.warn("No s'ha trobat l'empleat {} o el contacte {}", employeeId, contactId);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error eliminant contacte {} de l'empleat {}", contactId, employeeId, e);
                throw e;
            }
        }
    }

    /**
     * Actualitza la informació d'un contacte.
     */
    public static void updateContact(long contactId, String contactType, String value, String description) {
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
                if (tx != null) tx.rollback();
                logger.error("Error actualitzant contacte: {}", contactId, e);
                throw e;
            }
        }
    }

    // ******************************************
    // Mètodes relacionats amb Project
    // ******************************************

    /**
     * Afegeix un nou projecte.
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
                if (tx != null) tx.rollback();
                logger.error("Error creant projecte", e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Actualitza un projecte existent.
     */
    public static void updateProject(long projectId, String name, String description, String status) {
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
                if (tx != null) tx.rollback();
                logger.error("Error actualitzant projecte: {}", projectId, e);
                throw e;
            }
        }
    }

    // ******************************************
    // Mètodes generals de consulta i utilitat
    // ******************************************

    /**
     * Obté un objecte per ID.
     */
    public static <T> T getById(Class<? extends T> clazz, long id) {
        T obj = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                obj = session.get(clazz, id);
                if (obj != null) {
                    logger.info("Obtingut objecte de tipus {} amb ID: {}", clazz.getSimpleName(), id);
                } else {
                    logger.warn("No s'ha trobat objecte de tipus {} amb ID: {}", clazz.getSimpleName(), id);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error obtenint objecte de tipus {} amb ID: {}", clazz.getSimpleName(), id, e);
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
                    logger.info("Eliminat objecte de tipus {} amb ID: {}", clazz.getSimpleName(), id);
                } else {
                    logger.warn("No s'ha trobat objecte de tipus {} amb ID: {} per eliminar", 
                              clazz.getSimpleName(), id);
                }
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error eliminant objecte de tipus {} amb ID: {}", 
                           clazz.getSimpleName(), id, e);
                throw e;
            }
        }
    }

    /**
     * Llista entitats amb filtre opcional.
     */
    public static <T> Collection<?> listCollection(Class<? extends T> clazz, String where) {
        Collection<?> result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                String hql = where.isEmpty() 
                    ? "FROM " + clazz.getName()
                    : "FROM " + clazz.getName() + " WHERE " + where;
                result = session.createQuery(hql, clazz).list();
                logger.info("Obtinguts {} objectes de tipus {} amb filtre: {}", 
                          result.size(), clazz.getSimpleName(), where);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error llistant objectes de tipus {} amb filtre: {}", 
                           clazz.getSimpleName(), where, e);
                throw e;
            }
        }
        return result;
    }

    public static <T> Collection<?> listCollection(Class<? extends T> clazz) {
        return listCollection(clazz, "");
    }

    /**
     * Executa una consulta SQL nativa d'actualització.
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
                if (tx != null) tx.rollback();
                logger.error("Error executant consulta d'actualització: {}", queryString, e);
                throw e;
            }
        }
    }

    /**
     * Executa una consulta SQL nativa de selecció.
     */
    public static List<Object[]> queryTable(String queryString) {
        List<Object[]> result = null;
        try (Session session = factory.openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                NativeQuery<Object[]> query = session.createNativeQuery(queryString, Object[].class);
                result = query.getResultList();
                logger.info("Executada consulta de selecció amb {} resultats: {}", 
                          result.size(), queryString);
                tx.commit();
            } catch (HibernateException e) {
                if (tx != null) tx.rollback();
                logger.error("Error executant consulta de selecció: {}", queryString, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Converteix els resultats d'una consulta SQL a String.
     */
    public static String tableToString(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            logger.debug("No hi ha resultats per convertir a String");
            return "";
        }
        
        StringBuilder txt = new StringBuilder();
        for (Object[] row : rows) {
            for (Object cell : row) {
                txt.append(cell != null ? cell.toString() : "null").append(", ");
            }
            if (txt.length() >= 2) {
                txt.setLength(txt.length() - 2);  // Eliminem l'última coma i espai
            }
            txt.append("\n");
        }
        if (txt.length() >= 1) {
            txt.setLength(txt.length() - 1);  // Eliminem l'últim salt de línia
        }
        return txt.toString();
    }

    /**
     * Converteix una col·lecció d'entitats a String.
     */
    public static <T> String collectionToString(Class<? extends T> clazz, Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            logger.debug("No hi ha elements a la col·lecció per convertir a String");
            return "";
        }
        
        StringBuilder txt = new StringBuilder();
        for (Object obj : collection) {
            T cObj = clazz.cast(obj);
            txt.append("\n").append(cObj.toString());
        }
        if (txt.length() > 0) {
            txt.delete(0, 1);  // Eliminem el primer salt de línia
        }
        return txt.toString();
    }
}