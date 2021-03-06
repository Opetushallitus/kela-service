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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import fi.vm.sade.organisaatio.resource.dto.OrganisaatioRDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KelaHakukohdeV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.KelaHakukohteetV1RDTO;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.types.TarjontaTila;

@Component
@Configurable
public class WriteOPTILI extends AbstractOPTIWriter {

    private static final Logger LOG = LoggerFactory.getLogger(KelaGenerator.class);

    private String FILEIDENTIFIER;
    private String ALKUTIETUE;
    private String LOPPUTIETUE;
    private String KOULUTUSLAJI;

    private final static String[] errors = {
        "could not write hakukohde %s, tarjoaja %s : invalid values.",
        "hakukohde %s not found in DB although found in index.",
        "incorrect OID : '%s'",
        "OID cannot not be null",
        "hakukohde %s has no koulutukset",
        "invalid OID: '%s'",
        "komotoOID cannot not be null",
        "OPPIL_NRO may not be missing (org.oid=%s).",
        "HAK_NIMI may not be missing (org.oid=%s).",
        "duplicate oid suffix detected (%s oid %s).",
        "no hakukohde for OID : %s (%s)",
        "komo %s has no koulutus_uri"
    };

    private final static String[] warnings = {
        "Toimipisteen opetuspisteenjnro is empty : org.oid=%s",
        "komoto-oid converted from %s to %s (hakukohde: %s)",
        "Hakukohde %s has invalid %s"
    };

    private final static String[] infos = {
        "fetched %s hakukohde from index."
    };

    public WriteOPTILI() {
        super();
    }

    private boolean isHakukohdeOppilaitos(String tarjoajaOid) {
        return this.orgContainer.getOrgOidList().contains(tarjoajaOid);
    }

    private String getTila() {
        return "PAAT";
    }

    private String getAlkamiskausi(String kausi) {
        if (null != kausi && kausi.length() >= 1 && kausi.startsWith("kausi_")) {
            kausi = kausi.substring(6, 7).toUpperCase(); //S or K
            return StringUtils.rightPad(kausi, 12);
        }
        return StringUtils.rightPad("", 12);
    }

    private String getAlkupvm() {
        return DEFAULT_DATE;
    }

    private Object getTutkintotunniste(Koulutusmoduuli koulutusmoduuli) throws OPTFormatException {
        if (koulutusmoduuli == null) {
            error(8);
        }

        if (koulutusmoduuli.getKoulutusUri() == null) {
            error(12, koulutusmoduuli.getOid());
        }

        return getTutkintotunniste(koulutusmoduuli.getKoulutusUri(), " koulutusmoduuli-oid: " + koulutusmoduuli.getOid());
    }

    private String getKoulutuslaji() {
        return KOULUTUSLAJI;
    }

