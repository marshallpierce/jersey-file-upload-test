package com.palominolabs.jersey;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.google.inject.servlet.GuiceFilter;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.DispatcherType;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.util.EnumSet;
import java.util.logging.LogManager;

final class JerseyUploadMain {

    private final GuiceFilter guiceFilter;

    @Inject
    JerseyUploadMain(GuiceFilter guiceFilter) {
        this.guiceFilter = guiceFilter;
    }

    public static void main(String[] args) throws Exception {

        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.install();

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new AbstractModule() {
            @Override
            protected void configure() {
                binder().requireExplicitBindings();

                bind(JerseyUploadMain.class);

                bind(GuiceFilter.class);

                install(new JerseyServletModule());

                install(new ServletModule() {
                    @Override
                    protected void configureServlets() {
                        bind(GuiceContainer.class);
                        serve("/*").with(GuiceContainer.class);
                    }
                });

                bind(UploadResource.class);
            }
        });

        injector.getInstance(JerseyUploadMain.class).go();
    }

    void go() throws Exception {

        ServletContextHandler servletHandler = new ServletContextHandler();
        servletHandler.setContextPath("/");

        // jetty always wants one servlet
        servletHandler.addServlet(new ServletHolder(new DefaultServlet()), "/*");

        // add guice servlet filter
        FilterHolder filterHolder = new FilterHolder(guiceFilter);
        servletHandler.addFilter(filterHolder, "/*", EnumSet.allOf(DispatcherType.class));

        HandlerCollection handlerCollection = new HandlerCollection();
        handlerCollection.addHandler(servletHandler);

        Server server = new Server();
        server.setHandler(handlerCollection);

        Connector connector = new SelectChannelConnector();
        connector.setPort(8080);
        connector.setHost("127.0.0.1");
        server.addConnector(connector);

        server.start();
    }
}
