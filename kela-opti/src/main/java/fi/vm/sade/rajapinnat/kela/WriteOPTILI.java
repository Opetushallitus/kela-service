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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.search.HakukohdePerustieto;
import fi.vm.sade.tarjonta.service.search.HakukohteetKysely;
import fi.vm.sade.tarjonta.service.search.HakukohteetVastaus;
import fi.vm.sade.tarjonta.service.search.KoulutuksetKysely;
import fi.vm.sade.tarjonta.service.search.KoulutuksetVastaus;
import fi.vm.sade.tarjonta.service.search.KoulutusPerustieto;
import fi.vm.sade.koodisto.service.types.common.KoodiType;

/**
 * 
 * @author Markus
 */
@Component
@Configurable
public class WriteOPTILI extends AbstractOPTIWriter {
    
    private static final Logger LOG = LoggerFactory.getLogger(KelaGenerator.class);
    
    private static final String OPTILI = ".OPTILI";
    private static final String ALKUTIETUE = "0000000000ALKU\n";
    private static final String LOPPUTIETUE = "9999999999LOPPU??????\n";
    private static final String KOULUTUSLAJI = "N ";
    
    public WriteOPTILI() {
        super();
    }
    
    @Override
    public void writeFile() throws IOException {
        createFileName("", OPTILI);
        bos = new BufferedOutputStream(new FileOutputStream(new File(getFileName())));
        bos.write(toLatin1(ALKUTIETUE));
        HakukohteetKysely kysely = new HakukohteetKysely();
        HakukohteetVastaus vastaus = tarjontaSearchService.haeHakukohteet(kysely);
        for (HakukohdePerustieto curTulos : vastaus.getHakukohteet()) {
            try {
                String tarjoajaOid = curTulos.getTarjoajaOid();//getHakukohde().getTarjoaja().getTarjoajaOid();
                System.out.println("TarjoajaOid: " + tarjoajaOid);
                OrganisaatioDTO organisaatioDTO = this.organisaatioService.findByOid(tarjoajaOid);
                if (isHakukohdeToinenaste(tarjoajaOid)) {   
                    bos.write(toLatin1(createRecord(curTulos, organisaatioDTO)));
                    bos.flush();
                
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        bos.write(toLatin1(LOPPUTIETUE));
        bos.flush();
        bos.close();
    }

    private boolean isHakukohdeToinenaste(String tarjoajaOid) {
        return this.orgContainer.getOrgOidList().contains(tarjoajaOid);
    }

    private String createRecord(HakukohdePerustieto curTulos, OrganisaatioDTO organisaatioDTO) {
        return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //51 fields + line ending
                getHakukohdeId(curTulos),//Sisainen koodi
                getOppilaitosnumero(curTulos, organisaatioDTO),//OPPIL_NRO
                getOpetuspisteenJarjNro(curTulos, organisaatioDTO),//OPJNO
                StringUtils.leftPad("",12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                StringUtils.leftPad("",2),//Opintolinjan kieli
                StringUtils.leftPad("",2),//OPL_LASPER
                StringUtils.leftPad("",2),//Valintakoeryhma
                StringUtils.leftPad("",10),//Kokeilun yksiloiva tunniste
                StringUtils.leftPad("",5), //OPL_SUUNT
                getTutkintotunniste(curTulos),//TUT_ID 
                getHakukohdeKoodi(curTulos),//YHLINJA
                StringUtils.leftPad("",3), //OPL_OTTOALUE
                StringUtils.leftPad("",3), //Opetusmuoto
                StringUtils.leftPad("",10), //Pohjakoulutusvaat. yksiloiva tunniste
                StringUtils.leftPad("",2), //Koulutustyyppi
                StringUtils.leftPad("",3), //OPL-EKASITTELY
                StringUtils.leftPad("",2), //Koul. rahoitusmuoto
                StringUtils.leftPad("",2), //OPL_PKR_TUNNUS
                StringUtils.leftPad("",2), //OPL_VKOEJAR
                StringUtils.leftPad("",2), //OPL_VKOEKUT
                StringUtils.leftPad("",7), //Alinkeskiarvo
                StringUtils.leftPad("",12), //Koulutuksen kesto
                StringUtils.leftPad("",3), //OPL_KKERROIN
                StringUtils.leftPad("",10), //Keston yksikko
                getAlkupvm(), //Alkupaiva, voimassaolon alku
                DEFAULT_DATE,  //Loppupaiva
                DEFAULT_DATE, //Viimeisin paivityspaiva
                StringUtils.leftPad("",30), //Viimeisin paivittaja
                StringUtils.leftPad("",7), //Opiskelijamaksu
                StringUtils.leftPad("",7), //Lahiopetusta
                StringUtils.leftPad("",6), //OPL_LAHIOP.YKS
                StringUtils.leftPad("",2), //OPL_ERITYISVAL
                StringUtils.leftPad("",12), //OPL_KOULUTUS-AIKA
                StringUtils.leftPad("",6), //OPL_KOULUTUSAI-KAYKSIKKO
                StringUtils.leftPad("",6), //El-harpis
                getAlkamiskausi(curTulos), //ALKUKAUSI
                getTila(), //TILA
                getVuosi(curTulos), //VUOSI 
                DEFAULT_DATE, //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva, voimassaolon loppu
                StringUtils.leftPad("",1), //OPL_TULOSTUS
                StringUtils.leftPad("",15), //OPL_OMISTAJA
                StringUtils.leftPad("",3), //OPL_KUNTA
                StringUtils.leftPad("",3), //OPL_PAINOALUE
                StringUtils.leftPad("",1), //OPL_TYOHONSIJOITUSSEUR
                StringUtils.leftPad("",1), //OPL_LYHYTKESTO
                StringUtils.leftPad("",2), //OPL_MAKSUKER.
                StringUtils.leftPad("",10), //OPL_SOSIAALISETEDUT
                StringUtils.leftPad("",10), //OPL_SOPIMUSNRO
                StringUtils.leftPad("",14), //Tyhjaa
                "\n");
        
    }
    
    private String getVuosi(HakukohdePerustieto curTulos) {
    	if (null != curTulos && null != curTulos.getKoulutuksenAlkamisvuosi()){
    		return curTulos.getKoulutuksenAlkamisvuosi().toString();
    	}
    	return null;
    }

    private String getTila() {
        return "PAAT";
    }

    private String getAlkamiskausi(HakukohdePerustieto curTulos) {
        String kausi = curTulos.getKoulutuksenAlkamiskausi().getUri();
        kausi = kausi.substring(0, 1);
        return StringUtils.rightPad(kausi, 12);
    }

    private String getAlkupvm() {
        return DEFAULT_DATE;
    }
    
    private Object getHakukohdeKoodi(HakukohdePerustieto curTulos) {
        String koodiUri = curTulos.getKoodistoNimi();
        List<KoodiType> koodis = getKoodisByUriAndVersio(koodiUri);
        return (koodis.isEmpty()) ? StringUtils.leftPad("", 3) : koodis.get(0).getKoodiArvo();
    }
    
    

    private Object getTutkintotunniste(HakukohdePerustieto curTulos) {
        LOG.debug("HaeTutkintotunniste: " + curTulos.getOid());
        KoulutuksetKysely kysely = new KoulutuksetKysely();
        kysely.getHakukohdeOids().add(curTulos.getOid());
        KoulutuksetVastaus vastaus = tarjontaSearchService.haeKoulutukset(kysely);
        LOG.debug("Koulutustulos size: " + vastaus.getKoulutukset().size());
        if (vastaus == null || vastaus.getKoulutukset().isEmpty()) {
            LOG.warn("\n\n!!!Tutkintotunniste empty for hakukohde: " + curTulos.getOid() + "\n\n");
            StringUtils.leftPad("", 10);
        }
        KoulutusPerustieto tulos = vastaus.getKoulutukset().get(0);
        String koodiUri = tulos.getKoulutuskoodi().getUri();
        List<KoodiType> koodis = this.getKoodisByUriAndVersio(koodiUri);        
        KoodiType koulutuskoodi = null;
        if (!koodis.isEmpty()) {
            koulutuskoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(koulutuskoodi, kelaTutkintokoodisto);
        return (kelaKoodi == null) ? StringUtils.leftPad("", 10) : StringUtils.leftPad(kelaKoodi.getKoodiArvo(), 10);
    }

    private String getKoulutuslaji() {
        return KOULUTUSLAJI;
    }

    private String getOpetuspisteenJarjNro(HakukohdePerustieto curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatio.getOpetuspisteenJarjNro());
        } 
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            Organisaatio organisaatioE = kelaDAO.findFirstChildOrganisaatio(curTulos.getTarjoajaOid());
            return (organisaatioE != null && organisaatioE.getOpetuspisteenJarjNro() != null) ? organisaatioE.getOpetuspisteenJarjNro() : "01";
        }
        return "01";
    }

    private String getOppilaitosnumero(HakukohdePerustieto curTulos, OrganisaatioDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return String.format("%s", organisaatio.getOppilaitosKoodi());
        } else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPETUSPISTE)) {
            return String.format("%s", organisaatioService.findByOid(organisaatio.getParentOid()).getOppilaitosKoodi());
        }
        return StringUtils.leftPad("", 5);
    }

    private String getHakukohdeId(HakukohdePerustieto curTulos) {
        Hakukohde hakukE = kelaDAO.findHakukohdeByOid(curTulos.getOid());
        String hakukohdeId = String.format("%s", hakukE.getId());
        return StringUtils.leftPad(hakukohdeId, 10, '0');
    }

	@Override
	public void composeRecords() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String composeRecord(Object... args) throws OPTFormatException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAlkutietue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLopputietue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilenameSuffix() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPath() {
		// TODO Auto-generated method stub
		return null;
	}
}
