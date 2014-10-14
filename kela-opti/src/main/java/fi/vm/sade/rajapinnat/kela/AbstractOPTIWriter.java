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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.jaxrs.client.ClientWebApplicationException;
import org.apache.cxf.jaxrs.client.ServerWebApplicationException;
import org.apache.log4j.Logger;
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
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioDTO;
import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;
import fi.vm.sade.organisaatio.resource.OrganisaatioResource;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Organisaatio;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.OrganisaatioPerustieto;
import fi.vm.sade.tarjonta.service.search.KoodistoKoodi;
import fi.vm.sade.tarjonta.service.search.TarjontaSearchService;
/**
 * 
 * @author Markus
 */
@Configurable
public abstract class AbstractOPTIWriter {
	protected enum OrgType {
		OPPILAITOS, 
		TOIMIPISTE
	}

    public abstract void composeRecords() throws IOException, UserStopRequestException;
    public abstract String composeRecord(Object... args) throws OPTFormatException;
    
    public abstract String getAlkutietue();
    public abstract String getLopputietue();
    public abstract String getFileIdentifier();
    public abstract String[] getErrors();
    public abstract String[] getWarnings();
    public abstract String[] getInfos();
    
    protected static final Logger LOG = Logger.getLogger(KelaGenerator.class);

	protected String CHARSET;
	protected Charset LATIN1;
    protected String DATE_PATTERN_FILE;
    protected String DATE_PATTERN_RECORD;
    protected String NAMEPREFIX;
    protected String DEFAULT_DATE;
    protected String DIR_SEPARATOR;
    protected String logFileName;
	public final static String ALKULOPPUTIETUE_FORMAT="0+ALKU|9+LOPPU(\\?+)";
	private String PARENTPATH_SEPARATOR;

	
    protected final static String ERR_MESS_1="invalid number (%s) : '%s'";
    protected final static String ERR_MESS_2="length of %s ('%s') should be max %s.";
    protected final static String ERR_MESS_3="File not found : '%s'";
    protected final static String ERR_MESS_4="I/O error : '%s'";
    protected final static String ERR_MESS_5="%s does not preceed with enough zeros : %s (should be max %s significant numbers)";
	protected final static String ERR_MESS_6="Alkutietue '%s' must match "+ALKULOPPUTIETUE_FORMAT;
	protected final static String ERR_MESS_7="Lopputietue '%s' must match "+ALKULOPPUTIETUE_FORMAT;
	protected final static String ERR_MESS_8="Number of rows (%s) will not fit into lopputietue '%s'";
	protected final static String ERR_MESS_9="Kela koulutuskoodi not found for koodistokoodi %s";
	protected final static String ERR_MESS_10="<removed>";
	protected final static String ERR_MESS_11="Koodisto koodi has no uri ('%s') - %s";
	protected final static String ERR_MESS_12="Toimipiste should have parentoidpath (%s)";
	protected final static String ERR_MESS_13="Max errors (%s) exceeded. Aborting.";
	protected final static String ERR_MESS_14="Yhteystieto may not be null for %s";
	
    protected final static String WARN_MESS_1="'%s' was truncated to %s characters (%s)";
    protected final static String WARN_MESS_2="Perhaps toimipiste %s should not have oppilaitoskoodi (%s)";
    protected final static String WARN_MESS_3="Got exception: %s. Retry in %s seconds...";
    
    protected final static String INFO_MESS_1="%s records written, %s skipped.";
    
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

    private int errorLimit = 0;
    private int errorCoolDown = 10;
    
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

	protected String ophOpintoalakoodisto;
    protected String ophKoulutusastekoodisto;

    
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

    
    public int getErrorLimit() {
		return errorLimit;
	}

    @Value("${errorLimit:0}")
    public void setErrorLimit(String errorLimit) {
		this.errorLimit = Integer.valueOf(errorLimit);
	}

    public int getErrorCoolDown() {
		return errorCoolDown;
	}
    
