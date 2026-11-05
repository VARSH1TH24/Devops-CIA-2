package com.example.cicd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HomeController {

    /** Message shown on the home page. Edit this to demonstrate a redeployment. */
    public static final String MESSAGE = "Hello from the CI/CD pipeline - version 2! Automatic deployment successful!";

    @Value("${app.git-sha:local}")
    private String gitSha;

    @GetMapping(value = "/", produces = MediaType.TEXT_HTML_VALUE)
    public String home() {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8">
              <title>CI/CD Demo</title>
              <style>
                body { font-family: system-ui, sans-serif; background:#0f172a; color:#e2e8f0;
                       display:flex; align-items:center; justify-content:center; height:100vh; margin:0; }
                .card { background:#1e293b; padding:2.5rem 3rem; border-radius:12px; text-align:center; }
                h1 { margin:0 0 .5rem; }
                code { background:#0f172a; padding:.2rem .5rem; border-radius:6px; }
              </style>
            </head>
            <body>
              <div class="card">
                <h1>%s</h1>
                <p>Build commit: <code>%s</code></p>
              </div>
            </body>
            </html>
            """.formatted(MESSAGE, gitSha);
    }

    @GetMapping("/api/info")
    public Map<String, String> info() {
        return Map.of("message", MESSAGE, "commit", gitSha, "status", "UP");
    }
}
