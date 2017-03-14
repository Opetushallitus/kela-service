package fi.vm.sade.rajapinnat.kela;

import fi.vm.sade.integrationtest.tomcat.EmbeddedTomcat;
import fi.vm.sade.integrationtest.util.ProjectRootFinder;
import org.apache.catalina.LifecycleException;

import javax.servlet.ServletException;

public class StartDev extends EmbeddedTomcat {
    static final String VTJ_MODULE_ROOT = ProjectRootFinder.findProjectRoot() + "/kela/kela-service";
    static final String VTJ_CONTEXT_PATH = "/kela-service";
    static final int DEFAULT_PORT = 8081;
    static final int DEFAULT_AJP_PORT = 8006;

    public final static void main(String... args) throws ServletException, LifecycleException {
        new StartDev(8081, 8006).start().await();
    }

    public StartDev(int port, int ajpPort) {
        super(port, ajpPort, VTJ_MODULE_ROOT, VTJ_CONTEXT_PATH);
    }

}