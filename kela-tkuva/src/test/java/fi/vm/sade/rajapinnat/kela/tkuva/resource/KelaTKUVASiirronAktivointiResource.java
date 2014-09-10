package fi.vm.sade.rajapinnat.kela.tkuva.resource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import fi.vm.sade.rajapinnat.kela.tkuva.data.TKUVAYHVA;
import fi.vm.sade.rajapinnat.kela.tkuva.service.KelaFtpService;
import fi.vm.sade.rajapinnat.kela.tkuva.util.KelaUtil;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Alustava resurssi KELA-tiedonsiirron aktivointiin. mvn jetty:run ja
 *         suuntaa osoitteeseen,
 *         http://localhost:8180/kela-tkuva/resources/tkuva/aktivoi
 * 
 *         HUOM! Tee src/test/resources/kela-integraatio-testi-context.xml
 *         ohjeilla Putty PROXY QA:lle jos haluat kokeilla tiedonsiirtoa
 *         oikeasti! Tarvitset myos: Properties props = System.getProperties();
 *         props.put("socksProxyHost", "127.0.0.1"); props.put("socksProxyPort",
 *         "21"); System.setProperties(props); Esim kaynnistys propertyiksi
 */
@Path("tkuva")
@Controller
public class KelaTKUVASiirronAktivointiResource {

    private static final Logger LOG = LoggerFactory.getLogger(KelaTKUVASiirronAktivointiResource.class);

    @Autowired
    KelaFtpService kelaService;

    /**
     * KELA haluaa neljasti vuodessa tiedot paikan saaneista opiskelupaikan
     * vastaanottaneista tukien myontamiseksi.
     * 
     * KELA haluaa vain paikan vastaanottaneet!
     * 
     * @param oid
     *            // parametri placeholder kunnes selviaa milla perusteilla
     *            aktivointi oikeasti halutaan tehda
     * @return
     */
    @GET
    @Path("aktivoi")
    public String aktivoi(@QueryParam("oid") String oid) {
        try {
            if (StringUtils.isBlank(oid)) {
                return "Anna kaynnistys parametri! (oid)";
            } else {
                // robusti tavuformaatin rakentaja. utf8-latin1 konversio.
                // pakottaa oikeaan tavukokoon ja oikeisiin
                // paivamaaraformaatteihin
                // https://docs.google.com/spreadsheet/ccc?key=0ApG7JFquM4ZDdEhCNGNSUGc4a0llUmlYa0puT2ZEcUE#gid=4
                TKUVAYHVA.Builder builder = new TKUVAYHVA.Builder();
                builder.setEtunimet("Jorma Pentti");
                builder.setSukunimi("Lötjönen");
                /*
                 * PPKKVVNNNX - henkilötunnus oltava muodollisesti oikein -
                 * pakollinen, esim. 010478123X" Henkilö:htun    "Hakijan
                 * henkilötunnus PPKKVVNNNX ei ole muodollisesti oikea
                 * formaatti. Vuosisadan tunnus puuttuu. Mitä tehdään kun
                 * 2000-luvulla syntyneet hakevat toiselle asteelle parin vuoden
                 * päästä?"" +
                 */
                builder.setHenkilotunnus("010478-123X");
                builder.setAjankohtaSyksy(true); // syksyllä vai kevaalla alkava
                                                 // koulutus?
                builder.setLukuvuosi(new Date());
                builder.setValintapaivamaara(new Date()); // koska tuli
                                                          // valituksi
                builder.setPoimintapaivamaara(new Date()); // koska kela tekee
                                                           // poiminnan
                builder.setOppilaitosnumero("12345"); // oppilaitos nro

                builder.setHakukohde("223.223.223.223"); // oid:
                builder.setOrganisaatio("123.123.123.123"); // oid:

                ByteArrayOutputStream kooste = new ByteArrayOutputStream();
                // kirjoitellaan vastaanottajat binaaripotkoon
                kooste.write(builder.build().toByteArray());
                kooste.write(KelaUtil.RIVINVAIHTO);
                kooste.write(builder.build().toByteArray());
                kooste.write(KelaUtil.RIVINVAIHTO);
                kooste.write(builder.build().toByteArray());
                kooste.write(KelaUtil.RIVINVAIHTO);
                kooste.write(builder.build().toByteArray());
                // ei rivinvaihtoa loppuun speksin mukaan

                ByteArrayInputStream tavut = new ByteArrayInputStream(kooste.toByteArray());
                // kelan vaatima tiedostonimi
                Date tiedostonLuontiPaivamaara = new Date();
                String tiedosto = KelaUtil.createTiedostoNimiYhva14(tiedostonLuontiPaivamaara);
                kelaService.lahetaTiedosto(tiedosto, tavut);
                return "Aktivointi onnistui!";
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("{}", e.getMessage());
            return "Aktivointi epäonnistui! " + e.getMessage();
        }
    }
}
