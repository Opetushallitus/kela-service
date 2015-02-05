package fi.vm.sade.rajapinnat.kela.tkuva.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

import fi.vm.sade.rajapinnat.kela.tkuva.util.KelaUtil;

public class TKUVAALKU {
    private byte[] siirtotunnus; // pituus 15: "OUYHVA" tasoitettu alkuun, loppu
                                 // tyhja!
    private byte[] tietuetyyppi; // pituus 1: "A"
    private byte[] ajopaivamaara; // pituus 8: PPKKVVVV
    private byte[] siirtolaji; // pituus 5: "OPISK"
    private byte[] lajettajaryhma; // pituus 2: "OP"

    private byte[] varatila1; // pituus 5: tyhjaa!

    private byte[] organisaationimi;// pituus 63: tiedonvalittaja!
    private byte[] aineistonnimi; // pituus 40: aineiston selvakielinen
    // nimi!
    private byte[] varatila2; // pituus 61

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(200);
        buffer.put(siirtotunnus);
        buffer.put(tietuetyyppi);
        buffer.put(ajopaivamaara);
        buffer.put(siirtolaji);
        buffer.put(lajettajaryhma);

        buffer.put(varatila1);

        buffer.put(organisaationimi);
        buffer.put(aineistonnimi);

        buffer.put(varatila2);
        // buffer.compact(); no need for this as buffer is allocated 200 which
        // should be always the size

        return buffer.array();
    }

    public void setAineistonnimi(byte[] aineistonnimi) {
        this.aineistonnimi = aineistonnimi;
    }

    public void setAjopaivamaara(byte[] ajopaivamaara) {
        this.ajopaivamaara = ajopaivamaara;
    }

    public void setLajettajaryhma(byte[] lajettajaryhma) {
        this.lajettajaryhma = lajettajaryhma;
    }

    public void setOrganisaationimi(byte[] organisaationimi) {
        this.organisaationimi = organisaationimi;
    }

    public void setSiirtolaji(byte[] siirtolaji) {
        this.siirtolaji = siirtolaji;
    }

    public void setSiirtotunnus(byte[] siirtotunnus) {
        this.siirtotunnus = siirtotunnus;
    }

    public void setTietuetyyppi(byte[] tietuetyyppi) {
        this.tietuetyyppi = tietuetyyppi;
    }

    public void setVaratila1(byte[] varatila1) {
        this.varatila1 = varatila1;
    }

    public void setVaratila2(byte[] varatila2) {
        this.varatila2 = varatila2;
    }

    public byte[] getAineistonnimi() {
        return aineistonnimi;
    }

    public byte[] getAjopaivamaara() {
        return ajopaivamaara;
    }

    public byte[] getLajettajaryhma() {
        return lajettajaryhma;
    }

    public byte[] getOrganisaationimi() {
        return organisaationimi;
    }

    public byte[] getSiirtolaji() {
        return siirtolaji;
    }

    public byte[] getSiirtotunnus() {
        return siirtotunnus;
    }

    public byte[] getTietuetyyppi() {
        return tietuetyyppi;
    }

    public byte[] getVaratila1() {
        return varatila1;
    }

    public byte[] getVaratila2() {
        return varatila2;
    }

    public static class Builder {
        private static final Charset LATIN1 = Charset.forName("ISO8859-1");
        private static final FastDateFormat ajopaivaFormatter = FastDateFormat.getInstance("ddMMyyyy");

        private byte[] siirtotunnus; // pituus 15
        // alkuun,
        // loppu tyhja!
        // private byte[] tietuetyyppi; // pituus 1: "A"
        private byte[] ajopaivamaara; // pituus 8: PPKKVVVV

        // private byte[] siirtolaji; // pituus 5: "OPISK"
        // private byte[] lajettajaryhma; // pituus 2: "OP"

        // private byte[] varatila1; // pituus 5: tyhjaa!
        private byte[] organisaationimi;// pituus 63: tiedonvalittaja!

        private byte[] aineistonnimi; // pituus 40: aineiston selvakielinen
                                      // nimi!

        // private byte[] varatila2; // pituus 61
        

        public Builder setSiirtotunnus(String siirtotunnus) {
            this.siirtotunnus =  toLatin1(siirtotunnus, 15);
            return this;
        }
        
        public Builder setAjopaivamaara(Date ajopaivamaara) {
            this.ajopaivamaara = toLatin1(ajopaivaFormatter.format(ajopaivamaara), 8);
            return this;
        }      

        public Builder setOrganisaationimi(String organisaationimi) {
            this.organisaationimi = toLatin1(organisaationimi, 63);
            return this;
        }

        public Builder setAineistonnimi(String aineistonnimi) {
            this.aineistonnimi = toLatin1(aineistonnimi, 40);
            return this;
        }

        private byte[] toLatin1(String text, int size) {
            if (text == null) {
                return StringUtils.rightPad("", size, KelaUtil.TYHJA).getBytes(LATIN1);
            } else if (text.length() > size) {
                return text.substring(0, size).getBytes(LATIN1);
            }
            return StringUtils.rightPad(text, size, KelaUtil.TYHJA).getBytes(LATIN1);
        }

        public TKUVAALKU build() {
            TKUVAALKU t = new TKUVAALKU();
            t.setSiirtotunnus(siirtotunnus);
            t.setTietuetyyppi(toLatin1("A", 1));
            t.setAjopaivamaara(ajopaivamaara);
            t.setSiirtolaji(toLatin1("OPISK", 5));
            t.setLajettajaryhma(toLatin1("OP", 2));

            t.setVaratila1(toLatin1("", 5));
            t.setOrganisaationimi(organisaationimi);
            t.setAineistonnimi(aineistonnimi);

            t.setVaratila2(toLatin1("", 61));
            return t;
        }
    }
}
