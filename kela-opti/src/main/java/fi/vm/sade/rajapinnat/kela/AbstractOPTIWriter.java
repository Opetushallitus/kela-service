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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

import fi.vm.sade.koodisto.service.KoodiService;
import fi.vm.sade.koodisto.service.KoodistoService;
import fi.vm.sade.koodisto.service.types.SearchKoodisCriteriaType;
import fi.vm.sade.koodisto.service.types.common.KieliType;
import fi.vm.sade.koodisto.service.types.common.KoodiMetadataType;
import fi.vm.sade.koodisto.service.types.common.KoodiType;
import fi.vm.sade.koodisto.service.types.common.KoodiUriAndVersioType;
import fi.vm.sade.koodisto.service.types.common.SuhteenTyyppiType;
import fi.vm.sade.koodisto.util.KoodistoHelper;
import fi.vm.sade.organisaatio.api.model.OrganisaatioService;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.api.search.OrganisaatioPerustieto;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.tarjonta.service.search.TarjontaSearchService;
/**
 * 
 * @author Markus
 */
@Configurable
public abstract class AbstractOPTIWriter {
    //TODO: writeFile() is only for backward compatibility -- it must be replaced by composeRecords()
	public abstract void writeFile() throws IOException;
	//TODO: createFileName(..) is only for backward compatibility -- it must be replaced by getters getPath and getFilenameSuffix
	public void createFileName(String path, String suffix) {
		LOG.warn("depricated function createFileName(String path, String suffix) called!");
		createFileNames(path,suffix);
	}
	//TODO: bos is only for backward compatibility -- it must be accessed directly
	protected BufferedOutputStream bos;

    public abstract void composeRecords() throws IOException;
    public abstract String composeRecord(Object... args) throws OPTFormatException;
    
    public abstract String getAlkutietue();
    public abstract String getLopputietue();
    public abstract String getFilenameSuffix();
    public abstract String getPath();
    
    protected static final Logger LOG = LoggerFactory.getLogger(AbstractOPTIWriter.class);

	protected String CHARSET;
	protected Charset LATIN1;
    protected String DATE_PATTERN_FILE;
    protected String DATE_PATTERN_RECORD;
    protected String NAMEPREFIX;
    protected String DEFAULT_DATE;
    protected String DIR_SEPARATOR;

    protected final static String ERR_MESS_1="invalid number (%s) : '%s'";
    protected final static String ERR_MESS_2="length of %s ('%s') should be max %s.";
    protected final static String ERR_MESS_3="File not found : '%s'";
    protected final static String ERR_MESS_4="I/O error : '%s'";
    
    protected final static String WARN_MESS_1="'%s' was truncated to %s characters (%s)";
    
    protected final static String INFO_MESS_1="%s records written, %s skipped.";

    /*@Autowired
    protected TarjontaPublicService tarjontaService;*/
    
    @Autowired
    protected TarjontaSearchService tarjontaSearchService;

    @Autowired
    protected OrganisaatioService organisaatioService;
    
    @Autowired
    protected KoodiService koodiService;
    
    @Autowired
    protected KoodistoService koodistoService;
    
    @Autowired
    protected KelaDAO kelaDAO; 
    
    @Autowired
    protected OrganisaatioContainer orgContainer;

    protected OrganisaatioResource organisaatioResource;

    private String fileName=null;

    private String path=null;
    
    private BufferedOutputStream bostr;
    
    protected List<OrganisaatioPerustieto> organisaatiot;

    protected String kieliFi;
    protected String kieliSv;
    protected String kieliEn;
    
    //USED KOODISTO URIS
    protected String kelaTutkintokoodisto;
    protected String kelaOppilaitostyyppikoodisto;
    protected String yhKoulukoodiKoodisto;
    protected String koulutuskoodisto;
    protected String kelaOpintoalakoodisto;
    protected String kelaKoulutusastekoodisto;
    
    private String fileLocalName=null;
    
    @Value("${charset:ISO8859-1}")
    public void setCharset(String charset) {
		CHARSET = charset;
		LATIN1 = Charset.forName(charset);
	}
 
