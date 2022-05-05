package com.lingjoin.elasticsearch.util;

import org.elasticsearch.SpecialPermission;

import java.security.PrivilegedAction;

/**
 * Copy code from <a href="https://github.com/elastic/elasticsearch/blob/master/plugins/discovery-gce/src/main/java/org/elasticsearch/cloud/gce/util/Access.java">elasticsearch repo</a>
 */
@SuppressWarnings("removal")
public class Access {
    private Access() {
    }

    /**
     * Do privilege action.
     *
     * @param <T>       the type parameter
     * @param operation the operation
     * @return the t
     */
    public static <T> T doPrivileged(final PrivilegedAction<T> operation) {
        SpecialPermission.check();
        return java.security.AccessController.doPrivileged(operation);
    }
}