    @Value("${errorCooldown:10}")
	public void setErrorCoolDown(int errorCoolDown) {
		this.errorCoolDown = errorCoolDown;
	}

	static private Date startDate = new Date();
    private void createFileName() {
    	createFileNames("."+getFileIdentifier());
    }

    private void createFileNames(String suffix) {
    	String path = createPath();
    	SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_FILE);
        fileLocalName = NAMEPREFIX + sdf.format(startDate) + suffix;
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
    
    @Value("${fi-uri}")
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
    
    @Value("${koodisto-uris.opintoalaoph}")
    public void setOphOpintoalakoodisto(String ophOpintoalakoodisto) {
        this.ophOpintoalakoodisto = ophOpintoalakoodisto;
    }
    
    @Value("${koodisto-uris.koulutusasteoph}")
    public void setOphKoulutusastekoodisto(String ophKoulutusastekoodisto) {
        this.ophKoulutusastekoodisto = ophKoulutusastekoodisto;
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
    
	protected String getOppilaitosNro(Organisaatio org, OrgType orgType) throws OPTFormatException {
		String olKoodi = org.getOppilaitoskoodi();
		if (orgType.equals(OrgType.TOIMIPISTE)) {
			if (org.getOppilaitoskoodi() != null) {
				warn(String.format(WARN_MESS_2, org.getOid()+" "+org.getNimi(), org.getOppilaitoskoodi()));
			} else {
				if (null==org.getParentOidPath() ||org.getParentOidPath().length()==0 ) {
					error(String.format(ERR_MESS_12, org.getOid()+" "+org.getNimi()));
				}
				olKoodi = getOppilaitosNro(org.getParentOidPath());
			}
		}
		return olKoodi;
	}

	private String getOppilaitosNro(String parentOidPath) {
		String olKoodi=null;
		String[] parentsOids = parentOidPath.split("" + PARENTPATH_SEPARATOR);
		for (String parentOID : parentsOids) {
			if (parentOID.length() > 0 && this.orgContainer.getOppilaitosoidOppilaitosMap().containsKey(parentOID)) {
				olKoodi = this.orgContainer.getOppilaitosoidOppilaitosMap().get(parentOID).getOppilaitosKoodi();
				break;
			}
		}
		return olKoodi;
	}

    protected String getOppilaitosNro(OrganisaatioPerustieto curOrganisaatio) throws OPTFormatException {
    	String opnro = "";
        if (curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.TOIMIPISTE) 
                && !curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
        		opnro = StringUtils.leftPad(
                    this.orgContainer.getOppilaitosoidOppilaitosMap().get(
                    		curOrganisaatio.getParentOppilaitosOid()).getOppilaitosKoodi(), 5);
        } 
        if (curOrganisaatio.getOrganisaatiotyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
        	opnro = StringUtils.leftPad(curOrganisaatio.getOppilaitosKoodi(), 5);
        }
        return opnro;
    }
    
