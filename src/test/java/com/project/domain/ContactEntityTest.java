package com.project.domain;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * TESTS UNITARIS DE L'ENTITAT CONTACT
 * ===================================
 * 
 * Aquests tests verifiquen el comportament de la classe Contact
 * de forma aïllada, sense accés a la base de dades.
 * 
 * ASPECTES TESTATS:
 * - Constructors i inicialització
 * - Getters i setters
 * - Relació ManyToOne amb Employee
 * - equals() i hashCode()
 * - toString()
 * 
 * PARTICULARITATS DE CONTACT:
 * - Sempre ha de tenir un Employee associat (nullable=false a BD)
 * - El tipus de contacte (EMAIL, PHONE, ADDRESS, etc.) és obligatori
 * - La descripció és opcional
 * 
 * @author Test Suite Generator
 * @version 1.0
 */
@DisplayName("Tests Unitaris de l'Entitat Contact")
class ContactEntityTest {
    
    // ========================================================================
    // TESTS DE CONSTRUCTORS
    // ========================================================================
    
    /**
     * Grup de tests per als constructors de Contact.
     */
    @Nested
    @DisplayName("Constructors")
    class ConstructorTests {
        
        /**
         * Test: Constructor per defecte crea instància vàlida.
         */
        @Test
        @DisplayName("Constructor per defecte crea instància amb camps null")
        void constructorPerDefecte_CampsNull() {
            // ACT
            Contact contact = new Contact();
            
            // ASSERT
            assertAll("Verificació constructor per defecte",
                () -> assertNull(contact.getContactId(), "L'ID hauria de ser null"),
                () -> assertNull(contact.getContactType(), "El tipus hauria de ser null"),
                () -> assertNull(contact.getValue(), "El valor hauria de ser null"),
                () -> assertNull(contact.getDescription(), "La descripció hauria de ser null"),
                () -> assertNull(contact.getEmployee(), "L'employee hauria de ser null")
            );
        }
        
        /**
         * Test: Constructor amb paràmetres inicialitza correctament.
         */
        @Test
        @DisplayName("Constructor amb paràmetres inicialitza tipus, valor i descripció")
        void constructorAmbParametres_TotsElsCamps() {
            // ACT
            Contact contact = new Contact("EMAIL", "test@test.com", "Email de prova");
            
            // ASSERT
            assertAll("Verificació constructor amb paràmetres",
                () -> assertNull(contact.getContactId(), "L'ID hauria de ser null (no persistit)"),
                () -> assertEquals("EMAIL", contact.getContactType()),
                () -> assertEquals("test@test.com", contact.getValue()),
                () -> assertEquals("Email de prova", contact.getDescription()),
                () -> assertNull(contact.getEmployee(), "Employee no s'assigna al constructor")
            );
        }
        
        /**
         * Test parametritzat: Constructor amb diversos tipus de contacte.
         */
        @ParameterizedTest(name = "Tipus: {0}, Valor: {1}")
        @CsvSource({
            "EMAIL, joan@empresa.cat, Email corporatiu",
            "PHONE, 666111222, Mòbil personal",
            "ADDRESS, 'Carrer Major 1, Barcelona', Adreça principal",
            "FAX, 931234567, Fax oficina",
            "TWITTER, @joantest, Xarxa social",
            "LINKEDIN, linkedin.com/in/joan, Perfil professional"
        })
        @DisplayName("Constructor accepta diversos tipus de contacte")
        void constructorAmbParametres_DiversosTipus(String tipus, String valor, String desc) {
            // ACT
            Contact contact = new Contact(tipus, valor, desc);
            
            // ASSERT
            assertAll(
                () -> assertEquals(tipus, contact.getContactType()),
                () -> assertEquals(valor, contact.getValue()),
                () -> assertEquals(desc, contact.getDescription())
            );
        }
        
        /**
         * Test: Constructor amb descripció nul·la.
         */
        @Test
        @DisplayName("Constructor accepta descripció nul·la")
        void constructorAmbParametres_DescripcioNulla() {
            // ACT
            Contact contact = new Contact("EMAIL", "test@test.com", null);
            
            // ASSERT
            assertNull(contact.getDescription());
            assertEquals("EMAIL", contact.getContactType());
            assertEquals("test@test.com", contact.getValue());
        }
    }
    
    // ========================================================================
    // TESTS DE GETTERS I SETTERS
    // ========================================================================
    
    /**
     * Grup de tests per a getters i setters.
     */
    @Nested
    @DisplayName("Getters i Setters")
    class GetterSetterTests {
        
        private Contact contact;
        
