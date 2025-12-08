package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Entitat JPA que representa un projecte.
 * 
 * CONCEPTES CLAU:
 * - És el costat INVERS de la relació ManyToMany amb Employee (té mappedBy)
 * - Employee és el propietari perquè defineix @JoinTable
 */
@Entity
@Table(name = "projects")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long projectId;  // CANVIAT: de long a Long

    /**
     * Nom del projecte.
     * Sense @Column explícit, JPA crea una columna "name" automàticament.
     */
    @Column(nullable = false, length = 100)  // AFEGIT: constraints
    private String name;

    /**
     * Descripció detallada del projecte.
     */
    @Column(length = 500)  // AFEGIT: limit de longitud
    private String description;
    
    /**
     * Estat del projecte.
     * Podria ser un ENUM per major seguretat de tipus.
     */
    @Column(name = "status", length = 20)
    private String status;  // Ex: "ACTIU", "COMPLETAT", "PLANIFICAT"

    /**
     * RELACIÓ MANY-TO-MANY INVERSA AMB EMPLOYEE
     * 
     * @ManyToMany:
     *   - mappedBy = "projects": Indica que Employee.projects és el propietari
     *     (Employee té el @JoinTable, per tant controla la relació)
     *   - fetch = LAZY: RECOMANAT per rendiment
     * 
     * NO POSAR CASCADE aquí! El propietari (Employee) ja ho gestiona.
     * Si posem cascade aquí, podríem tenir comportaments inesperats.
     * 
     * IMPORTANT: Aquest costat NO pot afegir/eliminar relacions directament a BD.
     * Cal fer-ho sempre des del costat propietari (Employee).
     */
    @ManyToMany(
        mappedBy = "projects",     // Employee és el propietari
        fetch = FetchType.LAZY     // CANVIAT: de EAGER a LAZY
    )
    private Set<Employee> employees = new HashSet<>();

    /**
     * Constructor per defecte - OBLIGATORI per JPA.
     */
    public Project() {}

    /**
     * Constructor amb camps bàsics.
     */
    public Project(String name, String description, String status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    // ===================== GETTERS I SETTERS =====================
    
    public Long getProjectId() {  // CANVIAT: retorna Long
        return projectId;
    }

    public void setProjectId(Long projectId) {  // CANVIAT: rep Long
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

    // ===================== MÈTODES HELPER =====================
    
    /**
     * Afegeix un empleat al projecte.
     * 
     * NOTA IMPORTANT: Com Project és el costat INVERS (mappedBy),
     * per persistir la relació correctament, cal fer-ho des de Employee!
     * Aquest mètode és útil per mantenir consistència en memòria,
     * però la persistència real la fa Employee.addProject().
     */
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.getProjects().add(this);
    }

    /**
     * Elimina un empleat del projecte.
     */
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.getProjects().remove(this);
    }

    /**
     * Comprova si un empleat està assignat.
     */
    public boolean hasEmployee(Employee employee) {
        return employees.contains(employee);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Project[id=%d, name='%s', status='%s'", 
            projectId, name, status));
        
        // Només mostrem IDs dels empleats per evitar recursió
        if (employees != null && !employees.isEmpty()) {
            sb.append(", employees={");
            boolean first = true;
            for (Employee emp : employees) {
                if (!first) sb.append(", ");
                sb.append(String.format("%s %s", emp.getFirstName(), emp.getLastName()));
                first = false;
            }
            sb.append("}");
        }
        
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        
        if (this.projectId != null && project.projectId != null) {
            return Objects.equals(projectId, project.projectId);
        }
        
        // Identificador de negoci: nom del projecte (hauria de ser únic)
        return Objects.equals(name, project.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}