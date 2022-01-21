package com.marketPlace.itemstorageservice.eventHandlers;

import lombok.RequiredArgsConstructor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

@RequiredArgsConstructor
@Service
public class ObjectsEventListeners {

    private final EntityManagerFactory entityManagerFactory;
    private final PostInsertEventListener postInsertEventListener;
    private final UpdateListener updateListener;

    @PostConstruct
    private void init() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.POST_INSERT).appendListener(postInsertEventListener);
        registry.getEventListenerGroup(EventType.POST_UPDATE).appendListener(updateListener);
    }
}
