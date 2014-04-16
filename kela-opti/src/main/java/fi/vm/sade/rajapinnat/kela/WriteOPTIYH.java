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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatiosuhde;

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
    }
    
    @Override
    public void writeFile() throws IOException {
        this.createFileName("", OPTIYH);
        bos = new BufferedOutputStream(new FileOutputStream(new File(fileName)));
        bos.write(toLatin1(ALKUTIETUE));
        List<Organisaatiosuhde> liitokset = this.kelaDAO.findAllLiitokset();
        if (liitokset != null) {
            for (Organisaatiosuhde curLiitos : liitokset) {
                try {
                if (!StringUtils.isEmpty(curLiitos.getParent().getOppilaitoskoodi()) 
                        &&  !StringUtils.isEmpty(curLiitos.getChild().getOppilaitoskoodi())) {
                    bos.write(toLatin1(createRecord(curLiitos)));   
                    bos.flush();
                }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private String createRecord(Organisaatiosuhde liitos) {
        String record = String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s",//12 fields + EOL
                getLiitosId(liitos),//YHD_ID
                StringUtils.leftPad("", 10),//KAS_ID
                StringUtils.leftPad("", 10),//ALA_ID
                StringUtils.leftPad("", 5),//OPE_OPPILNRO
                StringUtils.leftPad("", 2),//OPE_OPJNO
                getOppilaitosnumero(liitos.getChild()), //OPPILNRO
                StringUtils.leftPad("", 60),//Suomenkielinen selite
                getDateStrOrDefault(liitos.getAlkuPvm()),//Oppilaitoksen yhdistamispaiva
                getOppilaitosnumero(liitos.getParent()), //KOHDE_ONRO
                StringUtils.leftPad("", 2),//KOHDE_OPJNO
                DEFAULT_DATE,//Viimeisin paivityspvm
                StringUtils.leftPad("", 30),//Viimeisin paivittaja
                "\n");
        return record;
    }
    

    private String getOppilaitosnumero(Organisaatio parent) {
        return StringUtils.leftPad(parent.getOppilaitoskoodi(), 5, '0');
    }

    private String getLiitosId(Organisaatiosuhde liitos) {
        return StringUtils.leftPad(String.format("%s", liitos.getId()), 10, '0');
    }

}
