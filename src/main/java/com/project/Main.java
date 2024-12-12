package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.project.dao.Manager;
import com.project.domain.Employee;
import com.project.domain.Project;

public class Main {

    public static void main(String[] args) {
        // Configuració inicial
        String basePath = System.getProperty("user.dir") + "/data/";
        File dir = new File(basePath);
        if (!dir.exists()) {
            if(!dir.mkdirs()) {
                System.out.println("Error en la creació de la carpeta 'data'");
            }
        }

        Manager.createSessionFactory();

        // Crear empleats desenvolupadors
        Employee refJoan = Manager.addEmployee("Joan", "Garcia", 35000);
        Employee refMarta = Manager.addEmployee("Marta", "Ferrer", 42000);
        Employee refPere = Manager.addEmployee("Pere", "Soler", 38000);
        Employee refLaia = Manager.addEmployee("Laia", "Puig", 45000);

        // Crear dades de contacte per cada empleat
        // Contactes d'en Joan
        Manager.addContactToEmployee(refJoan.getEmployeeId(), 
            "EMAIL", "joan.garcia@empresa.cat", "Email corporatiu");
        Manager.addContactToEmployee(refJoan.getEmployeeId(), 
            "PHONE", "666111222", "Telèfon mòbil");
        Manager.addContactToEmployee(refJoan.getEmployeeId(), 
            "ADDRESS", "Carrer Major 1, Barcelona", "Adreça personal");

        // Contactes de la Marta
        Manager.addContactToEmployee(refMarta.getEmployeeId(), 
            "EMAIL", "marta.ferrer@empresa.cat", "Email corporatiu");
        Manager.addContactToEmployee(refMarta.getEmployeeId(), 
            "EMAIL", "martaf@gmail.com", "Email personal");
        Manager.addContactToEmployee(refMarta.getEmployeeId(), 
            "PHONE", "666333444", "Telèfon mòbil");

        // Contactes d'en Pere
        Manager.addContactToEmployee(refPere.getEmployeeId(), 
            "EMAIL", "pere.soler@empresa.cat", "Email corporatiu");
        Manager.addContactToEmployee(refPere.getEmployeeId(), 
            "PHONE", "666555666", "Telèfon empresa");

        // Contactes de la Laia
        Manager.addContactToEmployee(refLaia.getEmployeeId(), 
            "EMAIL", "laia.puig@empresa.cat", "Email corporatiu");
        Manager.addContactToEmployee(refLaia.getEmployeeId(), 
            "PHONE", "666777888", "Telèfon mòbil");
        Manager.addContactToEmployee(refLaia.getEmployeeId(), 
            "ADDRESS", "Avinguda Diagonal 100, Barcelona", "Adreça oficina");
        
        // Crear projectes
        Project refWebApp = Manager.addProject("Web Corporativa", "Desenvolupament web responsive", "ACTIU");
        Project refAppMovil = Manager.addProject("App Mòbil", "Aplicació Android/iOS", "ACTIU");
        Project refIntranet = Manager.addProject("Intranet", "Portal intern", "PLANIFICAT");

        // Assignar empleats als projectes
        Set<Project> projectesJoan = new HashSet<>();
        projectesJoan.add(refWebApp);
        projectesJoan.add(refIntranet);
        Manager.updateEmployeeProjects(refJoan.getEmployeeId(), projectesJoan);

        Set<Project> projectesMarta = new HashSet<>();
        projectesMarta.add(refWebApp);
        projectesMarta.add(refAppMovil);
        Manager.updateEmployeeProjects(refMarta.getEmployeeId(), projectesMarta);

        Set<Project> projectesPere = new HashSet<>();
        projectesPere.add(refAppMovil);
        Manager.updateEmployeeProjects(refPere.getEmployeeId(), projectesPere);

        Set<Project> projectesLaia = new HashSet<>();
        projectesLaia.add(refAppMovil);
        projectesLaia.add(refIntranet);
        Manager.updateEmployeeProjects(refLaia.getEmployeeId(), projectesLaia);

        // Mostrar l'estat final
        System.out.println("\n=== Empleats i les seves dades de contacte ===");
        System.out.println(Manager.collectionToString(Employee.class, Manager.listCollection(Employee.class)));
        
        System.out.println("\n=== Projectes i els seus participants ===");
        System.out.println(Manager.collectionToString(Project.class, Manager.listCollection(Project.class)));

        // Exemple de cerca d'empleats per tipus de contacte
        System.out.println("\n=== Empleats amb telèfon mòbil ===");
        System.out.println(Manager.findEmployeesByContactType("PHONE"));
        
        // Exemple de cerca d'empleats per projecte
        System.out.println("\n=== Empleats treballant en el projecte 'App Mòbil' ===");
        System.out.println(Manager.findEmployeesByProject(refAppMovil.getProjectId()));

        Manager.close();
    }
}