/*
 * Copyright (c) 2012 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 */
package fi.vm.sade.rajapinnat.kela;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import fi.vm.sade.organisaatio.resource.api.TasoJaLaajuusDTO;
import fi.vm.sade.rajapinnat.kela.config.UrlConfiguration;
import fi.vm.sade.rajapinnat.kela.dto.TasoJaLaajuusContainer;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import fi.vm.sade.organisaatio.resource.api.KelaResource;
import fi.vm.sade.rajapinnat.kela.dao.KelaDAO;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Hakukohde;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.Koulutusmoduuli;
import fi.vm.sade.rajapinnat.kela.tarjonta.model.KoulutusmoduuliToteutus;

@Path("/kela")
@Component
@Api(value = "/kela", description = "Kelan operaatiot")
public class KelaResourceImpl implements KelaResource {

    @Autowired
    @Qualifier("kelaTask")
    private TaskExecutor taskExecutor;

    @Autowired
    protected KelaDAO kelaDAO;

    @Autowired
    private UrlConfiguration urlConfiguration;

    public enum Command {

        START,
        STOP,
        STATUS,
        LOG
    }

    public enum Response {

        UNKNOWN_COMMAND,
        ALREADY_RUNNING,
        FAILED,
        STARTED,
        STOP_REQUESTED,
        STATUS,
        NOT_STARTED,
        FILE_NOT_FOUND
    }

    @Autowired
    private KelaGenerator kelaGenerator;
    private Thread kelaGeneratorThread;

    @GET
    @Path("/export")
    @Produces("text/plain")
    @ApiOperation(value = "Vie Kelan tiedot", notes = "Operaatio vie Kelan tiedot.", response = String.class)
    public synchronized String exportKela(@QueryParam("command") final String command,
            @QueryParam("options") final String options
    ) {
        Command cmd;
        try {
            cmd = Command.valueOf(command);
        } catch (IllegalArgumentException e) {
            return Response.UNKNOWN_COMMAND.name() + ": " + command;
        }
        switch (cmd) {
            case START:
                return start(options);
            case STOP:
                return stop();
            case STATUS:
                return status();
            case LOG:
                return log();
        }
        return null; //should never be reached
    }

    private String start(String options) {
        if (kelaGenerator != null && !KelaGenerator.startableStates.contains(kelaGenerator.getThreadState().getRunState())) {
            return Response.ALREADY_RUNNING.name();
        }
        try {
            kelaGeneratorThread = new Thread((Runnable) kelaGenerator);
            kelaGenerator.clearOptions();
            if (!StringUtils.isEmpty(options)) {
                kelaGenerator.setOptions(options);
            }
            taskExecutor.execute(kelaGeneratorThread);
        } catch (Exception e) {
            e.printStackTrace();
            kelaGenerator = null;
            return Response.FAILED.name();
        }
        return Response.STARTED.name();
    }

    private String stop() {
        if (kelaGenerator == null) {
            return Response.NOT_STARTED.name();
        }
        kelaGenerator.stop();
        return Response.STOP_REQUESTED.name();
    }

    private String status() {
        if (null == kelaGenerator) {
            return Response.NOT_STARTED.name();
        }
        return Response.STATUS.name() + ": " + kelaGenerator.getThreadState();
    }

    private String log() {
        if (null == kelaGenerator) {
            return Response.NOT_STARTED.name();
        }
        return readFile(kelaGenerator.getThreadState().getLogFileName());
    }

    private String readFile(String file) {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e1) {
            return Response.FILE_NOT_FOUND.name() + ": " + file;
        }

