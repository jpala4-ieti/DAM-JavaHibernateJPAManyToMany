package com.project;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity  // Indica que aquesta classe és una entitat JPA
@Table(name = "contacts")  // Especifica el nom de la taula a la base de dades
public class Contact implements Serializable {
    
    @Id  // Defineix la clau primària
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Autoincrement
    @Column(name = "id")
    private long contactId;

    // Tipus de contacte (EMAIL, PHONE, ADDRESS, etc.)
    @Column(nullable = false, length = 50)
    private String contactType;
    
    // Valor del contacte (l'email, telèfon o adreça en si)
    @Column(nullable = false, length = 255)
    private String value;
    
    // Descripció opcional (p.ex. "Telèfon personal", "Email feina", etc.)
    @Column(length = 255)
    private String description;

    // Relació Many-to-One amb Employee (molts contactes poden pertànyer a un empleat)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Constructor per defecte requerit per JPA
    public Contact() {}
    
    // Constructor amb paràmetres
    public Contact(String contactType, String value, String description) {
        this.contactType = contactType;
        this.value = value;
        this.description = description;
    }

    // Getters i setters
    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // Sobreescrivim toString() per facilitar la depuració i visualització
    @Override
    public String toString() {
        return String.format("Contact[id=%d, type='%s', value='%s', desc='%s']", 
            contactId, contactType, value, description);
    }

    // Sobreescrivim equals() i hashCode() basant-nos en l'ID
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