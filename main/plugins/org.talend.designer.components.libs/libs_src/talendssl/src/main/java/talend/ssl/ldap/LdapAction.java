package talend.ssl.ldap;

import java.util.Hashtable;
import javax.naming.directory.*;

import java.util.*;
import javax.naming.*;

public class LdapAction {
	private SearchControls sc = new SearchControls();
	
	public LdapAction () {
		sc.setSearchScope(SearchControls.SUBTREE_SCOPE);
	}
//	/**
//	 * delete non-leaf node
//	 * 
//	 * @param base
//	 *            : node(here is "cn=Forrest33,cn=Forrest3")
//	 * @param filter
//	 *            ：especial(format"(objectclass=*)",*mean all，you can chose one
//	 *            type(example "person"))
//	 * @param dc
//	 *            :DirContestx message
//	 * @param root
//	 *            :basedn "dc=talend,dc=com"
//	 */
//	public void delete(String base, String filter, DirContext dc, String root) {
//		if (root.equals(base))  {
//			System.err.println("It is not permitted to remove the root node");
//			return;
//		}
//		try {
//			NamingEnumeration<SearchResult> ne = dc.search(base, filter, sc);
//			if (!ne.hasMore()) {
//				dc.destroySubcontext(base);
//				return;
//			}
//			while (ne.hasMore()) {
//				SearchResult sr = (SearchResult) ne.next();
//				String name = sr.getName();
//				if (name != null && !"".equals(name.trim())) {
//					delete(name + "," + base , filter, dc, root);
//				} else {
//					delete( base , filter, dc, root);
//				}
//			}
//		} catch (Exception nex) {
//			System.err.println("Error: " + nex.getMessage());
//			nex.printStackTrace();
//		}
//	}
	
	/**
	 * delete non-leaf node
	 * 
	 * @param base
	 *            : node(here is "cn=Forrest33,cn=Forrest3")
	 * @param filter
	 *            ：especial(format"(objectclass=*)",*mean all，you can chose one
	 *            type(example "person"))
	 * @param dc
	 *            :DirContestx message
	 * @param root
	 *            :basedn "dc=talend,dc=com"
	 */
	public int delete(String base, String filter, DirContext dc, String root) {
		NamingEnumeration<SearchResult> ne = null;
		int nb_line = 0;
		try {
			Vector dn = new Vector();
			ne = dc.search(base, filter, sc);
			while (ne.hasMore()) {
				SearchResult sr = (SearchResult) ne.next();
				String name = sr.getName();
				if (base != null && !"".equals(base)) {
					if (name != null && !"".equals(name.trim())) {
						dn.add(name + "," + base);
					} else {
						dn.add(base);
					}
				}
			}
			// delete the entries from leaf to parent node
			for (int i = dn.size() - 1; i >= 0; i--) {
				if (!root.equals(dn.get(i))) {
//					System.out.println((String) dn.get(i));
					nb_line ++;
					dc.destroySubcontext((String) dn.get(i));
				}
			}
		} catch (Exception nex) {
			System.err.println("Error: " + nex.getMessage());
			nex.printStackTrace();
		}
		return nb_line;
	}

	public static void main(String[] args) {
		LdapAction ldapction = new LdapAction();
		DirContext dc = null;
		Hashtable env_tLDAPOutput_2 = new Hashtable();
		env_tLDAPOutput_2.put(javax.naming.Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env_tLDAPOutput_2.put(javax.naming.Context.SECURITY_AUTHENTICATION,
				"simple");// "none","simple","strong"
		env_tLDAPOutput_2.put(javax.naming.Context.SECURITY_PRINCIPAL,
				"cn=Manager,dc=talend,dc=com");
		env_tLDAPOutput_2.put(javax.naming.Context.SECURITY_CREDENTIALS,
				"secret");
		env_tLDAPOutput_2.put(javax.naming.Context.REFERRAL, "ignore");
		env_tLDAPOutput_2.put("java.naming.ldap.derefAliases", "always");
		env_tLDAPOutput_2.put(javax.naming.Context.PROVIDER_URL, "ldap://"
				+ "192.168.0.232" + ":" + 389 + "/" + "dc=talend,dc=com");
		try {
			dc = new InitialDirContext(env_tLDAPOutput_2);//
			System.out.println("successful");//
		} catch (javax.naming.AuthenticationException e) {
			System.out.println("fail");
		} catch (Exception e) {
			System.out.println("fail：" + e);
		}
		String base = "dc=talend,dc=com";
		String filter = "(objectclass=*)";
		String root = "dc=talend,dc=com";
//		ldapction.delete("cn=Forrest", filter, dc, root);
		ldapction.delete("cn=Forrest", filter, dc, root);
		
		try {
			dc.close();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}