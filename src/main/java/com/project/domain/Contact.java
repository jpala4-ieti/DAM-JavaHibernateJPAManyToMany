package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;

/**
 * Entitat JPA que representa una dada de contacte d'un empleat.
 * 
 * @Entity - Marca la classe com una entitat JPA
 *        - Requereix un constructor sense arguments
 *        - La classe no pot ser final
 *        - Els atributs persistents no poden ser finals
 *
 * @Table - Especifica els detalls de la taula a la base de dades
 *        - name: nom de la taula (si és diferent del nom de la classe)
 *        - schema: esquema de la base de dades (opcional)
 *        - catalog: catàleg de la base de dades (opcional)
 */
@Entity  
@Table(name = "contacts")  
public class Contact implements Serializable {
    
    /**
     * Identificador únic del contacte.
     * @Id - Marca aquest camp com la clau primària
     * @GeneratedValue - Especifica com es genera el valor:
     *                 - IDENTITY: autoincrement gestionat per la base de dades
     *                 - Altres opcions: SEQUENCE, TABLE, AUTO
     * @Column - Personalitza la columna a la base de dades:
     *        - name: nom de la columna
     *        - altres propietats disponibles: unique, nullable, length, etc.
     */
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "id")
    private long contactId;

    /**
     * Tipus de contacte (EMAIL, PHONE, ADDRESS, etc.).
     * @Column amb:
     * - nullable = false: la columna NO pot ser NULL
     * - length = 50: longitud màxima de 50 caràcters
     * És important definir la longitud per optimitzar l'espai en bases de dades
     */
    @Column(nullable = false, length = 50)
    private String contactType;
    
    /**
     * Valor del contacte (l'email, telèfon o adreça).
     * Similar a contactType, però amb més longitud per permetre
     * valors més llargs com adreces o URLs.
     */
    @Column(nullable = false, length = 255)
    private String value;
    
    /**
     * Descripció opcional del contacte.
     * Permet NULL ja que no tots els contactes necessiten descripció.
     */
    @Column(length = 255)
    private String description;

    /**
     * Relació amb l'empleat propietari d'aquest contacte.
     * @ManyToOne - Defineix una relació molts-a-un amb Employee:
     *            - Molts contactes poden pertànyer a un empleat
     *            - fetch = EAGER: carrega la relació immediatament
     *
     * @JoinColumn - Especifica la columna de clau forana:
     *             - name: nom de la columna a la base de dades
     *             - nullable = false: un contacte sempre ha de tenir un empleat
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Constructor per defecte.
     * Requerit per JPA per crear instàncies de l'entitat.
     */
    public Contact() {}
    
    /**
     * Constructor amb paràmetres.
     * No inclou l'ID ja que es genera automàticament.
     * No inclou employee ja que es configura després mitjançant la relació.
     */
    public Contact(String contactType, String value, String description) {
        this.contactType = contactType;
        this.value = value;
        this.description = description;
    }

    // Getters i setters
    /**
     * Els getters i setters són necessaris per JPA.
     * JPA els utilitza per accedir i modificar els camps
     * fins i tot quan són privats.
     */
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

    /**
     * Sobreescrivim toString() per facilitar la depuració.
     * Important no incloure relacions per evitar cicles infinits.
     */
    @Override
    public String toString() {
        return String.format("Contact[id=%d, type='%s', value='%s', desc='%s']", 
            contactId, contactType, value, description);
    }

    /**
     * Sobreescrivim equals() basat en l'ID.
     * Important per:
     * - Col·leccions (HashSet, HashMap)
     * - Comparacions d'entitats
     * - Cerques en col·leccions
     * No utilitzem altres camps per evitar inconsistències
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        return contactId == contact.contactId;
    }

    /**
     * Sobreescrivim hashCode() basat en l'ID.
     * Ha de ser consistent amb equals().
     * Important per col·leccions basades en hash.
     */
    @Override
    public int hashCode() {
        return Long.hashCode(contactId);
    }
}