package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entitat JPA que representa un empleat.
 * 
 * CONCEPTES CLAU D'AQUESTA ENTITAT:
 * - És el costat "propietari" de la relació ManyToMany amb Project (té @JoinTable)
 * - És el costat "invers" de la relació OneToMany amb Contact (té mappedBy a Contact)
 * - Implements Serializable: Necessari per JPA/cache de segon nivell
 */
@Entity
@Table(name = "employees")
public class Employee implements Serializable {
    
    // Constant per serialització
    private static final long serialVersionUID = 1L;
    
    /**
     * Clau primària amb generació automàtica.
     * Utilitzem Long (objecte) per detectar entitats noves (null abans de persistir).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long employeeId;  // CANVIAT: de long a Long

    /**
     * Dades bàsiques de l'empleat.
     * nullable = false: camps obligatoris a nivell de BD (NOT NULL constraint)
     */
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    /**
     * Salari sense anotacions específiques.
     * JPA mapeja automàticament int a INTEGER.
     */
    private int salary;

    /**
     * RELACIÓ ONE-TO-MANY AMB CONTACT
     * 
     * @OneToMany: Un empleat té MOLTS contactes
     *   - mappedBy = "employee": Indica que Contact.employee és la FK (Contact és propietari)
     *   - cascade = CascadeType.ALL: Totes les operacions es propaguen als contactes
     *     (PERSIST, MERGE, REMOVE, REFRESH, DETACH)
     *   - fetch = FetchType.LAZY: RECOMANAT! Carrega contactes només quan s'accedeixen
     *   - orphanRemoval = true: Si un Contact es treu del Set, s'elimina de la BD
     *     (Un Contact sense Employee no té sentit en aquest model)
     * 
     * IMPORTANT: Inicialitzar sempre les col·leccions per evitar NullPointerException
     */
    @OneToMany(
        mappedBy = "employee",        // Contact és el propietari (té la FK)
        cascade = CascadeType.ALL,    // Operacions en cascada
        fetch = FetchType.LAZY,       // CANVIAT: de EAGER a LAZY per rendiment
        orphanRemoval = true          // Elimina contactes orfes
    )
    private Set<Contact> contacts = new HashSet<>();

    /**
     * RELACIÓ MANY-TO-MANY AMB PROJECT
     * 
     * @ManyToMany: Molts empleats treballen en MOLTS projectes
     *   - cascade: Només PERSIST i MERGE (MAI REMOVE en ManyToMany!)
     *     Si eliminem un empleat, NO volem eliminar els projectes
     *   - fetch = FetchType.LAZY: RECOMANAT per rendiment
     * 
     * @JoinTable: Defineix la TAULA INTERMÈDIA per la relació N:M
     *   - name = "employee_project": Nom de la taula pont
     *   - joinColumns: Columna que referencia AQUESTA entitat (Employee)
     *   - inverseJoinColumns: Columna que referencia l'ALTRA entitat (Project)
     * 
     * Employee és el PROPIETARI de la relació perquè defineix @JoinTable.
     * Project tindrà mappedBy = "projects".
     */
    @ManyToMany(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},  // MAI CascadeType.REMOVE!
        fetch = FetchType.LAZY  // CANVIAT: de EAGER a LAZY
    )
    @JoinTable(
        name = "employee_project",                           // Taula intermèdia
        joinColumns = @JoinColumn(name = "employee_id"),     // FK a employees
        inverseJoinColumns = @JoinColumn(name = "project_id") // FK a projects
    )
    private Set<Project> projects = new HashSet<>();

    /**
     * Constructor per defecte - OBLIGATORI per JPA.
     */
    public Employee() {}
    
    /**
     * Constructor amb paràmetres bàsics.
     * No inclou ID (generat) ni col·leccions (s'afegeixen després).
     */
    public Employee(String firstName, String lastName, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
    }

    // ===================== GETTERS I SETTERS =====================
    
    public Long getEmployeeId() {  // CANVIAT: retorna Long
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {  // CANVIAT: rep Long
        this.employeeId = employeeId;
    }

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

    public Set<Contact> getContacts() {
        return contacts;
    }

    public void setContacts(Set<Contact> contacts) {
        this.contacts = contacts;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    // ===================== MÈTODES DE GESTIÓ DE RELACIONS =====================
    
    /**
     * MÈTODES HELPER PER MANTENIR LA CONSISTÈNCIA BIDIRECCIONAL
     * 
     * IMPORTANT: En relacions bidireccionals, sempre hem d'actualitzar AMBDÓS costats!
     * Si només afegim el Contact al Set però no fem contact.setEmployee(this),
     * la relació no es persistirà correctament.
     */
    
    /**
     * Afegeix un contacte i manté la consistència bidireccional.
     */
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setEmployee(this);  // CRÍTIC: Actualitzar l'altre costat!
    }

    /**
     * Elimina un contacte i trenca la relació bidireccional.
     * Amb orphanRemoval=true, el Contact s'eliminarà de la BD.
     */
    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setEmployee(null);  // CRÍTIC: Trencar la relació!
    }

    /**
     * Afegeix un projecte i manté la consistència bidireccional ManyToMany.
     */
    public void addProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);  // CRÍTIC: Actualitzar l'altre costat!
    }

    /**
     * Elimina un projecte i trenca la relació bidireccional.
     */
    public void removeProject(Project project) {
        projects.remove(project);
        project.getEmployees().remove(this);  // CRÍTIC: Trencar la relació!
    }

    /**
     * toString() amb gestió de relacions per evitar recursió infinita.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Employee[id=%d, name='%s %s', salary=%d", 
            employeeId, firstName, lastName, salary));
            
        // Mostrem només informació bàsica dels contactes
        if (contacts != null && !contacts.isEmpty()) {
            sb.append(", contacts={");
            boolean first = true;
            for (Contact c : contacts) {
                if (!first) sb.append(", ");
                sb.append(String.format("%s: %s", c.getContactType(), c.getValue()));
                first = false;
            }
            sb.append("}");
        }
        
        // Mostrem només noms dels projectes (NO accedir a employees!)
        if (projects != null && !projects.isEmpty()) {
            sb.append(", projects={");
            boolean first = true;
            for (Project p : projects) {
                if (!first) sb.append(", ");
                sb.append(p.getName());  // Només el nom, NO p.getEmployees()!
                first = false;
            }
            sb.append("}");
        }
        
        sb.append("]");
        return sb.toString();
    }

    /**
     * EQUALS/HASHCODE - CORREGIT
     * 
     * Utilitzem firstName + lastName com a identificador de negoci.
     * Alternativament, es podria generar un UUID al constructor.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        
        // Si ambdós tenen ID, comparem per ID
        if (this.employeeId != null && employee.employeeId != null) {
            return Objects.equals(employeeId, employee.employeeId);
        }
        
        // Si no, comparem per identificador de negoci
        return Objects.equals(firstName, employee.firstName) &&
               Objects.equals(lastName, employee.lastName);
    }

    @Override
    public int hashCode() {
        // Utilitzem camps de negoci que no canvien
        return Objects.hash(firstName, lastName);
    }
}