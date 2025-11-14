package com.chat.chatbot_be.infra.bootstrap;

import com.chat.chatbot_be.infra.loader.GridDataLoader;
import com.chat.chatbot_be.infra.vector.VectorDbClient;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    private final GridDataLoader gridDataLoader;
    private final VectorDbClient vectorDbClient;

    public DataInitializer(GridDataLoader gridDataLoader,
                           VectorDbClient vectorDbClient) {
        this.gridDataLoader = gridDataLoader;
        this.vectorDbClient = vectorDbClient;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        boolean alreadyInitialized = vectorDbClient.isInitialized();
        if (alreadyInitialized) {
            System.out.println("[DataInitializer] Qdrant already initialized. Skip upsert.");
            return;
        }

        System.out.println("[DataInitializer] Start loading Excel data...");
        var rows = gridDataLoader.load();
        vectorDbClient.upsertAll(rows);
        System.out.println("[DataInitializer] Finished Qdrant initialization.");
    }
}
