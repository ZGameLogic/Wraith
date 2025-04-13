package com.zgamelogic.data.metra.tripUpdate;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MetraTripUpdateTimestamp {
    private String low;
    private long high;
    private boolean unsigned;
}