    private String getOpetuspisteenJarjNro(OrganisaatioRDTO organisaatio) {
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE.value())) {
            if (StringUtils.isEmpty(organisaatio.getOpetuspisteenJarjNro())) {
                warn(1, organisaatio.getOid());
                return "  ";
            } else {
                return organisaatio.getOpetuspisteenJarjNro();
            }
        }
        if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS.value())) {
            Organisaatio organisaatioE = kelaDAO.findFirstChildOrganisaatio(organisaatio.getOid());
            return (organisaatioE != null && !StringUtils.isEmpty(organisaatioE.getOpetuspisteenJarjNro().trim())) ? organisaatioE.getOpetuspisteenJarjNro() : "01";
        }
        return "01";
    }

    private String getOppilaitosnumero(OrganisaatioRDTO organisaatio) throws OPTFormatException {
        String oppil_nro = null;
        oppil_nro = getOppilaitosNro(organisaatio);
        if (oppil_nro == null || StringUtils.isEmpty(oppil_nro.trim())) {
            error(8, organisaatio.getOid() + " " + organisaatio.getNimi());
        }
        return strFormatter(oppil_nro, 5, "OPPIL_NRO");
    }

    private Hakukohde getHakukohde(KelaHakukohdeV1RDTO curTulos) throws OPTFormatException {
        Hakukohde hk = kelaDAO.findHakukohdeByOid(curTulos.getOid());

        if (hk == null) {
            error(2, curTulos.getOid() + " " + curTulos.getNimi());
        }

        return hk;
    }

    private String getHakukohdeId(Hakukohde hk) throws OPTFormatException {
        String hakukohdeId = String.format("%s", hk.getId());
        return numFormatter(hakukohdeId, 10, "hakukohdeid");
    }

    @Value("${OPTILI.alkutietue}")
    public void setAlkutietue(String alkutietue) {
        this.ALKUTIETUE = alkutietue;
    }

    @Value("${OPTILI.lopputietue}")
    public void setLopputietue(String lopputietue) {
        this.LOPPUTIETUE = lopputietue;
    }

    @Value("${OPTILI.fileIdentifier:OPTILI}")
    public void setFilenameSuffix(String fileIdentifier) {
        this.FILEIDENTIFIER = fileIdentifier;
    }

    @Value("${OPTILI.koulutuslaji:N }")
    public void setKoulutuslaji(String koulutuslaji) {
        this.KOULUTUSLAJI = koulutuslaji;
    }

    @Override
    public void composeRecords() throws IOException, UserStopRequestException {

        LOG.info("starting haeHakukohteet");
        KelaHakukohteetV1RDTO vastaus = tarjontaClient.getHakukohteet();
        LOG.info("got haeHakukohteet");

        Set<String> hakukohteet = new HashSet<String>();
        Set<String> kmos = new HashSet<String>();

        for (KelaHakukohdeV1RDTO curTulos : vastaus.getHakukohteet()) {
            String tarjoajaOid = curTulos.getTarjoajaOid();
            try {
                if (hakukohteet.contains(curTulos.getOid())) {
                    fatalError(10, "hakukohde", curTulos.getOid());
                }
                hakukohteet.add(curTulos.getOid().substring(curTulos.getOid().lastIndexOf('.')));

                if (TarjontaTila.JULKAISTU.equals(curTulos.getTila()) && isHakukohdeOppilaitos(tarjoajaOid)) {
                    Hakukohde hakukohde = kelaDAO.findHakukohdeByOid(curTulos.getOid());
                    if (hakukohde == null) {
                        error(11, curTulos.getOid(), curTulos.getNimi());
                    }
                    List<KoulutusmoduuliToteutus> komotos = hakukohde.getKoulutukset();

                    if (komotos.size() == 0) {
                        error(5, curTulos.getOid() + " " + curTulos.getNimi());
                    }

                    OrganisaatioRDTO organisaatioDTO = this.getOrganisaatio(tarjoajaOid);
                    for (KoulutusmoduuliToteutus komoto : komotos) {

                        if (kmos.contains(komoto.getOid())) {
                            fatalError(10, "komoto", komoto.getOid());
                        }
                        kmos.add(komoto.getOid().substring(komoto.getOid().lastIndexOf('.')));

                        this.writeRecord(curTulos, organisaatioDTO, komoto);
                    }
                }
            } catch (OPTFormatException e) {
                LOG.error(String.format(errors[0], (curTulos.getOid() + " " + curTulos.getNimi()), tarjoajaOid));
            } catch (Exception e) {
                LOG.error(String.format(errors[0], (curTulos.getOid() + " " + curTulos.getNimi()), tarjoajaOid));
                throw e;
            }
        }
    }

    Pattern isInteger;

    @Override
    public String composeRecord(Object... args) throws OPTFormatException {
        if (isInteger == null) {
            isInteger = Pattern.compile("\\d+");
        }
        KelaHakukohdeV1RDTO curTulos = (KelaHakukohdeV1RDTO) args[0];
        OrganisaatioRDTO tarjoajaOrganisaatioDTO = (OrganisaatioRDTO) args[1];
        KoulutusmoduuliToteutus komoto = (KoulutusmoduuliToteutus) args[2];
        String komoto_oid = "";

        switch (komoto.getOid()) {
            case "1.2.246.562.5.02998_11_900_1709_1509":
            case "1.2.246.562.5.02998_11_900_1709_1511":
            case "1.2.246.562.5.02998_11_900_1709_1510":
            case "1.2.246.562.5.02998_11_900_1709_1598":
            case "1.2.246.562.5.02998_11_900_1709_1514":
                komoto_oid = komoto.getOid().replaceAll("_", "");
                warn(2, komoto.getOid(), komoto_oid, curTulos.getOid());
                break;
            default:
                komoto_oid = komoto.getOid();
        }

        Hakukohde hk = getHakukohde(curTulos);

        return String.format("%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s%s", //34 fields
                getHakukohdeId(hk),//Sisainen koodi
                getOppilaitosnumero(tarjoajaOrganisaatioDTO),//OPPIL_NRO
                getOrgOid(tarjoajaOrganisaatioDTO), //OrganisaatioOID
                getOpetuspisteenJarjNro(tarjoajaOrganisaatioDTO),
                StringUtils.leftPad("", 12),//Op. linjan tai koulutuksen jarjestysnro
                getKoulutuslaji(),//Koulutuslaji
                getHakukohteenNimi(curTulos),//Hakukohteen nimi
                kelaDAO.getKKTutkinnonTaso(komoto).getTasoCode(),//TUT_TASO
                getTutkintotunniste(komoto.getKoulutusmoduuli()),//TUT_ID = koulutuskoodi
                getOrgOid(curTulos), //hakukohde OID
                StringUtils.leftPad("", 3), //filler
                StringUtils.leftPad("", 3), //OPL_OTTOALUE
                StringUtils.leftPad("", 3), //Opetusmuoto
                StringUtils.leftPad("", 2), //Koulutustyyppi
                getLinjaNro(hk), //LINJANRO
                getLinjaTarkenne(hk), //LINJTARK
                StringUtils.leftPad("", 2), //OPL_VKOEJAR
                StringUtils.leftPad("", 2), //OPL_VKOEKUT
                StringUtils.leftPad("", 7), //Alinkeskiarvo
                StringUtils.leftPad("", 12), //Koulutuksen kesto
                StringUtils.leftPad("", 3), //OPL_KKERROIN
                getAlkupvm(), //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva
                DEFAULT_DATE, //Viimeisin paivityspaiva
                StringUtils.leftPad("", 30), //Viimeisin paivittaja
                StringUtils.leftPad("", 6), //El-harpis
                getAlkamiskausi(komoto.getAlkamiskausi_uri()),
                getTila(), //TILA
                komoto.getAlkamisvuosi(),
                DEFAULT_DATE, //Alkupaiva, voimassaolon alku
                DEFAULT_DATE, //Loppupaiva, voimassaolon loppu
                StringUtils.leftPad("", 1), //OPL_TULOSTUS
                StringUtils.leftPad("", 15), //OPL_OMISTAJA
                getKomotoOid(komoto_oid));
    }

    private String getLinjaNro(Hakukohde hk) {
        String ln = hk.getKelaLinjaKoodi();
        if (StringUtils.isEmpty(ln)) {
            return StringUtils.leftPad("", 3);
        }

        if (ln.length() != 3) {
            warn(3, hk.getOid(), "linjakoodi");
        }

        return (ln.length() > 3) ? ln.substring(0, 3) : StringUtils.rightPad(ln, 3);
    }

    private String getLinjaTarkenne(Hakukohde hk) {
        String lt = hk.getKelaLinjaTarkenne();
        if (StringUtils.isEmpty(lt)) {
            return StringUtils.leftPad("", 2);
        }

        if (lt.length() != 2) {
            warn(3, hk.getOid(), "linjan tarkenne");
        }

        return (lt.length() > 2) ? lt.substring(0, 2) : StringUtils.rightPad(lt, 2);
    }

    private String getKomotoOid(String oid) throws OPTFormatException {
        String _oid = oid.substring(oid.lastIndexOf('.') + 1);
        if (_oid == null || _oid.length() == 0) {
            error(3, oid);
        }
        if (!isInteger.matcher(_oid).matches()) {
            error(6, oid);
        }
        return strFormatter(_oid, 22, "KOMOTOOID");
    }

    private String getOrgOid(OrganisaatioRDTO org) throws OPTFormatException {
        if (null == org.getOid()) {
            error(4);
        }
        String oid = org.getOid().substring(org.getOid().lastIndexOf('.') + 1);
        if (oid == null || oid.length() == 0) {
            error(3, org.getOid() + " " + org.getNimi());
        }
        return strFormatter(oid, 22, "OID");
    }

    private String getOrgOid(KelaHakukohdeV1RDTO pt) throws OPTFormatException {
        if (null == pt.getOid()) {
            error(4);
        }
        String oid = pt.getOid().substring(pt.getOid().lastIndexOf('.') + 1);
        if (oid == null || oid.length() == 0) {
            error(3, oid + " " + pt.getNimi());
        }
        if (!isInteger.matcher(oid).matches()) {
            error(6, pt.getOid() + " " + pt.getNimi());
        }
        return strFormatter(oid, 22, "OID");
    }

    private String getHakukohteenNimi(KelaHakukohdeV1RDTO curTulos) throws OPTFormatException {

        String hakNimi = curTulos.getNimiLocale("fi");
        if (StringUtils.isEmpty(hakNimi)) {
            hakNimi = curTulos.getNimiLocale("sv");
        }
        if (StringUtils.isEmpty(hakNimi)) {
            hakNimi = curTulos.getNimiLocale("en");
        }
        if (StringUtils.isEmpty(hakNimi)) {
            error(9, curTulos.getOid() + " " + curTulos.getNimi());
        }
        return strCutter(hakNimi, 62, "hakukohteen nimi", false);
    }

    @Override
    public String getAlkutietue() {
        return ALKUTIETUE;
    }

    @Override
    public String getLopputietue() {
        return LOPPUTIETUE;
    }

    @Override
    public String getFileIdentifier() {
        return FILEIDENTIFIER;
    }

    @Override
    public String[] getErrors() {
        return errors;
    }

    @Override
    public String[] getWarnings() {
        return warnings;
    }

    @Override
    public String[] getInfos() {
        return infos;
    }
}