    @Value("${fileDatePattern:yyMMdd}")
    public void setFileDatePattern(String fileDatePattern) {
		DATE_PATTERN_FILE = fileDatePattern;
	}
    
    @Value("${recordDatePattern:dd.MM.yyyy}")
	public void setRecordDatePattern(String recordDatePattern) {
		DATE_PATTERN_RECORD = recordDatePattern;
	}
    
    @Value("${filenamePrefix:RY.WYZ.SR.D}")
	public void setFileNamePrefix(String filenamePrefix) {
		NAMEPREFIX = filenamePrefix;
	}
    
    @Value("${defaultDate:01.01.0001}")
	public void setDefaultDate(String defaultDate) {
		DEFAULT_DATE = defaultDate;
	}

    @Value("${dirSeparator:/}")
	public void setDirSeparator(String dirSeparator) {
		DIR_SEPARATOR = dirSeparator;
	}
    
    private void createFileName() {
    	createFileNames(getPath(),getFilenameSuffix());
    }

    private void createFileNames(String path, String name) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_FILE);
        if (StringUtils.isEmpty(path)) {
            path = createPath();
        }
        fileLocalName = NAMEPREFIX + sdf.format(new Date()) + name;
        fileName =  path + fileLocalName;//NAMEPREFIX + sdf.format(new Date()) + name;
    }
    
    private String createPath() {
        File pathF = new File(path);
        if (!pathF.exists()) {
            pathF.mkdir();
        }
        return this.path + DIR_SEPARATOR;
    }
    
    public String getFileLocalName() {
    	if (fileLocalName==null) {
    		createFileName();
    	}
        return this.fileLocalName;
    }
    
    protected byte[] toLatin1(String text) {
        return text.getBytes(LATIN1);
    }
    
    @Value("${exportdir}")
    public void setPath(String path) {
        this.path = path;
    }
    
    @Value("$fi-uri}")
    public void setKieliFi(String kieliFi) {
        this.kieliFi = kieliFi;
    }

    @Value("${sv-uri}")
    public void setKieliSv(String kieliSv) {
        this.kieliSv = kieliSv;
    }

    @Value("${en-uri}")
    public void setKieliEn(String kieliEn) {
        this.kieliEn = kieliEn;
    }

    @Value("${koodisto-uris.tutkintokela}")
    public void setKelaTutkintokoodisto(String kelaTutkintokoodisto) {
        this.kelaTutkintokoodisto = kelaTutkintokoodisto;
    }
    
    @Value("${koodisto-uris.oppilaitostyyppikela}")
    public void setKelaOppilaitostyyppikoodisto(String kelaOppilaitostyyppikoodisto) {
        this.kelaOppilaitostyyppikoodisto = kelaOppilaitostyyppikoodisto;
    }

    @Value("${koodisto-uris.yhteishaunkoulukoodi}")
    public void setYhKoulukoodiKoodisto(String yhKoulukoodiKoodisto) {
        this.yhKoulukoodiKoodisto = yhKoulukoodiKoodisto;
    }
    
    @Value("${koodisto-uris.koulutus}")
    public void setKoulutuskoodisto(String koulutuskoodisto) {
        this.koulutuskoodisto = koulutuskoodisto;
    }
    
    @Value("${koodisto-uris.opintoalakela}")
    public void setKelaOpintoalakoodisto(String kelaOpintoalakoodisto) {
        this.kelaOpintoalakoodisto = kelaOpintoalakoodisto;
    }
    
    @Value("${koodisto-uris.koulutusastekela}")
    public void setKelaKoulutusastekoodisto(String kelaKoulutusastekoodisto) {
        this.kelaKoulutusastekoodisto = kelaKoulutusastekoodisto;
    }
    
    protected List<KoodiType> getKoodisByUriAndVersio(String koodiUri) {
        return this.koodiService.searchKoodis(createUriVersioCriteria(koodiUri));
    }
    

    protected KoodiType getRinnasteinenKoodi(KoodiType koulutuskoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(koulutuskoodi.getKoodiUri());
        uriAndVersio.setVersio(koulutuskoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.RINNASTEINEN);
        KoodiType targetKoodi = null;
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                targetKoodi = curKoodi;
            }
        }
        if (targetKoodi == null) {
            relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, true, SuhteenTyyppiType.RINNASTEINEN);
            for (KoodiType curKoodi : relatedKoodis) {
                if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                    targetKoodi = curKoodi;
                }
            }
        }
        return targetKoodi;
    }
    
    protected KoodiType getSisaltyvaKoodi(KoodiType sourcekoodi, String targetKoodisto) {
        KoodiUriAndVersioType uriAndVersio = new KoodiUriAndVersioType();
        uriAndVersio.setKoodiUri(sourcekoodi.getKoodiUri());
        uriAndVersio.setVersio(sourcekoodi.getVersio());
        List<KoodiType> relatedKoodis = koodiService.listKoodiByRelation(uriAndVersio, false, SuhteenTyyppiType.SISALTYY);
        for (KoodiType curKoodi : relatedKoodis) {
            if (curKoodi.getKoodisto().getKoodistoUri().equals(targetKoodisto)) {
                return curKoodi;
            }
        }
        return null;
    }
    
    protected String getOppilaitosNro(OrganisaatioPerustieto curOrganisaatio) {
        String opnro = "";
        if (curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            opnro = curOrganisaatio.getOppilaitosKoodi();
        }
        return StringUtils.leftPad(opnro, 5);
    }

    protected String getOpPisteenOppilaitosnumero(OrganisaatioPerustieto curOrganisaatio) {
        if (curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.TOIMIPISTE) 
                && !curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
            return StringUtils.leftPad(
                    this.orgContainer.getOppilaitosoidOppilaitosMap().get(
                            curOrganisaatio.getParentOid()).getOppilaitosKoodi(), 5);
        } 
        if (curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
            return StringUtils.leftPad(curOrganisaatio.getOppilaitosKoodi(), 5);
        }
        return StringUtils.leftPad("", 5);
    }
    
    protected String getOpPisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return StringUtils.leftPad(opPisteenJarjNro, 2);
    }

    protected String getYhteystietojenTunnus(Organisaatio orgE) {
        return StringUtils.leftPad(String.format("%s", kelaDAO.getKayntiosoiteIdForOrganisaatio(orgE.getId())), 10, '0');
    }
    
    protected String getDateStrOrDefault(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_RECORD);
        String dateStr = DEFAULT_DATE;
        if (date != null) {
            dateStr = sdf.format(date);
        }
        return StringUtils.leftPad(dateStr, 10);
    }
    
    protected String getOppilaitostyyppitunnus(
            OrganisaatioPerustieto curOppilaitos) {
        List<KoodiType> koodis = getKoodisByUriAndVersio(curOppilaitos.getOppilaitostyyppi());        
        KoodiType olTyyppiKoodi = null;
        if (!koodis.isEmpty()) {
            olTyyppiKoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(olTyyppiKoodi, kelaOppilaitostyyppikoodisto);
        //info("kelaKoodi:"+kelaKoodi);
        return (kelaKoodi == null) ? StringUtils.leftPad("", 10, '0') : StringUtils.leftPad(kelaKoodi.getKoodiArvo(), 10, '0');
    }

    protected String getKotikunta(Organisaatio orgE) {
        List<KoodiType> koodit = getKoodisByUriAndVersio(orgE.getKotipaikka());
        String kotikuntaArvo = "";
        if (koodit != null && !koodit.isEmpty()) {
            kotikuntaArvo = koodit.get(0).getKoodiArvo();
        }
        return StringUtils.leftPad(kotikuntaArvo, 3);
    }
    
    /**
     * Get koodi metadata by locale with language fallback to FI
     *
     * @param koodiType
     * @param locale
     * @return
     */
    public KoodiMetadataType getKoodiMetadataForLanguage(KoodiType koodiType, KieliType kieli) {
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodiType, kieli);
        return kmdt;
    }
    
    public KoodiMetadataType getAvailableKoodiMetadata(KoodiType koodi) {
        KoodiMetadataType kmdt = KoodistoHelper.getKoodiMetadataForLanguage(koodi, KieliType.FI);
        if (kmdt == null && !koodi.getMetadata().isEmpty()) {
            kmdt = koodi.getMetadata().get(0);
        }
        return kmdt;
    }
    
    public void setTarjontaSearchService(TarjontaSearchService tarjontaSearchService) {
        this.tarjontaSearchService = tarjontaSearchService;
    }

    public void setOrganisaatioService(OrganisaatioService organisaatioService) {
        this.organisaatioService = organisaatioService;
    }
        
    public void setHakukohdeDAO(KelaDAO hakukohdeDAO) {
        this.kelaDAO = hakukohdeDAO;
    }

    public void setOrganisaatioResource(OrganisaatioResource organisaatioResource) {
        this.organisaatioResource = organisaatioResource;
    }
    
	public String getFileName() {
		if (fileName==null) {
			createFileName();
		}
		return fileName;
	}

   public void setOrganisaatiot(List<OrganisaatioPerustieto> organisaatiot) {
       this.organisaatiot = organisaatiot;
   }
    
	private SearchKoodisCriteriaType createUriVersioCriteria(String koodiUri) {
		SearchKoodisCriteriaType criteria = new SearchKoodisCriteriaType();
		int versio = -1;
		if (koodiUri.contains("#")) {
			int endIndex = koodiUri.lastIndexOf('#');
			versio = Integer.parseInt(koodiUri.substring(endIndex + 1));
			koodiUri = koodiUri.substring(0, endIndex);
		}
		criteria.getKoodiUris().add(koodiUri);
		if (versio > -1) {
			criteria.setKoodiVersio(versio);
		}
		return criteria;
	}
    
    private int writesTries = 0;
    private int writes = 0;
    
	public void writeStream() {
		createFileName();
		writesTries = 0;
	    writes = 0;
		try {
			bostr = new BufferedOutputStream(new FileOutputStream(new File(getFileName())));
			bostr.write(toLatin1(getAlkutietue() + "\n"));
			bostr.flush();
			composeRecords();
			bostr.write(toLatin1(getLopputietue() + "\n"));
			bostr.flush();
			bostr.close();
			LOG.info(String.format(INFO_MESS_1, writes, writesTries-writes));
		} catch (FileNotFoundException e) {
			LOG.error(String.format(ERR_MESS_3, getFileName()));
			e.printStackTrace();
		} catch (IOException e) {
			LOG.error(String.format(ERR_MESS_4, getFileName()));
			e.printStackTrace();
		}
	}
    
	
	public void writeRecord(Object... args) throws IOException, OPTFormatException {
			++writesTries;
			bostr.write(toLatin1(composeRecord(args)));
			bostr.flush();
			++writes;
	}
	
    protected static class OPTFormatException extends Exception {
		private static final long serialVersionUID = 1L;
    }
    protected String strFormatter(String str, int len, String humanName) throws OPTFormatException {
		if (null == str || str.length() > len) {
			error(String.format(ERR_MESS_2, humanName, str, len));
		}
		return StringUtils.rightPad(str, len);
	}
    
    protected String strCutter(String str, int len, String humanName) throws OPTFormatException {
		if (null == str) {
			error(String.format(ERR_MESS_2, humanName, str, len));
		}
		if(str.length() > len) {
			warn(String.format(WARN_MESS_1, str, len, humanName));
			return str.substring(0,len);
		}
		return StringUtils.rightPad(str, len);
	}

    protected String numFormatter(String str, int len, String humanName) throws OPTFormatException {
		try {
			Float.parseFloat(str);
		}catch(NumberFormatException e) {
			error(String.format(ERR_MESS_1, humanName, str));
		}
		if (null == str || str.length() > len) {
			error(String.format(ERR_MESS_2, humanName, str, len));
		}
		return StringUtils.leftPad(str, len, '0');
	}
	
    protected void error(String errorMsg) throws OPTFormatException {
		LOG.error(errorMsg);
		throw new OPTFormatException();
	}

    protected void warn(String warnMsg) {
    	LOG.warn(warnMsg);
	}
    
    protected void info(String infoMsg) {
		LOG.info(infoMsg);
	}
}
