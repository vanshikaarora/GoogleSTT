package com.google.cloud.android.speech.Realm;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Model extends RealmObject {
    @Required
    private String savedText;

    public String getSavedText() {
        return savedText;
    }

    public void setSavedText(String savedText) {
        this.savedText = savedText;
    }
}
