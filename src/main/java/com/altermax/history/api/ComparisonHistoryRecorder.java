package com.altermax.history.api;

public interface ComparisonHistoryRecorder {
    Long record(HistoryRecordCommand command);
}
