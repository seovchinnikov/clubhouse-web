package edu.clubhouseapi.controller;

import edu.clubhouseapi.dto.AcceptSpeakerInviteRequest;
import edu.clubhouseapi.dto.AcceptSpeakerInviteResponse;
import edu.clubhouseapi.dto.BlockFromChannelRequest;
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
import edu.clubhouseapi.service.ClubHouseChannelApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
public class ChannelsController {

    @Autowired
    ClubHouseChannelApiService clubHouseChannelApiService;

    @GetMapping(value = "/api/get_channels")
    public Mono<ChannelsResponse> getChannels() {
        return clubHouseChannelApiService.getChannels();
    }

    @PostMapping(value = "/api/active_ping")
    public Mono<PingResponse> activePing(@RequestBody @Valid Mono<PingRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.activePing(x));
    }

    @PostMapping(value = "/api/join_channel")
    public Mono<JoinChannelResponse> joinChannel(@RequestBody @Valid Mono<JoinChannelRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.joinChannel(x));
    }

    @PostMapping(value = "/api/get_channel")
    public Mono<GetChannelResponse> getChannel(@RequestBody @Valid Mono<GetChannelRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.getChannel(x));
    }

    @PostMapping(value = "/api/accept_speaker_invite")
    public Mono<AcceptSpeakerInviteResponse>
            acceptSpeakerInvite(@RequestBody @Valid Mono<AcceptSpeakerInviteRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.acceptSpeakerInvite(x));
    }

    @PostMapping(value = "/api/reject_speaker_invite")
    public Mono<RejectSpeakerInviteResponse>
            rejectSpeakerInvite(@RequestBody @Valid Mono<RejectSpeakerInviteRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.rejectSpeakerInvite(x));
    }

    @PostMapping(value = "/api/leave_channel")
    public Mono<EmptyResponse> leaveChannel(@RequestBody @Valid Mono<LeaveChannelRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.leaveChannel(x));
    }

    @PostMapping(value = "/api/audience_reply")
    public Mono<EmptyResponse> raiseHands(@RequestBody @Valid Mono<RaiseHandsRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.raiseHands(x));
    }

    @GetMapping(value = "/api/get_events")
    public Mono<GetEventsResponse> getEvents(@RequestParam("page_size") @NotNull Integer pageSize,
            @RequestParam("page") @NotNull Integer page) {
        return clubHouseChannelApiService.getEvents(pageSize, page);
    }

    @PostMapping(value = "/api/invite_speaker")
    public Mono<InviteSpeakerResponse> inviteSpeaker(@RequestBody @Valid Mono<InviteSpeakerRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.inviteSpeaker(x));
    }

    @PostMapping(value = "/api/uninvite_speaker")
    public Mono<InviteSpeakerResponse> uninviteSpeaker(@RequestBody @Valid Mono<InviteSpeakerRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.unInviteSpeaker(x));
    }

    @PostMapping(value = "/api/make_moderator")
    public Mono<EmptyResponse> makeModerator(@RequestBody @Valid Mono<MakeModeratorRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.makeModerator(x));
    }

    @PostMapping(value = "/api/block_from_channel")
    public Mono<EmptyResponse> blockFromChannel(@RequestBody @Valid Mono<BlockFromChannelRequest> mono) {
        return mono.flatMap(x -> clubHouseChannelApiService.blockFromChannel(x));
    }

}
