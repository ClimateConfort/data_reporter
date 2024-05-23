package com.climateconfort.data_reporter.avro;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.concurrent.Callable;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AvroEncyptor implements Callable<String> {
    Cipher cipher;
    KeyGenerator keyGen;
    CipherOutputStream cipher_output;
    FileInputStream input;
    FileOutputStream output;

    public AvroEncyptor(FileInputStream dataToEncrypt)
    {
        input = dataToEncrypt;
    }

    @Override
    public String call() throws Exception {
        /* BouncyCastle segurtasun hornitzailea gehitu, zifraketa algoritmoak erabiltzeko */
        Security.addProvider(new BouncyCastleProvider());

        try {
            keyGen = KeyGenerator.getInstance("AES", "BC"); /* BC -> BouncyCastle */
            keyGen.init(256);
            SecretKey secretKey = keyGen.generateKey();
            cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }

        try {
            output = new FileOutputStream("data.encrypted");
            cipher_output = new CipherOutputStream(output, cipher);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = input.read(buffer)) != -1) {
                cipher_output.write(buffer, 0, read);
            }
            cipher_output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output.toString();
    }

}
