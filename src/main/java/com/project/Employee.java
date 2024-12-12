package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entitat JPA que representa un empleat.
 * 
 * @Entity - Marca la classe com una entitat JPA que serà mapejada a una taula
 *        - Requereix un constructor sense arguments
 *        - La classe no pot ser final
 *        - Els camps persistents no poden ser finals
 *
 * @Table - Defineix les característiques de la taula:
 *        - name: nom de la taula a la base de dades
 *        - Es poden afegir índexs i constraints únics si cal
 */
@Entity
@Table(name = "employees")
public class Employee implements Serializable {
    
    /**
     * Identificador únic de l'empleat.
     * @Id - Indica que és la clau primària de l'entitat
     * @GeneratedValue - Configuració de la generació automàtica de l'ID:
     *                 - IDENTITY: utilitza autoincrement de la base de dades
     *                 - Eficient per a la majoria de casos d'ús
     * @Column - Personalitza la columna a la base de dades
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long employeeId;

    /**
     * Informació bàsica de l'empleat.
     * @Column amb:
     * - nullable = false: camp obligatori
     * - length = 100: longitud màxima per optimitzar l'espai
     */
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    /**
     * Salari sense anotacions específiques.
     * - JPA mapeja automàticament els tipus primitius
     * - Utilitza el tipus de columna adequat (INTEGER en aquest cas)
     */
    private int salary;

    /**
     * Col·lecció de contactes de l'empleat.
     * @OneToMany - Defineix una relació un-a-molts:
     * - mappedBy: indica que Contact és el propietari de la relació
     * - cascade: especifica quines operacions es propaguen als contactes
     *   - CascadeType.ALL: inclou PERSIST, MERGE, REMOVE, REFRESH, DETACH
     * - fetch: estratègia de càrrega
     *   - EAGER: carrega tots els contactes immediatament
     * - orphanRemoval: elimina els contactes que es desvinculin
     */
    @OneToMany(
        mappedBy = "employee", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    private Set<Contact> contacts = new HashSet<>();

    /**
     * Col·lecció de projectes assignats.
     * @ManyToMany - Defineix una relació molts-a-molts:
     * - cascade: només PERSIST i MERGE per evitar esborrats en cascada
     * - fetch EAGER: carrega tots els projectes immediatament
     *
     * @JoinTable - Configura la taula intermèdia:
     * - name: nom de la taula d'unió
     * - joinColumns: columna que referencia aquesta entitat
     * - inverseJoinColumns: columna que referencia l'altra entitat
     */
    @ManyToMany(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.EAGER
    )
    @JoinTable(
        name = "employee_project",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id")
    )
    private Set<Project> projects = new HashSet<>();

    /**
     * Constructor per defecte.
     * Requerit per JPA per la creació d'instàncies.
     */
    public Employee() {}
    
    /**
     * Constructor amb paràmetres.
     * Útil per la creació d'instàncies amb dades inicials.
     * No inclou ID (generat automàticament) ni col·leccions.
     */
    public Employee(String firstName, String lastName, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
    }

    /**
     * Getters i setters per l'ID.
     * Tot i que l'ID es genera automàticament, aquests mètodes són útils per:
     * - Accedir a l'ID després de la persistència
     * - Operacions de merge/update
     */
    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
        this.employeeId = employeeId;
    }

    /**
     * Getters i setters per les dades bàsiques de l'empleat.
     * JPA els utilitza per accedir i modificar els camps
     * durant les operacions de persistència.
     */
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getSalary() {
        return salary;
    }

    public void setSalary(int salary) {
        this.salary = salary;
    }

    /**
     * Getters i setters per la col·lecció de contactes.
     * Important:
     * - getContacts() retorna la col·lecció directament
     * - setContacts() s'ha d'utilitzar amb cura per mantenir 
     *   la consistència bidireccional
     */
    public Set<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    /**
     * Getters i setters per la col·lecció de projectes.
     * Similar a contacts, cal anar amb compte amb la
     * consistència bidireccional quan es modifica la col·lecció.
     */
    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    /**
     * Mètodes d'utilitat per gestionar la relació bidireccional amb Contact.
     * Important mantenir la consistència per ambdós costats de la relació.
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setEmployee(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setEmployee(null);
    }

    /**
     * Mètodes d'utilitat per gestionar la relació bidireccional amb Project.
     * En relacions ManyToMany, cal mantenir la consistència per ambdós costats.
     */
    public void addProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.getEmployees().remove(this);
    }

    /**
     * toString() modificat per mostrar les relacions.
     * Cal anar amb compte amb les relacions bidireccionals
     * per evitar crides recursives infinites.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Employee[id=%d, name='%s %s', salary=%d", 
            employeeId, firstName, lastName, salary));
            
        // Mostrem els contactes sense accedir a les seves relacions inverses
        if (!contacts.isEmpty()) {
            sb.append(", contacts={");
            boolean first = true;
            for (Contact c : contacts) {
                if (!first) sb.append(", ");
                sb.append(String.format("%s: %s", c.getContactType(), c.getValue()));
                first = false;
            }
            sb.append("}");
        }
        
        // Mostrem els projectes sense accedir a les seves relacions inverses
        if (!projects.isEmpty()) {
            sb.append(", projects={");
            boolean first = true;
            for (Project p : projects) {
                if (!first) sb.append(", ");
                sb.append(p.getName());
                first = false;
            }
            sb.append("}");
        }
        
        sb.append("]");
        return sb.toString();
    }

    /**
     * equals() i hashCode() basats en l'ID.
     * Important per:
     * - Consistència en col·leccions
     * - Comparacions d'entitats
     * - Operacions de persistència
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return employeeId == employee.employeeId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(employeeId);
    }
}