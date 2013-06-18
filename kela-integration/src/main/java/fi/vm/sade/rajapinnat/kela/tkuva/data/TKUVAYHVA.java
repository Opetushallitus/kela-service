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
 *         Pakollisia tietoja on, - poimintapaivamaara - oppilaitos - linjakoodi
 *         - henkilotunnus - sukunimi - etunimet - valintapaivamaara - lukuvuosi
 *         - ajankohta (Syksy/Kevat)
 * 
 *         Loput on vakioita tai paateltavissa - joten builder-pattern
 *         rakentamiseen. Builderi ottaa huomioon UTF8 - Latin1 konversion ja
 *         tasaus tietorakenteessa vaadittuun reunaan ja padding kussakin
 *         tilanteessa oikealla merkilla.
 * 
 *         TKUVAYHVA tietue = new
 *         TKUVAYHVA.Builder().setHenkilotunnus("010478123X"
 *         ).setEtunimet(...).build();
 * 
 *         byte[] tavut = tietue.toByteArray();
 */
public class TKUVAYHVA {

    private byte[] siirtotunnus;
    private byte[] tietuetyyppi;
    private byte[] poimintapaivamaara;
    private byte[] siirtolaji;
    private byte[] lahettajaryhmanTunnus;
    private byte[] oppilaitos;
    private byte[] linjakoodi;

    private byte[] varatila1;

    private byte[] henkilotunnus;
    private byte[] sukunimi;
    private byte[] etunimet;
    private byte[] valintapaivamaara;
    private byte[] valinnanTila;
    private byte[] lukuvuosi;

    private byte[] varatila2;

    private byte[] tutkinnonTaso1;
    private byte[] tutkinnonTaso2;
    private byte[] ajankohta;
    private byte[] lukukaudenAloituspaiva;

    private byte[] varatila3;

    public byte[] toByteArray() {
        ByteBuffer buffer = ByteBuffer.allocate(150);
        buffer.put(siirtotunnus);
        buffer.put(tietuetyyppi);
        buffer.put(poimintapaivamaara);
        buffer.put(siirtolaji);
        buffer.put(lahettajaryhmanTunnus);
        buffer.put(oppilaitos);
        buffer.put(linjakoodi);

        buffer.put(varatila1);

        buffer.put(henkilotunnus);
        buffer.put(sukunimi);
        buffer.put(etunimet);
        buffer.put(valintapaivamaara);
        buffer.put(valinnanTila);
        buffer.put(lukuvuosi);

        buffer.put(varatila2);

        buffer.put(tutkinnonTaso1);
        buffer.put(tutkinnonTaso2);
        buffer.put(ajankohta);
        buffer.put(lukukaudenAloituspaiva);

        buffer.put(varatila3);
        // buffer.compact(); no need for this as buffer is allocated 150 which
        // should be always the size

        return buffer.array();
    }

    public void setLinjakoodi(byte[] linjakoodi) {
        this.linjakoodi = linjakoodi;
    }

    public void setAjankohta(byte[] ajankohta) {
        this.ajankohta = ajankohta;
    }

    public void setEtunimet(byte[] etunimet) {
        this.etunimet = etunimet;
    }

    public void setHenkilotunnus(byte[] henkilotunnus) {
        this.henkilotunnus = henkilotunnus;
    }

    public void setLahettajaryhmanTunnus(byte[] lahettajaryhmanTunnus) {
        this.lahettajaryhmanTunnus = lahettajaryhmanTunnus;
    }

    public void setLukukaudenAloituspaiva(byte[] lukukaudenAloituspaiva) {
        this.lukukaudenAloituspaiva = lukukaudenAloituspaiva;
    }

    public void setLukuvuosi(byte[] lukuvuosi) {
        this.lukuvuosi = lukuvuosi;
    }

    public void setOppilaitos(byte[] oppilaitos) {
        this.oppilaitos = oppilaitos;
    }

    public void setPoimintapaivamaara(byte[] poimintapaivamaara) {
        this.poimintapaivamaara = poimintapaivamaara;
    }

