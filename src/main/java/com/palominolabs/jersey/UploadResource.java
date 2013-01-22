package com.palominolabs.jersey;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("resource-test")
public final class UploadResource {

    // just to easily test if resource is wired
    @GET
    public String get() {
        return this.getClass().getCanonicalName();
    }
}
