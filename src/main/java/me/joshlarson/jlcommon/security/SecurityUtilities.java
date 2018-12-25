/***********************************************************************************
 * MIT License                                                                     *
 *                                                                                 *
 * Copyright (c) 2018 Josh Larson                                                  *
 *                                                                                 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy    *
 * of this software and associated documentation files (the "Software"), to deal   *
 * in the Software without restriction, including without limitation the rights    *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell       *
 * copies of the Software, and to permit persons to whom the Software is           *
 * furnished to do so, subject to the following conditions:                        *
 *                                                                                 *
 * The above copyright notice and this permission notice shall be included in all  *
 * copies or substantial portions of the Software.                                 *
 *                                                                                 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR      *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,        *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE     *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER          *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,   *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE   *
 * SOFTWARE.                                                                       *
 ***********************************************************************************/
package me.joshlarson.jlcommon.security;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Objects;

public enum SecurityUtilities {
	;
	
	/**
	 * Creates an SSL context with the specified algorithm/protocol customization
	 *
	 * @param keystoreFile          the file with the keystore
	 * @param passphrase            the passphrase for the keystore
	 * @param keyStoreType          the type of keystore (e.g. PKCS12), can be null. See {@link java.security.KeyStore#getInstance(String)}
	 * @param keyManagerAlgorithm   the key manager algorithm, can be null. See {@link javax.net.ssl.KeyManagerFactory#getInstance(String)}
	 * @param trustManagerAlgorithm the trust manager algorithm, can be null. See {@link javax.net.ssl.TrustManagerFactory#getInstance(String)}
	 * @param sslProtocol           the ssl protocol, can be null. See {@link javax.net.ssl.SSLContext#getInstance(String)}
	 * @param random                the secure random to use, can be null. See {@link javax.net.ssl.SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}
	 * @return a new SSLContext created from the keystore
	 */
	public static SSLContext createSSLContext(@NotNull File keystoreFile, @NotNull char[] passphrase, @Nullable String keyStoreType, @Nullable String keyManagerAlgorithm, @Nullable String trustManagerAlgorithm, @Nullable String sslProtocol, @Nullable SecureRandom random) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
		return createSSLContext(new FileInputStream(keystoreFile), passphrase, keyStoreType, keyManagerAlgorithm, trustManagerAlgorithm, sslProtocol, random);
	}
	
	/**
	 * Creates an SSL context with the specified algorithm/protocol customization
	 *
	 * @param keystoreStream        the file with the keystore
	 * @param passphrase            the passphrase for the keystore
	 * @param keyStoreType          the type of keystore (e.g. PKCS12), can be null. See {@link java.security.KeyStore#getInstance(String)}
	 * @param keyManagerAlgorithm   the key manager algorithm, can be null. See {@link javax.net.ssl.KeyManagerFactory#getInstance(String)}
	 * @param trustManagerAlgorithm the trust manager algorithm, can be null. See {@link javax.net.ssl.TrustManagerFactory#getInstance(String)}
	 * @param sslProtocol           the ssl protocol, can be null. See {@link javax.net.ssl.SSLContext#getInstance(String)}
	 * @param random                the secure random to use, can be null. See {@link javax.net.ssl.SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}
	 * @return a new SSLContext created from the keystore
	 */
	public static SSLContext createSSLContext(@NotNull InputStream keystoreStream, @NotNull char[] passphrase, @Nullable String keyStoreType, @Nullable String keyManagerAlgorithm, @Nullable String trustManagerAlgorithm, @Nullable String sslProtocol, @Nullable SecureRandom random) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException, KeyManagementException {
		Objects.requireNonNull(keystoreStream, "keystoreStream");
		Objects.requireNonNull(passphrase, "passphrase");
		
		KeyStore keystore = KeyStore.getInstance(keyStoreType == null ? KeyStore.getDefaultType() : keyStoreType);
		keystore.load(keystoreStream, passphrase);
		
		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(keyManagerAlgorithm == null ? KeyManagerFactory.getDefaultAlgorithm() : keyManagerAlgorithm);
		keyManagerFactory.init(keystore, passphrase);
		
		TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(trustManagerAlgorithm == null ? TrustManagerFactory.getDefaultAlgorithm() : trustManagerAlgorithm);
		trustManagerFactory.init(keystore);
		
		SSLContext ctx = (sslProtocol == null ? SSLContext.getDefault() : SSLContext.getInstance(sslProtocol));
		ctx.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), random);
		return ctx;
	}
	
}