    public void setSiirtolaji(byte[] siirtolaji) {
        this.siirtolaji = siirtolaji;
    }

    public void setSiirtotunnus(byte[] siirtotunnus) {
        this.siirtotunnus = siirtotunnus;
    }

    public void setSukunimi(byte[] sukunimi) {
        this.sukunimi = sukunimi;
    }

    public void setTietuetyyppi(byte[] tietuetyyppi) {
        this.tietuetyyppi = tietuetyyppi;
    }

    public void setTutkinnonTaso1(byte[] tutkinnonTaso1) {
        this.tutkinnonTaso1 = tutkinnonTaso1;
    }

    public void setTutkinnonTaso2(byte[] tutkinnonTaso2) {
        this.tutkinnonTaso2 = tutkinnonTaso2;
    }

    public void setValinnanTila(byte[] valinnanTila) {
        this.valinnanTila = valinnanTila;
    }

    public void setValintapaivamaara(byte[] valintapaivamaara) {
        this.valintapaivamaara = valintapaivamaara;
    }

    public void setVaratila1(byte[] varatila1) {
        this.varatila1 = varatila1;
    }

    public void setVaratila2(byte[] varatila2) {
        this.varatila2 = varatila2;
    }

    public void setVaratila3(byte[] varatila3) {
        this.varatila3 = varatila3;
    }

    public byte[] getLinjakoodi() {
        return linjakoodi;
    }

    public byte[] getAjankohta() {
        return ajankohta;
    }

    public byte[] getEtunimet() {
        return etunimet;
    }

    public byte[] getHenkilotunnus() {
        return henkilotunnus;
    }

    public byte[] getLahettajaryhmanTunnus() {
        return lahettajaryhmanTunnus;
    }

    public byte[] getLukukaudenAloituspaiva() {
        return lukukaudenAloituspaiva;
    }

    public byte[] getLukuvuosi() {
        return lukuvuosi;
    }

    public byte[] getOppilaitos() {
        return oppilaitos;
    }

    public byte[] getPoimintapaivamaara() {
        return poimintapaivamaara;
    }

    public byte[] getSiirtolaji() {
        return siirtolaji;
    }

    public byte[] getSiirtotunnus() {
        return siirtotunnus;
    }

    public byte[] getSukunimi() {
        return sukunimi;
    }

    public byte[] getTietuetyyppi() {
        return tietuetyyppi;
    }

    public byte[] getTutkinnonTaso1() {
        return tutkinnonTaso1;
    }

    public byte[] getTutkinnonTaso2() {
        return tutkinnonTaso2;
    }

    public byte[] getValinnanTila() {
        return valinnanTila;
    }

    public byte[] getValintapaivamaara() {
        return valintapaivamaara;
    }

    public byte[] getVaratila1() {
        return varatila1;
    }

    public byte[] getVaratila2() {
        return varatila2;
    }

    public byte[] getVaratila3() {
        return varatila3;
    }

    public static class Builder {
        private static final Charset LATIN1 = Charset.forName("ISO8859-1");

        // private byte[] siirtotunnus = toLatin1("OUYHVA",15);
        // private byte[] tietuetyyppi = toLatin1("T",1);
        private static final FastDateFormat poimintapaivamaaraJaValintapaivamaaraFormatter = FastDateFormat
                .getInstance("ddMMyyyy");
        private static final FastDateFormat aloitusvuosiFormatter = FastDateFormat.getInstance("yyyy");
        private byte[] poimintapaivamaara;
        // private byte[] siirtolaji = toLatin1("OPISK",1);
        // private byte[] lahettajaryhmanTunnus = toLatin1("OP",2);
        private byte[] oppilaitos;
        private byte[] linjakoodi;

        // private byte[] varatila1 = toLatin1("",3);

        private byte[] henkilotunnus;
        private byte[] sukunimi;
        private byte[] etunimet;

        private byte[] valintapaivamaara;
        // private byte[] valinnanTila;
        private Date lukuvuosi;

        // private byte[] varatila2;

        // private byte[] tutkinnonTaso1;
        // private byte[] tutkinnonTaso2;
        private boolean ajankohtaSyksy;

