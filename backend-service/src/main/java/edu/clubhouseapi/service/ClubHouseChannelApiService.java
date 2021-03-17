package edu.clubhouseapi.service;

import edu.clubhouseapi.config.ClubHouseConfigProperties;
import edu.clubhouseapi.dto.AcceptSpeakerInviteRequest;
import edu.clubhouseapi.dto.AcceptSpeakerInviteResponse;
import edu.clubhouseapi.dto.ChannelsResponse;
import edu.clubhouseapi.dto.EmptyResponse;
import edu.clubhouseapi.dto.GetChannelRequest;
import edu.clubhouseapi.dto.GetChannelResponse;
import edu.clubhouseapi.dto.GetEventsResponse;
import edu.clubhouseapi.dto.InviteSpeakerRequest;
import edu.clubhouseapi.dto.InviteSpeakerResponse;
import edu.clubhouseapi.dto.JoinChannelRequest;
import edu.clubhouseapi.dto.JoinChannelResponse;
import edu.clubhouseapi.dto.LeaveChannelRequest;
import edu.clubhouseapi.dto.MakeModeratorRequest;
import edu.clubhouseapi.dto.PingRequest;
import edu.clubhouseapi.dto.PingResponse;
import edu.clubhouseapi.dto.RaiseHandsRequest;
import edu.clubhouseapi.dto.RejectSpeakerInviteRequest;
import edu.clubhouseapi.dto.RejectSpeakerInviteResponse;
import edu.clubhouseapi.dto.BlockFromChannelRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class ClubHouseChannelApiService {

    @Autowired
    @Qualifier("clubHouseWebClientBuilder")
    WebClient.Builder clubHouseWebClientBuilder;

    @Autowired
    ClubHouseConfigProperties clubHouseConfigProperties;

    @Autowired
    ClubHouseStaticFilesService clubHouseStaticFilesService;

    public Mono<ChannelsResponse> getChannels() {
        String url = clubHouseConfigProperties.getApiUrl() + "/get_channels";
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(ChannelsResponse.class)
                .map(channelsResponse -> {
                    if (channelsResponse.getChannels() == null) {
                        return channelsResponse;
                    }
                    for (final ChannelsResponse.ChannelResponse channel : channelsResponse.getChannels()) {
                        if (channel.getUsers() == null) {
                            continue;
                        }
                        for (final ChannelsResponse.ChannelUsersResponse user : channel.getUsers()) {
                            user.setPhotoUrl(clubHouseStaticFilesService.encodeUrl(user.getPhotoUrl()));
                        }
                    }
                    return channelsResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(ChannelsResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<PingResponse> activePing(PingRequest pingRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/active_ping";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(pingRequest)
                .retrieve()
                .bodyToMono(PingResponse.class)
                .switchIfEmpty(Mono
                        .defer(() -> Mono.just(PingResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<JoinChannelResponse> joinChannel(JoinChannelRequest joinChannelRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/join_channel";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(joinChannelRequest)
                .retrieve()
                .bodyToMono(JoinChannelResponse.class)
                .map(joinChannelResponse -> {
                    joinChannelResponse.setPubnubPubKey(clubHouseConfigProperties.getPubnubPubKey());
                    joinChannelResponse.setPubnubSubKey(clubHouseConfigProperties.getPubnubSubKey());
                    joinChannelResponse.setAgoraAppId(clubHouseConfigProperties.getAgoraKey());
                    joinChannelResponse.setLocalWsAgoraUrl(clubHouseConfigProperties.getLocalWsAgoraUrl());
                    if (joinChannelResponse.getUsers() == null) {
                        return joinChannelResponse;
                    }
                    for (final JoinChannelResponse.JoinChannelResponseUser user : joinChannelResponse.getUsers()) {
                        user.setPhotoUrl(clubHouseStaticFilesService.encodeUrl(user.getPhotoUrl()));
                        user.setRaiseHands(false);

                    }
                    return joinChannelResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(JoinChannelResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<GetChannelResponse> getChannel(GetChannelRequest getChannelRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/get_channel";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(getChannelRequest)
                .retrieve()
                .bodyToMono(GetChannelResponse.class)
                .map(getChannelResponse -> {
                    if (getChannelResponse.getUsers() == null) {
                        return getChannelResponse;
                    }
                    for (final GetChannelResponse.GetChannelResponseUser user : getChannelResponse.getUsers()) {
                        user.setPhotoUrl(clubHouseStaticFilesService.encodeUrl(user.getPhotoUrl()));
                        user.setRaiseHands(false);
                    }

                    return getChannelResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(GetChannelResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<AcceptSpeakerInviteResponse>
            acceptSpeakerInvite(AcceptSpeakerInviteRequest acceptSpeakerInviteRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/accept_speaker_invite";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(acceptSpeakerInviteRequest)
                .retrieve()
                .bodyToMono(AcceptSpeakerInviteResponse.class)
                .switchIfEmpty(Mono.defer(() -> Mono
                        .just(AcceptSpeakerInviteResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<RejectSpeakerInviteResponse>
            rejectSpeakerInvite(RejectSpeakerInviteRequest rejectSpeakerInviteRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/reject_speaker_invite";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(rejectSpeakerInviteRequest)
                .retrieve()
                .bodyToMono(RejectSpeakerInviteResponse.class)
                .switchIfEmpty(Mono.defer(() -> Mono
                        .just(RejectSpeakerInviteResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> leaveChannel(LeaveChannelRequest leaveChannelRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/leave_channel";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(leaveChannelRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono
                        .defer(() -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));

    }

    public Mono<EmptyResponse> raiseHands(RaiseHandsRequest raiseHandsRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/audience_reply";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(raiseHandsRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono
                        .defer(() -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));

    }

    public Mono<GetEventsResponse> getEvents(Integer pageSize, Integer page) {
        pageSize = pageSize != null ? pageSize : 50;
        page = page != null ? page : 1;
        String url = clubHouseConfigProperties.getApiUrl()
                + String.format("/get_events?is_filtered=true&page_size=%s&page=%s", pageSize, page);
        return clubHouseWebClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(GetEventsResponse.class)
                .map(getEventsResponse -> {
                    if (!getEventsResponse.getSuccess() || getEventsResponse.getEvents() == null) {
                        return getEventsResponse;
                    }
                    for (final GetEventsResponse.GetEventEventResponse event : getEventsResponse.getEvents()) {
                        event.getHosts()
                                .forEach(host -> host
                                        .setPhotoUrl(clubHouseStaticFilesService.encodeUrl(host.getPhotoUrl())));
                        if (event.getClub() != null) {
                            event.getClub()
                                    .setPhotoUrl(clubHouseStaticFilesService.encodeUrl(event.getClub().getPhotoUrl()));
                        }
                    }

                    return getEventsResponse;
                })
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(GetEventsResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<InviteSpeakerResponse> inviteSpeaker(InviteSpeakerRequest inviteSpeakerRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/invite_speaker";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(inviteSpeakerRequest)
                .retrieve()
                .bodyToMono(InviteSpeakerResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(InviteSpeakerResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<InviteSpeakerResponse> unInviteSpeaker(InviteSpeakerRequest uninviteSpeakerRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/uninvite_speaker";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(uninviteSpeakerRequest)
                .retrieve()
                .bodyToMono(InviteSpeakerResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(InviteSpeakerResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> makeModerator(MakeModeratorRequest makeModeratorRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/make_moderator";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(makeModeratorRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));
    }

    public Mono<EmptyResponse> blockFromChannel(BlockFromChannelRequest removeFromChannelRequest) {
        String url = clubHouseConfigProperties.getApiUrl() + "/block_from_channel";
        return clubHouseWebClientBuilder.build()
                .post()
                .uri(url)
                .bodyValue(removeFromChannelRequest)
                .retrieve()
                .bodyToMono(EmptyResponse.class)
                .switchIfEmpty(Mono.defer(
                        () -> Mono.just(EmptyResponse.builder().success(false).errorMessage("Empty").build())));
    }
}
