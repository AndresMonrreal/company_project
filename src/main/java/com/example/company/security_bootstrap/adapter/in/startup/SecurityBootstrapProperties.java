package com.example.company.security_bootstrap.adapter.in.startup;

import java.util.Set;

import com.example.company.security_bootstrap.domain.port.in.SecurityBootstrapCommand;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "security.bootstrap")
public class SecurityBootstrapProperties {

    private boolean enabled = true;
    private Admin admin = new Admin();
    private DemoUsers demoUsers = new DemoUsers();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public DemoUsers getDemoUsers() {
        return demoUsers;
    }

    public void setDemoUsers(DemoUsers demoUsers) {
        this.demoUsers = demoUsers;
    }

    public SecurityBootstrapCommand toCommand(Set<String> activeProfiles, Environment environment) {
        return new SecurityBootstrapCommand(
                enabled,
                admin.username,
                admin.fullName,
                firstNonBlank(environment.getProperty("SECURITY_BOOTSTRAP_ADMIN_PASSWORD"), admin.password),
                demoUsers.enabled,
                activeProfiles,
                firstNonBlank(
                        environment.getProperty("SECURITY_BOOTSTRAP_SUPERVISOR_DEMO_PASSWORD"),
                        demoUsers.supervisor.password
                ),
                firstNonBlank(
                        environment.getProperty("SECURITY_BOOTSTRAP_OPERADOR_DEMO_PASSWORD"),
                        demoUsers.operador.password
                ),
                firstNonBlank(
                        environment.getProperty("SECURITY_BOOTSTRAP_CONSULTA_DEMO_PASSWORD"),
                        demoUsers.consulta.password
                )
        );
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback;
    }

    public static class Admin {

        private String username = "admin";
        private String fullName = "Initial Administrator";
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class DemoUsers {

        private boolean enabled = false;
        private DemoUser supervisor = new DemoUser("supervisor.demo", "Demo Supervisor");
        private DemoUser operador = new DemoUser("operador.demo", "Demo Operator");
        private DemoUser consulta = new DemoUser("consulta.demo", "Demo Consulta");

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public DemoUser getSupervisor() {
            return supervisor;
        }

        public void setSupervisor(DemoUser supervisor) {
            this.supervisor = supervisor;
        }

        public DemoUser getOperador() {
            return operador;
        }

        public void setOperador(DemoUser operador) {
            this.operador = operador;
        }

        public DemoUser getConsulta() {
            return consulta;
        }

        public void setConsulta(DemoUser consulta) {
            this.consulta = consulta;
        }
    }

    public static class DemoUser {

        private String username;
        private String fullName;
        private String password;

        public DemoUser() {
        }

        public DemoUser(String username, String fullName) {
            this.username = username;
            this.fullName = fullName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
