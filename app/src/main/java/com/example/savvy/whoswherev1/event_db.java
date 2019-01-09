package com.example.savvy.whoswherev1;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBIndexRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBRangeKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;
import java.util.Map;
import java.util.Set;

@DynamoDBTable(tableName = "whoswhere-mobilehub-99353394-Events")

public class event_db {
    private String _eventId;
    private String _end;
    private String _locationId;
    private String _name;
    private String _start;
    private List<String> _users;

    @DynamoDBHashKey(attributeName = "eventId")
    @DynamoDBAttribute(attributeName = "eventId")
    public String getEventId() {
        return _eventId;
    }

    public void setEventId(final String _eventId) { this._eventId = _eventId; }
    @DynamoDBAttribute(attributeName = "end")
    public String getEnd() {
        return _end;
    }

    public void setEnd(final String _end) {
        this._end = _end;
    }
    @DynamoDBAttribute(attributeName = "locationId")
    public String getLocationId() {
        return _locationId;
    }

    public void setLocationId(final String _locationId) {
        this._locationId = _locationId;
    }
    @DynamoDBAttribute(attributeName = "name")
    public String getName() {
        return _name;
    }

    public void setName(final String _name) {
        this._name = _name;
    }
    @DynamoDBAttribute(attributeName = "start")
    public String getStart() {
        return _start;
    }

    public void setStart(final String _start) {
        this._start = _start;
    }
    @DynamoDBAttribute(attributeName = "users")
    public List<String> getUsers() {
        return _users;
    }

    public void setUsers(final List<String> _users) {
        this._users = _users;
    }

    // setters and getters for other attributes ...

}

