package fi.vm.sade.rajapinnat.kela.tarjonta.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vm.sade.organisaatio.api.model.types.OrganisaatioTyyppi;

public class OrganisaatioPerustieto implements Serializable {

    private final static long serialVersionUID = 100L;
    private String oid;
    private String parentOppilaitosOid;
    private String ytunnus;
    private String oppilaitosKoodi;
    private String oppilaitostyyppi;
    private Map<String, String> nimi = new HashMap<String, String>();
    private List<OrganisaatioTyyppi> tyypit = new ArrayList<OrganisaatioTyyppi>();

	public Map<String, String> getNimi() {
		return nimi;
	}

	public void setNimi(Map<String, String> nimi) {
		this.nimi = nimi;
	}

	public OrganisaatioPerustieto() {
		super();
	}

	public String getOid() {
		return oid;
	}

	public void setOid(String value) {
		this.oid = value;
	}

	public String getParentOppilaitosOid() {
		return parentOppilaitosOid;
	}

	public void setParentOppilaitosOid(String value) {
		this.parentOppilaitosOid = value;
	}

	public String getYtunnus() {
		return ytunnus;
	}

	public void setYtunnus(String value) {
		this.ytunnus = value;
	}

	public String getOppilaitosKoodi() {
		return oppilaitosKoodi;
	}

	public void setOppilaitosKoodi(String value) {
		this.oppilaitosKoodi = value;
	}

	public String getOppilaitostyyppi() {
		return oppilaitostyyppi;
	}

	public void setOppilaitostyyppi(String value) {
		this.oppilaitostyyppi = value;
	}

	public List<OrganisaatioTyyppi> getOrganisaatiotyypit() {
		if (tyypit == null) {
			tyypit = new ArrayList<OrganisaatioTyyppi>();
		}
		return this.tyypit;
	}

	public void setNimi(String targetLanguage, String nimi) {
		this.nimi.put(targetLanguage, nimi);
	}

	public String getNimi(String language) {
		return this.nimi.get(language);
	}
}

