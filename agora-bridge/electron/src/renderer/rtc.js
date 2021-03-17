import AgoraRtcEngine from 'agora-electron-sdk';
import WebSocket from 'ws';

export function createRtcEngineAndWsServer() {
    const {rtcEngine, handlers} = createRtcEngine();
    rtcEngine.rtcEngineInited = false;
    const wsServer = initWsServer(rtcEngine, handlers);
    return {wsServer: wsServer, rtcEngine: rtcEngine, handlers: handlers}
}

function initWsServer(rtcEngine, handlers) {
    const wsServer = new WebSocket.Server({port: 8765});
    wsServer.on('connection', function connection(ws) {
        const wsAgoraConnection = new WsAgoraConnection(ws, handlers, rtcEngine);
        handlers.add(wsAgoraConnection);
        wsAgoraConnection.handleConnection();
    });

    wsServer.on('close', server => {
        for (let handler of handlers) {
            handler.close();
        }
    });

    return wsServer;

}

function initRtcEngine(appId, rtcEngine, handlers) {
    let res = rtcEngine.initialize(appId);
    if (!res) {
        res = 0;
    }
    console.info(`rtcEngine.initialized ${res}`);

    rtcEngine.disableVideo();
    rtcEngine.enableLocalVideo(false);
    const resMuteLocalAudioStream = rtcEngine.muteLocalAudioStream(true);
    console.info(`muteLocalAudioStream ${resMuteLocalAudioStream}`);
    const resEnableAudioVolumeIndication = rtcEngine.enableAudioVolumeIndication(800, 3);
    rtcEngine.setAudioProfile(5, 3);
    console.info(`enableAudioVolumeIndication ${resEnableAudioVolumeIndication}`);

    rtcEngine.on('userMuteAudio', (uid, muted) => {
        console.info(`${uid} userMuteAudio`);
        for (let handler of handlers) {
            if (muted) {
                handler.onMutedRemote(uid);
            } else {
                handler.onUnmutedRemote(uid);
            }

        }
    });

    rtcEngine.on('activeSpeaker', (uid) => {
        console.info(`${uid} activeSpeaker`);
        for (let handler of handlers) {
            handler.onActiveSpeaker(uid);
        }
    });

    rtcEngine.on('activeSpeaker', (uid) => {
        console.info(`${uid} activeSpeaker`);
        for (let handler of handlers) {
            handler.onActiveSpeaker(uid);
        }
    });

    rtcEngine.on('groupAudioVolumeIndication', (speakers, speakersNum, vol) => {
        console.info(`${speakers} groupAudioVolumeIndication`);
        for (let speaker of speakers) {
            for (let handler of handlers) {
                handler.onVolume(speaker.uid, speaker.volume);
            }
        }
    });

    rtcEngine.on('error', (err, msg) => {
        console.info(`${err} error`);
        for (let handler of handlers) {
            handler.onError(err, msg);
        }
    });

    return res;
}

function createRtcEngine() {
    const rtcEngine = new AgoraRtcEngine();
    const handlers = new Set([]);

    return {rtcEngine: rtcEngine, handlers: handlers}
}

class WsAgoraConnection {

    constructor(ws, handlers, rtcEngine) {
        this.ws = ws;
        this.uid = -1;
        this.prev_speaker = -1;
        this.joined = false;
        this.handlers = handlers;
        this.rtcEngine = rtcEngine;
    }

    join(request) {
        this.uid = Number(request.user_id);
        let res = 0;
        if (!this.rtcEngine.rtcEngineInited) {
            res += initRtcEngine(request.app_id, this.rtcEngine, this.handlers);
            this.rtcEngine.rtcEngineInited = true;
        }

        res += Math.abs(this.rtcEngine.joinChannel(request.token, request.channel_name, "", Number(request.user_id)));
        this.joined = true;
        if (res > 0) {
            console.error(`Error on connectiong to agora: ${res}`);
        }

        if (request.speaker) {
            res += Math.abs(this.rtcEngine.enableLocalAudio(true));
            res += Math.abs(this.rtcEngine.muteLocalAudioStream(true));
        }

        request.ok = res;
        console.info('send: ' + JSON.stringify(request));
        this.ws.send(JSON.stringify(request));
    }

