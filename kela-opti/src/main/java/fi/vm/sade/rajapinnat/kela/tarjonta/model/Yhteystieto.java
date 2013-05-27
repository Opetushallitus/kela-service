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

    public Long getOrganisaatioId() {
        return organisaatioId;
    }

    public void setOrganisaatioId(Long organisaatioId) {
        this.organisaatioId = organisaatioId;
    }

}
