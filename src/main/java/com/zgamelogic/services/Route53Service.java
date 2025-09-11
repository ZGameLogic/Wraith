package com.zgamelogic.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.*;

@Service
@AllArgsConstructor
public class Route53Service {
    private final Route53Client route53Client;

    public void addCnameRecord(String prefix) {
        String hostedZoneId = "Z10458543LUY6QMAE4J12";

        ResourceRecordSet recordSet = ResourceRecordSet.builder()
            .name(prefix + ".zgamelogic.com")
            .type(RRType.CNAME)
            .ttl(300L)
            .resourceRecords(ResourceRecord.builder().value("zgamelogic.com").build())
            .build();

        Change change = Change.builder()
            .action(ChangeAction.UPSERT)
            .resourceRecordSet(recordSet)
            .build();

        ChangeBatch changeBatch = ChangeBatch.builder()
            .changes(change)
            .build();

        ChangeResourceRecordSetsRequest request = ChangeResourceRecordSetsRequest.builder()
            .hostedZoneId(hostedZoneId)
            .changeBatch(changeBatch)
            .build();

        route53Client.changeResourceRecordSets(request);
    }
}