    protected String getOppilaitosNro(OrganisaatioDTO organisaatio) throws OPTFormatException {
    	String oppil_nro = "";
    	if (null != organisaatio.getTyypit()) {
    		if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.OPPILAITOS)) {
    			oppil_nro = String.format("%s", organisaatio.getOppilaitosKoodi());
    		} else if (organisaatio.getTyypit().contains(OrganisaatioTyyppi.TOIMIPISTE)) {
    			oppil_nro = getOppilaitosNro(organisaatio.getParentOidPath());
    		}
    	}
    	return oppil_nro;
    }
    
    protected String getToimipisteenJarjNro(Organisaatio orgE) {
        String opPisteenJarjNro = "";
        if (orgE.getOpetuspisteenJarjNro() != null) {
            opPisteenJarjNro = orgE.getOpetuspisteenJarjNro();
        }
        return StringUtils.leftPad(opPisteenJarjNro, 2);
    }

    private String _getYhteystietojenTunnus(Long id, String oid, String nimi) throws OPTFormatException {
    	Long yht_id = kelaDAO.getKayntiosoiteIdForOrganisaatio(id);
    	if ( null == yht_id) {
    		error(String.format(ERR_MESS_14, oid+" "+nimi));
    	}
        return StringUtils.leftPad(String.format("%s", yht_id), 10, '0');
    }

    protected String getYhteystietojenTunnus(Organisaatio orgE) throws OPTFormatException {
    	return _getYhteystietojenTunnus(orgE.getId(),orgE.getOid(),""+orgE.getNimi());
    }
    
    protected String getYhteystietojenTunnus(OrganisaatioPerustieto organisaatio) throws OPTFormatException {
    	Organisaatio orgE = kelaDAO.findOrganisaatioByOid(organisaatio.getOid());
    	return _getYhteystietojenTunnus(orgE.getId(),orgE.getOid(),""+orgE.getNimi());
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
    	if (null == curOppilaitos.getOppilaitostyyppi() || curOppilaitos.getOppilaitostyyppi().length()==0) {
    		return StringUtils.leftPad("", 10, '0');
    	}
        List<KoodiType> koodis = getKoodisByUriAndVersio(curOppilaitos.getOppilaitostyyppi());        
        KoodiType olTyyppiKoodi = null;
        if (!koodis.isEmpty()) {
            olTyyppiKoodi = koodis.get(0);
        }
        KoodiType kelaKoodi = getRinnasteinenKoodi(olTyyppiKoodi, kelaOppilaitostyyppikoodisto);
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
    
	private String getAlkutietueWithCheck() throws IOException {
		if (isValidAlkutietue(getAlkutietue())) {
			return getAlkutietue();
		}
		return null;
	}

	private String getLopputietueWithCheck() throws IOException {
		if (isValidLopputietue(getLopputietue())) {
			return getLopputietue();
		}
		return null;
	}

	private int writesTries = 0;
    private int writes = 0;
    
	public void writeStream() throws UserStopRequestException {
		createFileName();
		writesTries = 0;
	    writes = 0;
		try {
			bostr = new BufferedOutputStream(new FileOutputStream(new File(getFileName())));
			bostr.write(toLatin1(getAlkutietueWithCheck() + "\n"));
			bostr.flush();
			composeRecords();
			bostr.write(toLatin1(convertedLopputietue(writes) + "\n"));
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
    
	private static boolean stopRequest = false;
	
	public void stop() {
		info(this.getFileIdentifier()+" generation stopping.");
		stopRequest=true;
    }
	
	private int errorCount=0;
	public void writeRecord(Object... args) throws IOException, OPTFormatException, UserStopRequestException {
		if (stopRequest) {
			throw new UserStopRequestException();
		}
		++writesTries;
		String line = null;
		while(true) {
			try {
				line=composeRecord(args); 
				break;
			}catch(ServerWebApplicationException e /*CXF may throw*/) {
				handleException(e);					 
			} catch(ClientWebApplicationException e /*CXF may throw*/) {
				handleException(e);
			}
		}
		bostr.write(toLatin1(line));
		bostr.flush();
		++writes;
	}
	
	protected void handleException(Exception e) throws UserStopRequestException {
		errorCount++;
		warn(String.format(WARN_MESS_3,e.getCause(), getErrorCoolDown()));
		if (stopRequest) {
			throw new UserStopRequestException();
		}
		if (errorCount>getErrorLimit()) {
			String errStr=String.format(ERR_MESS_13, getErrorLimit());
			LOG.error(errStr);
			throw new UserStopRequestException();
		}
		try {
			Thread.sleep(getErrorCoolDown()*1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			throw new UserStopRequestException();
		}
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
    
    protected String strCutter(String str, int len, String humanName, boolean warn) throws OPTFormatException {
		if (null == str) {
			error(String.format(ERR_MESS_2, humanName, str, len));
		}
		if(str.length() > len) {
			if (warn) {
				warn(String.format(WARN_MESS_1, str, len, humanName));
			}
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
	
    private void error(String errorMsg) throws OPTFormatException {
		KelaGenerator.error("("+getFileIdentifier()+") : "+errorMsg);
		throw new OPTFormatException();
	}
    
    private void warn(String warnMsg) {
    	KelaGenerator.warn("("+getFileIdentifier()+") : " +warnMsg);
	}

    private void info(String infoMsg) {
    	KelaGenerator.info("("+getFileIdentifier()+") : "+infoMsg);
	}
    
    protected void error(int i, Object... args) throws OPTFormatException {
    	KelaGenerator.error("("+getFileIdentifier()+i+") : "+getErrors()[i-1],args);
		throw new OPTFormatException();
	}
    
    protected void warn(int i, Object... args) {
    	KelaGenerator.warn("("+getFileIdentifier()+i+") : " +getWarnings()[i-1],args);
	}

    protected void info(int i, Object... args) {
    	KelaGenerator.info("("+getFileIdentifier()+i+") : "+getInfos()[i-1],args);
	}
    
	private boolean isValidAlkuLopputietue(String stringToTest) {
	   	Pattern r = Pattern.compile(ALKULOPPUTIETUE_FORMAT);
	   	return (r.matcher(stringToTest)).matches();
	}
	
	private boolean isValidAlkutietue(String stringToTest) throws IOException {
		if (!isValidAlkuLopputietue(stringToTest)) {
			throw new IOException(String.format(ERR_MESS_6, stringToTest)); 
		}
		return true;
	}

	private boolean isValidLopputietue(String stringToTest) throws IOException {
		if (!isValidAlkuLopputietue(stringToTest)) {
			throw new IOException(String.format(ERR_MESS_7, stringToTest)); 
		}
		return true;
	}

	//i.e.  999LOPPU???? -> 999LOPPU057
	private String convertedLopputietue(int numOfRecords) throws IOException {
		String lopputietueToConvert = getLopputietueWithCheck();
		if (!isValidLopputietue(lopputietueToConvert)) {
			return null;
		}
		Pattern r = Pattern.compile(ALKULOPPUTIETUE_FORMAT);
    	Matcher m=r.matcher(lopputietueToConvert);
        if(!m.find() || m.groupCount()!=1 || m.group(1)==null) {
        	throw new IOException(String.format(ERR_MESS_7, lopputietueToConvert));
        }
        int numOfQuestionMarks = m.group(1).length();
        if((""+numOfRecords).length()>numOfQuestionMarks) {
        	throw new IOException(String.format(ERR_MESS_8, ""+numOfRecords, lopputietueToConvert));
        }
        return lopputietueToConvert.substring(0, lopputietueToConvert.length()-numOfQuestionMarks)
        		+StringUtils.leftPad(""+numOfRecords, numOfQuestionMarks, '0');
	}
	
    protected String getTutkintotunniste(KoodistoKoodi koodistoKoodi, String humanname) throws OPTFormatException {
	    String koodiUri = koodistoKoodi.getUri();
	    if (koodiUri==null) {
	    	warn(String.format(ERR_MESS_11, koodistoKoodi.getNimi(), humanname));
	    	return "";
	    }
	    return getTutkintotunniste(koodiUri, humanname);
    }
    
	protected String getTutkintotunniste(String koodiUri, String humanname) throws OPTFormatException {
	    List<KoodiType> koodis = this.getKoodisByUriAndVersio(koodiUri);        
	    KoodiType koulutuskoodi = null;
	    if (!koodis.isEmpty()) {
	        koulutuskoodi = koodis.get(0);
	        return StringUtils.rightPad(koulutuskoodi.getKoodiArvo(),6,"kela - tutkintotunniste");
	    }
	    error(String.format(ERR_MESS_9,koodiUri));
	    return null; //not reached
    }
    
	@Value("${organisaatiot.parentPathSeparator:\\|}")
    public void setParentPathSeparator(String parentPathSeparator) {
        this.PARENTPATH_SEPARATOR = parentPathSeparator;
    }
}
