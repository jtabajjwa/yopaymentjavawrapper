/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package yo.co.ug.yopaymentssdk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author josephtabajjwa
 */
public class VerifySignature {
    
    //Set here the correcct path where the certificate was stored.
    static String PUBLIC_KEY_PATH = "/Users/josephtabajjwa/Desktop/Joe/projects/yopayments API/Java/yopaymentssdk/target/keys/Yo_Uganda_Public_Certificate.crt";
    static String PUBLIC_KEY_PATH_SECONDARY = "/Users/josephtabajjwa/Desktop/Joe/projects/yopayments API/Java/yopaymentssdk/target/keys/Yo_Uganda_Secondary_Public_Certificate.crt";
    //static String PRIVATE_KEY_PATH = "/Users/josephtabajjwa/Desktop/Joe/projects/yopayments API/Java/yopaymentssdk/target/keys/private_key.key";
    static String PRIVATE_KEY_PATH = Paths.get(".")
            .toAbsolutePath()
            .normalize().toString()+"/target/keys/private_key.key";
    
    public static void main(String[] args) throws IOException {
        
        //Test WithdrawRequest
        testWithdawRequest();
        
        /*Uncomment the line below to test normalVerification*/
        //normalVerification();
        
        
        /*Uncomment the line below to test secondary verification*/
        //secondaryVerification();
    }
    
    static void testWithdawRequest() throws IOException {
        YoPayments yp = new YoPayments(
                "90003066053", 
                "do9t-IqUe-FxJW-IgUI-NV1E-fDee-YhOQ-iikQ", 
                "TEST", 
                true);
        
        //Now set the private key path
        yp.setPrivateKeyFilePath(PRIVATE_KEY_PATH);
        
        //Initiate a withdraw request
        YoPayments.YoPaymentsResponse r = yp.runAcWithdrawFunds(
                "256783086794", 
                "500", 
                "Java Sample", 
                "R1004-"+YoPaymentsUtils.getRandomNumericString());
        
        //Display the response 
        if (r != null) {
            System.out.println("Status: "+r.status);
            System.out.println("StatusCode: "+r.statusCode);
            System.out.println("StatusMessage: "+r.statusMessage);
            System.out.println("ErrorMessage: "+r.errorMessage);
            System.out.println("TransactionStatus: "+r.transactionStatus);
            System.out.println("TransactionReference: "+r.transactionReference);
            System.out.println("NetworkReferenceId: "+r.mnoTransactionReferenceId);
        }
        
        //Request Trace
        System.out.println(yp.requestAndResponse);
        
    }
    
    static void normalVerification() {
        String datetime = "2014-02-07 14:48:07";
        String amount = "1000";
        String narrative = "SpinApp Userid:7 Number:256783086794";
        String network_ref = "1327659406";
        String external_ref = "";
        String msisdn = "256783086794";
        String signatureBase64 = "05b4cTk+IDhI8aqRhsFR2zXbbl9xfWJPHO+WAn/sSWCCB0zQeePvqjUTONk6w8wcaue0YbCO2cd1ER3l0K8aJUj8Ob7Ixl7o5cNsYwCHu8cDenBFxUL8UBnlSxZAkOXf/vi47rwT3Eon9KpPJxJISLnp1vyVJgkWAH9GFsX1zLY33314sekJ1KFzPxY55vkTaUic9BfpIKsj+L4XFcgHpnJHqA20byAEE8uYmdrrSbwlCnEdqJx3ROE3gxMS/M0gAwPcjZFziawAfFaUARogFmrkRA9KKjA9XLPMvN8tN8vNwVbg8xV5p/K4pmBA3Z4DtnJAaYAeUXvgW8Dij+UDdw==";
        
        String signedData = datetime+amount+narrative+network_ref+external_ref
                +msisdn;
        
        //First read the public key from the file
        String pKeyString = YoPaymentsUtils.readAllBytesFromFile(PUBLIC_KEY_PATH);
        if (pKeyString == null) {
            Logger.getLogger(VerifySignature.class.getName())
                    .log(Level.SEVERE, "Couldn't read PK from file", "");
            return;
        }
        
        try {
            Signature sign = Signature.getInstance("SHA1withRSA");
            
            //Obtain the public key resource
            PublicKey publicKey = YoPaymentsUtils.getPublicKeyFromBase64String(pKeyString);
            //PublicKey publicKey = YoPaymentsUtils.getKey(PUBLIC_KEY_PATH);
            if (publicKey==null) {
                System.out.print("PublicKey must not be null\n");
                return;
            }
            
            sign.initVerify(publicKey);
            sign.update(signedData.getBytes());
            
            //Try to decode base64 to byte array
            byte[] signature_content;
            try{
                signature_content = Base64.getDecoder().decode(signatureBase64);
            } catch (Exception e) {
                Logger.getLogger(VerifySignature.class.getName())
                        .log(Level.SEVERE, e.getMessage(), "");
                return;
            }

            //Now check the signature
            if (signature_content.length < 256) {
                System.out.print("Invalid Base64 signature data\n");
                return;
            }

            if (!sign.verify(signature_content)) {
                System.out.print("Signature verification FAILED\n");
                return;
            }
            
            
            System.out.print("Signature verification PASSED\n");
            

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        } catch (SignatureException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        }
    }
    
