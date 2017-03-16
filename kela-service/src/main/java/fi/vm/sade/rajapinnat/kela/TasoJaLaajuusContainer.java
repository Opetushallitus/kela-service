package fi.vm.sade.rajapinnat.kela;

import fi.vm.sade.organisaatio.resource.api.TasoJaLaajuusDTO;
import org.apache.log4j.Logger;

public class TasoJaLaajuusContainer {

    private static final Logger LOG = Logger.getLogger(TasoJaLaajuusContainer.class);

    private static final String ONLYALEMPI = "050";
    private static final String ALEMPIYLEMPI = "060";
    private static final String ONLYYLEMPI = "061";
    private static final String LAAKIS = "070";
    private static final String HAMMASLAAKIS = "071";
    private static final String EITASOA = "   ";

    private String tasoCode;
    private String komoId1;
    private String komoId2;

    public String getTasoCode() {
        return tasoCode;
    }

    public String getKomoId1() {
        return komoId1;
    }

    public String getKomoId2() {
        return komoId2;
    }


    public TasoJaLaajuusContainer laakis(String komoId) {
        tasoCode = LAAKIS;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer hammasLaakis(String komoId) {
        tasoCode = HAMMASLAAKIS;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyAlempi(String komoId) {
        tasoCode = ONLYALEMPI;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer onlyYlempi(String komoId) {
        tasoCode = ONLYYLEMPI;
        this.komoId1 = komoId;
        return this;
    }

    public TasoJaLaajuusContainer alempiYlempi(String komoId1, String komoId2) {
        tasoCode = ALEMPIYLEMPI;
        this.komoId1 = komoId1;
        this.komoId2 = komoId2;
        return this;
    }

    public TasoJaLaajuusContainer eiTasoa() {
        tasoCode = EITASOA;
        return this;
    }

    public boolean isYlempi() { return ONLYYLEMPI.equals(tasoCode); }
    public boolean isLaakis() { return LAAKIS.equals(tasoCode); }
    public boolean isHammaslaakis() { return HAMMASLAAKIS.equals(tasoCode); }
    public boolean isAlempi() { return ONLYALEMPI.equals(tasoCode); }
    public boolean isAlempiYlempi() { return ALEMPIYLEMPI.equals(tasoCode); }

    public boolean hasTaso() {
        return this.tasoCode != null && EITASOA.equals(tasoCode) == false;
    }

    public TasoJaLaajuusDTO toDTO(TarjontaClient tarjontaClient) {
        TasoJaLaajuusDTO resp = new TasoJaLaajuusDTO();
        resp.setTasoCode(this.tasoCode);

        String laajuus1 = tarjontaClient.getLaajuus(this.komoId1);
        String laajuus2 = tarjontaClient.getLaajuus(this.komoId2);

        if(laajuus1 != null && laajuus1.contains("+")) {
            String[] vals = parseLaajuus(laajuus1);
            resp.setLaajuus1(vals[0]);
            resp.setLaajuus2(vals[1]);
        } else if(laajuus2 != null && laajuus2.contains("+")) {
            String[] vals = parseLaajuus(laajuus2);
            resp.setLaajuus1(vals[0]);
            resp.setLaajuus2(vals[1]);
        } else {
            resp.setLaajuus1(laajuus1);
            resp.setLaajuus2(laajuus2);
        }

        resp.setKomoId1(this.komoId1);
        resp.setKomoId2(this.komoId2);
        return resp;
    }

    private String[] parseLaajuus(String laajuus) {
        String[] str = new String[2];

        try {
            if (laajuus != null) {
                if (laajuus.contains("/")) {
                    // case: 180+120/150 = laajuus1 = 330
                    String[] laajuusParts = laajuus.split("/");
                    int sum = new Integer(laajuusParts[0].substring(0, laajuusParts[0].indexOf('+'))).intValue() + new Integer(laajuusParts[1]).intValue();
                    str[0] = "" + sum;
                } else {
                    String[] laajuusParts = laajuus.split("\\+");
                    str[0] = laajuusParts[0];
                    if (laajuusParts.length > 1) {
                        // case: 180+120
                        str[1] = laajuusParts[1];
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Bad opintojen laajuus value:" + laajuus, e);
        }

        return str;
    }

}

