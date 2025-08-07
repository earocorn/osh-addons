package com.botts.impl.comm.kafka;

public class SSLConfig {

    public String trustStorePath = "";
    public String trustStorePassword = "";
    public KeystoreFormat trustStoreFormat = KeystoreFormat.JKS;
    public String keyStorePath = "";
    public String keyStorePassword = "";
    public KeystoreFormat keyStoreFormat = KeystoreFormat.JKS;

    public enum KeystoreFormat {
        JKS("JKS"),
        PKCS12("PKCS12"),
        PEM("PEM");

        final String name;

        KeystoreFormat(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
