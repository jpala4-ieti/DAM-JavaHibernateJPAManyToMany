package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

// Indica que aquesta classe és una entitat JPA que es mapeja a una taula
@Entity
// Especifica el nom de la taula a la base de dades
@Table(name = "projects")
public class Project implements Serializable {

    // Defineix la clau primària de l'entitat
    @Id
    // Indica que el valor de la clau primària es genera automàticament
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long projectId;

    // Camps bàsics que es mapejaran directament a columnes de la taula
    private String name;
    private String description;
    
    // Podem utilitzar @Column per especificar detalls addicionals de la columna
    @Column(name = "status", length = 20)
    private String status;  // Ex: "ACTIVE", "COMPLETED", "ON_HOLD"

    // Defineix la relació Many-To-Many amb Employee
    // - mappedBy indica que la relació està gestionada per l'atribut "projects" a Employee
    // - això significa que Employee és el propietari de la relació
    // - fetch EAGER significa que els empleats es carreguen immediatament amb el projecte
    @ManyToMany(
        mappedBy = "projects",
        fetch = FetchType.EAGER
    )
    private Set<Employee> employees = new HashSet<>();

    // Constructor per defecte requerit per JPA
    public Project() {}

    // Constructor amb paràmetres per crear nous projectes
    public Project(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // Getters i setters
    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    // Mètodes d'utilitat per gestionar la relació bidireccional amb Employee
    // Tot i que Employee és el propietari de la relació, mantenim la consistència
    // actualitzant ambdós costats
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.getProjects().add(this);
    }

    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.getProjects().remove(this);
    }

    // Mètode d'utilitat per comprovar si un empleat està assignat al projecte
    public boolean hasEmployee(Employee employee) {
        return employees.contains(employee);
    }

    // Sobreescrivim toString() per facilitar la depuració i visualització
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project[id=%d, name='%s', status='%s'", 
            projectId, name, status));
        
        if (employees != null && !employees.isEmpty()) {
            sb.append(", employees={");
            boolean first = true;
            for (Employee emp : employees) {
                if (!first) {
                    sb.append(", ");
                }
                sb.append(String.format("%s %s (id:%d)", 
                    emp.getFirstName(), emp.getLastName(), emp.getEmployeeId()));
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
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(projectId);
    }
}