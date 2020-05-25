package fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.ProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import javax.crypto.KeyAgreement;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bson.internal.Base64;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import fr.gouv.stopc.robert.crypto.grpc.server.storage.cryptographic.service.ICryptographicStorageService;
import lombok.extern.slf4j.Slf4j;
import sun.security.pkcs11.SunPKCS11;

@Slf4j
@Service
public class CryptographicStorageServiceImpl implements ICryptographicStorageService {

    private KeyStore keyStore;

    private static final String ALIAS_SERVER_PUBLIC_KEY = "serverPublicKey";
    private static final String ALIAS_SERVER_PRIVATE_KEY = "serverPrivateKey";
    private Provider provider;

    private PublicKey publicKey;

    @Override
    public void init(String password, String configFile) {

        if (!StringUtils.hasText(password) || !StringUtils.hasText(configFile)) {
            throw new IllegalArgumentException("The init argument cannot be empty");
        }

        try {
            this.provider = new SunPKCS11(configFile);

            if (-1 == Security.addProvider(provider)) {
                throw new RuntimeException("could not add security provider");
            }

            char[] keyStorePassword = password.toCharArray();
            this.keyStore = KeyStore.getInstance("PKCS11", provider);
            this.keyStore.load(null, keyStorePassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | ProviderException  e) {

            log.error("An expected error occured when trying to initialize the keyStore {} due to {}", e.getClass(), e.getMessage());
            throw new RuntimeException("could not add security provider");
        } 

    }

    @Override
    public boolean contains(String alias) {

        try {
            return this.keyStore.containsAlias(alias);
        } catch (KeyStoreException e) {
            log.error("An expected error occured when trying to check containing the alias {} due to {}", alias, e.getMessage());
        }
        return false;
    }

    @Override
    public void delete(String alias) {

        try {
            this.keyStore.deleteEntry(alias);
        } catch (KeyStoreException e) {
            log.error("An expected error occured when trying to delete the alias {} due to {}", alias, e.getMessage());
        }
    }

    @Override
    public void store(String alias, String secretKey) {

        try {
            if (!isServerKeyAlias(alias) && this.contains(alias)) {
                this.delete(alias);
                this.keyStore.setKeyEntry(alias, secretKey.getBytes(), null);
            }
        } catch (KeyStoreException e) {
            log.error("An expected error occured when trying to store the alias {} due to {}", alias, e.getMessage());
        }

    }

    @Override
    public void addKeys(String serverPublicKey, String serverPrivateKey) {

        try {
            log.info("PUBLIC : {}", serverPublicKey);
            log.info("PRIVATE : {}", serverPrivateKey);
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.decode(serverPrivateKey));
            KeyFactory generator = KeyFactory.getInstance("EC");
            PrivateKey privateKey = generator.generatePrivate(privateKeySpec);

            EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(Base64.decode(serverPublicKey));
            this.publicKey = generator.generatePublic(publicKeySpec);
            KeyPair keyPair = new KeyPair(publicKey, privateKey);

            log.info("BEFORE THE STORING {}", keyPair.getPrivate().getEncoded());
            generateCertificate(keyPair).ifPresent(x509Certificate -> {

                X509Certificate[] chain = new X509Certificate[1];
                chain[0] = x509Certificate;
                try {
                    if(!this.contains(ALIAS_SERVER_PRIVATE_KEY)) {
//                        this.delete(ALIAS_SERVER_PRIVATE_KEY);
                        this.keyStore.setKeyEntry(ALIAS_SERVER_PRIVATE_KEY, privateKey, null, chain);
                    }
                } catch (KeyStoreException e) {
                    log.error("An expected error occured when trying to store the server public/private key due to {}", e.getMessage());
                    throw new RuntimeException("Unoble to store the private key of the server");
                }
            });

        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            log.error("An expected error occured when trying to convert the server public/private key due to {}", e.getMessage());
        } 

    }

