package com.fraudwatch.repository;

public interface RuleFlagCount {
    Long getRuleId();
    String getRuleName();
    Long getFlaggedCount();
}
