package org.talend.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;

// import javax.crypto.Cipher;

public class IntegrityUtil {

    /**
     * Is used to check if the zip file is corrupted/destroyed
     * 
     * @param file
     * @return
     */
    public static boolean isZipValid(final File file) {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(file);

            return true;
        } catch (IOException e) {
            return false;
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException e) {

            }
        }
    }

    public static void validate(final File file) {
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(file);

        } catch (IOException e) {
        } finally {
            try {
                if (zipFile != null) {
                    zipFile.close();
                    zipFile = null;
                }
            } catch (IOException e) {

            }
        }
    }

    /**
     * To check if the encrpted zip file is corrupted or not
     * 
     * @param file
     * @param password
     * @return
     */
    public static boolean isEncryptedZipValid(final File file, String password) {
        ZipArchiveInputStream input = null;
        InputStream target = null;
        try {
            target = new FileInputStream(file);
            target = new CipherInputStream(target, createCipher(Cipher.DECRYPT_MODE, password));
            input = new ZipArchiveInputStream(target);
            ArchiveEntry entry = input.getNextEntry();
            return true;

        } catch (IOException e) {
            return false;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (input != null) {
                    input.close();
                    input = null;
                }
                if (target != null) {
                    target.close();
                    target = null;
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * Used to check tar.gz/.tgz/.gz file is corrupted/destroyed
     * 
     * @param fileName
     * @return
     */
    public static boolean isGZIPValid(final String fileName) {
        GZIPInputStream inputStream = null;
        InputStream is = null;
        try {
    		is = new FileInputStream(new File(fileName));
            inputStream = new GZIPInputStream(is);
            return true;
        } catch (IOException e) {
            return false;
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                } else if(is != null) {
                	is.close();
                	is = null;
                }
            } catch (IOException e) {
            }
        }
    }
    
    /**
     * Used to check tar.tar file is corrupted/destroyed
     * 
     * @param fileName
     * @return
     */
    public static boolean isTarValid(final String fileName) {
    	TarArchiveInputStream inputStream = null;
        InputStream is = null;
        try {
    		is = new FileInputStream(new File(fileName));
            inputStream = new TarArchiveInputStream(is);
            return inputStream.canReadEntryData(inputStream.getNextEntry());
            
        } catch (IOException e) {
            return false;
        }finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                } else if(is != null) {
                	is.close();
                	is = null;
                }
            } catch (IOException e) {
            }
        }
    }

    /**
     * 
     * @param mode
     * @param password
     * @return
     * @throws Exception
     */
    public static Cipher createCipher(int mode, String password) throws Exception {
        String alg = "PBEWithSHA1AndDESede"; // BouncyCastle has better algorithms
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(alg);
        SecretKey secretKey = keyFactory.generateSecret(keySpec);

        Cipher cipher = Cipher.getInstance("PBEWithSHA1AndDESede");
        cipher.init(mode, secretKey, new PBEParameterSpec("saltsalt".getBytes(), 2000));

        return cipher;
    }
}
