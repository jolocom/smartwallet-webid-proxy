package com.danubetech.webidproxy.ssl;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Security;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class SSLGenerator {

	static {

		Security.addProvider(new BouncyCastleProvider());
	}

	public static oracle.security.crypto.core.KeyPair generateKeyPair(int keyLength) {

		try {

			oracle.security.crypto.core.KeyPairGenerator keyPairGenerator = new oracle.security.crypto.core.RSAKeyPairGenerator();
			keyPairGenerator.initialize(keyLength, oracle.security.crypto.core.RandomBitsSource.getDefault());

			return keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static oracle.security.crypto.cert.SPKAC generateSPKAC(oracle.security.crypto.core.KeyPair keyPair) {

		try {

			return new oracle.security.crypto.cert.SPKAC(keyPair);
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static KeyPair convertKeyPair(oracle.security.crypto.core.KeyPair keyPair) {

		return new KeyPair(keyPair.getPublic(), keyPair.getPrivate());
	}

	public static X509Certificate generateCertificate(String dn, String altname, KeyPair keyPair, int days, String algorithm) {

		try {

			X509V3CertificateGenerator certGen = new X509V3CertificateGenerator();

			BigInteger serialNumber = BigInteger.valueOf(System.currentTimeMillis());
			X500Principal issuerName = new X500Principal(dn);
			X500Principal subjectName = new X500Principal(dn);
			Date startDate = new Date();
			Date expiryDate = new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * days));

			certGen.setSerialNumber(serialNumber);
			certGen.setIssuerDN(issuerName);
			certGen.setSubjectDN(subjectName);
			certGen.setNotBefore(startDate);
			certGen.setNotAfter(expiryDate);
			certGen.setPublicKey(keyPair.getPublic());
			certGen.setSignatureAlgorithm("SHA256WithRSAEncryption");

			certGen.addExtension(X509Extensions.SubjectKeyIdentifier, false, new SubjectKeyIdentifierStructure(keyPair.getPublic()));
			certGen.addExtension(X509Extensions.SubjectAlternativeName, false, new GeneralNames(new GeneralName(GeneralName.uniformResourceIdentifier, altname)));

			return certGen.generate(keyPair.getPrivate(), "BC");   // note: private key of CA
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}

		/*
		try {

			PrivateKey privkey = pair.getPrivate();
			X509CertInfo info = new X509CertInfo();
			Date from = new Date();
			Date to = new Date(from.getTime() + days * 86400000l);
			CertificateValidity interval = new CertificateValidity(from, to);
			BigInteger sn = new BigInteger(64, new SecureRandom());
			X500Name owner = new X500Name(dn);

			info.set(X509CertInfo.VALIDITY, interval);
			info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
			info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
			info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
			info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
			info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
			AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
			info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

			// Sign the cert to identify the algorithm that's used.
			X509CertImpl cert = new X509CertImpl(info);
			cert.sign(privkey, algorithm);

			// Update the algorith, and resign.
			algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
			info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
			cert = new X509CertImpl(info);
			cert.sign(privkey, algorithm);
			return cert;
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}*/
	}   
}
