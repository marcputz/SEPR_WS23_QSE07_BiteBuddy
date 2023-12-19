package at.ac.tuwien.sepr.groupphase.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bitebuddy")
public class ConfigProperties {

    private Email email;

    public Email getEmail() {
        return email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public static class Email {

        private String address;
        private Smtp smtp;

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public Smtp getSmtp() {
            return smtp;
        }

        public void setSmtp(Smtp smtp) {
            this.smtp = smtp;
        }

        public static class Smtp {

            private String host = "localhost";
            private int port = 25;
            private String username = "admin";
            private String password = "admin";

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getUsername() {
                return username;
            }

            public void setUsername(String username) {
                this.username = username;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }
        }

    }

}