package org.talend.ms.crm.sdk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.InvalidParameterException;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Microsoft Online (Microsoft account as well as Microsoft Office 365 a.k.a. OSDP / OrgId)) Authentication Policy for
 * CRM Web Services.
 */
public final class OnlineAuthenticationPolicy {

    static Logger logger = LoggerFactory.getLogger(OnlineAuthenticationPolicy.class.getName());

    /**
     * Construct an Instance of the OnlineAuthenticationPolicy class.
     * 
     * @param flatWsdlUrl URL to the Flattened WSDL
     */
    public OnlineAuthenticationPolicy(String flatWsdlUrl) throws ParserConfigurationException, SAXException, IOException,
            URISyntaxException, XPathExpressionException, WSDLException {
        this.initialize(flatWsdlUrl);
    }

    /**
     * Construct an Instance of the OnlineAuthenticationPolicy class
     * 
     * @param appliesTo AppliesTo for the web service
     * @param policy Microsoft account Policy that should be used
     * @param issuerUri Issuer URI that should be used for authenticating tokens
     */
    public OnlineAuthenticationPolicy(String appliesTo, String policy, URI issuerUri) {
        this.setAppliesTo(appliesTo);
        this.setPolicy(policy);
        this.setIssuerUri(issuerUri);
    }

    private String privateAppliesTo;

    /**
     * AppliesTo value that should be set on the service
     */
    public final String getAppliesTo() {
        return privateAppliesTo;
    }

    private void setAppliesTo(String value) {
        privateAppliesTo = value;
    }

    private String privatePolicy;

    /**
     * Microsoft account / Org Id Policy
     */
    public final String getPolicy() {
        return privatePolicy;
    }

    private void setPolicy(String value) {
        privatePolicy = value;
    }

    private URI privateIssuerUri;

    /**
     * Microsoft account / Org Id Issuer that issues the tokens
     */
    public final URI getIssuerUri() {
        return privateIssuerUri;
    }

    private void setIssuerUri(URI value) {
        privateIssuerUri = value;
    }

    private void initialize(String flatWsdlUrl) throws ParserConfigurationException, SAXException, IOException,
            URISyntaxException, XPathExpressionException, WSDLException {
        if (flatWsdlUrl == null) {
            throw new NullPointerException("flatWsdlUrl");
        }

        // Download the WSDL document.
        String document = DownloadWsdlInToString(flatWsdlUrl);

        // Fetching target wsdl uri from "wsdl:import" node.
        String targetWsdlSuffix = selectFirstMatchOrDefault(document, "(?i)(<wsdl:import.*?wsdl)(.+?)(\".*?/>)");
        String targetDocument = DownloadWsdlInToString(flatWsdlUrl + targetWsdlSuffix);

        // Initialize with the values from the downloaded WSDL.
        this.initialize(flatWsdlUrl, targetDocument);
    }

