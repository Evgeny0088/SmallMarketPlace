package com.marketPlace.itemstorageservice.eventHandlers;


import lombok.extern.slf4j.Slf4j;
import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Slf4j
public class PostInsertListener implements PostInsertEventListener {
    @Override
    public void onPostInsert(PostInsertEvent postInsertEvent) {
        log.info("post event happened!!!" + Arrays.toString(postInsertEvent.getState()));
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return true;
    }
}
