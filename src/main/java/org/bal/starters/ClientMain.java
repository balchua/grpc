package org.bal.starters;

import org.bal.app.client.PersonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Timer;
import java.util.TimerTask;

@SpringBootApplication(scanBasePackages = "org.bal.app.client.config")
public class ClientMain implements CommandLineRunner {

    @Autowired
    private PersonClient personService;

    public static void main(String... args) throws Exception {
        SpringApplication.run(ClientMain.class, args);

    }

    @Override
    public void run(String... strings) throws Exception {
        Timer task = new Timer();

        task.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                personService.getPersonById(123);
            }
        }, 10, 500);
    }
}
