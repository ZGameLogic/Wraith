package com.zgamelogic.services;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.route53.Route53Client;
import software.amazon.awssdk.services.route53.model.GetHostedZoneRequest;
import software.amazon.awssdk.services.route53.model.ListResourceRecordSetsRequest;

@Service
@AllArgsConstructor
public class AwsDebugService {
    private final Route53Client route53Client;

    @PostConstruct
    public void listAllSecrets() {
//        System.out.println(route53Client.getHostedZone(GetHostedZoneRequest.builder().id("Z10458543LUY6QMAE4J12").build()).hostedZone().name());
//        route53Client.listResourceRecordSets(ListResourceRecordSetsRequest.builder().hostedZoneId("Z10458543LUY6QMAE4J12").build()).resourceRecordSets().forEach(r -> {
//            System.out.println(r.name());
//            System.out.println(r.typeAsString());
//        });

//        secretsManagerClient.listSecrets().secretList().forEach(s -> {
//            System.out.println(s.name());
//            System.out.println(secretsManagerClient.getSecretValue(r -> r.secretId(s.arn())).secretString());
//            secretsManagerClient.deleteSecret(r -> r.secretId(s.arn()));
//        });
    }
}
