package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

// Indica que aquesta classe és una entitat JPA que es mapeja a una taula
@Entity
// Especifica el nom de la taula a la base de dades
@Table(name = "employees")
public class Employee implements Serializable {
    
    // Defineix la clau primària de l'entitat
    @Id
    // Indica que el valor de la clau primària es genera automàticament
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long employeeId;

    // Camps bàsics que es mapejaran directament a columnes de la taula
    private String firstName;
    private String lastName;
    private int salary;

    // Defineix la relació Many-To-One amb Contact
    // - @ManyToOne indica que molts Employee poden tenir el mateix Contact
    // - @JoinColumn especifica la columna que contindrà la clau forana
    // - name = "contact_id" serà el nom de la columna a la base de dades
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private Contact contact;

    // Defineix la relació Many-To-Many amb Project
    // - cascade especifica quines operacions es propaguen als projectes relacionats
    // - CascadeType.PERSIST: quan es guarda un Employee, es guarden els seus projectes
    // - CascadeType.MERGE: quan s'actualitza un Employee, s'actualitzen els seus projectes
    // - fetch EAGER significa que els projectes es carreguen immediatament amb l'empleat
    @ManyToMany(
        cascade = {CascadeType.PERSIST, CascadeType.MERGE},
        fetch = FetchType.EAGER
    )
    // Configuració de la taula intermèdia per la relació Many-To-Many
    @JoinTable(
        name = "employee_project",  // Nom de la taula intermèdia
        joinColumns = @JoinColumn(name = "employee_id"),  // Clau forana que referencia a Employee
        inverseJoinColumns = @JoinColumn(name = "project_id")  // Clau forana que referencia a Project
    )
    private Set<Project> projects = new HashSet<>();

    // Constructor per defecte requerit per JPA
    public Employee() {}
    
    // Constructor amb paràmetres per crear nous empleats
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

    public Contact getContact() {
        return contact;
    }

    // Important: Aquest setter s'utilitza per mantenir la relació bidireccional amb Contact
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    // Mètodes d'utilitat per gestionar la relació bidireccional amb Project
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
        
        if (contact != null) {
            sb.append(String.format(", contact=%s (%s)", 
                contact.getName(), contact.getEmail()));
        }
        
        if (projects != null && !projects.isEmpty()) {
            sb.append(", projects={");
            boolean first = true;
            for (Project proj : projects) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(String.format("%s (%s)", 
                    proj.getName(), proj.getStatus()));
                first = false;
            }
            sb.append("}");
        }
        
        sb.append("]");
        return sb.toString();
    }

    // Sobreescrivim equals() i hashCode() basant-nos en l'ID
    // És important per al correcte funcionament de col·leccions i comparacions
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