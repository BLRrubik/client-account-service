package ru.rubik.client.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ru.rubik.client.request.AmountAddRequest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.*;

@Component
public class Client {
    private final RestTemplate restTemplate;

    private final Long value = 100L;

    @Value("${rCount}")
    private Integer rCount;

    @Value("${wCount}")
    private Integer wCount;

    @Value("${idList}")
    private List<Integer> idList = new ArrayList<>();

    @Autowired
    public Client(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void run() throws InterruptedException {
        BlockingQueue<Runnable> queue = generateTasks();

        Thread thread;

        while (!queue.isEmpty()) {
            thread = new Thread(queue.take());
            thread.start();
            thread.join();
        }
    }

    private BlockingQueue<Runnable> generateTasks() throws InterruptedException {
        Random random = new Random();

        BlockingQueue<Runnable> queue =
                new ArrayBlockingQueue<>(rCount + wCount);

        Set<Runnable> tasks = new HashSet<>();

        for (int i = 0; i < wCount; i++) {
            final int copyOfI = i;
            tasks.add(() -> {
                var id = idList.get(random.nextInt(0, idList.size()));
                try {
                    restTemplate.postForEntity(
                            new URI("http://localhost:8080/api/v1/accounts/amount"),
                            new AmountAddRequest(id, value),
                            Long.class);
                    System.out.println("Post ok: " + copyOfI + ", id: " + id);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        for (int i = 0; i < rCount; i++) {
            final int copyOfI = i;
            tasks.add(() -> {
                var id = idList.get(random.nextInt(0, idList.size()));
                try {
                    Long amount = restTemplate.getForObject(
                            new URI("http://localhost:8080/api/v1/accounts/amount/" + id),
                            Long.class);
                    System.out.println("Get ok: " + copyOfI  + ", id: " + id + ", value: " + amount);

                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        for (Runnable task : tasks) {
            queue.put(task);
        }

        return queue;
    }
}