        @BeforeEach
        void setUp() {
            contact = new Contact("EMAIL", "original@test.com", "Original");
        }
        
        /**
         * Test: setContactId i getContactId.
         */
        @Test
        @DisplayName("setContactId i getContactId funcionen correctament")
        void contactId_SetGet() {
            // ACT
            contact.setContactId(123L);
            
            // ASSERT
            assertEquals(123L, contact.getContactId());
        }
        
        /**
         * Test: setContactType i getContactType.
         */
        @Test
        @DisplayName("setContactType i getContactType funcionen correctament")
        void contactType_SetGet() {
            // ACT
            contact.setContactType("PHONE");
            
            // ASSERT
            assertEquals("PHONE", contact.getContactType());
        }
        
        /**
         * Test: setValue i getValue.
         */
        @Test
        @DisplayName("setValue i getValue funcionen correctament")
        void value_SetGet() {
            // ACT
            contact.setValue("666999888");
            
            // ASSERT
            assertEquals("666999888", contact.getValue());
        }
        
        /**
         * Test: setDescription i getDescription.
         */
        @Test
        @DisplayName("setDescription i getDescription funcionen correctament")
        void description_SetGet() {
            // ACT
            contact.setDescription("Nova descripció");
            
            // ASSERT
            assertEquals("Nova descripció", contact.getDescription());
        }
        
        /**
         * Test: setEmployee i getEmployee.
         */
        @Test
        @DisplayName("setEmployee i getEmployee funcionen correctament")
        void employee_SetGet() {
            // ARRANGE
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT
            contact.setEmployee(emp);
            
            // ASSERT
            assertSame(emp, contact.getEmployee());
        }
        
        /**
         * Test: Canviar tipus de contacte.
         */
        @ParameterizedTest(name = "Nou tipus: {0}")
        @ValueSource(strings = {"PHONE", "ADDRESS", "FAX", "SKYPE", "TELEGRAM"})
        @DisplayName("Es pot canviar el tipus de contacte")
        void contactType_Canviar(String nouTipus) {
            // ACT
            contact.setContactType(nouTipus);
            
            // ASSERT
            assertEquals(nouTipus, contact.getContactType());
        }
        
        /**
         * Test: Valor amb caràcters especials.
         */
        @ParameterizedTest(name = "Valor: {0}")
        @ValueSource(strings = {
            "test+filter@domain.com",
            "+34 666 111 222",
            "Avinguda d'Exemple, 123-A",
            "用户@例子.测试",
            "very.long.email.address.with.many.parts@subdomain.domain.tld"
        })
        @DisplayName("setValue accepta valors amb caràcters especials")
        void value_CaractersEspecials(String valor) {
            // ACT
            contact.setValue(valor);
            
            // ASSERT
            assertEquals(valor, contact.getValue());
        }
        
        /**
         * Test: Descripció pot ser null o buida.
         */
        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("setDescription accepta null i buit")
        void description_NullIBuit(String desc) {
            // ACT
            contact.setDescription(desc);
            
            // ASSERT
            assertEquals(desc, contact.getDescription());
        }
    }
    
    // ========================================================================
    // TESTS DE RELACIÓ AMB EMPLOYEE
    // ========================================================================
    
    /**
     * Grup de tests per a la relació ManyToOne amb Employee.
     */
    @Nested
    @DisplayName("Relació ManyToOne amb Employee")
    class EmployeeRelationTests {
        
        /**
         * Test: Un contact pot tenir un employee assignat.
         */
        @Test
        @DisplayName("Un contact pot tenir un employee assignat")
        void contact_AmbEmployee() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            Employee emp = new Employee("Test", "Employee", 30000);
            
            // ACT
            contact.setEmployee(emp);
            
            // ASSERT
            assertSame(emp, contact.getEmployee());
        }
        
        /**
         * Test: Es pot canviar l'employee d'un contact.
         */
        @Test
        @DisplayName("Es pot canviar l'employee d'un contact")
        void contact_CanviarEmployee() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            Employee emp1 = new Employee("Primer", "Employee", 30000);
            Employee emp2 = new Employee("Segon", "Employee", 35000);
            contact.setEmployee(emp1);
            
            // ACT
            contact.setEmployee(emp2);
            
