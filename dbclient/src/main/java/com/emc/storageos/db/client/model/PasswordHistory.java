/*
 * Copyright (c) 2014 EMC Corporation
 * All Rights Reserved
 */

package com.emc.storageos.db.client.model;

import java.util.*;

/**
 * Local user password history data object
 */
@Cf("PasswordHistory")
public class PasswordHistory extends DataObject {

    /**
     * storing password history in a form <hashedPassword, long>
     * long value here is TimeMillis from epoch.
     */
    private LongMap _userPasswordHash = new LongMap();

    /**
     * user's password expire date
     */
    private Calendar _expireDate;

    /**
     * the last password-to-be-expired mail sent date
     */
    private Calendar _lastNotificationMailSent;

    @Encrypt
    @Name("userPasswordHash")
    public LongMap getUserPasswordHash() {
        return _userPasswordHash;
    }

    public void setUserPasswordHash(LongMap user_password_hash) {
        _userPasswordHash = user_password_hash;
        setChanged("userPasswordHash");
    }

    @Name("expireDate")
    public Calendar getExpireDate() {
        return _expireDate;
    }

    public void setExpireDate(Calendar expireDate) {
        _expireDate = expireDate;
        setChanged("expireDate");
    }

    @Name("lastNotificationMailSent")
    public Calendar getLastNotificationMailSent() {
        return _lastNotificationMailSent;
    }

    public void setLastNotificationMailSent(Calendar lastNotificationMailSent) {
        _lastNotificationMailSent = lastNotificationMailSent;
        setChanged("lastNotificationMailSent");
    }
}
