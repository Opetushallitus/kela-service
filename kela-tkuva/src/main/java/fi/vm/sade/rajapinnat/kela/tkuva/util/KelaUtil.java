package fi.vm.sade.rajapinnat.kela.tkuva.util;

import java.util.Date;

import org.apache.commons.lang.time.FastDateFormat;

public class KelaUtil {

    public static final char TYHJA = (char) 0x20;
    public static final byte[] RIVINVAIHTO = "\n".getBytes();
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
    public static String createTiedostoNimiOuhare(Date tiedostonLuontiPaivamaara) {
        StringBuilder nimi = new StringBuilder();
        nimi.append("RO.WOT.SR.D").append(FORMATTER.format(tiedostonLuontiPaivamaara)).append(".OUHARY");
        return nimi.toString();
    }
}
