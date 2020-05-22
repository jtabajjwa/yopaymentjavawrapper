/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yo.co.ug.yopaymentssdk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import static yo.co.ug.yopaymentssdk.YoPaymentsUtils.HTTP_REQUEST_READTIMEOUT_MILLISECONDS;
import static yo.co.ug.yopaymentssdk.YoPaymentsUtils.HTTP_REQUEST_TIMEOUT_MILLISECONDS;

/**
 *
 * @author josephtabajjwa
 */
public class YoPayments {
    String apiUsername;
    String apiPassword;
    String production_url = "https://paymentsapi1.yo.co.ug/ybs/task.php";
    String sandbox_url = "http://sandbox.yo.co.ug/services/yopaymentsdev/task.php";
    String mode = "TEST";//SET TO TEST or PRODUCTION
    String url = "";
    String pemPrivateKeyFilePath = "";
    boolean signData = false;
    HttpRequestResponse requestAndResponse;
    
    /*
    * @Param apiUsername: Obtain this from Yo! Payments dashboard
    * @Param apiPassword: Obtain this from Yo! Payments dashboard under Api Details
    * @Param mode: Set this to TEST or PRODUCTION depending on the system you are testing.
    * @oaramm signData: Set this to true if you want to sign data
    */
    public YoPayments(String apiUsername, String apiPassword, String mode, Boolean signData) {
        this.apiUsername = apiUsername;
        this.apiPassword = apiPassword;
        this.mode = mode;
        if (mode.equals("TEST")) {
            this.url = this.sandbox_url;
        } else {
            this.url = this.production_url;
        }
        if (signData) {
            this.signData = true;
            //You have to call setPrivateKeyFilePath to set the path to private key file.
        }
    }
    
    
    /*
    * @Param pemPrivateKeyFilePath: This should be a valid path to the private .pem file 
    */
    public void setPrivateKeyFilePath(String pemPrivateKeyFilePath) {
        this.pemPrivateKeyFilePath = pemPrivateKeyFilePath;
    }
    
    //TODO: take a leaf from runAcWithdrawFunds method and implement acdeposit funds. 
    /*
    * @Param account: This is the mobile money number to send money to.
    * @Param amount: The amount to send.
    * @Param narrative: Simple description about the transaction.
    * @Param reference: This is the external reference.
    */
    public YoPayments.YoPaymentsResponse runAcDepositFunds(String account, String amount, String narrative, 
            String reference) {
        return null;
    }
    
    
    
    //TODO: implement transactionCheck Status
    /*
    * @Param transactionReference: This is the transaction reference which was returned in original request.
    * 
    */
    public YoPayments.YoPaymentsResponse runAcTransactionCheckStatus(String transactionReference) {
        return null;
    }
    
    
    
    /*
    * @Param account: This is the mobile money number to send money to.
    * @Param amount: The amount to send.
    * @Param narrative: Simple description about the transaction.
    * @Param reference: This is the external reference.
    */
    public YoPayments.YoPaymentsResponse runAcWithdrawFunds(String account, String amount, String narrative, 
            String reference) throws IOException {
         
            String xmlData = getAcWithdrawFundsXml(account, amount, narrative, reference);
        
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "text/xml");
            headers.put("Content-transfer-encoding", "text");
            
            HttpRequestResponse rs = YoPaymentsUtils.doHttpRequest("POST", url, xmlData, headers);
            this.requestAndResponse = rs;
            
