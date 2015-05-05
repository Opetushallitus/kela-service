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

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledExport {
    private static final Logger LOG = Logger.getLogger(ScheduledExport.class);
    
    @Autowired
    KelaResourceImpl kelaResource;
    
    private String reportList;
    private boolean schedulingEnabled = false;
    
    @Scheduled(cron = "${kela.scheduler.cron.expression}")
    public void doExport() {
        if (schedulingEnabled) {
            LOG.info("Starting scheduled export of the following reports: " + reportList);
            kelaResource.exportKela("START", reportList);
        } else {
            LOG.info("Scheduled export is disabled, do nothing.");
        }
    }

    @Value("${kela.scheduler.reportlist}")
    public void setReportList(String reportList) {
        this.reportList = reportList;
    }

    @Value("${kela.scheduler.enabled}")
    public void setSchedulingEnabled(boolean schedulingEnabled) {
        this.schedulingEnabled = schedulingEnabled;
    }
}