    private void initialize(String url, String wsdl) throws URISyntaxException, InvalidParameterException {
        final String PolicyNodeContentRegexPatern = "(?i)(<wsp:All.*?>)(.+?)(</wsp:All>)";
        final String LiveIdentityProviderTrustRegexPatern = "(?i)(<ms-xrm:LiveTrust.*?>)(.+?)(</ms-xrm:LiveTrust>)";
        final String OrgIdentityProviderTrustRegexPatern = "(?i)(<ms-xrm:OrgTrust.*?>)(.+?)(</ms-xrm:OrgTrust>)";
        final String IssuerContainerRegexPater = "(?i)(<Issuer.*?>)(.+?)(</Issuer>)";

        final String AppliesToRegexPattern = "(?i)(<ms-xrm:AppliesTo.*?>)(.+?)(</ms-xrm:AppliesTo>)";
        final String LivePolicyRegexPattern = "(?i)(<ms-xrm:LivePolicy.*?>)(.+?)(</ms-xrm:LivePolicy>)";

        final String LiveIdReferenceIssuerUriNodeNameRegexPattern = "(?i)(<LiveIssuer.*?>)(.+?)(</LiveIssuer>)";
        final String OrgIdReferenceIssuerUriNodeNameRegexPattern = "(?i)(<OrgIdIssuer.*?>)(.+?)(</OrgIdIssuer>)";
        final String AddressIssuerUriNodeNameRegexPattern = "(?i)(<Address.*?>)(.+?)(</Address>)";

        // Retrieve the root policy node that will contain all of the values
        String policyNode = selectFirstMatchOrDefault(wsdl, PolicyNodeContentRegexPatern);
        if (null != policyNode) {
            // Set authentication type to OnlineFederation.
            boolean authWithOrgId = true;

            // Retrieve the OrgId authentication policy (that contains the AppliesTo and LivePolicy values).
            String IdentityProviderTrustConfiguration = selectFirstMatchOrDefault(policyNode,
                    OrgIdentityProviderTrustRegexPatern);

            // If OrgId authentication policy node not found then try LiveId policy node.
            if (null == IdentityProviderTrustConfiguration || IdentityProviderTrustConfiguration == "") {
                IdentityProviderTrustConfiguration = selectFirstMatchOrDefault(policyNode, LiveIdentityProviderTrustRegexPatern);
                // Set OnlineFederation authentication flag to false.
                authWithOrgId = false;
            }

            if (null != IdentityProviderTrustConfiguration) {
                String appliesTo = selectFirstMatchOrDefault(IdentityProviderTrustConfiguration, AppliesToRegexPattern);
                String livePolicy = selectFirstMatchOrDefault(IdentityProviderTrustConfiguration, LivePolicyRegexPattern);
                // The issuer container node contains the Issuer URI. Since the
                // Discovery Service exposes both Office 365
                // and Microsoft account authentication mechanisms, it lists
                // multiple issuers. In that case, the issuer is
                // listed under the reference parameters. In other scenarios, it
                // is listed in the Address node instead.
                String issuerContainerNode = selectFirstMatchOrDefault(policyNode, IssuerContainerRegexPater);
                if (null != issuerContainerNode) {
                    // Read the value from the reference parameters. If it is
                    // not set, check the Address node.
                    String issuerUri = selectFirstMatchOrDefault(issuerContainerNode, (authWithOrgId)
                            ? OrgIdReferenceIssuerUriNodeNameRegexPattern : LiveIdReferenceIssuerUriNodeNameRegexPattern);
                    // Try Address node to find issuer Uri.
                    if (null == issuerUri || "" == issuerUri) {
                        issuerUri = selectFirstMatchOrDefault(issuerContainerNode, AddressIssuerUriNodeNameRegexPattern);
                    }
                    // If the issuer was discovered, it means that all of the
                    // required information has been found.
                    if (null != issuerUri && "" != issuerUri) {
                        this.initialize(appliesTo, livePolicy, new URI(issuerUri));
                        return;
                    }
                }
            }
        }

        throw new InvalidParameterException(
                String.format(Locale.getDefault(), "Unable to parse the authentication policy from the WSDL \"%s\".", url));

    }

    private void initialize(String appliesTo, String policy, URI issuerUri) {
        if (null == appliesTo || "" == appliesTo.trim()) {
            throw new NullPointerException("appliesTo");
        } else if (null == policy || "" == policy.trim()) {
            throw new NullPointerException("policy");
        } else if (null == issuerUri || "" == issuerUri.toString().trim()) {
            throw new NullPointerException("issuerUri");
        }

        this.privateAppliesTo = appliesTo;
        this.privatePolicy = policy;
        this.privateIssuerUri = issuerUri;
    }

    private String selectFirstMatchOrDefault(String input, String searchPattern) {
        Pattern pattern = Pattern.compile(searchPattern);
        Matcher myMatcher = pattern.matcher(input);
        if (myMatcher.find()) {
            return myMatcher.group(2);
        } else
            return "";
    }

    private String DownloadWsdlInToString(String flatWsdlUrl)
            throws ParserConfigurationException, IOException, URISyntaxException, SAXException, WSDLException {
        URL url = new URL(flatWsdlUrl);

        InputStream ins = url.openConnection().getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        // Get wsdl content in a string.
        String wsdlFile = "";
        String str;
        while ((str = reader.readLine()) != null)
            wsdlFile = wsdlFile + str;
        reader.close();

        return wsdlFile;
    }
}
