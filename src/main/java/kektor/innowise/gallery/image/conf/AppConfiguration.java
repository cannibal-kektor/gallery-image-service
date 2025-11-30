package kektor.innowise.gallery.image.conf;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class AppConfiguration {

    @Bean
    public ApplicationRunner startupHealthCheck() {
        return _ -> {
            File healthCheckFile = new File("/tmp/healthy");
            healthCheckFile.createNewFile();
            Runtime.getRuntime().addShutdownHook(new Thread(healthCheckFile::delete));
        };
    }

}
