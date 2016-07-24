package com.jolocom.webidproxy.ssl;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.mozilla.PublicKeyAndChallenge;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extensions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mozilla.SignedPublicKeyAndChallenge;
import org.bouncycastle.x509.X509V3CertificateGenerator;
import org.bouncycastle.x509.extension.SubjectKeyIdentifierStructure;

public class SSLGenerator {

	public static final String CHALLENGE_STRING = "";
	
	static {

		Security.addProvider(new BouncyCastleProvider());
	}

	public static KeyPair generateKeyPair(int keyLength) {

		try {

			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
			keyPairGenerator.initialize(keyLength);

			return keyPairGenerator.generateKeyPair();
		} catch (Exception ex) {

			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	public static SignedPublicKeyAndChallenge generateSignedPublicKeyAndChallenge(KeyPair keyPair) throws GeneralSecurityException {

		String challengeString = CHALLENGE_STRING;

		PublicKeyAndChallenge publicKeyAndChallenge = makePublicKeyAndChallenge(keyPair.getPublic(), challengeString);
		SignedPublicKeyAndChallenge signedPublicKeyAndChallenge = makeSignedPublicKeyAndChallenge(publicKeyAndChallenge, keyPair.getPrivate());

		return signedPublicKeyAndChallenge;
	}

	private static SubjectPublicKeyInfo makeSubjectPublicKeyInfo(PublicKey publicKey) {

		return new SubjectPublicKeyInfo(ASN1Sequence.getInstance(publicKey.getEncoded()));
	}

	private static PublicKeyAndChallenge makePublicKeyAndChallenge(PublicKey publicKey, String challengeString) {

		SubjectPublicKeyInfo spki = makeSubjectPublicKeyInfo(publicKey);
		DERIA5String challenge = challengeString == null ? null : new DERIA5String(challengeString);

		ASN1Encodable[] objects = new ASN1Encodable[2];
		objects[0] = spki;
		objects[1] = challenge;

		return new PublicKeyAndChallenge(new DERSequence(objects));
	}

	private static SignedPublicKeyAndChallenge makeSignedPublicKeyAndChallenge(PublicKeyAndChallenge publicKeyAndChallenge, PrivateKey privateKey) throws GeneralSecurityException {

		Signature sig = Signature.getInstance("MD5withRSA", "BC");
		sig.initSign(privateKey, new SecureRandom());
		sig.update(publicKeyAndChallenge.getDEREncoded());
		byte[] sigBytes = sig.sign();

		AlgorithmIdentifier signatureAlgorithm = AlgorithmIdentifier.getInstance("1.2.840.113549.1.1.4");

		DERBitString signature = new DERBitString(sigBytes);

		ASN1Encodable[] objects = new ASN1Encodable[3];
		objects[0] = publicKeyAndChallenge;
		objects[1] = signatureAlgorithm;
		objects[2] = signature;

		return new SignedPublicKeyAndChallenge(new DERSequence(objects).getDEREncoded());
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
