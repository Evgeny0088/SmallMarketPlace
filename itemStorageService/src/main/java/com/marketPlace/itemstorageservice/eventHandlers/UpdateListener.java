package com.marketPlace.itemstorageservice.eventHandlers;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class UpdateListener implements PostUpdateEventListener {
    @Override
    public void onPostUpdate(PostUpdateEvent postUpdateEvent) {
        log.info("update event happened!!!" + Arrays.toString(postUpdateEvent.getState()));
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return true;
    }
}
