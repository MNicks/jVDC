package de.nikxs.digitalstrom;


import de.nikxs.digitalstrom.vdc.VdcHost;
import de.nikxs.digitalstrom.vdc.config.VdcProperties;
import de.nikxs.digitalstrom.vdc.server.VdcServer;
import de.nikxs.digitalstrom.vdc.util.DSUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
//@EnableConfigurationProperties(VdcConfig.class)
public class Application implements CommandLineRunner {

    @Autowired
    private VdcServer server;

    @Autowired
    private VdcProperties config;

    @Autowired
    private VdcHost host;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {

        host.addVdc(getMyTestVdc());

//        System.out.println("Spring Version: " + config.getSpringVersion());
//        System.out.println(server.isConnected());
//        System.out.println(host);

        // Wait a bit
        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Exiting vDC Host...processBye.");
        System.exit(0);
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyConfigIn() {
        return new PropertySourcesPlaceholderConfigurer();
    }

    @Bean
    public static MyTestVdc getMyTestVdc() {
        return new MyTestVdc(DSUID.fromDSUID("9888dd3db3454109b088777777777700"), "myTestVDC");
    }
}
