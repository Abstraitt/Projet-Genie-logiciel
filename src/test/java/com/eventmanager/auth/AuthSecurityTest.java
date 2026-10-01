package com.eventmanager.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityTest {

    static final String PASSWORD = "Secret123";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    // ---------- helpers ----------
    private String uniqueEmail() { return "user-" + UUID.randomUUID() + "@test.cm"; }

    private void register(String email, String password) throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email, "password", password, "firstName", "Jean", "lastName", "Dupont"))))
           .andExpect(status().isCreated());
    }

    private JsonNode login(String email, String password, int expectedStatus) throws Exception {
        String body = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().is(expectedStatus)).andReturn().getResponse().getContentAsString();
        return json.readTree(body);
    }

    // ---------- inscription ----------
    @Test
    void register_ok_and_password_never_returned() throws Exception {
        String email = uniqueEmail();
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email, "password", PASSWORD, "firstName", "Jean", "lastName", "Dupont"))))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.email").value(email))
           .andExpect(jsonPath("$.role").value("USER"))
           .andExpect(jsonPath("$.password").doesNotExist())
           .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void register_duplicate_email_conflict() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", email, "password", PASSWORD, "firstName", "A", "lastName", "B"))))
           .andExpect(status().isConflict());
    }

    @Test
    void register_weak_password_rejected() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", uniqueEmail(), "password", "abc", "firstName", "A", "lastName", "B"))))
           .andExpect(status().isBadRequest());
    }

    @Test
    void register_cannot_self_assign_admin() throws Exception {
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of(
                        "email", uniqueEmail(), "password", PASSWORD, "firstName", "A", "lastName", "B",
                        "role", "ADMIN"))))
           .andExpect(status().isBadRequest());
    }

    // ---------- connexion ----------
    @Test
    void login_ok_returns_tokens() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        JsonNode res = login(email, PASSWORD, 200);
        org.junit.jupiter.api.Assertions.assertFalse(res.get("accessToken").asText().isBlank());
        org.junit.jupiter.api.Assertions.assertFalse(res.get("refreshToken").asText().isBlank());
    }

    @Test
    void login_wrong_password_and_unknown_email_give_same_message() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String a = login(email, "Wrong123x", 401).get("error").asText();
        String b = login(uniqueEmail(), "Wrong123x", 401).get("error").asText();
        org.junit.jupiter.api.Assertions.assertEquals(a, b);
    }

    @Test
    void login_sql_injection_payload_is_harmless() throws Exception {
        login("' OR '1'='1' --", "' OR '1'='1", 401);
    }

    @Test
    void brute_force_locks_account() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        for (int i = 0; i < 5; i++) login(email, "Wrong123x", 401);
        login(email, PASSWORD, 423); // même le bon mot de passe est refusé pendant le verrouillage
    }

    // ---------- accès protégé ----------
    @Test
    void protected_route_without_token_is_401() throws Exception {
        mvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void forged_and_tampered_tokens_are_rejected() throws Exception {
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer aaa.bbb.ccc"))
           .andExpect(status().isUnauthorized());

        String email = uniqueEmail();
        register(email, PASSWORD);
        String token = login(email, PASSWORD, 200).get("accessToken").asText();
        String tampered = token.substring(0, token.length() - 3) + (token.endsWith("AAA") ? "BBB" : "AAA");
        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + tampered))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void profile_read_and_update() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String token = login(email, PASSWORD, 200).get("accessToken").asText();

        mvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
           .andExpect(status().isOk()).andExpect(jsonPath("$.email").value(email));

        mvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("firstName", "Marie", "lastName", "Kagho", "phone", "+237600000000"))))
           .andExpect(status().isOk()).andExpect(jsonPath("$.firstName").value("Marie"));
    }

    @Test
    void normal_user_cannot_access_admin_route() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String token = login(email, PASSWORD, 200).get("accessToken").asText();
        mvc.perform(get("/api/admin/users").header("Authorization", "Bearer " + token))
           .andExpect(status().isForbidden());
    }

    // ---------- refresh / logout ----------
    @Test
    void refresh_rotation_and_reuse_detection() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String r1 = login(email, PASSWORD, 200).get("refreshToken").asText();

        String body = mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken", r1))))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String r2 = json.readTree(body).get("refreshToken").asText();

        // Réutiliser l'ancien token doit échouer et révoquer aussi le nouveau
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken", r1))))
           .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken", r2))))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_revokes_refresh_token() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String r = login(email, PASSWORD, 200).get("refreshToken").asText();
        mvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken", r))))
           .andExpect(status().isNoContent());
        mvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("refreshToken", r))))
           .andExpect(status().isUnauthorized());
    }

    @Test
    void change_password_flow() throws Exception {
        String email = uniqueEmail();
        register(email, PASSWORD);
        String token = login(email, PASSWORD, 200).get("accessToken").asText();
        mvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(Map.of("currentPassword", PASSWORD, "newPassword", "NewSecret456"))))
           .andExpect(status().isNoContent());
        login(email, PASSWORD, 401);
        login(email, "NewSecret456", 200);
    }
}