            //Now parse the response
            if (!rs.response.isEmpty()) {
                try {
                    YoPaymentsResponse res = new YoPaymentsResponse(rs.response);
                    //Now you can use the response, check the fields
                    
                    return res;
                    
                } catch (ParserConfigurationException ex) {
                    Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, rs.toString(), "");
            return null;
            
    }
    
    
    /*
    * @Param account: This is the mobile money number to send money to.
    * @Param amount: The amount to send.
    * @Param narrative: Simple description about the transaction.
    * @Param reference: This is the external reference.
    */
    private String getAcWithdrawFundsXml(String account, String amount, String narrative, 
            String reference) throws IOException {
        String nonce = YoPaymentsUtils.getRandomNumericString();
        String data = this.apiUsername+amount+account+narrative+reference+nonce;
        String sha1 = YoPaymentsUtils.generateSha1String(data);
        String signatureBase64 = "";
        
        if (this.signData) {
            try {
                PrivateKey privKey = YoPaymentsUtils.getPrivateKeyFromFile(this.pemPrivateKeyFilePath);
                Signature sign = Signature.getInstance("SHA1withRSA");
                sign.initSign(privKey);
                sign.update(sha1.getBytes());
                byte[] realSig = sign.sign();
                signatureBase64 = Base64.getEncoder().encodeToString(realSig);
            } catch (NoSuchAlgorithmException ex) {

                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
                return null;
            } catch (URISyntaxException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
                return null;
            } catch (InvalidKeyException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
                return null;
            } catch (SignatureException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
                ex.printStackTrace();
                return null;
            }
        }
        
        String rXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        rXml += "<AutoCreate>"
                + "<Request>"
                + "<APIUsername>"+this.apiUsername+"</APIUsername>"
                + "<APIPassword>"+this.apiPassword+"</APIPassword>"
                + "<Method>acwithdrawfunds</Method>"
                + "<Amount>"+amount+"</Amount>"
                + "<Account>"+account+"</Account>"
                + "<Narrative>"+narrative+"</Narrative>"
                + "<ExternalReference>"+reference+"</ExternalReference>";
              if (this.signData) {
                rXml += "<PublicKeyAuthenticationNonce>"+nonce+"</PublicKeyAuthenticationNonce>";
                rXml += "<PublicKeyAuthenticationSignatureBase64>"+signatureBase64+"</PublicKeyAuthenticationSignatureBase64>";
              } else {
                  rXml += "<PublicKeyAuthenticationNonce></PublicKeyAuthenticationNonce>";
                  rXml += "<PublicKeyAuthenticationSignatureBase64></PublicKeyAuthenticationSignatureBase64>";
              }
              rXml += "</Request>"
            + "</AutoCreate>";
                
        
        return rXml;
    }
    
    class YoPaymentsResponse {
        String responseXml;
        String statusCode = "";
        String status = "";
        String transactionReference = "";
        String statusMessage = "";
        String errorMessage = "";
        String mnoTransactionReferenceId = "";
        String transactionStatus = "";
                
        
        public YoPaymentsResponse(String responseXml) throws ParserConfigurationException {
            this.responseXml = responseXml;
            this.parse();
        }
        
        public void parse() throws ParserConfigurationException {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            try {
                ByteArrayInputStream input = new ByteArrayInputStream(this.responseXml.getBytes("UTF-8"));
                
                Document doc = dBuilder.parse(input);
                doc.getDocumentElement().normalize();
                NodeList nList = doc.getElementsByTagName("Response");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        if (eElement.getElementsByTagName("Status") != null 
                                && eElement.getElementsByTagName("Status").item(0) != null) {
                                
                                this.status = eElement.getElementsByTagName("Status").item(0).getTextContent();
                                
                            
                        }
                        if (eElement.getElementsByTagName("StatusCode") != null 
                                && eElement.getElementsByTagName("StatusCode").item(0) != null) {
                                this.statusCode = eElement.getElementsByTagName("StatusCode").item(0).getTextContent();
                                
                        }
                        if (eElement.getElementsByTagName("StatusMessage") != null 
                                && eElement.getElementsByTagName("StatusMessage").item(0) != null) {
                                this.statusMessage = eElement.getElementsByTagName("StatusMessage").item(0).getTextContent();
                                
                        }
                        if (eElement.getElementsByTagName("TransactionReference") != null
                                && eElement.getElementsByTagName("TransactionReference").item(0) != null) {
                            this.transactionReference = eElement.getElementsByTagName("TransactionReference").item(0).getTextContent();
                            
                        }  
                        if (eElement.getElementsByTagName("MNOTransactionReferenceId") != null && 
                                eElement.getElementsByTagName("MNOTransactionReferenceId").item(0) != null) {
                                this.mnoTransactionReferenceId = eElement.getElementsByTagName("MNOTransactionReferenceId").item(0).getTextContent();
                                
                        }
                        if (eElement.getElementsByTagName("TransactionStatus") != null 
                                && eElement.getElementsByTagName("TransactionStatus").item(0) != null) {
                                this.transactionStatus = eElement.getElementsByTagName("TransactionStatus").item(0).getTextContent();
                           
                        }
                        break;
                    }
                }
            } catch (SAXException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(YoPayments.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
