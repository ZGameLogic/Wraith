package com.zgamelogic.data.metra.tripUpdate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class MetraTripUpdateData {
    private String id;
    @JsonProperty("is_deleted")
    private boolean deleted;
    private MetraTripUpdateVehicle vehicle;
    @JsonProperty("trip_update")
    private MetraTripUpdate tripUpdate;
}
