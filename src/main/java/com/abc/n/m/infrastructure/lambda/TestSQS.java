package com.abc.n.m.infrastructure.lambda;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder(toBuilder = true)
@Jacksonized
public class TestSQS {

    @NonNull
    private String trcaeId;
}
