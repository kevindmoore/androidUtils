
package com.mastertechsoftware.sql.cookies;

import org.apache.http.cookie.Cookie;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.impl.cookie.BasicClientCookie2;

/**
 * Cookie object that also has a db id.
 */
public class DatabaseCookie extends BasicClientCookie2 {
    protected long id;

    /**
     * Default Constructor taking a name and a value. The value may be null.
     * 
     * @param name The name.
     * @param value The value.
     */
    public DatabaseCookie(String name, String value) {
        super(name, value);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void fillCookie(Cookie cookie) {
        if (cookie instanceof BasicClientCookie) {
            BasicClientCookie basicClientCookie = (BasicClientCookie) cookie;
            setComment(basicClientCookie.getComment());
            setCommentURL(basicClientCookie.getCommentURL());
            setDomain(basicClientCookie.getDomain());
            setPath(basicClientCookie.getPath());
            setExpiryDate(basicClientCookie.getExpiryDate());
            setVersion(basicClientCookie.getVersion());
            setSecure(basicClientCookie.isSecure());
        }
    }
}
