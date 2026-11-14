package com.gamerlinker.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/password-recovery")
public class PasswordRecoveryController {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int CODE_VALIDITY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final JdbcTemplate jdbcTemplate;
    private final RestClient restClient;
    private final JavaMailSender mailSender;

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-role-key}")
    private String serviceRoleKey;

    @Value("${app.mail.from:${spring.mail.username}}")
    private String mailFrom;

    public PasswordRecoveryController(JdbcTemplate jdbcTemplate, JavaMailSender mailSender) {
        this.jdbcTemplate = jdbcTemplate;
        this.mailSender = mailSender;
        this.restClient = RestClient.builder().build();
    }

    public record RequestCode(String email) {}
    public record ConfirmCode(String email, String code, String newPassword) {}
    private record StoredCode(String codeHash, Timestamp expiresAt, int attempts) {}

    /** Gera e envia por e-mail um código temporário; o código nunca é devolvido ao navegador. */
    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestCode(@RequestBody RequestCode request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || !request.email().trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Informe um e-mail válido."));
        }

        String email = normalizeEmail(request.email());
        try {
            // Não revela ao navegador se o e-mail existe ou não no sistema.
            String userId = findSupabaseUserIdByEmail(email);
            if (userId != null) {
                String code = String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000));
                String codeHash = sha256(code);
                Timestamp expiresAt = Timestamp.from(Instant.now().plusSeconds(CODE_VALIDITY_MINUTES * 60L));

                jdbcTemplate.update("""
                    INSERT INTO public.password_recovery_codes (email, code_hash, expires_at, attempts, created_at)
                    VALUES (?, ?, ?, 0, NOW())
                    ON CONFLICT (email) DO UPDATE SET
                        code_hash = EXCLUDED.code_hash,
                        expires_at = EXCLUDED.expires_at,
                        attempts = 0,
                        created_at = NOW()
                    """, email, codeHash, expiresAt);

                try {
                    SimpleMailMessage message = new SimpleMailMessage();
                    message.setFrom(mailFrom);
                    message.setTo(email);
                    message.setSubject("GamerLinker - Código de recuperação de senha");
                    message.setText("Olá!\n\n"
                            + "Recebemos uma solicitação para redefinir a senha da sua conta GamerLinker.\n\n"
                            + "Seu código de recuperação é: " + code + "\n\n"
                            + "O código expira em 10 minutos e pode ser usado uma única vez.\n"
                            + "Se você não solicitou essa alteração, ignore este e-mail.\n\n"
                            + "Equipe GamerLinker");
                    mailSender.send(message);
                } catch (MailException ex) {
                    jdbcTemplate.update("DELETE FROM public.password_recovery_codes WHERE email = ?", email);
                    throw ex;
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Se o e-mail estiver cadastrado, enviaremos um código de recuperação."
            ));
        
        } catch (MailException ex) {
            System.err.println("ERRO NO ENVIO DE EMAIL:");
            ex.printStackTrace();

            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Falha no envio do e-mail. Confira os logs."
            ));

        } catch (RestClientResponseException ex) {
            System.err.println("ERRO NA RESPOSTA DO SUPABASE:");
            System.err.println("HTTP: " + ex.getStatusCode());
            System.err.println("RESPOSTA: " + ex.getResponseBodyAsString());
            ex.printStackTrace();

            return ResponseEntity.status(502).body(Map.of(
                "error", "Falha na comunicação com o Supabase."
            ));

        } catch (Exception ex) {
            System.err.println("ERRO INTERNO NA RECUPERAÇÃO DE SENHA:");
            ex.printStackTrace();

            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro interno ao solicitar a recuperação."
            ));
        }

    }

    /** Valida o código e altera a senha diretamente no Supabase Auth Admin API. */
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmCode(@RequestBody ConfirmCode request) {
        if (request == null || request.email() == null || request.code() == null
                || request.newPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Preencha todos os campos."));
        }

        String email = normalizeEmail(request.email());
        String code = request.code().trim();
        String newPassword = request.newPassword();

        if (!code.matches("\\d{6}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Código inválido."));
        }
        if (newPassword.length() < 8) {
            return ResponseEntity.badRequest().body(Map.of("error", "A senha deve ter pelo menos 8 caracteres."));
        }

        StoredCode stored = jdbcTemplate.query("""
            SELECT code_hash, expires_at, attempts
            FROM public.password_recovery_codes
            WHERE email = ?
            """, rs -> rs.next()
                ? new StoredCode(rs.getString("code_hash"), rs.getTimestamp("expires_at"), rs.getInt("attempts"))
                : null, email);

        if (stored == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Solicite um novo código de recuperação."));
        }

        if (stored.expiresAt().toInstant().isBefore(Instant.now())) {
            jdbcTemplate.update("DELETE FROM public.password_recovery_codes WHERE email = ?", email);
            return ResponseEntity.badRequest().body(Map.of("error", "O código expirou. Solicite outro."));
        }

        if (stored.attempts() >= MAX_ATTEMPTS) {
            jdbcTemplate.update("DELETE FROM public.password_recovery_codes WHERE email = ?", email);
            return ResponseEntity.badRequest().body(Map.of("error", "Limite de tentativas atingido. Solicite outro código."));
        }

        if (!MessageDigest.isEqual(stored.codeHash().trim().getBytes(StandardCharsets.UTF_8),
                sha256(code).getBytes(StandardCharsets.UTF_8))) {
            jdbcTemplate.update("UPDATE public.password_recovery_codes SET attempts = attempts + 1 WHERE email = ?", email);
            return ResponseEntity.badRequest().body(Map.of("error", "Código incorreto."));
        }

        try {
            String userId = findSupabaseUserIdByEmail(email);
            if (userId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Não foi encontrada uma conta com esse e-mail."));
            }

            restClient.put()
                .uri(supabaseUrl.replaceAll("/$", "") + "/auth/v1/admin/users/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .header("apikey", serviceRoleKey)
                .body(Map.of("password", newPassword))
                .retrieve()
                .toBodilessEntity();

            jdbcTemplate.update("DELETE FROM public.password_recovery_codes WHERE email = ?", email);
            return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));

        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(502).body(Map.of(
                "error", "O Supabase não aceitou a alteração da senha. Confira a URL e a chave administrativa no backend."
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Erro interno ao atualizar a senha. Confira o console do backend."
            ));
        }
    }

    private String findSupabaseUserIdByEmail(String email) {
    JsonNode response = restClient.get()
        .uri(supabaseUrl.replaceAll("/$", "") + "/auth/v1/admin/users?page=1&per_page=1000")
        .header("apikey", serviceRoleKey)
        .retrieve()
        .body(JsonNode.class);

    if (response == null || !response.has("users") || !response.get("users").isArray()) {
        return null;
    }

    for (JsonNode user : response.get("users")) {
        String userEmail = user.path("email").asText("");
        if (email.equalsIgnoreCase(userEmail)) {
            return user.path("id").asText(null);
        }
    }

    return null;
}

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível gerar o hash do código.", ex);
        }
    }
}
