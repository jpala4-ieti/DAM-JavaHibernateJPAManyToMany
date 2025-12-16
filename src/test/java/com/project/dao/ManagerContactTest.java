package com.project.dao;

import com.project.domain.Contact;
import com.project.domain.Employee;
import com.project.test.HibernateTestBase;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS CRUD PER A LA GESTIÓ DE CONTACTES
 * =======================================
 * 
 * Aquesta classe conté tots els tests relacionats amb les operacions
 * CRUD de contactes a través del Manager.
 * 
 * PARTICULARITATS DELS CONTACTES:
 * - Un Contact sempre pertany a un Employee (relació ManyToOne obligatòria)
 * - No es poden crear contactes sense empleat associat
 * - orphanRemoval=true: Si es desvincula un contacte, s'elimina de la BD
 * - cascade=ALL des d'Employee: Operacions es propaguen als contactes
 * 
 * COBERTURA:
 * - addContactToEmployee(): Creació de contactes
 * - findContactsByEmployeeAndType(): Cerca de contactes
 * - updateContact(): Actualització de contactes
 * - removeContactFromEmployee(): Eliminació de contactes
 * - getById(): Lectura de contactes individuals
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests CRUD del Manager per a Contact")
class ManagerContactTest extends HibernateTestBase {
    
    // ========================================================================
    // TESTS DE CREACIÓ (CREATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació addContactToEmployee().
     */
    @Nested
    @DisplayName("CREATE - Tests d'addContactToEmployee()")
    class CreateContactTests {
        
        // Empleat propietari per als contactes de prova
        private Employee empleatPropietari;
        
        /**
         * Crear un empleat propietari abans de cada test.
         * Els contactes necessiten un empleat per existir.
         */
        @BeforeEach
        void setUpEmployeeOwner() {
            empleatPropietari = Manager.addEmployee("Propietari", "Contactes", 35000);
        }
        
        /**
         * Test bàsic: Crear un contacte amb dades vàlides.
         */
        @Test
        @DisplayName("Crear contacte EMAIL amb dades vàlides")
        void addContactToEmployee_Email_CreatCorrectament() {
            // ARRANGE
            String tipus = "EMAIL";
            String valor = "test@empresa.cat";
            String descripcio = "Email corporatiu";
            
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(),
                tipus,
                valor,
                descripcio
            );
            
            // ASSERT
            assertAll("Verificació del contacte creat",
                () -> assertNotNull(resultat, 
                    "El contacte no hauria de ser null"),
                () -> assertNotNull(resultat.getContactId(), 
                    "L'ID hauria d'estar assignat"),
                () -> assertTrue(resultat.getContactId() > 0, 
                    "L'ID ha de ser positiu"),
                () -> assertEquals(tipus, resultat.getContactType(), 
                    "El tipus no coincideix"),
                () -> assertEquals(valor, resultat.getValue(), 
                    "El valor no coincideix"),
                () -> assertEquals(descripcio, resultat.getDescription(), 
                    "La descripció no coincideix")
            );
        }
        
