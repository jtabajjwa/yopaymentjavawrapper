/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yo.co.ug.yopaymentssdk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import static yo.co.ug.yopaymentssdk.VerifySignature.PUBLIC_KEY_PATH;

/**
 *
 * @author josephtabajjwa
 */
public class YoPaymentsUtils {
    
    /*
    * @Param base64String: This is the base64 string you want to decode. Should be 
    * in PEM format.
    * 
    * return null | PublicKey resourse.
    */
    
    public static int HTTP_REQUEST_READTIMEOUT_MILLISECONDS = 60000;
    public static int HTTP_REQUEST_TIMEOUT_MILLISECONDS = 30000;
    
    public static PublicKey getPublicKeyFormPEMFormat(String base64String) {
        try {
            String base_str = base64String;
            base_str = base_str.replace("-----BEGIN PUBLIC KEY-----", "");
            base_str = base_str.replace("-----END PUBLIC KEY-----", "");
            base_str = base_str.replace("\n", "");
            base_str = base_str.replace("\r", "");
            
            byte[] key = Base64.getDecoder().decode(base_str);
            
            X509EncodedKeySpec spec = new X509EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
      
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        } catch (java.lang.IllegalArgumentException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        }
    }
    
    
    /*
    * @Param base64String: This is the base64 string you want to decode
    * 
    * return null | PublicKey resourse.
    */
    public static PublicKey getPublicKeyFromBase64String(String base64String) {
        try {
            
            Certificate cert=CertificateFactory.getInstance("X.509")
                    .generateCertificate(new ByteArrayInputStream(base64String.getBytes("utf-8")));
            
            return cert.getPublicKey();
        } catch (CertificateException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return null;
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }
    
    /*
    * @Param base64String: This is the base64 string you want to decode
    * 
    * return null | PublicKey resourse.
    */
        public static PrivateKey getPrivateKeyFromFile(String filePath) throws IOException, URISyntaxException, NoSuchAlgorithmException, InvalidKeySpecException {
            String privateKeyContent = readAllBytesFromFile(filePath);
            
            privateKeyContent = privateKeyContent.replaceAll("\\n", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(
                    Base64.getDecoder().decode(privateKeyContent)
            );
            PrivateKey privKey = kf.generatePrivate(keySpecPKCS8);
            return privKey;
    }
    
    
    public static PublicKey getKey(String file) {
        try {
            CertificateFactory fact = CertificateFactory.getInstance("X.509");
            FileInputStream is = new FileInputStream (file);
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            PublicKey key = cer.getPublicKey();
            return key;
        } catch (CertificateException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(YoPaymentsUtils.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return null;
    }
    
    public static String readAllBytesFromFile(String filePath) 
    {
        String content = "";
        try
        {
            content = new String ( Files.readAllBytes( Paths.get(filePath) ) );
        } 
        catch (IOException e) 
        {
            e.printStackTrace();
            return null;
        }
        return content;
    }
    
    public static String getRandomNumericString() {
        int int_random = ThreadLocalRandom.current().nextInt();  
        return int_random+"";
    }
    
    public static String generateSha1String(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(data.getBytes("utf8"));
            String sha1 = String.format("%040x", new BigInteger(1, digest.digest()));
            return sha1;
        } catch (Exception e){
                e.printStackTrace();
                return "";
        }
    }
    
    /*
    * 
    * Helper method to make http requests.
    * 
    *
    * @Param method: This may be set to GET, POST, PUT, DELETE.
    * @Param url: This is the url to call.
    * @Param data: This is the data to be sent.
    * @Param headers: a hashmap of headers.
    * 
    * Returns HttpRequestREsponse class
    */
    
    public static HttpRequestResponse doHttpRequest(String method, String url, 
            String data, Map<String, String> headers) {
        HttpRequestResponse r = new HttpRequestResponse();
            r.setUrl(url);
            r.setRequestData(data);
            r.setRequestHeaders(headers);
        TrustManager[] dummyTrustManager = getTrustmanager();
        try {
            SSLContext sc;
            try {
                //sc = SSLContext.getInstance("TLS");
                sc = SSLContext.getInstance("SSL");
                sc.init(null, dummyTrustManager, new java.security.SecureRandom());
                
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (NoSuchAlgorithmException ex) {
                r.setResponse("");
                r.setErrorMessage(ex.getMessage());
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                return r;
            }
            
            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
            
            HttpURLConnection con;
            URL rquestUrl = new URL(url);
            if (rquestUrl.getProtocol().toLowerCase().equals("https")) {
                con = (HttpsURLConnection) rquestUrl.openConnection();
                //con.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } else {
                con = (HttpURLConnection) rquestUrl.openConnection();
            }
            
            con.setRequestMethod(method);
            
            for (Map.Entry<String, String> h : headers.entrySet()) {
                con.setRequestProperty(h.getKey(), h.getValue());
            }
            
            con.setConnectTimeout(HTTP_REQUEST_TIMEOUT_MILLISECONDS);
            con.setReadTimeout(HTTP_REQUEST_READTIMEOUT_MILLISECONDS);
            con.setDoOutput(true);
            
            
            
            //methods without the body.
            List<String> methods = new ArrayList();
            methods.add("DELETE");
            methods.add("PUT");
            methods.add("POST");
            
            if (methods.contains(method)) {
                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(data);
                out.flush();
                out.close();
            }
            
            //Now read the content of the response
            int status = con.getResponseCode();
            
            Reader streamReader = null;
            if (status > 299) {
                if (con.getErrorStream() == null) {
                    streamReader = null;
                } else {
                    streamReader = new InputStreamReader(con.getErrorStream());
                }
            } else {
                streamReader = new InputStreamReader(con.getInputStream());
            }
            
            //streamReader = new InputStreamReader(con.getInputStream());
            StringBuffer content = new StringBuffer();
            if (streamReader != null) {
                BufferedReader in = new BufferedReader(streamReader);
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                r.setStatusCode(status);
                r.setResponse(content.toString());
                r.setRequestHeaders(headers);
            } else {
                content.append("");
                r.setStatusCode(status);
                r.setResponse(content.toString());
                r.setRequestHeaders(headers);
            }
            Map<String, String> rHeaders = new HashMap<>();
           
            con.getHeaderFields().entrySet().stream()
                .filter(entry -> entry.getKey() != null)
                .forEach(entry -> {
                
                    List headerValues = entry.getValue();
                    String sHeaderValue = "";
                    Iterator it = headerValues.iterator();
                    if (it.hasNext()) {
                        sHeaderValue += it.next();
                        while (it.hasNext()) {
                            sHeaderValue += it.next();
                        }
                    }

                    rHeaders.put(entry.getKey(), sHeaderValue);   
            });
            r.setResponseHeaders(rHeaders);
            r.setErrorMessage("");
            con.disconnect(); 
            
            return r;
        } catch (MalformedURLException ex) {
            r.setResponse("");
            r.setErrorMessage(ex.getMessage());
            Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return r;
        } catch (IOException ex) {
            r.setResponse("");
            r.setErrorMessage(ex.getMessage());
            Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return r;
        } catch (KeyManagementException ex) {
            r.setResponse("");
            r.setErrorMessage(ex.getMessage());
            Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return r;
        } catch (Exception ex) {
            r.setResponse("");
            r.setErrorMessage(ex.getMessage());
            Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return r;
        }
    }
    
    public static TrustManager[] getTrustmanager() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };
        return trustAllCerts;
    }
    
}
