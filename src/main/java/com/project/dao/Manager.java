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

import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.domain.Project;

/**
 * Gestor principal per les operacions amb la base de dades utilitzant Hibernate/JPA.
 * Proporciona mètodes per gestionar empleats, contactes i projectes.
 */
public class Manager {
    // SessionFactory és thread-safe i s'ha de crear una sola vegada per l'aplicació
    private static SessionFactory factory;

    /**
     * Crea la SessionFactory amb la configuració per defecte.
     * La SessionFactory és un objecte pesat que s'hauria de crear una sola vegada.
     * Utilitza el fitxer hibernate.properties per la configuració.
     */
    public static void createSessionFactory() {
        try {
            // Creem una configuració de Hibernate
            Configuration configuration = new Configuration();
            
            // Registrem les classes que tenen anotacions JPA
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            // Creem el registre de serveis i la SessionFactory
            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("No s'ha pogut crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Crea la SessionFactory amb un fitxer de propietats específic.
     * Útil per tenir diferents configuracions (desenvolupament, test, producció).
     */
    public static void createSessionFactory(String propertiesFileName) {
        try {
            Configuration configuration = new Configuration();
            
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            // Carreguem les propietats des del fitxer
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
        } catch (Throwable ex) {
            System.err.println("Error creant la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    /**
     * Tanca la SessionFactory i allibera els recursos.
     * Important cridar aquest mètode al finalitzar l'aplicació.
     */
    public static void close() {
        factory.close();
    }

    // ---------- Mètodes per gestionar Empleats ----------

    /**
     * Afegeix un nou empleat a la base de dades.
     * Hibernate s'encarrega de generar l'ID automàticament.
     */
    public static Employee addEmployee(String firstName, String lastName, int salary) {
        // Session NO és thread-safe, cada operació necessita la seva pròpia sessió
        Session session = factory.openSession();
        Transaction tx = null;
        Employee result = null;
        try {
            // Iniciem la transacció
            tx = session.beginTransaction();
            
            // Creem i persistim el nou empleat
            result = new Employee(firstName, lastName, salary);
            session.persist(result);
            
            // Confirmem la transacció
            tx.commit();
        } catch (HibernateException e) {
            // En cas d'error, fem rollback de la transacció
            if (tx != null) tx.rollback();
            e.printStackTrace();
            result = null;
        } finally {
            // Sempre tanquem la sessió
            session.close();
        }
        return result;
    }

    /**
     * Afegeix una nova dada de contacte a un empleat existent.
     * Utilitza la relació OneToMany entre Employee i Contact.
     */
    public static Contact addContactToEmployee(long employeeId, String contactType, String value, String description) {
        Session session = factory.openSession();
        Transaction tx = null;
        Contact result = null;
        try {
            tx = session.beginTransaction();
            
            // Obtenim l'empleat
            Employee emp = session.get(Employee.class, employeeId);
            if (emp != null) {
                // Creem i associem el nou contacte
                Contact contact = new Contact(contactType, value, description);
                emp.addContact(contact);  // Aquest mètode gestiona la relació bidireccional
                session.persist(contact);
                session.merge(emp);
                result = contact;
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    /**
     * Actualitza la informació bàsica d'un empleat.
     * Utilitza session.merge() per actualitzar l'entitat gestionada.
     */
    public static void updateEmployee(long employeeId, String firstName, String lastName, int salary) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Employee emp = session.get(Employee.class, employeeId);
            if (emp != null) {
                emp.setFirstName(firstName);
                emp.setLastName(lastName);
                emp.setSalary(salary);
                session.merge(emp);
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Actualitza els projectes assignats a un empleat.
     * Gestiona la relació ManyToMany entre Employee i Project.
     */
    public static void updateEmployeeProjects(long employeeId, Set<Project> projects) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Employee emp = session.get(Employee.class, employeeId);
            // Netegem els projectes existents
            emp.getProjects().clear();
            
            // Afegim els nous projectes
            for (Project project : projects) {
                // És important obtenir la versió gestionada del projecte
                Project managedProject = session.get(Project.class, project.getProjectId());
                emp.addProject(managedProject);  // Aquest mètode gestiona la relació bidireccional
            }
            
            session.merge(emp);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // ---------- Mètodes per gestionar Projectes ----------

    /**
     * Afegeix un nou projecte a la base de dades.
     */
    public static Project addProject(String name, String description, String status) {
        Session session = factory.openSession();
        Transaction tx = null;
        Project result = null;
        try {
            tx = session.beginTransaction();
            result = new Project(name, description, status);
            session.persist(result);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            result = null;
        } finally {
            session.close();
        }
        return result;
    }

    /**
     * Actualitza la informació d'un projecte existent.
     */
    public static void updateProject(long projectId, String name, String description, String status) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            if (project != null) {
                project.setName(name);
                project.setDescription(description);
                project.setStatus(status);
                session.merge(project);
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // ---------- Mètodes de cerca específics ----------

    /**
     * Troba tots els empleats que tenen un cert tipus de contacte.
     * Utilitza una consulta HQL amb JOIN.
     */
    public static Collection<Employee> findEmployeesByContactType(String contactType) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<Employee> result = null;
        try {
            tx = session.beginTransaction();
            
            // Consulta HQL amb JOIN i DISTINCT per evitar duplicats
            String hql = "SELECT DISTINCT e FROM Employee e JOIN e.contacts c WHERE c.contactType = :type";
            result = session.createQuery(hql, Employee.class)
                          .setParameter("type", contactType)
                          .list();
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    /**
     * Troba tots els empleats assignats a un projecte específic.
     */
    public static Collection<Employee> findEmployeesByProject(long projectId) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<Employee> result = null;
        try {
            tx = session.beginTransaction();
            
            Project project = session.get(Project.class, projectId);
            if (project != null) {
                // Utilitzem la col·lecció d'empleats del projecte directament
                result = project.getEmployees();
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    // ---------- Mètodes genèrics ----------

    /**
     * Obté una entitat per ID.
     * Mètode genèric que funciona amb qualsevol entitat JPA.
     */
    public static <T> T getById(Class<? extends T> clazz, long id) {
        Session session = factory.openSession();
        Transaction tx = null;
        T obj = null;
        try {
            tx = session.beginTransaction();
            obj = clazz.cast(session.get(clazz, id));
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return obj;
    }

    /**
     * Elimina una entitat per ID.
     * Mètode genèric que funciona amb qualsevol entitat JPA.
     */
    public static <T> void delete(Class<? extends T> clazz, Serializable id) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            T obj = clazz.cast(session.get(clazz, id));
            if (obj != null) {
                session.remove(obj);
                tx.commit();
            }
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Llista totes les entitats d'una classe específica.
     * Permet filtrar amb una clàusula WHERE opcional.
     */
    public static <T> Collection<?> listCollection(Class<? extends T> clazz, String where) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<?> result = null;
        try {
            tx = session.beginTransaction();
            
            // Construïm la consulta HQL
            if (where.length() == 0) {
                result = session.createQuery("FROM " + clazz.getName(), clazz).list();
            } else {
                result = session.createQuery("FROM " + clazz.getName() + " WHERE " + where, clazz).list();
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    public static <T> Collection<?> listCollection(Class<? extends T> clazz) {
        return listCollection(clazz, "");
    }

    /**
     * Converteix una col·lecció d'entitats a String.
     * Útil per mostrar resultats.
     */
    public static <T> String collectionToString(Class<? extends T> clazz, Collection<?> collection) {
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

    // ---------- Mètodes per consultes SQL natives ----------

    /**
     * Executa una consulta SQL nativa d'actualització.
     */
    public static void queryUpdate(String queryString) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            NativeQuery<?> query = session.createNativeQuery(queryString, Void.class);
            query.executeUpdate();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Executa una consulta SQL nativa de selecció.
     */
    public static List<Object[]> queryTable(String queryString) {
        Session session = factory.openSession();
        Transaction tx = null;
        List<Object[]> result = null;
        try {
            tx = session.beginTransaction();
            NativeQuery<Object[]> query = session.createNativeQuery(queryString, Object[].class);
            result = query.getResultList();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    /**
     * Converteix els resultats d'una consulta SQL nativa a String.
     * Útil per mostrar els resultats de queryTable().
     */
    public static String tableToString(List<Object[]> rows) {
        StringBuilder txt = new StringBuilder();
        for (Object[] row : rows) {
            for (Object cell : row) {
                txt.append(cell.toString()).append(", ");
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
     * Cerca contactes per empleat i tipus.
     * Exemple de consulta amb múltiples criteris.
     */
    public static Collection<Contact> findContactsByEmployeeAndType(long employeeId, String contactType) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<Contact> result = null;
        try {
            tx = session.beginTransaction();
            
            // Consulta HQL amb múltiples condicions
            String hql = "FROM Contact c WHERE c.employee.id = :empId AND c.contactType = :type";
            result = session.createQuery(hql, Contact.class)
                          .setParameter("empId", employeeId)
                          .setParameter("type", contactType)
                          .list();
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
        return result;
    }

    /**
     * Elimina un contacte específic d'un empleat.
     * Exemple de com gestionar relacions en operacions d'eliminació.
     */
    public static void removeContactFromEmployee(long employeeId, long contactId) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Employee emp = session.get(Employee.class, employeeId);
            Contact contact = session.get(Contact.class, contactId);
            
            if (emp != null && contact != null) {
                // Utilitzem el mètode d'utilitat que manté la consistència bidireccional
                emp.removeContact(contact);
                session.remove(contact);
                session.merge(emp);
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    /**
     * Actualitza la informació d'un contacte.
     * Exemple d'actualització simple d'una entitat.
     */
    public static void updateContact(long contactId, String contactType, String value, String description) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            
            Contact contact = session.get(Contact.class, contactId);
            if (contact != null) {
                contact.setContactType(contactType);
                contact.setValue(value);
                contact.setDescription(description);
                session.merge(contact);
            }
            
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }
}
