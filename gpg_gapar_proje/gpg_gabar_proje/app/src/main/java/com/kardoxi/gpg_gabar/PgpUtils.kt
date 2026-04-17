package com.kardoxi.gpg_gabar

import org.bouncycastle.bcpg.ArmoredOutputStream
import org.bouncycastle.bcpg.HashAlgorithmTags
import org.bouncycastle.bcpg.PublicKeyAlgorithmTags
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openpgp.PGPEncryptedData
import org.bouncycastle.openpgp.PGPException
import org.bouncycastle.openpgp.PGPKeyPair
import org.bouncycastle.openpgp.PGPKeyRingGenerator
import org.bouncycastle.openpgp.PGPPublicKey
import org.bouncycastle.openpgp.PGPSignature
import org.bouncycastle.openpgp.PGPSecretKey
import org.bouncycastle.openpgp.PGPSecretKeyRing
import org.bouncycastle.openpgp.PGPPublicKeyRing
import org.bouncycastle.openpgp.PGPPublicKeyRingCollection
import org.bouncycastle.openpgp.PGPLiteralData
import org.bouncycastle.openpgp.PGPCompressedData
import org.bouncycastle.openpgp.PGPOnePassSignatureList
import org.bouncycastle.openpgp.PGPSignatureList
import org.bouncycastle.openpgp.PGPPrivateKey
import org.bouncycastle.openpgp.PGPEncryptedDataList
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData
import org.bouncycastle.openpgp.PGPObjectFactory
import org.bouncycastle.openpgp.PGPLiteralDataGenerator
import org.bouncycastle.openpgp.PGPCompressedDataGenerator
import org.bouncycastle.openpgp.PGPEncryptedDataGenerator
import org.bouncycastle.openpgp.operator.PBESecretKeyEncryptor
import org.bouncycastle.openpgp.operator.PGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.PGPDigestCalculator
import org.bouncycastle.openpgp.operator.PGPDigestCalculatorProvider
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPContentSignerBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPDigestCalculatorProviderBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyEncryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcaPGPKeyPair
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyKeyEncryptionMethodGenerator
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder
import java.io.ByteArrayOutputStream
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.Security
import java.util.*

object PgpUtils {
    data class ArmoredKeyPair(
        val publicKeyArmored: String,
        val secretKeyArmored: String
    )
    data class DecryptionResult(
        val bytes: ByteArray,
        val fileName: String?
    )

    @Volatile
    private var providerInitialized = false

    // Use an explicit provider instance so crypto continues to work even if Android
    // prevents replacing the system "BC" provider in the global Security registry.
    private val bcProvider by lazy { BouncyCastleProvider() }

    private fun ensureProvider() {
        if (providerInitialized) return
        synchronized(this) {
            if (providerInitialized) return
            val name = BouncyCastleProvider.PROVIDER_NAME // "BC"
            val existing = Security.getProvider(name)
            // Best-effort: try to prefer our BC in the global registry, but do not rely on it.
            // Some Android builds disallow removing/replacing the system provider.
            if (existing == null) {
                Security.insertProviderAt(bcProvider, 1)
            } else if (existing !is BouncyCastleProvider) {
                runCatching { Security.removeProvider(name) }
                Security.insertProviderAt(bcProvider, 1)
            }
            providerInitialized = true
        }
    }

