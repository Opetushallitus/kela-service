package fi.vm.sade.rajapinnat.kela.tkuva.util;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
public class KelaUtil {

    private static final FastDateFormat FORMATTER = FastDateFormat.getInstance("yyMMdd");

    /**
     * Formatoi Kelan vaatiman FTP-tiedostonimen paivamaaran perusteella.
     * 
     * RO.WOT.SR.Dvvkkpp.YHVA14
     * 
     * @param tiedostonLuontiPaivamaara
     * @return tiedostonimi
     */
    public static String createTiedostoNimiYhva14(Date tiedostonLuontiPaivamaara) {
        StringBuilder nimi = new StringBuilder();
        nimi.append("RO.WOT.SR.D").append(FORMATTER.format(tiedostonLuontiPaivamaara)).append(".YHVA14");
        return nimi.toString();
    }
}