        // private byte[] lukukaudenAloituspaiva;

        // private byte[] varatila3;

        public Builder setPoimintapaivamaara(Date poimintapaivamaara) {
            this.poimintapaivamaara = toLatin1(
                    poimintapaivamaaraJaValintapaivamaaraFormatter.format(poimintapaivamaara), 8);
            return this;
        }

        public Builder setOppilaitos(String oppilaitos) {
            this.oppilaitos = toLatin1(oppilaitos, 4);
            return this;
        }

        public Builder setLinjakoodi(String linjakoodi) {
            this.linjakoodi = toLatin1(linjakoodi, 3);
            return this;
        }

        public Builder setLukuvuosi(Date lukuvuosi) {
            this.lukuvuosi = lukuvuosi;
            return this;
        }

        public Builder setHenkilotunnus(String henkilotunnus) {
            this.henkilotunnus = toLatin1(henkilotunnus, 10);
            return this;
        }

        public Builder setSukunimi(String sukunimi) {
            this.sukunimi = toLatin1(sukunimi, 30);
            return this;
        }

        public Builder setEtunimet(String etunimet) {
            this.etunimet = toLatin1(etunimet, 30);
            return this;
        }

        public Builder setAjankohtaSyksy(boolean syksy) {
            this.ajankohtaSyksy = syksy;
            return this;
        }

        public Builder setSyksyllaAlkavaKoulutus() {
            setAjankohtaSyksy(true);
            return this;
        }

        public Builder setKevaallaAlkavaKoulutus() {
            setAjankohtaSyksy(false);
            return this;
        }

        public Builder setValintapaivamaara(Date valintapaivamaara) {
            this.valintapaivamaara = toLatin1(poimintapaivamaaraJaValintapaivamaaraFormatter.format(valintapaivamaara),
                    8);
            return this;
        }

        public TKUVAYHVA build() {
            TKUVAYHVA t = new TKUVAYHVA();
            t.setSiirtotunnus(toLatin1("OUYHVA", 15));
            t.setTietuetyyppi(toLatin1("T", 1));
            t.setPoimintapaivamaara(poimintapaivamaara);
            t.setSiirtolaji(toLatin1("OPISK", 1));
            t.setLahettajaryhmanTunnus(toLatin1("OP", 2));
            t.setOppilaitos(oppilaitos);
            t.setLinjakoodi(linjakoodi);

            t.setVaratila1(toLatin1("", 3));

            t.setHenkilotunnus(henkilotunnus);
            t.setSukunimi(sukunimi);
            t.setEtunimet(etunimet);
            t.setValintapaivamaara(valintapaivamaara);
            t.setValinnanTila(toLatin1("V", 1));
            t.setLukuvuosi(toLatin1(aloitusvuosiFormatter.format(lukuvuosi), 4));

            t.setVaratila2(toLatin1("", 1));

            t.setTutkinnonTaso1(toLatin1("", 3));
            t.setTutkinnonTaso2(toLatin1("", 3));

            if (ajankohtaSyksy) {
                t.setAjankohta(toLatin1("S", 1));
                StringBuilder a = new StringBuilder();
                t.setLukukaudenAloituspaiva(toLatin1(a.append("0108").append(aloitusvuosiFormatter.format(lukuvuosi))
                        .toString(), 8));
            } else {
                t.setAjankohta(toLatin1("K", 1));
                StringBuilder a = new StringBuilder();
                t.setLukukaudenAloituspaiva(toLatin1(a.append("0101").append(aloitusvuosiFormatter.format(lukuvuosi))
                        .toString(), 8));
            }
            t.setVaratila3(toLatin1("", 10));
            return t;
        }

        private byte[] toLatin1(String text, int size) {
            if (text == null) {
                return StringUtils.rightPad("", size).getBytes(LATIN1);
            } else if (text.length() > size) {
                return text.substring(0, size).getBytes(LATIN1);
            }
            return StringUtils.rightPad(text, size).getBytes(LATIN1);
        }
    }
}