        String line = null;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        try {
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append(ls);
            }
        } catch (IOException e) {
            return e.getMessage();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    @Value("${socksProxy:off}")
    public void setSocksProxy(String socksProxyOn) {
        if (socksProxyOn.equalsIgnoreCase("ON")) {
            KelaGenerator.setSocksProxyOn();
        }
    }

    @Override
    public TasoJaLaajuusDTO tutkinnontaso(String hakukohdeOid) {
        Hakukohde hakukohde = haeHakukohde(hakukohdeOid);
        TasoJaLaajuusContainer ylempiTaso = null;
        TasoJaLaajuusContainer alempiTaso = null;
        for (KoulutusmoduuliToteutus komoto : hakukohde.getKoulutukset()) {
            TasoJaLaajuusContainer komotoTutkinnonTaso = kelaDAO.getKKTutkinnonTaso(komoto);
            if (komotoTutkinnonTaso.hasTaso()) {
                if (komotoTutkinnonTaso.isYlempi()) {
                    ylempiTaso = komotoTutkinnonTaso;
                }
                if (komotoTutkinnonTaso.isAlempi()) {
                    alempiTaso = komotoTutkinnonTaso;
                }
                if(komotoTutkinnonTaso.isHammaslaakis() || komotoTutkinnonTaso.isLaakis() || komotoTutkinnonTaso.isYlempiAlempi()) {
                    return komotoTutkinnonTaso.toDTO(urlConfiguration);
                }
            }
        }
        /*
         * jos pelkkiä ylempiä => 061 (erillinen ylempi kk.tutkinto)
         */
        if (ylempiTaso != null && alempiTaso == null) {
            return ylempiTaso.toDTO(urlConfiguration);
        }
        /*
         * jos pelkkiä alempia => 050  (alempi kk.tutkinto)
         */
        if (ylempiTaso == null && alempiTaso != null) {
            return alempiTaso.toDTO(urlConfiguration);
        }
        /*
         * jos väh. 1 ylempiä ja väh. 1 => 060 (alempi+ylempi)
         */
        if (ylempiTaso != null && alempiTaso != null) {
            TasoJaLaajuusContainer cont = new TasoJaLaajuusContainer();
            cont.ylempiAlempi(ylempiTaso.getKomoId1(), alempiTaso.getKomoId1());
            return cont.toDTO(urlConfiguration);
        }
        /*
         *  jos ei kumpiakaan : koulutuksen tasoa ei merkitä
         */
        return new TasoJaLaajuusContainer().eiTasoa().toDTO(urlConfiguration);

    }


    /*
     * Jos uri = koulutusasteoph2002_62 tai koulutusasteoph2002_71 -> AMK
     */
    private boolean amk(String s) {
        return s != null && (s.startsWith("koulutusasteoph2002_62#") || s.startsWith("koulutusasteoph2002_71#"));
    }

    /*
     * Jos uri = koulutusasteoph2002_63 tai koulutusasteoph2002_72 tai koulutusasteoph2002_73 tai koulutusasteoph2002_80 tai koulutusasteoph2002_81 tai koulutusasteoph2002_82 ->yliopisto
     */
    private boolean yliopisto(String s) {
        return s != null && (s.startsWith("koulutusasteoph2002_63#") || s.startsWith("koulutusasteoph2002_72#")
                || s.startsWith("koulutusasteoph2002_73#") || s.startsWith("koulutusasteoph2002_80#")
                || s.startsWith("koulutusasteoph2002_81#") || s.startsWith("koulutusasteoph2002_82#")
                || s.startsWith("koulutusasteoph2002_70#"));
    }

    private Hakukohde haeHakukohde(String oid) {
        if (oid == null) {
            throw new RuntimeException("hakukohde oid on tyhjä");
        }
        Hakukohde hakukohde = kelaDAO.findHakukohdeByOid(oid);
        if (hakukohde == null) {
            throw new RuntimeException("hakukohde (oid:" + oid + ") ei löydy hakukohteita");
        }
        return hakukohde;
    }

    @Override
    public String koulutusaste(String hakukohdeOid) {
        Hakukohde hakukohde = haeHakukohde(hakukohdeOid);
        boolean amk = false;
        boolean yliopisto = false;
        for (KoulutusmoduuliToteutus komoto : hakukohde.getKoulutukset()) {
            Koulutusmoduuli koulutusmoduuli = komoto.getKoulutusmoduuli();
            if (koulutusmoduuli != null && koulutusmoduuli.getKoulutusaste_uri() != null) {
                String koulutusaste_uri = koulutusmoduuli.getKoulutusaste_uri();
                if (!amk) {
                    amk = amk(koulutusaste_uri);
                }
                if (!yliopisto) {
                    yliopisto = yliopisto(koulutusaste_uri);
                }
                if (amk && yliopisto) {
                    break; //should not be!
                }
            }
        }
        if (amk && yliopisto) {
            return "ERROR ";
        }
        if (amk) {
            return "OUHARA";
        }
        if (yliopisto) {
            return "OUHARE";
        }
        return "OUYHVA"; //2.asteen koulutus (default)
    }
}
