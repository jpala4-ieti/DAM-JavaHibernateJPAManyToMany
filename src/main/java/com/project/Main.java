package com.project;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

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

        // Crear contactes (caps d'equip)
        Contact refCarla = Manager.addContact("Carla", "carla.vidal@empresa.cat");
        Contact refJordi = Manager.addContact("Jordi", "jordi.marti@empresa.cat");
        
        // Crear projectes
        Project refWebApp = Manager.addProject("Web Corporativa", "Desenvolupament web responsive", "ACTIU");
        Project refAppMovil = Manager.addProject("App Mòbil", "Aplicació Android/iOS", "ACTIU");
        Project refIntranet = Manager.addProject("Intranet", "Portal intern", "PLANIFICAT");

        // Assignar empleats als seus caps d'equip
        Set<Employee> equipCarla = new HashSet<>();
        equipCarla.add(refJoan);
        equipCarla.add(refMarta);
        Manager.updateContact(refCarla.getContactId(), refCarla.getName(), refCarla.getEmail(), equipCarla);

        Set<Employee> equipJordi = new HashSet<>();
        equipJordi.add(refPere);
        equipJordi.add(refLaia);
        Manager.updateContact(refJordi.getContactId(), refJordi.getName(), refJordi.getEmail(), equipJordi);

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
        System.out.println("=== Empleats i els seus projectes ===");
        System.out.println(Manager.collectionToString(Employee.class, Manager.listCollection(Employee.class)));
        
        System.out.println("\n=== Contactes i els seus equips ===");
        System.out.println(Manager.collectionToString(Contact.class, Manager.listCollection(Contact.class)));
        
        System.out.println("\n=== Projectes i els seus participants ===");
        System.out.println(Manager.collectionToString(Project.class, Manager.listCollection(Project.class)));

        Manager.close();
    }
}