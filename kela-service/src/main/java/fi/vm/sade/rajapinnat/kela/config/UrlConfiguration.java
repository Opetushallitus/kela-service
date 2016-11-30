package fi.vm.sade.rajapinnat.kela.config;


import fi.vm.sade.properties.OphProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Paths;

@Component
public class UrlConfiguration extends OphProperties  {

    public UrlConfiguration() {
        addFiles("/kela-service-oph.properties");
        addOptionalFiles(Paths.get(System.getProperties().getProperty("user.home"), "/oph-configuration/common.properties").toString());
    }
}
