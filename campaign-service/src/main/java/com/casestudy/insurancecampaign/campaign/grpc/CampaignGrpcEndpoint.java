package com.casestudy.insurancecampaign.campaign.grpc;

import com.casestudy.insurancecampaign.campaign.service.CampaignService;
import com.casestudy.insurancecampaign.grpc.CampaignGrpcServiceGrpc;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryItem;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryRequest;
import com.casestudy.insurancecampaign.grpc.CampaignHistoryResponse;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsRequest;
import com.casestudy.insurancecampaign.grpc.CampaignStatisticsResponse;
import com.casestudy.insurancecampaign.grpc.CampaignStatusCount;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
public class CampaignGrpcEndpoint extends CampaignGrpcServiceGrpc.CampaignGrpcServiceImplBase {
    private final CampaignService campaignService;

    public CampaignGrpcEndpoint(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @Override
    public void getCampaignStatistics(CampaignStatisticsRequest request, StreamObserver<CampaignStatisticsResponse> responseObserver) {
        CampaignStatisticsResponse.Builder response = CampaignStatisticsResponse.newBuilder();
        campaignService.getStatistics().forEach((status, count) -> response.addCounts(
                CampaignStatusCount.newBuilder().setStatus(status).setCount(count).build()
        ));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getCampaignHistory(CampaignHistoryRequest request, StreamObserver<CampaignHistoryResponse> responseObserver) {
        CampaignHistoryResponse.Builder response = CampaignHistoryResponse.newBuilder();
        campaignService.getHistory(UUID.fromString(request.getCampaignId())).forEach(history -> response.addItems(
                CampaignHistoryItem.newBuilder()
                        .setPreviousStatus(history.getPreviousStatus() == null ? "" : history.getPreviousStatus().name())
                        .setNewStatus(history.getNewStatus().name())
                        .setReason(history.getReason())
                        .setChangedAt(history.getChangedAt().toString())
                        .build()
        ));
        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }
}
