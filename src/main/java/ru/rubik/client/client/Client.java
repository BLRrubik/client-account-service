package ru.rubik.client.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.rubik.client.request.AmountAddRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
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
        Integer rCount = 10;
        //количество читателей вызывающих метод addAmount(id,value)
        Integer wCount = 10;

        List<Runnable> tasks = new ArrayList<>();

        for (int i = 0; i < wCount; i++) {
            final int copyOfI = i;
            tasks.add(() -> {
                try {
                    restTemplate.postForObject(
                            new URI("http://localhost:8080/amount"),
                            new AmountAddRequest(1, 100L),
                            Long.class);
                    System.out.println("Post ok: " + copyOfI);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

//        for (int i = 0; i < rCount; i++) {
//            final int copyOfI = i;
//            tasks.add(() -> {
//                try {
//                    System.out.println( "get Ok " + copyOfI + " value " +
//                            restTemplate.getForObject(new URI("http://localhost:8080/amount/1"), Long.class)
//                    );
//                } catch (URISyntaxException e) {
//                    throw new RuntimeException(e);
//                }
//            });
//        }



        ExecutorService executor = Executors.newFixedThreadPool(wCount + rCount);

        for (Runnable task: tasks) {
            executor.execute(task);
        }

        executor.shutdown();
        while (!executor.awaitTermination(1, TimeUnit.SECONDS)) {}
    }
}
