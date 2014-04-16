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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ContextConfiguration(locations = "classpath:spring/test-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTIYHTest {
    
    private static final String ALKUTIETUE = "0000000000ALKU";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????";
    
    @Autowired
    private WriteOPTIYH optiyhWriter;
    
    private static final String GEN_PATH = "target/ftps";
   @Test
   public void testWriteOptiyhHappyPath() {
       /*try {
           optiyhWriter.setPath(GEN_PATH);
           optiyhWriter.writeFile();
           
           FileInputStream fstream = new FileInputStream(optiyhWriter.getFileName());
           DataInputStream in = new DataInputStream(fstream);
           BufferedReader br = new BufferedReader(new InputStreamReader(in));
           String strLine;
           
           int lineCount = 0;
           while ((strLine = br.readLine()) != null)   {
               if (lineCount == 0) {
                   assertTrue(strLine.contains(ALKUTIETUE));
               } 
               if (lineCount == 1) {
                   assertTrue(strLine.contains(LOPPUTIETUE));
               }
               if (lineCount > 1) {
                   fail();
               }
               ++lineCount;
           }
           assertTrue(lineCount == 2);
           
           in.close();
           
       } catch (Exception ex) {
           ex.printStackTrace();
           fail();
       }*/
       
   }

}
