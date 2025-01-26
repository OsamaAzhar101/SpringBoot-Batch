package com.javapulses.batch.listener;

import org.springframework.batch.core.listener.ChunkListenerSupport;
import org.springframework.batch.core.scope.context.ChunkContext;

public class CustomRewardChunkListener extends ChunkListenerSupport {
    @Override
    public void beforeChunk(ChunkContext context) {
        System.out.println("Starting new chunk...");
    }

    @Override
    public void afterChunk(ChunkContext context) {
        System.out.println("Chunk completed. Items processed: " +
                context.getStepContext().getStepExecution().getReadCount());
    }

    @Override
    public void afterChunkError(ChunkContext context) {
        System.out.println("Error in chunk processing.");
    }
}
