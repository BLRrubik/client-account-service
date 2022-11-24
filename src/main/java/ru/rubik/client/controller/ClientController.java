package ru.rubik.client.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.rubik.client.client.Client;

@RestController
public class ClientController {
    @Autowired
    private Client client;

    @GetMapping("/startClient")
    public void runClient() throws InterruptedException {
        client.run();
    }
}
