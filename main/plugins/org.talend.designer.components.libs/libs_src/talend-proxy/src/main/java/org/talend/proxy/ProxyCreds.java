package org.talend.proxy;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class ProxyCreds {
    private Pair<String, String> credPair;

    public ProxyCreds(String userName, String pass) {
        this.credPair = new ImmutablePair<>(userName, pass);
    }

    public String getUser() {
        return credPair.getKey();
    }

    public String getPass() {
        return credPair.getValue();
    }
}
