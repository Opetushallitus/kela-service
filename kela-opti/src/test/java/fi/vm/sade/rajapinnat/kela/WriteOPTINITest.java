package fi.vm.sade.rajapinnat.kela;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@ContextConfiguration(locations = "classpath:META-INF/spring/context/bundle-context.xml")
@TestExecutionListeners(listeners = {
        DependencyInjectionTestExecutionListener.class,
        DirtiesContextTestExecutionListener.class,
        TransactionalTestExecutionListener.class
    })
@RunWith(SpringJUnit4ClassRunner.class)
public class WriteOPTINITest {
    
    @Autowired
    private WriteOPTINI optiniWriter;
    
    @Test
    public void testWriteOptiniHappyPath() {
        try {
            //optiniWriter.writeFile();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
