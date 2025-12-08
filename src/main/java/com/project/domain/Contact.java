package com.project.domain;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * Entitat JPA que representa una dada de contacte d'un empleat.
 * 
 * CONCEPTES CLAU:
 * - @Entity: Marca la classe com una entitat JPA que es mapeja a una taula
 * - @Table: Permet especificar el nom de la taula (si és diferent del nom de la classe)
 * - Requereix constructor sense arguments (JPA l'utilitza per instanciar objectes)
 * - La classe i els atributs persistents NO poden ser final
 */
@Entity  
@Table(name = "contacts")  
public class Contact implements Serializable {
    
    /**
     * Clau primària de l'entitat.
     * 
     * @Id: Marca aquest camp com la clau primària
     * @GeneratedValue(strategy = IDENTITY): La BD genera el valor automàticament (autoincrement)
     *   - Altres estratègies: SEQUENCE (Oracle), TABLE, AUTO (JPA decideix)
     * @Column(name = "id"): Nom de la columna a la BD (opcional si coincideix amb l'atribut)
     * 
     * NOTA: Utilitzem Long (objecte) en lloc de long (primitiu) per permetre valors null
     * abans de la persistència. Això és important per detectar entitats noves.
     */
    @Id  
    @GeneratedValue(strategy = GenerationType.IDENTITY)  
    @Column(name = "id")
    private Long contactId;  // CANVIAT: de long a Long per permetre null

    /**
     * Tipus de contacte (EMAIL, PHONE, ADDRESS, etc.).
     * 
     * @Column:
     *   - nullable = false: La columna NO pot ser NULL (camp obligatori)
     *   - length = 50: Longitud màxima de 50 caràcters (optimitza espai a BD)
     */
    @Column(nullable = false, length = 50)
    private String contactType;
    
    /**
     * Valor del contacte (l'email, telèfon o adreça concreta).
     */
    @Column(nullable = false, length = 255)
    private String value;
    
    /**
     * Descripció opcional del contacte.
     * No té nullable=false, per tant permet NULL per defecte.
     */
    @Column(length = 255)
    private String description;

    /**
     * RELACIÓ MANY-TO-ONE AMB EMPLOYEE
     * 
     * @ManyToOne: Molts contactes pertanyen a UN empleat
     *   - fetch = FetchType.LAZY: RECOMANAT! Carrega l'empleat només quan s'accedeix
     *     (EAGER carrega sempre, pot causar problemes de rendiment)
     * 
     * @JoinColumn: Defineix la columna de clau forana
     *   - name = "employee_id": Nom de la columna FK a la taula contacts
     *   - nullable = false: Tot contacte HA de tenir un empleat (integritat referencial)
     * 
     * IMPORTANT: Contact és el costat "propietari" d'aquesta relació bidireccional
     * perquè té la @JoinColumn (la FK està a la taula contacts).
     */
    @ManyToOne(fetch = FetchType.LAZY)  // CANVIAT: de EAGER a LAZY
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Constructor per defecte - OBLIGATORI per JPA.
     * JPA l'utilitza per crear instàncies via reflexió.
     */
    public Contact() {}
    
    /**
     * Constructor amb paràmetres.
     * No inclou ID (es genera automàticament) ni employee (es configura via relació).
     */
    public Contact(String contactType, String value, String description) {
        this.contactType = contactType;
        this.value = value;
        this.description = description;
    }

    // ===================== GETTERS I SETTERS =====================
    
    public Long getContactId() {  // CANVIAT: retorna Long
        return contactId;
    }

    public void setContactId(Long contactId) {  // CANVIAT: rep Long
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
     * toString() per facilitar depuració.
     * 
     * IMPORTANT: NO incloure la relació employee.toString() perquè
     * causaria recursió infinita (Employee -> Contact -> Employee -> ...)
     * Només mostrem l'ID de l'empleat si cal.
     */
    @Override
    public String toString() {
        return String.format("Contact[id=%d, type='%s', value='%s', desc='%s', empId=%d]", 
            contactId, 
            contactType, 
            value, 
            description,
            employee != null ? employee.getEmployeeId() : null);
    }

    /**
     * EQUALS I HASHCODE - CORRECCIÓ D'UN ERROR CONCEPTUAL GREU
     * 
     * PROBLEMA ORIGINAL: Utilitzar l'ID generat per equals/hashCode
     *   - Quan l'entitat és nova (no persistida), l'ID és null/0
     *   - Dues entitats noves tindrien el mateix hashCode = problema amb HashSet/HashMap
     *   - Després de persistir, el hashCode canvia = l'objecte "desapareix" del Set
     * 
     * SOLUCIÓ: Utilitzar identificador de negoci (camps que identifiquen únicament l'entitat)
     *   - En aquest cas: contactType + value + employeeId defineixen un contacte únic
     *   - Si no hi ha identificador de negoci clar, considerar UUID generat al constructor
     * 
     * ALTERNATIVA: Utilitzar només l'ID però amb Objects.equals per gestionar nulls
     *   i retornar hashCode constant (menys eficient però segur)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Contact contact = (Contact) o;
        
        // Si ambdós tenen ID, comparem per ID (entitats persistides)
        if (this.contactId != null && contact.contactId != null) {
            return Objects.equals(contactId, contact.contactId);
        }
        
        // Si no, comparem per identificador de negoci
        return Objects.equals(contactType, contact.contactType) &&
               Objects.equals(value, contact.value) &&
               Objects.equals(
                   employee != null ? employee.getEmployeeId() : null,
                   contact.employee != null ? contact.employee.getEmployeeId() : null
               );
    }

    /**
     * HashCode consistent amb equals.
     * Utilitzem els mateixos camps que a equals per garantir el contracte.
     */
    @Override
    public int hashCode() {
        // Utilitzem camps immutables o de negoci
        return Objects.hash(contactType, value);
    }
}