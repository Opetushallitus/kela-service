package fi.vm.sade.organisaatio.resource.api;

import java.io.Serializable;

public class TasoJaLaajuusDTO implements Serializable {

    private String tasoCode;
    private String laajuus1;
    private String laajuus2;
    private String komoId1;
    private String komoId2;

    public String getTasoCode() {
        return tasoCode;
    }

    public void setTasoCode(String tasoCode) {
        this.tasoCode = tasoCode;
    }

    public String getLaajuus1() {
        return laajuus1;
    }

    public void setLaajuus1(String laajuus1) {
        this.laajuus1 = laajuus1;
    }

    public String getLaajuus2() {
        return laajuus2;
    }

    public void setLaajuus2(String laajuus2) {
        this.laajuus2 = laajuus2;
    }

    public String getKomoId1() {
        return komoId1;
    }

    public void setKomoId1(String komoId1) {
        this.komoId1 = komoId1;
    }

    public String getKomoId2() {
        return komoId2;
    }

    public void setKomoId2(String komoId2) {
        this.komoId2 = komoId2;
    }


}