    static void secondaryVerification() {
        String datetime = "2020-04-07 23:34:34";
        String amount = "15000";
        String narrative = "8218000126135 EMPLOYEE";
        String network_ref = "9322253676";
        String external_ref = "";
        String msisdn = "256774003539";
        String signatureBase64 = "Dlg+HD/O9xIS0JP1WyGgJizDZPhe67QB5lJIH4PRPtTyr1tkIbZN9ZDwUifmZOmbUfSEXxS10BaZsJuiMjayZcLfRdPhcVr4CQ/riMzoKuduqGRPRHGeYsvo+ZWFTPt6/vhaGVozPxhyflAEMhzZZYyMPRyBrflIZSWVVLIcgxzrzjd5h46kzF11IU1FMvXpHIz3q+xNdK3uO6igyQHRdlCqt+mcccTTdQ4bZDJ3MWfKGP2bUgOgjME1NAAwsOkIZHNgQHdpyzu/VFFJRsBUyMzoge7VPLwhEBLbYbiM0AqEIXRGfWVk19t9OkQ8frutyQT+PEVMAHfkBF3NuJFH3g==";
        
        String signedData = datetime+amount+narrative+network_ref+external_ref
                +msisdn;
        
        //First read the public key from the file
        String pKeyString = YoPaymentsUtils.readAllBytesFromFile(PUBLIC_KEY_PATH_SECONDARY);
        if (pKeyString == null) {
            Logger.getLogger(VerifySignature.class.getName())
                    .log(Level.SEVERE, "Couldn't read PK from file", "");
            return;
        }
        
        try {
            Signature sign = Signature.getInstance("SHA1withRSA");
            
            //Obtain the public key resource
            PublicKey publicKey = YoPaymentsUtils.getPublicKeyFormPEMFormat(pKeyString);
            //PublicKey publicKey = YoPaymentsUtils.getKey(PUBLIC_KEY_PATH);
            if (publicKey==null) {
                System.out.print("PublicKey must not be null\n");
                return;
            }
            
            sign.initVerify(publicKey);
            sign.update(signedData.getBytes());
            
            //Try to decode base64 to byte array
            byte[] signature_content;
            try{
                signature_content = Base64.getDecoder().decode(signatureBase64);
            } catch (Exception e) {
                Logger.getLogger(VerifySignature.class.getName())
                        .log(Level.SEVERE, e.getMessage(), "");
                return;
            }

            //Now check the signature
            if (signature_content.length < 256) {
                System.out.print("Invalid Base64 signature data\n");
                return;
            }

            if (!sign.verify(signature_content)) {
                System.out.print("Signature verification FAILED\n");
                return;
            }
            
            
            System.out.print("Signature verification PASSED\n");
            

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        } catch (InvalidKeyException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        } catch (SignatureException ex) {
            Logger.getLogger(VerifySignature.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            return;
        }
    }
    
}
