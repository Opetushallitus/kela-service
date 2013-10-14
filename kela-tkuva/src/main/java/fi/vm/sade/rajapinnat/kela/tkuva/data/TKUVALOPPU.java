package fi.vm.sade.rajapinnat.kela.tkuva.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.FastDateFormat;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         TKUVALOPPU_VAL.doc/htr
 */
public class TKUVALOPPU {
    private byte[] siirtotunnus; // pituus 15: "OUYHVA" tasoitettu alkuun,
    // loppu tyhja!
    private byte[] tietuetyyppi; // pituus 1: "X"
    private byte[] ajopaivamaara; // pituus 8: PPKKVVVV

    private byte[] tietuelukumaara; // pituus 14: tietueiden maara, oikeaan
                                    // reunaan taytto, etunollat
    private byte[] varatila1; // pituus 112: tyhjaa

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(150);
        buffer.put(siirtotunnus);
        buffer.put(tietuetyyppi);
        buffer.put(ajopaivamaara);
        buffer.put(tietuelukumaara);
        buffer.put(varatila1);
        // buffer.compact(); no need for this as buffer is allocated 150 which
        // should be always the size

        return buffer.array();
    }

    public void setAjopaivamaara(byte[] ajopaivamaara) {
        this.ajopaivamaara = ajopaivamaara;
    }

    public void setSiirtotunnus(byte[] siirtotunnus) {
        this.siirtotunnus = siirtotunnus;
    }

    public void setTietuelukumaara(byte[] tietuelukumaara) {
        this.tietuelukumaara = tietuelukumaara;
    }

    public void setTietuetyyppi(byte[] tietuetyyppi) {
        this.tietuetyyppi = tietuetyyppi;
    }

    public void setVaratila1(byte[] varatila1) {
        this.varatila1 = varatila1;
    }

    public byte[] getAjopaivamaara() {
        return ajopaivamaara;
    }

    public byte[] getSiirtotunnus() {
        return siirtotunnus;
    }

    public byte[] getTietuelukumaara() {
        return tietuelukumaara;
    }

    public byte[] getTietuetyyppi() {
        return tietuetyyppi;
    }

    public byte[] getVaratila1() {
        return varatila1;
    }

    public static class Builder {
        private static final Charset LATIN1 = Charset.forName("ISO8859-1");
        private static final FastDateFormat ajopaivaFormatter = FastDateFormat.getInstance("ddMMyyyy");

        // private byte[] siirtotunnus; // pituus 15: "OUYHVA" tasoitettu
        // alkuun,
        // loppu tyhja!
        // private byte[] tietuetyyppi; // pituus 1: "X"
        private byte[] ajopaivamaara; // pituus 8: PPKKVVVV

        private byte[] tietuelukumaara; // pituus 14: tietueiden maara, oikeaan
                                        // reunaan taytto, etunollat

        // private byte[] varatila1; // pituus 112: tyhjaa

        public Builder setAjopaivamaara(Date ajopaivamaara) {
            this.ajopaivamaara = toLatin1(ajopaivaFormatter.format(ajopaivamaara), 8);
            return this;
        }

        public Builder setTietuelukumaara(Integer tietuelukumaara) {
            this.tietuelukumaara = toLatin1(tietuelukumaara, 14);
            return this;
        }

        private byte[] toLatin1(String text, int size) {
            if (text == null) {
                return StringUtils.rightPad("", size).getBytes(LATIN1);
            } else if (text.length() > size) {
                return text.substring(0, size).getBytes(LATIN1);
            }
            return StringUtils.rightPad(text, size).getBytes(LATIN1);
        }

        private byte[] toLatin1(Integer number, int size) {
            if (number == null) {
                return StringUtils.leftPad("", size, "0").getBytes(LATIN1);
            } else if (number.toString().length() > size) {
                return number.toString().substring(0, size).getBytes(LATIN1);
            }
            return StringUtils.leftPad(number.toString(), size, "0").getBytes(LATIN1);
        }

        public TKUVALOPPU build() {
            TKUVALOPPU t = new TKUVALOPPU();
            t.setSiirtotunnus(toLatin1("OUYHVA", 15));
            t.setTietuetyyppi(toLatin1("X", 1));
            t.setAjopaivamaara(ajopaivamaara);
            t.setTietuelukumaara(tietuelukumaara);

            t.setVaratila1(toLatin1("", 112));
            return t;
        }
    }
}
