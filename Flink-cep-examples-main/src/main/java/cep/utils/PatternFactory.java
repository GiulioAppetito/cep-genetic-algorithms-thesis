package cep.utils;

import cep.events.LoginEvent;
import cep.patterns.*;
import org.apache.flink.cep.pattern.Pattern;

public class PatternFactory {

    public enum PatternType {
        MULTIPLE_SUCCESS,
        FAIL_THEN_SUCCESS,
        FAIL_THEN_SUCCESS_WITHIN_TIME_PATTERN,
        MULTIPLE_FAILURE_WITHIN_TIME_PATTERN,
        LOGIN_BURST_PATTERN
    }

    public static Pattern<LoginEvent, ?> getPattern(PatternType patternType) {
        switch (patternType) {
            case MULTIPLE_SUCCESS:
                return MultipleSuccessPattern.getPattern();
            case FAIL_THEN_SUCCESS:
                return FailThenSuccessPattern.getPattern();
            case FAIL_THEN_SUCCESS_WITHIN_TIME_PATTERN:
                return FailThenSuccessWithinTimePattern.getPattern();
            case MULTIPLE_FAILURE_WITHIN_TIME_PATTERN:
                return MultipleFailuresWithinTimePattern.getPattern();
            case LOGIN_BURST_PATTERN:
                return LoginBurstPattern.getPattern(10,1);
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
    }
}