    private boolean isServerKeyAlias(String alias) {
        return Stream.of(ALIAS_SERVER_PUBLIC_KEY, ALIAS_SERVER_PRIVATE_KEY).anyMatch(item -> item.equals(alias));
    }

    @Override
    public Optional<Key> getServerPublicKey() {

        try {
            return Optional.ofNullable(this.keyStore.getCertificate(ALIAS_SERVER_PRIVATE_KEY).getPublicKey());
        } catch (KeyStoreException e) {
            log.error("Unable to retrieve the public key due to {}", e.getMessage());
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<Key> getServerPrivateKey() {

        try {
            PrivateKeyEntry entry = (PrivateKeyEntry) this.keyStore.getEntry(ALIAS_SERVER_PRIVATE_KEY, null);
            return Optional.ofNullable(entry.getPrivateKey());
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            // TODO Auto-generated catch block
            log.error("Unable to retrieve the entry for alias {} from the keyStrore due to {}", ALIAS_SERVER_PRIVATE_KEY, e.getMessage());
        }

        return Optional.empty();
    }

    private Optional<Key> getKey(String alias) {
        try {
            return Optional.ofNullable(this.keyStore.getKey(alias,  null));
        } catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
            log.error("Unable to retrieve the alias {} from the keyStrore", alias);
        }
        return Optional.empty();
    }

    @Override
    public byte[] getEntry(String alias) {

        try {
            if(this.contains(alias)) {
                return this.keyStore.getKey(alias, null).getEncoded();
            }
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            log.error("An expected error occured when trying to get the entry {} due to {}", alias, e.getMessage());
        }
        return null;
    }

    private Optional<X509Certificate> generateCertificate(KeyPair keyPair) {
        try {

            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(new X500Name("CN=Test Certificate"),
                    BigInteger.valueOf(System.currentTimeMillis()),
                    new Date(System.currentTimeMillis() - 50000), new Date(System.currentTimeMillis() + 50000), 
                    new X500Name("CN=Test Certificate"), SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded()));
            JcaContentSignerBuilder builder = new JcaContentSignerBuilder("SHA512withECDSA");
            ContentSigner signer = builder.build(keyPair.getPrivate());

            byte[] certBytes = certBuilder.build(signer).getEncoded();
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate certificate = (X509Certificate)certificateFactory.generateCertificate(new ByteArrayInputStream(certBytes));
            log.info("CERTIFICATE = {}", certificate);
            return Optional.ofNullable(certificate);

        } catch (Exception e) {

            log.error("An error occured when trying to generate the certificate: {} due to {}", e.getClass(), e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Provider getProvider() {
        // TODO Auto-generated method stub
        return this.provider;
    }

    @Override
    public byte[] getSharedSecret() {

        try {
            Key staticPrivateKey =  this.keyStore.getKey(ALIAS_SERVER_PRIVATE_KEY, null);
            Key staticPublicKey = this.keyStore.getCertificate(ALIAS_SERVER_PRIVATE_KEY).getPublicKey();
            log.info("PPPRIVATE  = {}", staticPrivateKey);
            if (Objects.isNull(Objects.isNull(staticPublicKey)) || Objects.isNull(staticPublicKey)) {
                log.error("At least one of the public or the private key is null");
                return null;
            }
            // Generate an ephemeral key-pair on the device
            log.info("Generating ephemeral ECDH keys");
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
            kpg.initialize(((ECPublicKey) staticPublicKey).getParams());
            KeyPair ephemeralKeys = kpg.generateKeyPair();

            // Now perform ECDH key agreement between static private and ephemeral public keys
           log.info("Performing ECDH key agreement");
            KeyAgreement ecdh = KeyAgreement.getInstance("ECDH", this.getProvider());
            ecdh.init(staticPrivateKey);
            ecdh.doPhase(ephemeralKeys.getPublic(), true);
            return ecdh.generateSecret();
        } catch (KeyStoreException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | UnrecoverableKeyException | InvalidKeyException | IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return null;
    }
}
