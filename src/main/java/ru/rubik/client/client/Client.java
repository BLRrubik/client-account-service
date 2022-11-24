package ru.rubik.client.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.rubik.client.request.AmountAddRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class Client {
    private final RestTemplate restTemplate;

    @Autowired
    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void run() throws InterruptedException {

        //количество читателей вызывающих метод getAmount(id)
        Integer rCount = 5;
        //количество читателей вызывающих метод addAmount(id,value)
        Integer wCount = 5;

        Set<Runnable> tasks = new HashSet<>();

        for (int i = 0; i < wCount; i++) {
            final int copyOfI = i;
            tasks.add(() -> {
                try {
                    restTemplate.postForEntity(
                            new URI("http://localhost:8080/amount"),
                            new AmountAddRequest(1, 100L),
                            Long.class);
                    System.out.println("Post ok: " + copyOfI);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        for (int i = 0; i < rCount; i++) {
            final int copyOfI = i;
            tasks.add(() -> {
                try {
                    Long amount = restTemplate.getForObject(
                            new URI("http://localhost:8080/amount/1"),
                            Long.class);
                    System.out.println("Get ok: " + copyOfI  + " value " + amount);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        ExecutorService executor = Executors.newFixedThreadPool(wCount + rCount);

        for (Runnable task: tasks) {
            executor.execute(task);
            Thread.sleep(400);
        }

        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {}
    }
}
