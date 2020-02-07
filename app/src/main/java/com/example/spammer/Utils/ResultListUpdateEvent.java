package com.example.spammer.Utils;

import com.example.spammer.Models.Result;

import java.util.ArrayList;

public class ResultListUpdateEvent {
    private ArrayList<Result> resultList;

    public ResultListUpdateEvent(ArrayList<Result> resultList) {
        this.resultList = resultList;
    }

    public ArrayList<Result> getResultList() {
        return resultList;
    }
}
