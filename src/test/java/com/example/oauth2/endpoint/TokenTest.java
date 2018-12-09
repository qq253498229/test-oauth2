package com.example.oauth2.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;

@SpringBootTest
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
public class TokenTest {
    @Resource
    private MockMvc mockMvc;

    private static final String CLIENT = "client";
    private static final String SECRET = "secret";
    private static final String USERNAME = "user";
    private static final String PASSWORD = "password";
    private static final String REDIRECT_URI = "http://www.baidu.com";


    /**
     * 测试获取token
     */
    @Test
    public void testGetToken() throws Exception {
        // 获取code
        String code = getCode();
        // 通过code获取token
        Map map = getToken(code);
        String token = (String) map.get("access_token");
        // 校验token是否正确
        checkToken(token);
        // 刷新token
        String refreshToken = (String) map.get("refresh_token");
        Map refresh = refreshToken(refreshToken);
        String token1 = (String) refresh.get("access_token");
        // 校验刷新之后的token是否正确
        checkToken(token1);
    }

    /**
     * 获取code
     */
    private String getCode() throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth/authorize")
                .param("client_id", CLIENT)
                .param("response_type", "code")
                .param("redirect_uri", REDIRECT_URI)
                .with(httpBasic(USERNAME, PASSWORD))
                .with(csrf()))
                .andExpect(redirectedUrlPattern(REDIRECT_URI + "?code=*"))
                .andReturn();

        String redirectedUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectedUrl);
        String code = redirectedUrl.split("code=")[1];
        assertNotNull(code);
        return code;
    }

    /**
     * 通过code获取token
     */
    private Map getToken(String code) throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(CLIENT, SECRET))
                .param("grant_type", "authorization_code")
                .param("redirect_uri", REDIRECT_URI)
                .param("code", code))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.expires_in").isNotEmpty())
                .andExpect(jsonPath("$.scope").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        ObjectMapper om = new ObjectMapper();
        return om.readValue(body, HashMap.class);
    }

    /**
     * 校验token是否正确
     */
    private void checkToken(String token) throws Exception {
        mockMvc.perform(get("/oauth/check_token")
                .with(httpBasic(CLIENT, SECRET))
                .param("token", token))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.active").isNotEmpty())
                .andExpect(jsonPath("$.exp").isNotEmpty())
                .andExpect(jsonPath("$.user_name").isNotEmpty())
                .andExpect(jsonPath("$.authorities").isNotEmpty())
                .andExpect(jsonPath("$.client_id").isNotEmpty())
                .andExpect(jsonPath("$.scope").isNotEmpty())
        ;
    }

    /**
     * 刷新token
     */
    private Map refreshToken(String refreshToken) throws Exception {
        MvcResult result = mockMvc.perform(post("/oauth/token")
                .with(httpBasic(CLIENT, SECRET))
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken))
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").isNotEmpty())
                .andExpect(jsonPath("$.refresh_token").isNotEmpty())
                .andExpect(jsonPath("$.expires_in").isNotEmpty())
                .andExpect(jsonPath("$.scope").isNotEmpty())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        ObjectMapper om = new ObjectMapper();
        return om.readValue(body, HashMap.class);
    }


}