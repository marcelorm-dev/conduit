package com.marcelormdev.conduit_service.apitest;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.marcelormdev.conduit_service.common.security.JwtTokenService;

@SpringBootTest
public abstract class ControllerTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    protected JwtTokenService jwtTokenService;

    protected RestTestClient restClient;

    @BeforeEach
    void setupClient() {
        restClient = RestTestClient.bindTo(MockMvcBuilders.webAppContextSetup(wac).build()).build();
    }

}
