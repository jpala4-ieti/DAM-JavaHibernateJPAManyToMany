package com.project;

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

public class Manager {
    private static SessionFactory factory;

    public static void createSessionFactory() {
        try {
            Configuration configuration = new Configuration();
            
            // Afegim les classes amb anotacions
            configuration.addAnnotatedClass(Employee.class);
            configuration.addAnnotatedClass(Contact.class);
            configuration.addAnnotatedClass(Project.class);

            StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();
                
            factory = configuration.buildSessionFactory(serviceRegistry);
        } catch (Throwable ex) {
            System.err.println("No s'ha pogut crear la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

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
        } catch (Throwable ex) {
            System.err.println("Error creant la SessionFactory: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void close() {
        factory.close();
    }

    // Mètodes per Employee
    public static Employee addEmployee(String firstName, String lastName, int salary) {
        Session session = factory.openSession();
        Transaction tx = null;
        Employee result = null;
        try {
            tx = session.beginTransaction();
            result = new Employee(firstName, lastName, salary);
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

    public static void updateEmployee(long employeeId, String firstName, String lastName, int salary) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            emp.setFirstName(firstName);
            emp.setLastName(lastName);
            emp.setSalary(salary);
            session.merge(emp);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void updateEmployeeProjects(long employeeId, Set<Project> projects) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Employee emp = session.get(Employee.class, employeeId);
            emp.getProjects().clear();
            for (Project project : projects) {
                Project managedProject = session.get(Project.class, project.getProjectId());
                emp.addProject(managedProject);
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

    // Mètodes per Contact
    public static Contact addContact(String name, String email) {
        Session session = factory.openSession();
        Transaction tx = null;
        Contact result = null;
        try {
            tx = session.beginTransaction();
            result = new Contact(name, email);
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

    public static void updateContact(long contactId, String name, String email, Set<Employee> employees) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Contact contact = session.get(Contact.class, contactId);
            contact.setName(name);
            contact.setEmail(email);
            
            // Netegem les relacions existents
            for (Employee emp : contact.getEmployees()) {
                emp.setContact(null);
            }
            contact.getEmployees().clear();
            
            // Afegim les noves relacions
            for (Employee emp : employees) {
                Employee managedEmp = session.get(Employee.class, emp.getEmployeeId());
                contact.addEmployee(managedEmp);
            }
            
            session.merge(contact);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Mètodes per Project
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

    public static void updateProject(long projectId, String name, String description, String status) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            project.setName(name);
            project.setDescription(description);
            project.setStatus(status);
            session.merge(project);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    public static void updateProjectEmployees(long projectId, Set<Employee> employees) {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Project project = session.get(Project.class, projectId);
            project.getEmployees().clear();
            for (Employee emp : employees) {
                Employee managedEmp = session.get(Employee.class, emp.getEmployeeId());
                project.addEmployee(managedEmp);
            }
            session.merge(project);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Mètodes genèrics
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

    public static <T> Collection<?> listCollection(Class<? extends T> clazz) {
        return listCollection(clazz, "");
    }

    public static <T> Collection<?> listCollection(Class<? extends T> clazz, String where) {
        Session session = factory.openSession();
        Transaction tx = null;
        Collection<?> result = null;
        try {
            tx = session.beginTransaction();
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

    // Mètodes per consultes natives SQL
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
}