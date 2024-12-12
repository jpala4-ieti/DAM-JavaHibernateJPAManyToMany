package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity  // Indica que aquesta classe és una entitat JPA
@Table(name = "employees")  // Especifica el nom de la taula a la base de dades
public class Employee implements Serializable {
    
    @Id  // Defineix la clau primària
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Autoincrement
    @Column(name = "id")
    private long employeeId;

    // Informació bàsica de l'empleat
    @Column(nullable = false, length = 100)
    private String firstName;
    
    @Column(nullable = false, length = 100)
    private String lastName;
    
    private int salary;

    // Un empleat pot tenir múltiples contactes
    // - mappedBy indica que la relació està gestionada per l'atribut "employee" a Contact
    // - cascade especifica que les operacions s'han de propagar als contactes relacionats
    // - fetch EAGER significa que els contactes es carreguen immediatament amb l'empleat
    @OneToMany(
        mappedBy = "employee", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.EAGER,
        orphanRemoval = true  // Elimina els contactes que es desvinculin de l'empleat
    )
    private Set<Contact> contacts = new HashSet<>();

    // Relació Many-to-Many amb Project
    // - cascade especifica quines operacions es propaguen als projectes relacionats
    @ManyToMany(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.EAGER
    )
    @JoinTable(
        name = "employee_project",  // Nom de la taula intermèdia
        joinColumns = @JoinColumn(name = "employee_id"),  // Clau forana a Employee
        inverseJoinColumns = @JoinColumn(name = "project_id")  // Clau forana a Project
    )
    private Set<Project> projects = new HashSet<>();

    // Constructor per defecte requerit per JPA
    public Employee() {}
    
    // Constructor amb paràmetres
    public Employee(String firstName, String lastName, int salary) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
    }

    // Getters i setters
    public long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(long employeeId) {
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

    // Mètodes d'utilitat per gestionar contactes
    public void addContact(Contact contact) {
        contacts.add(contact);
        contact.setEmployee(this);
    }

    public void removeContact(Contact contact) {
        contacts.remove(contact);
        contact.setEmployee(null);
    }

    // Mètodes d'utilitat per gestionar projectes
    public void addProject(Project project) {
        projects.add(project);
        project.getEmployees().add(this);
    }

    public void removeProject(Project project) {
        projects.remove(project);
        project.getEmployees().remove(this);
    }

    // Sobreescrivim toString() per facilitar la depuració i visualització
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Employee[id=%d, name='%s %s', salary=%d", 
            employeeId, firstName, lastName, salary));
            
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

    // Sobreescrivim equals() i hashCode() basant-nos en l'ID
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