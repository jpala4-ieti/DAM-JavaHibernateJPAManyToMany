# Exemple Hibernate JPA Many To Many (Gestió d'Empleats i Projectes)

Aquest projecte és un exemple complet d'implementació de relacions JPA amb Hibernate, incloent relacions **One-to-Many** i **Many-to-Many** bidireccionals. Serveix com a referència per entendre els conceptes clau de persistència amb JPA.

---

## Organització del projecte

### `com.project.domain`
Conté les **entitats JPA** que representen els objectes del domini de negoci.

| Entitat | Descripció | Relacions |
|---------|------------|-----------|
| **Employee** | Empleats de l'empresa (nom, cognom, salari) | `1:N` amb Contact, `N:M` amb Project |
| **Contact** | Dades de contacte (email, telèfon, adreça) | `N:1` amb Employee |
| **Project** | Projectes de l'empresa (nom, descripció, estat) | `N:M` amb Employee |

### `com.project.dao` (Data Access Object)
Conté la classe **`Manager`** que gestiona tot l'accés a dades:
- Creació i configuració de la `SessionFactory`
- Operacions CRUD per a cada entitat
- Consultes HQL i SQL natives
- Mètodes d'utilitat per formatejar resultats

### `com.project.sqliteutils`
Utilitats addicionals per inspeccionar la base de dades SQLite directament sense passar per Hibernate. Útil per verificar que les taules i relacions s'han creat correctament.

---

## Diagrama de Relacions (Entitats)

```
┌─────────────────┐       ┌─────────────────┐       ┌─────────────────┐
│    Employee     │       │     Contact     │       │     Project     │
├─────────────────┤       ├─────────────────┤       ├─────────────────┤
│ employeeId (PK) │──┐    │ contactId (PK)  │       │ projectId (PK)  │
│ firstName       │  │    │ contactType     │       │ name            │
│ lastName        │  │    │ value           │       │ description     │
│ salary          │  │    │ description     │       │ status          │
│                 │  │    │ employee_id(FK) │◄──────│                 │
│ contacts (Set)  │──┼───►│                 │       │ employees (Set) │
│ projects (Set)  │──┼────┼─────────────────┼───────│                 │
└─────────────────┘  │    └─────────────────┘       └─────────────────┘
                     │                                      │
                     │    ┌─────────────────┐               │
                     │    │ employee_project│               │
                     │    │  (Taula Pont)   │               │
                     │    ├─────────────────┤               │
                     └───►│ employee_id(FK) │               │
                          │ project_id (FK) │◄──────────────┘
                          └─────────────────┘
```

### Tipus de Relacions

| Relació | Tipus | Propietari | Descripció |
|---------|-------|------------|------------|
| Employee ↔ Contact | **OneToMany / ManyToOne** | Contact (té la FK) | Un empleat pot tenir múltiples contactes. Cada contacte pertany a un únic empleat. |
| Employee ↔ Project | **ManyToMany** | Employee (té @JoinTable) | Un empleat pot treballar en múltiples projectes. Un projecte pot tenir múltiples empleats. |

---

## Conceptes Clau Implementats

### 1. Anotacions JPA Principals
- `@Entity` - Marca una classe com a entitat persistible
- `@Table` - Configura el nom de la taula
- `@Id` i `@GeneratedValue` - Clau primària amb generació automàtica
- `@Column` - Configuració de columnes (nullable, length, etc.)

### 2. Relacions
- `@OneToMany` / `@ManyToOne` - Relació un a molts
- `@ManyToMany` - Relació molts a molts
- `@JoinColumn` - Columna de clau forana
- `@JoinTable` - Taula intermèdia per a relacions N:M

### 3. Atributs de Relació
- **`mappedBy`**: Indica el costat invers (no propietari) de la relació
- **`cascade`**: Operacions que es propaguen a les entitats relacionades
- **`fetch`**: Estratègia de càrrega (LAZY vs EAGER)
- **`orphanRemoval`**: Elimina entitats òrfenes automàticament