    leave(request) {
        console.info("leave request");
        let res = 0;
        try {
            res = Math.abs(this.rtcEngine.leaveChannel());
            this.joined = false;
        } catch (e) {
            console.error("Error during leave", e);
        }

        try {
            res += Math.abs(this.rtcEngine.enableLocalAudio(false));
        } catch (e) {
            console.error("Error during enableLocalAudio", e);
        }

        request.ok = res;
        this.ws.send(JSON.stringify(request));
    }

    mute() {
        if (!this.joined) {
            return;
        }
        console.info("mute request");
        this.rtcEngine.muteLocalAudioStream(true);
        this.ws.send(JSON.stringify({'action': 'on_muted', 'ok': 0, 'uid': this.uid}));
    }

    unmute() {
        if (!this.joined) {
            return;
        }
        console.info("unmute request");
        this.rtcEngine.muteLocalAudioStream(false);
        this.ws.send(JSON.stringify({'action': 'on_unmuted', 'ok': 0, 'uid': this.uid}));
    }

    handleConnection() {
        const that = this;
        this.ws.on('message', function incoming(message) {
            console.log('received: %s', message);
            try {
                const data = JSON.parse(message);
                if (data.action === 'join') {
                    that.join(data);
                } else if (data.action === 'leave') {
                    that.leave(data);
                } else if (data.action === 'mute') {
                    that.mute();
                } else if (data.action === 'unmute') {
                    that.unmute();
                }
            } catch (e) {
                console.error("During onMessage handling", e);
            }
        });

        this.ws.on('close', function close(err) {
            console.info("ws connection closed", err);
            that.cleanUp(true);
        });


    }

    onMutedRemote(uid) {
        if (!this.ws) {
            return;
        }

        if (uid === 0) {
            uid = this.uid;
        }
        this.ws.send(JSON.stringify({'action': 'on_muted', 'ok': 0, 'uid': uid}))
    }

    onUnmutedRemote(uid) {
        if (!this.ws) {
            return;
        }

        if (uid === 0) {
            uid = this.uid;
        }
        this.ws.send(JSON.stringify({'action': 'on_unmuted', 'ok': 0, 'uid': uid}))
    }

    onVolume(uid, vol) {
        if (!this.ws) {
            return;
        }
        if (uid === 0) {
            uid = this.uid;
        }
        this.ws.send(JSON.stringify({'action': 'on_speaking', 'ok': 0, 'uid': uid, 'speaker': vol > 30 ? 1 : 0}));
    }

    onActiveSpeaker(uid) {
        if (!this.ws) {
            return;
        }

        if (uid === 0) {
            uid = this.uid;
        }
        if (uid !== this.prev_speaker) {
            this.ws.send(
                JSON.stringify({'action': 'on_speaking', 'ok': 0, 'uid': this.prev_speaker, 'speaker': 0}));
        }

        this.prev_speaker = uid;
        this.ws.send(JSON.stringify({'action': 'on_speaking', 'ok': 0, 'uid': uid, 'speaker': 1}));
    }

    onError(err, msg) {
        if (!this.ws) {
            return;
        }

        this.ws.send(JSON.stringify({'action': 'on_leave', 'ok': 0, 'uid': this.uid}));
        this.cleanUp(false);
    }

    close() {
        this.cleanUp(false);
    }

    cleanUp(wsClosed) {
        console.info(`cleanup wsClosed:${wsClosed}`);
        if (this.handlers.has(this)) {
            this.handlers.delete(this);
        }
        try {
            if (this.joined) {
                this.rtcEngine.leaveChannel();
            }
        } catch (e) {
            console.error("error during leaveChannel", e);
        }
        if (!wsClosed && this.ws) {
            try {
                this.ws.close();
            } catch (e) {
                console.error("error during ws.close", e);
            }
        }
        this.ws = null;
    }

}
