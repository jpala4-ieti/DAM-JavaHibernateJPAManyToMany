package com.project;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

// Indica que aquesta classe és una entitat JPA que es mapeja a una taula
@Entity
// Especifica el nom de la taula a la base de dades
@Table(name = "contacts")
public class Contact implements Serializable {

    // Defineix la clau primària de l'entitat
    @Id
    // Indica que el valor de la clau primària es genera automàticament
    // IDENTITY significa que la base de dades s'encarrega de generar el valor
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long contactId;

    // Camps bàsics que es mapejaran directament a columnes de la taula
    private String name;
    private String email;

    // Defineix una relació One-To-Many amb Employee
    // - mappedBy indica que la relació està gestionada per l'atribut "contact" a Employee
    // - cascade especifica que les operacions s'han de propagar als empleats relacionats
    // - CascadeType.ALL significa que totes les operacions (PERSIST, MERGE, REMOVE, REFRESH) es propaguen
    // - fetch configura quan s'han de carregar les dades relacionades
    // - FetchType.EAGER significa que els empleats es carreguen immediatament amb el contacte
    @OneToMany(
        mappedBy = "contact", 
        cascade = CascadeType.ALL, 
        fetch = FetchType.EAGER
    )
    private Set<Employee> employees = new HashSet<>();

    // Constructor per defecte requerit per JPA
    public Contact() {}

    // Constructor amb paràmetres per crear nous contactes
    public Contact(String name, String email) {
        this.name = name;
        this.email = email;
    }

    // Getters i setters
    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Employee> getEmployees() {
        return employees;
    }

    public void setEmployees(Set<Employee> employees) {
        this.employees = employees;
    }

    // Mètode d'utilitat per gestionar la relació bidireccional amb Employee
    // És important mantenir la consistència per ambdós costats de la relació
    public void addEmployee(Employee employee) {
        employees.add(employee);
        employee.setContact(this);
    }

    // Mètode d'utilitat per eliminar la relació bidireccional amb Employee
    public void removeEmployee(Employee employee) {
        employees.remove(employee);
        employee.setContact(null);
    }

    // Sobreescrivim toString() per facilitar la depuració i visualització
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Contact[id=%d, name='%s', email='%s'", 
            contactId, name, email));
        
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
        Contact contact = (Contact) o;
        return contactId == contact.contactId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(contactId);
    }
}