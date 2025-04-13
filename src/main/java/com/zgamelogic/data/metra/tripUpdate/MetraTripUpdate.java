package com.zgamelogic.data.metra.tripUpdate;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MetraTripUpdate {
    private MetraTripUpdateTimestamp timestamp;
    private MetraTripUpdateVehicle vehicle;
}
