/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.rajapinnat.kela;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class KelaGenerator {

    @Autowired
    private WriteOPTILI optiliWriter;
    @Autowired
    private WriteOPTINI optiniWriter;
    @Autowired
    private WriteOPTIOL optiolWriter;
    @Autowired
    private WriteOPTIOP optiopWriter;
    @Autowired
    private WriteOPTITU optituWriter;
    @Autowired
    private WriteOPTIYH optiyhWriter;
    @Autowired
    private WriteOPTIYT optiytWriter;
    @Autowired
    private OrganisaatioContainer orgContainer;

    public void generateKelaFiles() {
        System.out.println("Fetching organisaatiot");
        long time = System.currentTimeMillis();
        orgContainer.fetchOrgnaisaatiot();
        System.out.println("Fetch time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        
        System.out.println("Generating optili");
        time = System.currentTimeMillis();
        writeKelaFile(optiliWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optini");
        writeKelaFile(optiniWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optiol");
        writeKelaFile(optiolWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optiop");
        writeKelaFile(optiopWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optitu");
        writeKelaFile(optituWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optiyh");
        writeKelaFile(optiyhWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        time = System.currentTimeMillis();
        System.out.println("Generating optiyt");
        writeKelaFile(optiytWriter);
        System.out.println("Generation time: " + (System.currentTimeMillis() - time)/1000.0 + " seconds");
        System.out.println("All files generated");
    }
    
    private void writeKelaFile(AbstractOPTIWriter kelaWriter) {
        try {
            kelaWriter.writeFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main (String[] args) {
        final ApplicationContext context = new ClassPathXmlApplicationContext("META-INF/spring/context/bundle-context.xml");
        KelaGenerator kelaGenerator = context.getBean(KelaGenerator.class);
        kelaGenerator.generateKelaFiles();
    }
}