    // --- Utilities to help auto-select secret key for decryption ---
    @Throws(PGPException::class)
    fun extractRecipientKeyIds(encInput: InputStream): List<Long> {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(encInput)
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList
            val ids = mutableListOf<Long>()
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val pked = it.next() as? PGPPublicKeyEncryptedData ?: continue
                ids.add(pked.keyID)
            }
            return ids
        } catch (e: Exception) {
            throw PGPException("Failed to read recipient key IDs: ${e.message}", e)
        }
    }

    @Throws(PGPException::class)
    fun firstSecretKeyId(secretKeyArmored: String): Long? {
        ensureProvider()
        try {
            val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(secretKeyArmored.toByteArray()))
            val factory = PGPObjectFactory(input, JcaKeyFingerprintCalculator())
            var obj = factory.nextObject()
            while (obj != null) {
                if (obj is PGPSecretKeyRing) {
                    val it = obj.secretKeys
                    if (it.hasNext()) {
                        val sk = it.next() as PGPSecretKey
                        return sk.keyID
                    }
                }
                obj = factory.nextObject()
            }
            return null
        } catch (e: Exception) {
            throw PGPException("Failed to read secret key id: ${e.message}", e)
        }
    }

    @Throws(PGPException::class)
    fun secretContainsKeyId(secretKeyArmored: String, keyId: Long): Boolean {
        ensureProvider()
        try {
            val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(secretKeyArmored.toByteArray()))
            val factory = PGPObjectFactory(input, JcaKeyFingerprintCalculator())
            var obj = factory.nextObject()
            while (obj != null) {
                if (obj is PGPSecretKeyRing) {
                    val sk = obj.getSecretKey(keyId)
                    if (sk != null) return true
                }
                obj = factory.nextObject()
            }
            return false
        } catch (e: Exception) {
            throw PGPException("Failed to check secret key ring: ${e.message}", e)
        }
    }

    @Throws(PGPException::class)
    fun generateArmoredKeyPair(identity: String, passphrase: CharArray): ArmoredKeyPair {
        ensureProvider()

        return try {
            // Generate RSA keypair for PGP (sign+encrypt general use)
            val random = SecureRandom()
            // Do not force provider here; some Android devices fail to provide RSA via "BC"
            // when the system provider cannot be replaced. Default providers support RSA.
            val kpg = KeyPairGenerator.getInstance("RSA")
            kpg.initialize(4096, random)
            val kp: KeyPair = kpg.generateKeyPair()

            val creationTime = Date()
            val pgpKeyPair: PGPKeyPair = JcaPGPKeyPair(PublicKeyAlgorithmTags.RSA_GENERAL, kp, creationTime)

            // SHA1 for secret key checksum
            val sha1Calc: PGPDigestCalculator = JcaPGPDigestCalculatorProviderBuilder()
                .setProvider(bcProvider)
                .build()
                .get(HashAlgorithmTags.SHA1)

            // SHA256 for S2K (passphrase derivation)
            val s2kCalc: PGPDigestCalculator = JcaPGPDigestCalculatorProviderBuilder()
                .setProvider(bcProvider)
                .build()
                .get(HashAlgorithmTags.SHA256)

            val contentSignerBuilder: PGPContentSignerBuilder = JcaPGPContentSignerBuilder(
                pgpKeyPair.publicKey.algorithm,
                HashAlgorithmTags.SHA256
            ).setProvider(bcProvider)

            val secretKeyEncryptor: PBESecretKeyEncryptor = JcePBESecretKeyEncryptorBuilder(
                PGPEncryptedData.AES_256,
                s2kCalc
            )
                .setProvider(bcProvider)
                .setSecureRandom(random)
                .build(passphrase)

            // Generate key ring generator
            val keyRingGen: PGPKeyRingGenerator = PGPKeyRingGenerator(
                PGPSignature.POSITIVE_CERTIFICATION,
                pgpKeyPair,
                identity,
                sha1Calc,
                null,
                null,
                contentSignerBuilder,
                secretKeyEncryptor
            )

            val secretRing: PGPSecretKeyRing = keyRingGen.generateSecretKeyRing()
            val publicRing: PGPPublicKeyRing = keyRingGen.generatePublicKeyRing()

            val publicArmored = armorPublicKey(publicRing.publicKey)
            val secretArmored = armorSecretKey(secretRing)

            ArmoredKeyPair(publicArmored, secretArmored)
        } catch (e: Exception) {
            throw PGPException("PGP key generation failed: ${e.message}", e)
        }
    }

    private fun armorPublicKey(pub: PGPPublicKey): String {
        val out = ByteArrayOutputStream()
        ArmoredOutputStream(out).use { aos ->
            pub.encode(aos)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    private fun armorSecretKey(secret: PGPSecretKeyRing): String {
        val out = ByteArrayOutputStream()
        ArmoredOutputStream(out).use { aos ->
            secret.encode(aos)
        }
        return out.toString(Charsets.UTF_8.name())
    }

    /**
     * Derive the armored public key block from an armored secret key ring block.
     * Useful when the imported file only contains the secret key.
     */
    @Throws(PGPException::class)
    fun derivePublicFromSecret(secretKeyArmored: String): String {
        ensureProvider()
        try {
            val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(secretKeyArmored.toByteArray()))
            val factory = PGPObjectFactory(input, JcaKeyFingerprintCalculator())
            var obj = factory.nextObject()
            while (obj != null) {
                if (obj is PGPSecretKeyRing) {
                    val pub = obj.publicKey
                    if (pub != null) {
                        return armorPublicKey(pub)
                    }
                    // Fallback: try to obtain from first secret key in ring
                    val it = obj.secretKeys
                    if (it.hasNext()) {
                        val sk = it.next() as PGPSecretKey
                        return armorPublicKey(sk.publicKey)
                    }
                }
                obj = factory.nextObject()
            }
            throw PGPException("No secret key ring found to derive public key")
        } catch (e: Exception) {
            throw PGPException("Failed to derive public key: ${e.message}", e)
        }
    }

    // --- Helpers to parse armored keys ---
    private fun readPublicKey(armored: String): PGPPublicKey {
        ensureProvider()
        val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(armored.toByteArray()))
        val rings = PGPPublicKeyRingCollection(input, JcaKeyFingerprintCalculator())
        val ringIter = rings.keyRings
        while (ringIter.hasNext()) {
            val ring = ringIter.next() as PGPPublicKeyRing
            val keyIter = ring.publicKeys
            while (keyIter.hasNext()) {
                val k = keyIter.next() as PGPPublicKey
                val alg = k.algorithm
                val algOk = when (alg) {
                    PublicKeyAlgorithmTags.RSA_GENERAL,
                    PublicKeyAlgorithmTags.RSA_ENCRYPT,
                    PublicKeyAlgorithmTags.ELGAMAL_ENCRYPT,
                    PublicKeyAlgorithmTags.ECDH -> true
                    else -> false
                }
                if (algOk && (k.isEncryptionKey || alg == PublicKeyAlgorithmTags.ECDH)) {
                    return k
                }
            }
        }
        throw PGPException("No encryption public key found in armored block")
    }

    private fun readSecretKey(armored: String): PGPSecretKey {
        ensureProvider()
        val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(armored.toByteArray()))
        val factory = PGPObjectFactory(input, JcaKeyFingerprintCalculator())
        var obj = factory.nextObject()
        while (obj != null) {
            if (obj is PGPSecretKeyRing) {
                val it = obj.secretKeys
                while (it.hasNext()) {
                    val sk = it.next() as PGPSecretKey
                    return sk
                }
            }
            obj = factory.nextObject()
        }
        throw PGPException("No secret key found in armored block")
    }

    private fun readSecretKeyRing(armored: String): PGPSecretKeyRing {
        ensureProvider()
        val input: InputStream = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(armored.toByteArray()))
        val factory = PGPObjectFactory(input, JcaKeyFingerprintCalculator())
        var obj = factory.nextObject()
        while (obj != null) {
            if (obj is PGPSecretKeyRing) {
                return obj
            }
            obj = factory.nextObject()
        }
        throw PGPException("No secret key ring found in armored block")
    }

    private fun extractPrivateKey(secretKey: PGPSecretKey, passphrase: CharArray): PGPPrivateKey {
        val dec = JcePBESecretKeyDecryptorBuilder()
            .setProvider(bcProvider)
            .build(passphrase)
        return secretKey.extractPrivateKey(dec)
    }

    /**
     * Returns true if the secret key requires a passphrase. We attempt to extract
     * the private key with an empty passphrase; if it fails, we assume protected.
     */
    @Throws(PGPException::class)
    fun secretRequiresPassphrase(secretKeyArmored: String): Boolean {
        ensureProvider()
        return try {
            val sk = readSecretKey(secretKeyArmored)
            // Try with empty passphrase
            extractPrivateKey(sk, CharArray(0))
            false
        } catch (e: Exception) {
            true
        }
    }

    /**
     * Returns true if the provided passphrase can decrypt the given secret key.
     * This avoids false positives during UI validation.
     */
    @Throws(PGPException::class)
    fun verifySecretPassphrase(secretKeyArmored: String, passphrase: CharArray): Boolean {
        ensureProvider()
        return try {
            val sk = readSecretKey(secretKeyArmored)
            extractPrivateKey(sk, passphrase)
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- Encrypt bytes to binary .gpg ---
    @Throws(PGPException::class)
    fun encryptBytes(publicKeyArmored: String, data: ByteArray, fileName: String = "data.bin"): ByteArray {
        ensureProvider()
        try {
            val pubKey = readPublicKey(publicKeyArmored)

            // Literal data
            val literalBuf = ByteArrayOutputStream()
            val lGen = PGPLiteralDataGenerator()
            val lOut = lGen.open(literalBuf, PGPLiteralData.BINARY, fileName, data.size.toLong(), Date())
            lOut.write(data)
            lOut.close()
            try { lGen.close() } catch (_: Exception) {}

            // Compress
            val compBuf = ByteArrayOutputStream()
            val cGen = PGPCompressedDataGenerator(PGPCompressedData.ZIP)
            val cOut1 = cGen.open(compBuf)
            cOut1.write(literalBuf.toByteArray())
            cOut1.close()
            try { cGen.close() } catch (_: Exception) {}

            // Encrypt
            val encGen = PGPEncryptedDataGenerator(
                org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(SecureRandom())
                    .setProvider(bcProvider)
            )
            encGen.addMethod(
                JcePublicKeyKeyEncryptionMethodGenerator(pubKey)
                    .setProvider(bcProvider)
            )
            val encOut = ByteArrayOutputStream()
            val cOut = encGen.open(encOut, ByteArray(1 shl 16))
            cOut.write(compBuf.toByteArray())
            cOut.close()
            return encOut.toByteArray()
        } catch (e: Exception) {
            throw PGPException("PGP encryption failed: ${e.message}", e)
        }
    }

    // --- Multi-recipient stream encryption ---
    @Throws(PGPException::class)
    fun encryptStream(
        publicKeysArmored: List<String>,
        input: InputStream,
        fileName: String = "data.bin",
        output: OutputStream,
        onProgress: ((Long) -> Unit)? = null,
        shouldCancel: (() -> Boolean)? = null
    ) {
        ensureProvider()
        try {
            if (publicKeysArmored.isEmpty()) throw PGPException("No public keys provided")

            val encGen = PGPEncryptedDataGenerator(
                org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(SecureRandom())
                    .setProvider(bcProvider)
            )

            // Add each recipient method
            publicKeysArmored.forEach { armored ->
                val k = readPublicKey(armored)
                encGen.addMethod(
                    JcePublicKeyKeyEncryptionMethodGenerator(k)
                        .setProvider(bcProvider)
                )
            }

            val encryptedOut = encGen.open(output, ByteArray(1 shl 16))
            try {
                val cGen = PGPCompressedDataGenerator(PGPCompressedData.ZIP)
                val compressedOut = cGen.open(encryptedOut)
                try {
                    val lGen = PGPLiteralDataGenerator()
                    val literalOut = lGen.open(
                        compressedOut,
                        PGPLiteralData.BINARY,
                        fileName,
                        Date(),
                        ByteArray(1 shl 16)
                    )
                    try {
                        val buffer = ByteArray(64 * 1024)
                        var total: Long = 0
                        while (true) {
                            if (shouldCancel?.invoke() == true) {
                                throw java.util.concurrent.CancellationException("User cancelled")
                            }
                            val read = input.read(buffer)
                            if (read <= 0) break
                            literalOut.write(buffer, 0, read)
                            total += read
                            onProgress?.invoke(total)
                        }
                    } finally {
                        try { lGen.close() } catch (_: Exception) {}
                    }
                } finally {
                    try { cGen.close() } catch (_: Exception) {}
                }
            } finally {
                try { encryptedOut.close() } catch (_: Exception) {}
                try { encGen.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            throw PGPException("PGP multi-recipient encryption failed: ${e.message}", e)
        }
    }

    // --- Encrypt text to ASCII-armored message (suitable for saving as .asc) ---
    @Throws(PGPException::class)
    fun encryptTextArmored(publicKeyArmored: String, text: String, fileName: String = "data.txt"): String {
        ensureProvider()
        try {
            val binOut = ByteArrayOutputStream()
            encryptStream(
                publicKeyArmored = publicKeyArmored,
                input = ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)),
                fileName = fileName,
                output = binOut,
                onProgress = null
            )
            val binaryMessage = binOut.toByteArray()

            val armoredOut = ByteArrayOutputStream()
            ArmoredOutputStream(armoredOut).use { aos ->
                aos.write(binaryMessage)
            }
            return armoredOut.toString(Charsets.UTF_8.name())
        } catch (e: Exception) {
            throw PGPException("PGP text encryption failed: ${e.message}", e)
        }
    }

    // --- Encrypt text to ASCII-armored message for multiple recipients ---
    @Throws(PGPException::class)
    fun encryptTextArmored(publicKeysArmored: List<String>, text: String, fileName: String = "data.txt"): String {
        ensureProvider()
        try {
            if (publicKeysArmored.isEmpty()) throw PGPException("No public keys provided")

            val binOut = ByteArrayOutputStream()
            encryptStream(
                publicKeysArmored = publicKeysArmored,
                input = ByteArrayInputStream(text.toByteArray(Charsets.UTF_8)),
                fileName = fileName,
                output = binOut,
                onProgress = null
            )
            val binaryMessage = binOut.toByteArray()

            val armoredOut = ByteArrayOutputStream()
            ArmoredOutputStream(armoredOut).use { aos ->
                aos.write(binaryMessage)
            }
            return armoredOut.toString(Charsets.UTF_8.name())
        } catch (e: Exception) {
            throw PGPException("PGP text encryption failed: ${e.message}", e)
        }
    }

    // --- Stream encrypt from input to output to handle large files without freezing UI ---
    @Throws(PGPException::class)
    fun encryptStream(
        publicKeyArmored: String,
        input: InputStream,
        fileName: String = "data.bin",
        output: OutputStream,
        onProgress: ((Long) -> Unit)? = null
    ) {
        ensureProvider()
        try {
            val pubKey = readPublicKey(publicKeyArmored)

            val encGen = PGPEncryptedDataGenerator(
                org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(SecureRandom())
                    .setProvider(bcProvider)
            )
            encGen.addMethod(
                JcePublicKeyKeyEncryptionMethodGenerator(pubKey)
                    .setProvider(bcProvider)
            )

            // Open encrypted output stream on the provided output
            val encryptedOut = encGen.open(output, ByteArray(1 shl 16))
            try {
                // Compress within the encryption layer
                val cGen = PGPCompressedDataGenerator(PGPCompressedData.ZIP)
                val compressedOut = cGen.open(encryptedOut)
                try {
                    // Literal data layer with unknown length (streaming/partial packets)
                    val lGen = PGPLiteralDataGenerator()
                    val literalOut = lGen.open(
                        compressedOut,
                        PGPLiteralData.BINARY,
                        fileName,
                        Date(),
                        ByteArray(1 shl 16)
                    )
                    try {
                        // Copy input to literalOut in chunks
                        val buffer = ByteArray(64 * 1024)
                        var total: Long = 0
                        while (true) {
                            val read = input.read(buffer)
                            if (read <= 0) break
                            literalOut.write(buffer, 0, read)
                            total += read
                            onProgress?.invoke(total)
                        }
                    } finally {
                        try { lGen.close() } catch (_: Exception) {}
                    }
                } finally {
                    try { cGen.close() } catch (_: Exception) {}
                }
            } finally {
                try { encryptedOut.close() } catch (_: Exception) {}
                try { encGen.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            throw PGPException("PGP encryption failed: ${e.message}", e)
        }
    }

    // --- Decrypt bytes from binary .gpg ---
    @Throws(PGPException::class)
    fun decryptBytes(secretKeyArmored: String, passphrase: CharArray, encData: ByteArray): DecryptionResult {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(ByteArrayInputStream(encData))
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList

            // Find matching recipient packet for our secret key ring
            val secRing = readSecretKeyRing(secretKeyArmored)
            var pked: PGPPublicKeyEncryptedData? = null
            var priv: PGPPrivateKey? = null
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val obj = it.next()
                val candidate = obj as? PGPPublicKeyEncryptedData ?: continue
                val sk = secRing.getSecretKey(candidate.keyID) ?: continue
                // Found a matching secret subkey; try extracting private key with provided passphrase
                priv = extractPrivateKey(sk, passphrase)
                pked = candidate
                break
            }
            if (pked == null || priv == null) throw PGPException("No matching secret key found for this message")

            val dataDecFactory = JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(bcProvider)
                .build(priv)
            val clear = pked.getDataStream(dataDecFactory)
            var currentFactory = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
            var message: Any? = currentFactory.nextObject()
            while (true) {
                when (message) {
                    is PGPCompressedData -> {
                        currentFactory = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
                        message = currentFactory.nextObject()
                    }
                    is PGPOnePassSignatureList -> {
                        // Skip signature preamble and move to literal data
                        message = currentFactory.nextObject()
                    }
                    is PGPLiteralData -> {
                        val name = message.fileName
                        val data = message.inputStream.readBytes()
                        return DecryptionResult(data, if (name.isNullOrBlank()) null else name)
                    }
                    else -> throw PGPException("Unexpected PGP message type")
                }
            }
        } catch (e: Exception) {
            throw PGPException("PGP decryption failed: ${e.message}", e)
        }
    }

    // --- Peek decrypted file name without loading content (for suggesting output name) ---
    @Throws(PGPException::class)
    fun decryptPeekFileName(secretKeyArmored: String, passphrase: CharArray, encInput: InputStream): String? {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(encInput)
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList

            val secRing = readSecretKeyRing(secretKeyArmored)
            var pked: PGPPublicKeyEncryptedData? = null
            var priv: PGPPrivateKey? = null
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val obj = it.next()
                val candidate = obj as? PGPPublicKeyEncryptedData ?: continue
                val sk = secRing.getSecretKey(candidate.keyID) ?: continue
                priv = extractPrivateKey(sk, passphrase)
                pked = candidate
                break
            }
            if (pked == null || priv == null) throw PGPException("No matching secret key found for this message")

            val dataDecFactory = JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(bcProvider)
                .build(priv)
            val clear = pked.getDataStream(dataDecFactory)
            var currentFactory = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
            var message: Any? = currentFactory.nextObject()
            while (true) {
                when (message) {
                    is PGPCompressedData -> {
                        currentFactory = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
                        message = currentFactory.nextObject()
                    }
                    is PGPOnePassSignatureList -> {
                        message = currentFactory.nextObject()
                    }
                    is PGPLiteralData -> {
                        val name = message.fileName
                        return if (name.isNullOrBlank()) null else name
                    }
                    null -> return null
                    else -> return null
                }
            }
        } catch (e: Exception) {
            throw PGPException("PGP decryption peek failed: ${e.message}", e)
        }
    }

    // --- Stream decryption from input to output to handle large files ---
    @Throws(PGPException::class)
    fun decryptStream(
        secretKeyArmored: String,
        passphrase: CharArray,
        encInput: InputStream,
        output: OutputStream,
        shouldCancel: (() -> Boolean)? = null,
        onProgress: ((Long) -> Unit)? = null
    ): String? {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(encInput)
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList

            val secRing = readSecretKeyRing(secretKeyArmored)
            var pked: PGPPublicKeyEncryptedData? = null
            var priv: PGPPrivateKey? = null
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val obj = it.next()
                val candidate = obj as? PGPPublicKeyEncryptedData ?: continue
                val sk = secRing.getSecretKey(candidate.keyID) ?: continue
                priv = extractPrivateKey(sk, passphrase)
                pked = candidate
                break
            }
            if (pked == null || priv == null) throw PGPException("No matching secret key found for this message")

            val dataDecFactory = JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(bcProvider)
                .build(priv)
            val clear = pked.getDataStream(dataDecFactory)
            var currentFactory = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
            var message: Any? = currentFactory.nextObject()
            while (true) {
                when (message) {
                    is PGPCompressedData -> {
                        currentFactory = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
                        message = currentFactory.nextObject()
                    }
                    is PGPOnePassSignatureList -> {
                        message = currentFactory.nextObject()
                    }
                    is PGPLiteralData -> {
                        val name = message.fileName
                        val literalIn = message.inputStream
                        val buffer = ByteArray(64 * 1024)
                        var total: Long = 0
                        while (true) {
                            if (shouldCancel?.invoke() == true) {
                                throw java.util.concurrent.CancellationException("User cancelled")
                            }
                            val read = literalIn.read(buffer)
                            if (read <= 0) break
                            output.write(buffer, 0, read)
                            total += read
                            onProgress?.invoke(total)
                        }
                        output.flush()
                        return if (name.isNullOrBlank()) null else name
                    }
                    else -> throw PGPException("Unexpected PGP message type")
                }
            }
        } catch (e: Exception) {
            throw PGPException("PGP decryption failed: ${e.message}", e)
        }
    }

    // --- Decrypt and auto-unzip if the decrypted literal is a ZIP with a single entry ---
    @Throws(PGPException::class)
    fun decryptStreamAutoUnzip(
        secretKeyArmored: String,
        passphrase: CharArray,
        encInput: InputStream,
        output: OutputStream,
        shouldCancel: (() -> Boolean)? = null,
        onProgress: ((Long) -> Unit)? = null
    ): String? {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(encInput)
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList

            val secRing = readSecretKeyRing(secretKeyArmored)
            var pked: PGPPublicKeyEncryptedData? = null
            var priv: PGPPrivateKey? = null
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val obj = it.next()
                val candidate = obj as? PGPPublicKeyEncryptedData ?: continue
                val sk = secRing.getSecretKey(candidate.keyID) ?: continue
                priv = extractPrivateKey(sk, passphrase)
                pked = candidate
                break
            }
            if (pked == null || priv == null) throw PGPException("No matching secret key found for this message")

            val dataDecFactory = JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(bcProvider)
                .build(priv)
            val clear = pked.getDataStream(dataDecFactory)
            var currentFactory = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
            var message: Any? = currentFactory.nextObject()
            while (true) {
                when (message) {
                    is PGPCompressedData -> {
                        currentFactory = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
                        message = currentFactory.nextObject()
                    }
                    is PGPOnePassSignatureList -> {
                        message = currentFactory.nextObject()
                    }
                    is PGPLiteralData -> {
                        val outerName = message.fileName
                        val literalIn = message.inputStream

                        // Peek first 4 bytes to check for ZIP signature (PK**)
                        val push = java.io.PushbackInputStream(literalIn, 8)
                        val header = ByteArray(4)
                        val n = push.read(header)
                        if (n > 0) push.unread(header, 0, n)

                        val isZip = n >= 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
                        if (!isZip) {
                            // Maybe GZIP? (1F 8B)
                            val isGzip = n >= 2 && header[0] == 0x1F.toByte() && header[1] == 0x8B.toByte()
                            if (isGzip) {
                                val gis = java.util.zip.GZIPInputStream(push)
                                val buffer = ByteArray(64 * 1024)
                                var total: Long = 0
                                while (true) {
                                    val read = gis.read(buffer)
                                    if (read <= 0) break
                                    output.write(buffer, 0, read)
                                    total += read
                                    onProgress?.invoke(total)
                                }
                                output.flush()
                                try { gis.close() } catch (_: Exception) {}
                                if (total == 0L) throw PGPException("Decrypted GZIP content is empty")
                                return if (outerName.isNullOrBlank()) null else outerName.removeSuffix(".gz").ifBlank { outerName }
                            } else {
                                // Not ZIP/GZIP: stream as-is
                                val buffer = ByteArray(64 * 1024)
                                var total: Long = 0
                                while (true) {
                                    val read = push.read(buffer)
                                    if (read <= 0) break
                                    output.write(buffer, 0, read)
                                    total += read
                                    onProgress?.invoke(total)
                                }
                                output.flush()
                                if (total == 0L) throw PGPException("Decrypted content is empty")
                                return if (outerName.isNullOrBlank()) null else outerName
                            }
                        }

                        // ZIP: extract the first non-directory entry only
                        val zis = java.util.zip.ZipInputStream(push)
                        var entry = zis.nextEntry ?: throw PGPException("ZIP archive is empty")
                        while (entry.isDirectory) {
                            try { zis.closeEntry() } catch (_: Exception) {}
                            entry = zis.nextEntry ?: throw PGPException("ZIP archive contains no files")
                        }
                        var innerName = entry.name
                        var total: Long
                        val buffer = ByteArray(64 * 1024)
                        while (true) {
                            total = 0
                            while (true) {
                                val read = zis.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                total += read
                                onProgress?.invoke(total)
                            }
                            if (total > 0) break
                            // If this entry had 0 bytes, try next non-directory entry
                            try { zis.closeEntry() } catch (_: Exception) {}
                            val next = zis.nextEntry ?: break
                            if (next.isDirectory) {
                                continue
                            } else {
                                innerName = next.name
                                continue
                            }
                        }
                        output.flush()
                        try { zis.closeEntry() } catch (_: Exception) {}
                        try { zis.close() } catch (_: Exception) {}
                        if (total == 0L) throw PGPException("ZIP contains no file data")
                        return if (innerName.isNullOrBlank()) null else innerName
                    }
                    else -> throw PGPException("Unexpected PGP message type")
                }
            }
        } catch (e: Exception) {
            throw PGPException("PGP decryption failed: ${e.message}", e)
        }
    }

    // --- Encrypt stream by wrapping the content into a single-entry ZIP inside the literal packet ---
    @Throws(PGPException::class)
    fun encryptStreamZip(
        publicKeysArmored: List<String>,
        input: InputStream,
        originalFileName: String = "data.bin",
        output: OutputStream,
        shouldCancel: (() -> Boolean)? = null,
        onProgress: ((Long) -> Unit)? = null
    ) {
        ensureProvider()
        try {
            if (publicKeysArmored.isEmpty()) throw PGPException("No public keys provided")

            val encGen = PGPEncryptedDataGenerator(
                org.bouncycastle.openpgp.operator.jcajce.JcePGPDataEncryptorBuilder(PGPEncryptedData.AES_256)
                    .setWithIntegrityPacket(true)
                    .setSecureRandom(SecureRandom())
                    .setProvider(bcProvider)
            )
            publicKeysArmored.forEach { armored ->
                val k = readPublicKey(armored)
                encGen.addMethod(
                    JcePublicKeyKeyEncryptionMethodGenerator(k)
                        .setProvider(bcProvider)
                )
            }

            val encryptedOut = encGen.open(output, ByteArray(1 shl 16))
            try {
                // Do NOT add extra PGP compression to avoid double compression; write literal directly
                val lGen = PGPLiteralDataGenerator()
                val literalOut = lGen.open(
                    encryptedOut,
                    PGPLiteralData.BINARY,
                    // Outer literal name will look like a zip file
                    if (originalFileName.endsWith(".zip", true)) originalFileName else "$originalFileName.zip",
                    Date(),
                    ByteArray(1 shl 16)
                )
                try {
                    // Now write a ZIP stream containing a single entry with originalFileName
                    val zos = java.util.zip.ZipOutputStream(literalOut)
                    try {
                        val entryName = originalFileName
                        val entry = java.util.zip.ZipEntry(entryName)
                        zos.putNextEntry(entry)
                        val buffer = ByteArray(64 * 1024)
                        var total: Long = 0
                        while (true) {
                            if (shouldCancel?.invoke() == true) {
                                throw java.util.concurrent.CancellationException("User cancelled")
                            }
                            val read = input.read(buffer)
                            if (read <= 0) break
                            zos.write(buffer, 0, read)
                            total += read
                            onProgress?.invoke(total)
                        }
                        zos.closeEntry()
                        zos.finish()
                    } finally {
                        try { zos.close() } catch (_: Exception) {}
                    }
                } finally {
                    try { lGen.close() } catch (_: Exception) {}
                }
            } finally {
                try { encryptedOut.close() } catch (_: Exception) {}
                try { encGen.close() } catch (_: Exception) {}
            }
        } catch (e: Exception) {
            throw PGPException("PGP zip+encrypt failed: ${e.message}", e)
        }
    }

    // Peek inner ZIP file name (first entry) after decryption
    @Throws(PGPException::class)
    fun decryptPeekInnerZipName(secretKeyArmored: String, passphrase: CharArray, encInput: InputStream): String? {
        ensureProvider()
        try {
            val decoder = org.bouncycastle.openpgp.PGPUtil.getDecoderStream(encInput)
            val factory = PGPObjectFactory(decoder, JcaKeyFingerprintCalculator())
            val first = factory.nextObject()
            val encList: PGPEncryptedDataList = if (first is PGPEncryptedDataList) first else factory.nextObject() as PGPEncryptedDataList

            val secRing = readSecretKeyRing(secretKeyArmored)
            var pked: PGPPublicKeyEncryptedData? = null
            var priv: PGPPrivateKey? = null
            val it = encList.encryptedDataObjects
            while (it.hasNext()) {
                val obj = it.next()
                val candidate = obj as? PGPPublicKeyEncryptedData ?: continue
                val sk = secRing.getSecretKey(candidate.keyID) ?: continue
                priv = extractPrivateKey(sk, passphrase)
                pked = candidate
                break
            }
            if (pked == null || priv == null) return null

            val dataDecFactory = JcePublicKeyDataDecryptorFactoryBuilder()
                .setProvider(bcProvider)
                .build(priv)
            val clear = pked.getDataStream(dataDecFactory)
            var currentFactory = PGPObjectFactory(clear, JcaKeyFingerprintCalculator())
            var message: Any? = currentFactory.nextObject()
            while (true) {
                when (message) {
                    is PGPCompressedData -> {
                        currentFactory = PGPObjectFactory(message.dataStream, JcaKeyFingerprintCalculator())
                        message = currentFactory.nextObject()
                    }
                    is PGPOnePassSignatureList -> {
                        message = currentFactory.nextObject()
                    }
                    is PGPLiteralData -> {
                        val push = java.io.PushbackInputStream(message.inputStream, 8)
                        val header = ByteArray(4)
                        val n = push.read(header)
                        if (n > 0) push.unread(header, 0, n)
                        val isZip = n >= 2 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
                        if (!isZip) return null
                        val zis = java.util.zip.ZipInputStream(push)
                        val entry = zis.nextEntry ?: return null
                        val name = entry.name
                        try { zis.closeEntry() } catch (_: Exception) {}
                        try { zis.close() } catch (_: Exception) {}
                        return name
                    }
                    null -> return null
                    else -> return null
                }
            }
        } catch (e: Exception) {
            throw PGPException("PGP inner zip peek failed: ${e.message}", e)
        }
    }
}
