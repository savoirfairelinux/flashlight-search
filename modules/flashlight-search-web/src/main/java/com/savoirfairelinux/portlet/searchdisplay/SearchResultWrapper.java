package com.savoirfairelinux.portlet.searchdisplay;

import java.util.List;

import com.liferay.portal.kernel.search.Document;

public class SearchResultWrapper {
    private String key;
    private List<Document> documents;

    public SearchResultWrapper() {

    }

    public SearchResultWrapper(String key, List<Document> documents) {
        setKey(key);
        setDocuments(documents);
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

}