### 4. Consistència Bidireccional
Els mètodes helper (`addContact()`, `removeProject()`, etc.) mantenen sincronitzats ambdós costats de les relacions bidireccionals.

---

## Compilació i Execució

Cal tenir **Maven** instal·lat per compilar el projecte.

### Neteja i compilació bàsica
```bash
mvn clean          # Neteja fitxers generats
mvn compile        # Compila el projecte
mvn test           # Executa els tests (si n'hi ha)
mvn package        # Genera el JAR
```

### Execució amb script (Recomanat)
Aquests scripts configuren automàticament les opcions de Java necessàries (`--add-opens`).

**Windows (PowerShell):**
```bash
.\run.ps1 com.project.Main
```

**Linux/macOS:**
```bash
./run.sh com.project.Main
```

### Altres mètodes d'execució

#### 1. Execució amb Maven (mvn exec)
```bash
mvn exec:java -q "-Dexec.mainClass=com.project.Main"
```

Per executar la utilitat SQLite i veure les taules creades:
```bash
mvn exec:java -q "-Dexec.mainClass=com.project.sqliteutils.MainSQLite"
```

#### 2. Execució directa amb Java (classpath)
Primer, prepara les dependències:
```bash
mvn clean package dependency:copy-dependencies
```

Després, executa:
```bash
# Windows
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -cp "target/classes;target/dependency/*" com.project.Main

# Linux/macOS
java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.nio=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED -cp "target/classes:target/dependency/*" com.project.Main
```

---

## Base de Dades

### SQLite (Per defecte)
El projecte utilitza SQLite com a base de dades.
- **Ubicació del fitxer:** `./data/database.db`
- **Creació automàtica:** Hibernate crea les taules automàticament (`hibernate.hbm2ddl.auto=create`)
- **No requereix instal·lació:** SQLite és una BD embeguda

### Configuració (hibernate.properties)
```properties
# Driver i connexió
hibernate.connection.driver_class=org.sqlite.JDBC
hibernate.connection.url=jdbc:sqlite:./data/database.db

# Dialecte SQLite
hibernate.dialect=org.hibernate.community.dialect.SQLiteDialect

# Mostrar SQL generat (útil per depuració)
hibernate.show_sql=false

# Estratègia de generació de l'esquema
# create: Recrea les taules cada execució (pèrdua de dades!)
# update: Actualitza l'esquema mantenint dades
# validate: Només valida, no modifica
hibernate.hbm2ddl.auto=create
```

---

## Estructura de Fitxers

```
jpala4-ieti-dam-javahibernatejpamanytomany/
├── pom.xml                           # Configuració Maven
├── run.ps1                           # Script execució Windows
├── run.sh                            # Script execució Linux/macOS
├── data/
│   └── database.db                   # BD SQLite (generada automàticament)
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── project/
        │           ├── Main.java             # Punt d'entrada principal
        │           ├── dao/
        │           │   └── Manager.java      # Gestor d'accés a dades
        │           ├── domain/
        │           │   ├── Contact.java      # Entitat Contact
        │           │   ├── Employee.java     # Entitat Employee
        │           │   └── Project.java      # Entitat Project
        │           └── sqliteutils/
        │               ├── MainSQLite.java   # Utilitat per inspeccionar BD
        │               └── UtilsSQLite.java  # Funcions auxiliars SQLite
        └── resources/
            ├── hibernate.properties          # Configuració Hibernate
            └── logback.xml                   # Configuració logging
```

---

## Guia Ràpida de les Entitats

### Employee.java
```java
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;
    
    private String firstName;
    private String lastName;
    private int salary;
    
    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Contact> contacts = new HashSet<>();
    
    @ManyToMany
    @JoinTable(name = "employee_project",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "project_id"))
    private Set<Project> projects = new HashSet<>();
}
```

