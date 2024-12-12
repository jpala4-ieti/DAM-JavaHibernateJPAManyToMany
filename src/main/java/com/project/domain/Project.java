package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entitat JPA que representa un projecte.
 * Implementa Serializable per permetre la serialització de l'entitat,
 * necessari per determinades operacions de JPA i per cache.
 */
@Entity
@Table(name = "projects")
public class Project implements Serializable {

    /**
     * Clau primària del projecte.
     * - @Id marca aquest camp com la clau primària
     * - @GeneratedValue configura la generació automàtica d'IDs
     * - strategy = IDENTITY delega la generació a la base de dades
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long projectId;

    /**
     * Nom del projecte.
     * JPA mapeja automàticament aquesta propietat com una columna
     * amb el mateix nom que l'atribut.
     */
    private String name;

    /**
     * Descripció detallada del projecte.
     * JPA crea una columna 'description' per defecte.
     */
    private String description;
    
    /**
     * Estat actual del projecte.
     * @Column permet especificar propietats de la columna:
     * - name: nom personalitzat a la base de dades
     * - length: longitud màxima per al camp String
     */
    @Column(name = "status", length = 20)
    private String status;  // Ex: "ACTIU", "COMPLETAT", "EN_ESPERA"

    /**
     * Relació bidireccional Many-to-Many amb Employee.
     * - mappedBy indica que Employee és el propietari de la relació
     * - fetch EAGER carrega sempre els empleats amb el projecte
     * La col·lecció s'inicialitza per evitar NullPointerException
     */
    @ManyToMany(
        mappedBy = "projects",
        fetch = FetchType.EAGER
    )
    private Set<Employee> employees = new HashSet<>();

    /**
     * Constructor per defecte.
     * Requerit per JPA per instanciar l'entitat.
     */
    public Project() {}

    /**
     * Constructor amb els camps bàsics.
     * No inclou l'ID (generat automàticament) ni col·leccions.
     */
    public Project(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    /**
     * Getters i setters per l'ID del projecte.
     */
    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    /**
     * Getters i setters per les propietats bàsiques.
     */
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

    /**
     * Getters i setters per la col·lecció d'empleats.
     */
    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    /**
     * Mètode per afegir un empleat al projecte.
     * Manté la consistència bidireccional actualitzant ambdós costats
     * de la relació.
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.getProjects().add(this);
    }

    /**
     * Mètode per eliminar un empleat del projecte.
     * Manté la consistència bidireccional eliminant la relació
     * per ambdós costats.
     */
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.getProjects().remove(this);
    }

    /**
     * Comprova si un empleat està assignat al projecte.
     * Útil per validacions abans d'operacions amb empleats.
     */
    public boolean hasEmployee(Employee employee) {
        return employees.contains(employee);
    }

    /**
     * Genera una representació en String del projecte.
     * Inclou informació bàsica i la llista d'empleats assignats.
     * Evita recursió infinita no accedint a les relacions inverses.
     */
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

    /**
     * Implementació d'equals basada en l'ID.
     * Necessari per al correcte funcionament en col·leccions
     * i comparacions d'entitats.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return projectId == project.projectId;
    }

    /**
     * Implementació de hashCode basada en l'ID.
     * Ha de ser consistent amb equals.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(projectId);
    }
}
