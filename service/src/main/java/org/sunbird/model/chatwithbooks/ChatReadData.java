package org.sunbird.model.chatwithbooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatReadData implements Serializable {

    private String id;
    private String userId;
    private String saveQuery;
    private String searchQueryDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSaveQuery() {
        return saveQuery;
    }

    public void setSaveQuery(String saveQuery) {
        this.saveQuery = saveQuery;
    }

    public String getSearchQueryDate() {
        return searchQueryDate;
    }

    public void setSearchQueryDate(String searchQueryDate) {
        this.searchQueryDate = searchQueryDate;
    }

    @Override
    public String toString() {
        return "ChatReadData{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", saveQuery='" + saveQuery + '\'' +
                ", searchQueryDate='" + searchQueryDate + '\'' +
                '}';
    }
}
