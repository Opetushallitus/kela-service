package fi.vm.sade.rajapinnat.kela.tarjonta.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="yhteystieto")
public class Yhteystieto {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    @GeneratedValue
    private Long id;
    
    @Column(name="organisaatio_id")
    private Long organisaatioId;
    
    @Column(name="osoitetyyppi")
    private String osoiteTyyppi;
    
    @Column(name="puhelinnumero")
    private String puhelinnumero;
    
    @Column(name="email")
    private String email;
    
    @Column(name="tyyppi")
    private String tyyppi;

    @Column(name="dtype")
    private String dtype;

    public String getOsoiteTyyppi() {
        return osoiteTyyppi;
    }

    public void setOsoiteTyyppi(String osoiteTyyppi) {
        this.osoiteTyyppi = osoiteTyyppi;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPuhelinnumero() {
		return puhelinnumero;
	}

	public void setPuhelinnumero(String puhelinnumero) {
		this.puhelinnumero = puhelinnumero;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Long getOrganisaatioId() {
        return organisaatioId;
    }

    public void setOrganisaatioId(Long organisaatioId) {
        this.organisaatioId = organisaatioId;
    }

	public String getTyyppi() {
		return tyyppi;
	}

	public void setTyyppi(String tyyppi) {
		this.tyyppi = tyyppi;
	}

	public String getDtype() {
		return dtype;
	}

	public void setDtype(String dtype) {
		this.dtype = dtype;
	}
    
}
