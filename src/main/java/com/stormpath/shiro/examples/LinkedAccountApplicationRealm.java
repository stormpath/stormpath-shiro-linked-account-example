package com.stormpath.shiro.examples;

import com.stormpath.sdk.account.Account;
import com.stormpath.sdk.account.AccountList;
import com.stormpath.sdk.account.Accounts;
import com.stormpath.sdk.application.Application;
import com.stormpath.sdk.authc.AuthenticationRequest;
import com.stormpath.sdk.directory.Directory;
import com.stormpath.sdk.resource.ResourceException;
import com.stormpath.shiro.realm.ApplicationRealm;
import org.apache.shiro.authc.*;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.UUID;

/**
 * Example Realm illustrating the switching an read-only ActiveDirectory <code>account</code> to an editable Cloud
 * Directory account, where Groups and attributes can be edited through the Stormpath UI.
 */
public class LinkedAccountApplicationRealm extends ApplicationRealm {

    private static final Logger log = LoggerFactory.getLogger(LinkedAccountApplicationRealm.class);

    /**
     * The HREF of the cloud directory that will used to managed the linked accounts.
     */
    private String cloudDirHref;

    /**
     * Custom Data attribute used to link accounts.
     */
    private String linkedAccountAttribute = "cloudAccount";

    /**
     * Custom Data attribute used to link the editable cloud account back to the source mirrored account.
     */
    private String sourceAccountAttribute = "sourceAccount";

    private Directory cloudDir;

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authcToken) throws AuthenticationException {

        // NOTE: This is copied from the parent class
        UsernamePasswordToken token = (UsernamePasswordToken) authcToken;

        AuthenticationRequest request = createAuthenticationRequest(token);

        Account account;

        try {
            // the following line is what has been changed from the parent class
            account = authenticateAndSwitchAccount(request);
        } catch (ResourceException e) {
            //todo error code translation to throw more detailed exceptions
            String msg = StringUtils.clean(e.getMessage());
            if (msg == null) {
                msg = StringUtils.clean(e.getDeveloperMessage());
            }
            if (msg == null) {
                msg = "Invalid login or password.";
            }
            throw new AuthenticationException(msg, e);
        }

        PrincipalCollection principals;

        try {
            principals = createPrincipals(account);
        } catch (Exception e) {
            throw new AuthenticationException("Unable to obtain authenticated account properties.", e);
        }

        return new SimpleAuthenticationInfo(principals, null);
    }

    /**
     * Attempts authenticate the <code>authenticationRequest</code>, if successful an equivalent
     * <code>cloud directory</code> <code>Account</code> will be created and/or returned.
     * @param authenticationRequest Authentication request info.
     * @throws ResourceException when authentication fails.
     * @return An
     */
    private Account authenticateAndSwitchAccount(AuthenticationRequest authenticationRequest) {

        Application application = ensureApplicationReference();

        // normal login
        Account account = application.authenticateAccount(authenticationRequest).getAccount();
        // if the above line was successful switch to cloud directories user.
        return switchToCloud(account);
    }


    /**
     * Returns an equivalent <code>cloud</code> Account based on the <code>sourceAccount</code>. If the
     * <code>sourceAccount</code> contains a custom data attribute defined by <code>linkedAccountAttribute</code>,
     * that account will be returned.
     *
     * If a linked account has not been found, the <code>Account</code>'s email address
     * will be used to search for an equivalent Cloud Directory Account.  If found the custom data attribute defined by
     * <code>linkedAccountAttribute</code> will be added to speed up lookups in the future.
     *
     * If an cloud directory account is still not found a new <code>Account</code> will be created with a strong random
     * password.
     *
     * @param sourceAccount The <code>Account</code> used to lookup or create the cloud account.
     * @return An editable account equivalent to <code>sourceAccount</code>
     */
    private Account switchToCloud(Account sourceAccount) {
        // 1. see if we have an existing account

        // first check to see if we have a link
        String cloudAccountHref = (String) sourceAccount.getCustomData().get(linkedAccountAttribute);
        if (cloudAccountHref != null) { // already linked
            try {
                return getClient().getResource(cloudAccountHref, Account.class);
            }
            catch(ResourceException e) {
                String msg = "Source account '{}' has link to cloud account '{}', but this account no longer exists, a new cloud account will be recreated.";
                if(log.isTraceEnabled()) {
                    log.trace(msg, new Object[] {sourceAccount.getUsername(), cloudAccountHref, e});
                }
                else {
                    log.info(msg, sourceAccount.getUsername(), cloudAccountHref);
                }
            }
        }

        Directory cloudDirectory = ensureCloudDirectory();

        // next, check by email
        AccountList accounts = cloudDirectory.getAccounts(Accounts.where(Accounts.email().eqIgnoreCase(sourceAccount.getEmail())));
        Iterator<Account> accountIterator = accounts.iterator();
        if (accountIterator.hasNext()) {
            Account account = accountIterator.next();

            // ensure links - will want to make this more efficient
            account.getCustomData().put(sourceAccountAttribute, sourceAccount.getHref());
            account.save();
            sourceAccount.getCustomData().put(linkedAccountAttribute, account.getHref());
            sourceAccount.save();

            return account;
        }

        // 2. create a new account (Note: It's here where you could dump them into a registration flow
        //    the account will be created in the default AccountStore - the cloud Directory
        Account account = getClient().instantiate(Account.class);
        account
                .setGivenName(sourceAccount.getGivenName())
                .setSurname(sourceAccount.getSurname())
                .setEmail(sourceAccount.getEmail())
                .setUsername(sourceAccount.getUsername())
                // hack - meet minimum password requirements
                .setPassword("A" + UUID.randomUUID().toString());

        // 3. link accounts
        account.getCustomData().put(sourceAccountAttribute, sourceAccount.getHref());


        // create the new account in the cloud directory
        cloudDirectory.createAccount(account);

        // update the source account for future lookups
        sourceAccount.getCustomData().put(linkedAccountAttribute, account.getHref());
        sourceAccount.save();

        return account;
    }

    public String getCloudDirHref() {
        return cloudDirHref;
    }

    public void setCloudDirHref(String cloudDirHref) {
        this.cloudDirHref = cloudDirHref;
    }

    public String getLinkedAccountAttribute() {
        return linkedAccountAttribute;
    }

    public void setLinkedAccountAttribute(String linkedAccountAttribute) {
        this.linkedAccountAttribute = linkedAccountAttribute;
    }

    public String getSourceAccountAttribute() {
        return sourceAccountAttribute;
    }

    public void setSourceAccountAttribute(String sourceAccountAttribute) {
        this.sourceAccountAttribute = sourceAccountAttribute;
    }

    private Directory ensureCloudDirectory() {
        if( cloudDir == null )
        {
            cloudDir = getClient().getResource(getCloudDirHref(), Directory.class);
        }
        return cloudDir;
    }
}