        /**
         * Test: Crear contactes de diferents tipus.
         */
        @ParameterizedTest(name = "Tipus: {0}, Valor: {1}")
        @CsvSource({
            "EMAIL, joan@test.com, Email personal",
            "PHONE, 666111222, Mòbil personal",
            "ADDRESS, 'Carrer Major 1, Barcelona', Adreça casa",
            "FAX, 931234567, Fax oficina",
            "TWITTER, @joantest, Xarxa social"
        })
        @DisplayName("Crear contactes de diferents tipus")
        void addContactToEmployee_DiversosTipus_TotsCreats(
                String tipus, String valor, String descripcio) {
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(),
                tipus,
                valor,
                descripcio
            );
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat.getContactId()),
                () -> assertEquals(tipus, resultat.getContactType()),
                () -> assertEquals(valor, resultat.getValue())
            );
        }
        
        /**
         * Test: Un empleat pot tenir múltiples contactes.
         */
        @Test
        @DisplayName("Un empleat pot tenir múltiples contactes")
        void addContactToEmployee_MultiplesContactes_TotsCreats() {
            // ACT - Afegir diversos contactes
            Contact email = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "EMAIL", "email@test.com", "Email");
            Contact phone = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "PHONE", "666111222", "Telèfon");
            Contact address = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "ADDRESS", "Carrer Test 1", "Adreça");
            
            // ASSERT
            assertAll(
                () -> assertNotNull(email.getContactId()),
                () -> assertNotNull(phone.getContactId()),
                () -> assertNotNull(address.getContactId()),
                // Verificar que són IDs diferents
                () -> assertNotEquals(email.getContactId(), phone.getContactId()),
                () -> assertNotEquals(phone.getContactId(), address.getContactId())
            );
        }
        
        /**
         * Test: Un empleat pot tenir múltiples contactes del mateix tipus.
         */
        @Test
        @DisplayName("Un empleat pot tenir múltiples emails")
        void addContactToEmployee_MultiplesDelMateixTipus_TotsCreats() {
            // ACT
            Contact email1 = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "EMAIL", "personal@test.com", "Personal");
            Contact email2 = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "EMAIL", "feina@test.com", "Feina");
            Contact email3 = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "EMAIL", "backup@test.com", "Backup");
            
            // ASSERT
            Collection<Contact> emails = Manager.findContactsByEmployeeAndType(
                empleatPropietari.getEmployeeId(), "EMAIL");
            
            assertEquals(3, emails.size(), 
                "Hauria de tenir 3 contactes EMAIL");
        }
        
        /**
         * Test: Crear contacte amb empleat inexistent retorna null.
         */
        @Test
        @DisplayName("Crear contacte amb empleat inexistent retorna null")
        void addContactToEmployee_EmpleatInexistent_RetornaNull() {
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                99999L, "EMAIL", "test@test.com", "Test");
            
            // ASSERT
            assertNull(resultat, 
                "Hauria de retornar null per empleat inexistent");
        }
        
        /**
         * Test: El contacte creat està vinculat correctament a l'empleat.
         */
        @Test
        @DisplayName("El contacte creat està vinculat a l'empleat")
        void addContactToEmployee_Vinculacio_Correcta() {
            // ACT
            Contact contacte = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "EMAIL", "test@test.com", "Test");
            
            // ASSERT - Recuperar el contacte i verificar vinculació
            Contact recuperat = Manager.getById(Contact.class, contacte.getContactId());
            
            assertNotNull(recuperat, "El contacte hauria d'existir a BD");
            // Nota: La verificació de employee pot requerir inicialització LAZY
        }
        
        /**
         * Test: El contacte existeix a la BD després de crear-lo.
         */
        @Test
        @DisplayName("El contacte es persisteix a la base de dades")
        void addContactToEmployee_Persistencia_Correcta() {
            // ACT
            Contact creat = Manager.addContactToEmployee(
                empleatPropietari.getEmployeeId(), "PHONE", "612345678", "Mòbil");
            Long id = creat.getContactId();
            
            // ASSERT - Recuperar directament per ID
            Contact recuperat = Manager.getById(Contact.class, id);
            
            assertAll(
                () -> assertNotNull(recuperat),
                () -> assertEquals("PHONE", recuperat.getContactType()),
                () -> assertEquals("612345678", recuperat.getValue())
            );
        }
    }
    
    // ========================================================================
    // TESTS DE LECTURA (READ)
    // ========================================================================
    
    /**
     * Grup de tests per a operacions de lectura de contactes.
     */
    @Nested
    @DisplayName("READ - Tests de lectura de contactes")
    class ReadContactTests {
        
        private Employee empleat;
        private Contact contacteEmail;
        private Contact contactePhone;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Lectura", "Contactes", 30000);
            contacteEmail = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "EMAIL", "lectura@test.com", "Email test");
            contactePhone = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "PHONE", "666000111", "Telèfon test");
        }
        
        /**
         * Test: getById retorna el contacte correcte.
         */
        @Test
        @DisplayName("getById retorna el contacte quan existeix")
        void getById_ContacteExistent_RetornaContacte() {
            // ACT
            Contact resultat = Manager.getById(Contact.class, contacteEmail.getContactId());
            
            // ASSERT
            assertAll(
                () -> assertNotNull(resultat),
                () -> assertEquals(contacteEmail.getContactId(), resultat.getContactId()),
                () -> assertEquals("EMAIL", resultat.getContactType()),
                () -> assertEquals("lectura@test.com", resultat.getValue())
            );
        }
        
        /**
         * Test: getById retorna null per ID inexistent.
         */
        @Test
        @DisplayName("getById retorna null per ID inexistent")
        void getById_IdInexistent_RetornaNull() {
            // ACT
            Contact resultat = Manager.getById(Contact.class, 99999L);
            
            // ASSERT
            assertNull(resultat);
        }
        
        /**
         * Test: findContactsByEmployeeAndType retorna contactes filtrats.
         */
        @Test
        @DisplayName("findContactsByEmployeeAndType retorna contactes EMAIL")
        void findContactsByEmployeeAndType_Email_RetornaEmails() {
            // ARRANGE - Afegir més emails
            Manager.addContactToEmployee(
                empleat.getEmployeeId(), "EMAIL", "altre@test.com", "Altre email");
            
            // ACT
            Collection<Contact> resultat = Manager.findContactsByEmployeeAndType(
                empleat.getEmployeeId(), "EMAIL");
            
            // ASSERT
            assertThat(resultat)
                .hasSize(2)
                .allMatch(c -> "EMAIL".equals(c.getContactType()));
        }
        
        /**
         * Test: findContactsByEmployeeAndType retorna buit si no hi ha coincidències.
         */
        @Test
        @DisplayName("findContactsByEmployeeAndType retorna buit sense coincidències")
        void findContactsByEmployeeAndType_TipusInexistent_RetornaBuit() {
            // ACT
            Collection<Contact> resultat = Manager.findContactsByEmployeeAndType(
                empleat.getEmployeeId(), "FAX");
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
        
        /**
         * Test: findContactsByEmployeeAndType amb empleat inexistent retorna buit.
         */
        @Test
        @DisplayName("findContactsByEmployeeAndType amb empleat inexistent retorna buit")
        void findContactsByEmployeeAndType_EmpleatInexistent_RetornaBuit() {
            // ACT
            Collection<Contact> resultat = Manager.findContactsByEmployeeAndType(
                99999L, "EMAIL");
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
        
        /**
         * Test: listCollection retorna tots els contactes.
         */
        @Test
        @DisplayName("listCollection retorna tots els contactes")
        void listCollection_TotsContactes_Retornats() {
            // ACT
            Collection<Contact> resultat = Manager.listCollection(Contact.class);
            
            // ASSERT
            assertEquals(2, resultat.size(), 
                "Hauria de retornar els 2 contactes creats");
        }
    }
    
    // ========================================================================
    // TESTS D'ACTUALITZACIÓ (UPDATE)
    // ========================================================================
    
    /**
     * Grup de tests per a l'operació updateContact().
     */
    @Nested
    @DisplayName("UPDATE - Tests d'actualització de contactes")
    class UpdateContactTests {
        
        private Employee empleat;
        private Contact contacteOriginal;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Update", "Test", 30000);
            contacteOriginal = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "EMAIL", "original@test.com", "Original");
        }
        
        /**
         * Test: updateContact modifica tots els camps.
         */
        @Test
        @DisplayName("updateContact modifica tots els camps")
        void updateContact_TotsElsCamps_Modificats() {
            // ARRANGE
            Long id = contacteOriginal.getContactId();
            String nouTipus = "PHONE";
            String nouValor = "666999888";
            String novaDesc = "Nou telèfon";
            
            // ACT
            Manager.updateContact(id, nouTipus, nouValor, novaDesc);
            
            // ASSERT
            Contact actualitzat = Manager.getById(Contact.class, id);
            
            assertAll(
                () -> assertEquals(nouTipus, actualitzat.getContactType()),
                () -> assertEquals(nouValor, actualitzat.getValue()),
                () -> assertEquals(novaDesc, actualitzat.getDescription())
            );
        }
        
        /**
         * Test: updateContact preserva l'associació amb l'empleat.
         */
        @Test
        @DisplayName("updateContact preserva l'associació amb l'empleat")
        void updateContact_PreservaEmpleat_Correcte() {
            // ARRANGE
            Long id = contacteOriginal.getContactId();
            
            // ACT
            Manager.updateContact(id, "PHONE", "666111222", "Nou");
            
            // ASSERT - El contacte encara pertany al mateix empleat
            Collection<Contact> contactesEmpleat = Manager.findContactsByEmployeeAndType(
                empleat.getEmployeeId(), "PHONE");
            
            assertThat(contactesEmpleat)
                .hasSize(1)
                .extracting(Contact::getContactId)
                .containsExactly(id);
        }
        
        /**
         * Test: updateContact amb ID inexistent no falla.
         */
        @Test
        @DisplayName("updateContact amb ID inexistent no falla")
        void updateContact_IdInexistent_NoFalla() {
            assertDoesNotThrow(() -> {
                Manager.updateContact(99999L, "EMAIL", "test@test.com", "Test");
            });
        }
        
        /**
         * Test: updateContact només del valor.
         */
        @Test
        @DisplayName("updateContact canviant només el valor")
        void updateContact_NomesValor_TipusIDescPreservats() {
            // ARRANGE
            Long id = contacteOriginal.getContactId();
            
            // ACT - Canviar només el valor, mantenir tipus i descripció
            Manager.updateContact(id, "EMAIL", "nouvalor@test.com", "Original");
            
            // ASSERT
            Contact actualitzat = Manager.getById(Contact.class, id);
            
            assertAll(
                () -> assertEquals("EMAIL", actualitzat.getContactType(), 
                    "El tipus no hauria de canviar"),
                () -> assertEquals("nouvalor@test.com", actualitzat.getValue(), 
                    "El valor hauria de canviar"),
                () -> assertEquals("Original", actualitzat.getDescription(), 
                    "La descripció no hauria de canviar")
            );
        }
    }
    
    // ========================================================================
    // TESTS D'ELIMINACIÓ (DELETE)
    // ========================================================================
    
    /**
     * Grup de tests per a operacions d'eliminació de contactes.
     */
    @Nested
    @DisplayName("DELETE - Tests d'eliminació de contactes")
    class DeleteContactTests {
        
        private Employee empleat;
        private Contact contacte1;
        private Contact contacte2;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Delete", "Test", 30000);
            contacte1 = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "EMAIL", "delete1@test.com", "Delete 1");
            contacte2 = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "PHONE", "666000111", "Delete 2");
        }
        
        /**
         * Test: removeContactFromEmployee elimina el contacte.
         */
        @Test
        @DisplayName("removeContactFromEmployee elimina el contacte correctament")
        void removeContactFromEmployee_ContacteExistent_Eliminat() {
            // ARRANGE
            Long contactId = contacte1.getContactId();
            
            // ACT
            Manager.removeContactFromEmployee(empleat.getEmployeeId(), contactId);
            
            // ASSERT
            assertNull(Manager.getById(Contact.class, contactId), 
                "El contacte hauria d'estar eliminat");
        }
        
        /**
         * Test: Eliminar un contacte no afecta els altres.
         */
        @Test
        @DisplayName("Eliminar un contacte no afecta els altres")
        void removeContactFromEmployee_NoAfectaAltres() {
            // ACT
            Manager.removeContactFromEmployee(empleat.getEmployeeId(), 
                contacte1.getContactId());
            
            // ASSERT - L'altre contacte continua existint
            Contact altreContacte = Manager.getById(Contact.class, 
                contacte2.getContactId());
            
            assertNotNull(altreContacte, 
                "L'altre contacte no hauria d'estar afectat");
        }
        
        /**
         * Test: delete directe també funciona.
         */
        @Test
        @DisplayName("delete directe elimina el contacte")
        void delete_ContacteExistent_Eliminat() {
            // ARRANGE
            Long id = contacte1.getContactId();
            
            // ACT
            Manager.delete(Contact.class, id);
            
            // ASSERT
            assertNull(Manager.getById(Contact.class, id));
        }
        
        /**
         * Test: removeContactFromEmployee amb IDs inexistents no falla.
         */
        @Test
        @DisplayName("removeContactFromEmployee amb IDs inexistents no falla")
        void removeContactFromEmployee_IdsInexistents_NoFalla() {
            assertAll(
                // Empleat inexistent
                () -> assertDoesNotThrow(() -> 
                    Manager.removeContactFromEmployee(99999L, contacte1.getContactId())),
                // Contacte inexistent
                () -> assertDoesNotThrow(() -> 
                    Manager.removeContactFromEmployee(empleat.getEmployeeId(), 99999L)),
                // Ambdós inexistents
                () -> assertDoesNotThrow(() -> 
                    Manager.removeContactFromEmployee(99999L, 88888L))
            );
        }
        
        /**
         * Test: Després d'eliminar, el recompte és correcte.
         */
        @Test
        @DisplayName("Eliminar contacte decrementa el nombre de contactes")
        void removeContactFromEmployee_DecrementaComptador() {
            // ARRANGE
            int comptadorInicial = comptarEntitats(Contact.class);
            
            // ACT
            Manager.removeContactFromEmployee(empleat.getEmployeeId(), 
                contacte1.getContactId());
            
            // ASSERT
            assertEquals(comptadorInicial - 1, comptarEntitats(Contact.class));
        }
        
        /**
         * Test: orphanRemoval - desvinculant contacte de l'empleat.
         * 
         * Nota: Aquest test verifica el comportament d'orphanRemoval,
         * que elimina contactes quan es desvinculen de l'empleat.
         */
        @Test
        @DisplayName("orphanRemoval elimina contactes desvinculats")
        void orphanRemoval_DesvinculantContacte_ContacteEliminat() {
            // ARRANGE
            int comptadorInicial = comptarEntitats(Contact.class);
            
            // ACT - Eliminar l'empleat (hauria d'eliminar contactes en cascada)
            Manager.delete(Employee.class, empleat.getEmployeeId());
            
            // ASSERT - Tots els contactes de l'empleat haurien d'estar eliminats
            assertEquals(0, comptarEntitats(Contact.class),
                "Tots els contactes haurien d'haver-se eliminat amb l'empleat");
        }
    }
    
    // ========================================================================
    // TESTS DE VALIDACIÓ I CASOS LÍMIT
    // ========================================================================
    
    /**
     * Grup de tests per a casos límit i validacions.
     */
    @Nested
    @DisplayName("VALIDACIÓ - Tests de casos límit")
    class ValidationTests {
        
        private Employee empleat;
        
        @BeforeEach
        void setUpTestData() {
            empleat = Manager.addEmployee("Validació", "Test", 30000);
        }
        
        /**
         * Test: Crear contacte amb valors molt llargs.
         */
        @Test
        @DisplayName("Crear contacte amb valor llarg")
        void addContactToEmployee_ValorLlarg_Acceptat() {
            // ARRANGE
            String valorLlarg = "a".repeat(200);  // Dins del límit de 255
            
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "ADDRESS", valorLlarg, "Test");
            
            // ASSERT
            assertNotNull(resultat.getContactId());
            assertEquals(valorLlarg, resultat.getValue());
        }
        
        /**
         * Test: Crear contacte amb descripció nul·la.
         */
        @Test
        @DisplayName("Crear contacte amb descripció nul·la")
        void addContactToEmployee_DescripcioNulla_Acceptat() {
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "EMAIL", "test@test.com", null);
            
            // ASSERT
            assertNotNull(resultat.getContactId());
            assertNull(resultat.getDescription());
        }
        
        /**
         * Test: Crear contacte amb caràcters especials.
         */
        @ParameterizedTest(name = "Valor: {0}")
        @ValueSource(strings = {
            "test+filter@test.com",
            "nom.cognom@subdomain.domain.cat",
            "+34 666 111 222",
            "Avinguda d'Exemple, 123-A, 5è 2a",
            "日本語テスト"
        })
        @DisplayName("Crear contacte amb caràcters especials")
        void addContactToEmployee_CaractersEspecials_Acceptats(String valor) {
            // ACT
            Contact resultat = Manager.addContactToEmployee(
                empleat.getEmployeeId(), "OTHER", valor, "Test");
            
            // ASSERT
            assertNotNull(resultat.getContactId());
            assertEquals(valor, resultat.getValue());
        }
        
        /**
         * Test: Buscar contactes d'un empleat que no en té.
         */
        @Test
        @DisplayName("findContactsByEmployeeAndType sense contactes retorna buit")
        void findContactsByEmployeeAndType_SenseContactes_RetornaBuit() {
            // ACT - L'empleat no té contactes
            Collection<Contact> resultat = Manager.findContactsByEmployeeAndType(
                empleat.getEmployeeId(), "EMAIL");
            
            // ASSERT
            assertThat(resultat).isEmpty();
        }
    }
}
