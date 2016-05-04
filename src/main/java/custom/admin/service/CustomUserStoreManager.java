package custom.admin.service;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.ldap.LDAPConstants;
import org.wso2.carbon.user.core.ldap.ReadWriteLDAPUserStoreManager;
import org.wso2.carbon.user.core.util.JNDIUtil;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.UserRealm;

public class CustomUserStoreManager extends ReadWriteLDAPUserStoreManager {

    public CustomUserStoreManager() {

    }

    public CustomUserStoreManager(org.wso2.carbon.user.api.RealmConfiguration realmConfig,
                                  Map<String, Object> properties,
                                  ClaimManager claimManager,
                                  ProfileConfigurationManager profileManager,
                                  UserRealm realm, Integer tenantId)
            throws UserStoreException {

        super(realmConfig, properties, claimManager, profileManager, realm, tenantId);

    }

    public String[] runSearchFilter(String filter) throws UserStoreException {

            List<String> values = new ArrayList<String>();
            String userPropertyName = realmConfig
                    .getUserStoreProperty(LDAPConstants.USER_NAME_ATTRIBUTE);

            String searchFilter = filter;

            DirContext dirContext = this.connectionSource.getContext();
            NamingEnumeration<?> answer = null;
            NamingEnumeration<?> attrs = null;
            try {
                answer = this.searchForUser(searchFilter, new String[] { userPropertyName }, dirContext);
                while (answer.hasMoreElements()) {
                    SearchResult sr = (SearchResult) answer.next();
                    Attributes attributes = sr.getAttributes();
                    if (attributes != null) {
                        Attribute attribute = attributes.get(userPropertyName);
                        if (attribute != null) {
                            StringBuffer attrBuffer = new StringBuffer();
                            for (attrs = attribute.getAll(); attrs.hasMore();) {
                                String attr = (String) attrs.next();
                                if (attr != null && attr.trim().length() > 0) {
                                    attrBuffer.append(attr + ",");
                                }
                            }
                            String propertyValue = attrBuffer.toString();
                            // Length needs to be more than one for a valid
                            // attribute, since we
                            // attach ",".
                            if (propertyValue != null && propertyValue.trim().length() > 1) {
                                propertyValue = propertyValue.substring(0, propertyValue.length() - 1);
                                values.add(propertyValue);
                            }
                        }
                    }
                }

            } catch (NamingException e) {
                throw new UserStoreException(e.getMessage(), e);
            } finally {
                // close the naming enumeration and free up resources
                JNDIUtil.closeNamingEnumeration(attrs);
                JNDIUtil.closeNamingEnumeration(answer);
                // close directory context
                JNDIUtil.closeContext(dirContext);
            }
            return values.toArray(new String[values.size()]);
        }

}