### Contact.java
```java
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contactId;
    
    private String contactType;  // EMAIL, PHONE, ADDRESS
    private String value;
    private String description;
    
    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
```

### Project.java
```java
@Entity
@Table(name = "projects")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long projectId;
    
    private String name;
    private String description;
    private String status;  // ACTIU, COMPLETAT, PLANIFICAT
    
    @ManyToMany(mappedBy = "projects")
    private Set<Employee> employees = new HashSet<>();
}
```

---

## Operacions del Manager

### Operacions CRUD bàsiques
| Mètode | Descripció |
|--------|------------|
| `addEmployee(firstName, lastName, salary)` | Crea un nou empleat |
| `addProject(name, description, status)` | Crea un nou projecte |
| `addContactToEmployee(empId, type, value, desc)` | Afegeix contacte a empleat |
| `updateEmployee(id, firstName, lastName, salary)` | Actualitza dades d'empleat |
| `updateEmployeeProjects(empId, projectsSet)` | Assigna projectes a empleat |
| `delete(Class, id)` | Elimina qualsevol entitat |
| `getById(Class, id)` | Obté entitat per ID |

### Consultes
| Mètode | Descripció |
|--------|------------|
| `findEmployeesByContactType(type)` | Cerca empleats per tipus de contacte |
| `findEmployeesByProject(projectId)` | Cerca empleats d'un projecte |
| `findContactsByEmployeeAndType(empId, type)` | Cerca contactes d'empleat per tipus |
| `listCollection(Class)` | Llista totes les entitats d'un tipus |
| `listCollection(Class, where)` | Llista amb filtre HQL |

---

## Exemple de Sortida

```
=== Empleats i les seves dades de contacte ===
Employee[id=1, name='Joan Garcia', salary=35000, contacts={EMAIL: joan.garcia@empresa.cat, PHONE: 666111222, ADDRESS: Carrer Major 1, Barcelona}, projects={Web Corporativa, Intranet}]
Employee[id=2, name='Marta Ferrer', salary=42000, contacts={EMAIL: marta.ferrer@empresa.cat, EMAIL: martaf@gmail.com, PHONE: 666333444}, projects={Web Corporativa, App Mòbil}]
...

=== Projectes i els seus participants ===
Project[id=1, name='Web Corporativa', status='ACTIU', employees={Joan Garcia, Marta Ferrer}]
Project[id=2, name='App Mòbil', status='ACTIU', employees={Marta Ferrer, Pere Soler, Laia Puig}]
...

=== Empleats amb telèfon mòbil ===
[Employee[id=1, name='Joan Garcia'...], Employee[id=2, name='Marta Ferrer'...], ...]
```

---

## Errors Comuns i Solucions

| Error | Causa | Solució |
|-------|-------|---------|
| `LazyInitializationException` | Accedir a col·lecció LAZY fora de sessió | Usar `FetchType.EAGER` o `JOIN FETCH` en consultes |
| `TransientObjectException` | Persistir entitat amb referència a objecte no persistit | Assegurar que l'objecte relacionat ja està persistit o usar `cascade` |
| `StackOverflowError` a `toString()` | Recursió infinita en relacions bidireccionals | No cridar `toString()` de l'entitat relacionada |
| Taula pont no es crea | Falta `@JoinTable` o `mappedBy` incorrecte | Revisar que el propietari tingui `@JoinTable` |

---

## Recursos Addicionals

- [Documentació oficial Hibernate](https://hibernate.org/orm/documentation/)
- [Guia JPA de Jakarta EE](https://jakarta.ee/specifications/persistence/)
- [Tutorial Baeldung Hibernate](https://www.baeldung.com/hibernate-5-spring)

---

## Llicència

Aquest projecte està sota la llicència **GNU General Public License v3.0**. Consulta el fitxer `LICENSE` per més detalls.