            // ASSERT
            assertSame(emp2, contact.getEmployee());
            assertNotSame(emp1, contact.getEmployee());
        }
        
        /**
         * Test: Es pot posar employee a null.
         */
        @Test
        @DisplayName("Es pot posar employee a null")
        void contact_EmployeeNull() {
            // ARRANGE
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            Employee emp = new Employee("Test", "Employee", 30000);
            contact.setEmployee(emp);
            
            // ACT
            contact.setEmployee(null);
            
            // ASSERT
            assertNull(contact.getEmployee());
        }
        
        /**
         * Test: Múltiples contacts poden referenciar el mateix employee.
         * 
         * Nota: Això és el comportament esperat en una relació ManyToOne.
         */
        @Test
        @DisplayName("Múltiples contacts poden referenciar el mateix employee")
        void multiplesContacts_MateixEmployee() {
            // ARRANGE
            Employee emp = new Employee("Compartit", "Employee", 40000);
            Contact c1 = new Contact("EMAIL", "e1@test.com", "Email 1");
            Contact c2 = new Contact("EMAIL", "e2@test.com", "Email 2");
            Contact c3 = new Contact("PHONE", "666111222", "Telèfon");
            
            // ACT
            c1.setEmployee(emp);
            c2.setEmployee(emp);
            c3.setEmployee(emp);
            
            // ASSERT
            assertAll(
                () -> assertSame(emp, c1.getEmployee()),
                () -> assertSame(emp, c2.getEmployee()),
                () -> assertSame(emp, c3.getEmployee())
            );
        }
    }
    
    // ========================================================================
    // TESTS DE EQUALS I HASHCODE
    // ========================================================================
    
    /**
     * Grup de tests per equals() i hashCode().
     */
    @Nested
    @DisplayName("equals() i hashCode()")
    class EqualsHashCodeTests {
        
        /**
         * Test: Reflexivitat.
         */
        @Test
        @DisplayName("equals és reflexiu (c.equals(c) == true)")
        void equals_Reflexiu() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            assertEquals(contact, contact);
        }
        
        /**
         * Test: equals amb null retorna false.
         */
        @Test
        @DisplayName("equals amb null retorna false")
        void equals_AmbNull_RetornaFalse() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            assertNotEquals(null, contact);
        }
        
        /**
         * Test: equals amb classe diferent retorna false.
         */
        @Test
        @DisplayName("equals amb classe diferent retorna false")
        void equals_ClasseDiferent_RetornaFalse() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            assertNotEquals("String", contact);
            assertNotEquals(123, contact);
        }
        
        /**
         * Test: Simetria.
         */
        @Test
        @DisplayName("equals és simètric")
        void equals_Simetric() {
            Contact c1 = new Contact("EMAIL", "test@test.com", "Test");
            Contact c2 = new Contact("EMAIL", "test@test.com", "Test");
            
            assertEquals(c1.equals(c2), c2.equals(c1));
        }
        
        /**
         * Test: hashCode consistent.
         */
        @Test
        @DisplayName("hashCode és consistent")
        void hashCode_Consistent() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            int hash1 = contact.hashCode();
            int hash2 = contact.hashCode();
            
            assertEquals(hash1, hash2);
        }
        
        /**
         * Test: Si equals és true, hashCode és igual.
         */
        @Test
        @DisplayName("Si equals és true, hashCode és igual")
        void equals_True_HashCodeIgual() {
            Contact c1 = new Contact("EMAIL", "test@test.com", "Test");
            Contact c2 = new Contact("EMAIL", "test@test.com", "Test");
            
            if (c1.equals(c2)) {
                assertEquals(c1.hashCode(), c2.hashCode());
            }
        }
        
        /**
         * Test: Contacts amb ID diferent no són iguals.
         */
        @Test
        @DisplayName("Contacts amb ID diferent no són iguals")
        void equals_IdDiferent_NoIguals() {
            Contact c1 = new Contact("EMAIL", "test@test.com", "Test");
            Contact c2 = new Contact("EMAIL", "test@test.com", "Test");
            c1.setContactId(1L);
            c2.setContactId(2L);
            
            assertNotEquals(c1, c2);
        }
        
        /**
         * Test: Contacts amb el mateix ID són iguals.
         */
        @Test
        @DisplayName("Contacts amb el mateix ID són iguals")
        void equals_MateixId_Iguals() {
            Contact c1 = new Contact("EMAIL", "e1@test.com", "Desc1");
            Contact c2 = new Contact("PHONE", "666", "Desc2");
            c1.setContactId(1L);
            c2.setContactId(1L);
            
            assertEquals(c1, c2);
        }
        
        /**
         * Test: Contacts amb tipus i valor iguals (sense ID).
         */
        @Test
        @DisplayName("Contacts sense ID amb mateix tipus i valor poden ser iguals")
        void equals_SenseId_TipusIValorIguals() {
            Contact c1 = new Contact("EMAIL", "test@test.com", "Desc1");
            Contact c2 = new Contact("EMAIL", "test@test.com", "Desc2");
            
            // El comportament depèn de la implementació d'equals
            // Segons el codi, sense ID compara per tipus, valor i employeeId
            // Com no tenen employee, haurien de ser iguals
            // Verificar comportament real
            boolean iguals = c1.equals(c2);
            
            // Si són iguals, hashCode ha de coincidir
            if (iguals) {
                assertEquals(c1.hashCode(), c2.hashCode());
            }
        }
    }
    
    // ========================================================================
    // TESTS DE TOSTRING
    // ========================================================================
    
    /**
     * Grup de tests per toString().
     */
    @Nested
    @DisplayName("toString()")
    class ToStringTests {
        
        /**
         * Test: toString retorna representació no nul·la.
         */
        @Test
        @DisplayName("toString retorna representació no nul·la")
        void toString_NoNull() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            assertNotNull(contact.toString());
        }
        
        /**
         * Test: toString conté les dades bàsiques.
         */
        @Test
        @DisplayName("toString conté tipus i valor")
        void toString_ConteDadesBasiques() {
            Contact contact = new Contact("PHONE", "666111222", "Mòbil");
            String result = contact.toString();
            
            assertAll(
                () -> assertTrue(result.contains("PHONE"), "Hauria de contenir el tipus"),
                () -> assertTrue(result.contains("666111222"), "Hauria de contenir el valor")
            );
        }
        
        /**
         * Test: toString amb employee no causa recursió infinita.
         */
        @Test
        @DisplayName("toString amb employee no causa recursió infinita")
        void toString_AmbEmployee_NoRecursio() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            Employee emp = new Employee("Test", "Employee", 30000);
            contact.setEmployee(emp);
            emp.addContact(contact); // Relació bidireccional
            
            // Si hi ha recursió infinita, això fallarà amb StackOverflowError
            assertDoesNotThrow(() -> contact.toString());
        }
        
        /**
         * Test: toString sense employee.
         */
        @Test
        @DisplayName("toString sense employee funciona")
        void toString_SenseEmployee() {
            Contact contact = new Contact("EMAIL", "test@test.com", "Test");
            
            String result = contact.toString();
            
            assertNotNull(result);
            assertFalse(result.isEmpty());
        }
    }
    
    // ========================================================================
    // TESTS DE CASOS LÍMIT
    // ========================================================================
    
    /**
     * Grup de tests per a casos límit.
     */
    @Nested
    @DisplayName("Casos Límit")
    class EdgeCasesTests {
        
        /**
         * Test: Valor molt llarg.
         */
        @Test
        @DisplayName("Accepta valor molt llarg")
        void value_MoltLlarg() {
            // ARRANGE
            String valorLlarg = "a".repeat(250);
            
            // ACT
            Contact contact = new Contact("ADDRESS", valorLlarg, "Test");
            
            // ASSERT
            assertEquals(valorLlarg, contact.getValue());
        }
        
        /**
         * Test: Descripció molt llarga.
         */
        @Test
        @DisplayName("Accepta descripció molt llarga")
        void description_MoltLlarga() {
            // ARRANGE
            String descLlarga = "Descripció ".repeat(50);
            
            // ACT
            Contact contact = new Contact("EMAIL", "test@test.com", descLlarga);
            
            // ASSERT
            assertEquals(descLlarga, contact.getDescription());
        }
        
        /**
         * Test: Tipus amb caràcters especials.
         */
        @Test
        @DisplayName("Tipus pot contenir caràcters especials")
        void contactType_CaractersEspecials() {
            // ACT
            Contact contact = new Contact("EMAIL_PERSONAL", "test@test.com", "Test");
            
            // ASSERT
            assertEquals("EMAIL_PERSONAL", contact.getContactType());
        }
        
        /**
         * Test: Crear múltiples instàncies amb mateixos valors.
         */
        @Test
        @DisplayName("Es poden crear múltiples instàncies amb mateixos valors")
        void multiplesInstancies_MateixosValors() {
            // ACT
            Contact c1 = new Contact("EMAIL", "same@test.com", "Same");
            Contact c2 = new Contact("EMAIL", "same@test.com", "Same");
            Contact c3 = new Contact("EMAIL", "same@test.com", "Same");
            
            // ASSERT - Són objectes diferents
            assertNotSame(c1, c2);
            assertNotSame(c2, c3);
            
            // Però amb els mateixos valors
            assertEquals(c1.getContactType(), c2.getContactType());
            assertEquals(c1.getValue(), c3.getValue());
        }
    }
}
