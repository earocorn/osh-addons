package com.botts.api.parser;


import net.opengis.swe.v20.DataBlock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vast.util.Asserts;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class LineBasedStreamProcessor implements IStreamProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LineBasedStreamProcessor.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final IDataParser parser;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile Future<?> task;
    private final Object taskLock = new Object();

    public LineBasedStreamProcessor(IDataParser parser) {
        this.parser = Asserts.checkNotNull(parser, "parser");
    }

    @Override
    public void processStream(InputStream inputStream, Consumer<DataBlock> consumer) {
        Asserts.checkNotNull(inputStream, "inputStream");
        Asserts.checkNotNull(consumer, "consumer");

        if (isRunning.compareAndSet(false, true)) {
            logger.warn("Stream is already running");
            return;
        }

        logger.debug("Starting stream processing");
        synchronized (taskLock) {
            task = executor.submit(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while (isRunning.get() && (line = reader.readLine()) != null) {
                        try {
                            DataBlock dataBlock = parser.parse(line.getBytes());
                            if (dataBlock != null) {
                                consumer.accept(dataBlock);
                            }
                        } catch (Exception e) {
                            logger.error("Error parsing line: {}", line, e);
                        }
                    }
                } catch (IOException e) {
                    logger.error("Stream processing error", e);
                }
            });
        }
    }

    @Override
    public void stop() {
        logger.debug("Stopping stream processor");

        isRunning.set(false);

        synchronized (taskLock) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
        }

        logger.debug("Stream processor stopped");
    }

}
