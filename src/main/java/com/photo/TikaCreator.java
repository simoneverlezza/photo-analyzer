package com.photo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Produces;
import org.apache.tika.Tika;

@ApplicationScoped
public class TikaCreator {

    @Produces
    @ApplicationScoped
    public Tika tika() {
        return new Tika();
    }

}
