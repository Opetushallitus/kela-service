package fi.vm.sade.rajapinnat.kela.formaatti;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.test.context.ContextConfiguration;

@Configuration
@ContextConfiguration(classes = KelaHaeSijoittelunTuloksetTesti.class)
@ImportResource({ "classpath:kela-testi-context.xml" })
public class KelaFakeFtpSuite {

}
