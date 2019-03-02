package com.google.cloud.android.speech.Realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import io.realm.annotations.Required;

public class Model extends RealmObject {
    @Required
    @PrimaryKey
    private String id;
    @Required
    private String savedText;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSavedText() {
        return savedText;
    }

    public void setSavedText(String savedText) {
        this.savedText = savedText;
    }
}
