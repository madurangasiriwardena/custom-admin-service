package custom.admin.service;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.core.AbstractAdmin;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.core.common.AbstractUserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;

public class CustomAdminService extends AbstractAdmin {

    protected Log log = LogFactory.getLog(CustomAdminService.class);

    public String[] filterUsers(String filter) throws Exception {
        //get super tenant context and get realm service which is an osgi service
        RealmService realmService = (RealmService)
                PrivilegedCarbonContext.getThreadLocalCarbonContext().getOSGiService(RealmService.class);
        if (realmService == null) {
            String error = "Can not obtain carbon realm service..";
            throw new Exception(error);
        }

        int tenantId = -1234;
        UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
        if (userRealm == null || !(userRealm instanceof org.wso2.carbon.user.core.UserRealm)) {
            String error = "Can not obtain user realm for tenant carbon.super.";
            throw new Exception(error);
        }
        AbstractUserStoreManager userStoreManager = (AbstractUserStoreManager) userRealm.getUserStoreManager();
        if (userStoreManager instanceof CustomUserStoreManager) {
            return ((CustomUserStoreManager)userStoreManager).runSearchFilter(filter);
        }else {
            throw new Exception("Operation not supported");
        }
    }
}
