package com.mulaflow.mulaflow.service.notification.templates;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.mulaflow.mulaflow.model.notification.NotificationType;

@Component
public class NotificationTemplateEngine {

    private final Map<NotificationType, NotificationTemplate> templates;
    private final ResourceLoader resourceLoader;

    public NotificationTemplateEngine(ResourceLoader resourceLoader) {
        this.templates = new HashMap<>();
        this.resourceLoader = resourceLoader;
        initializeTemplates();
    }

    public String compileBody(NotificationType type, Map<String, String> data) {
        return StringSubstitutor.replace(
            templates.get(type).getBodyTemplate(),
            data
        );
    }

    public String processTemplate(NotificationType type, Map<String, Object> variables) {
        NotificationTemplate template = templates.get(type);
        return StringSubstitutor.replace(
            template.getBodyTemplate(),
            variables
        );
    }

    private void initializeTemplates() {
        for (NotificationType type: NotificationType.values()) {
            templates.put(type, loadTemplate(type));
        }
    }

    private NotificationTemplate loadTemplate(NotificationType type) {
        try {
            Resource resource = resourceLoader.getResource(
                "classpath:templates/notifications/" + type.getTemplateName() + ".html"
            );

            String htmlContent = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
            );

            return new NotificationTemplate(
                type.getDefaultTitle(),
                htmlContent,
                htmlContent
            );
        } catch (Exception ex) {
            throw new RuntimeException("failed to load templates for " + type, ex);
        }
    }
}
