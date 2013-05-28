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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTIYH extends AbstractOPTIWriter {

private static final String OPTIYH = ".OPTIYH";
    
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    
    public WriteOPTIYH() {
        super();
        this.createFileName("", OPTIYH);
    }
    
    public WriteOPTIYH(String path) {
        super();
        this.createFileName(path, OPTIYH);
    }
    
    @Override
    public void writeFile() throws IOException {
        
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

}
