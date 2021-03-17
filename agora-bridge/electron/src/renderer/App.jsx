import React, {Component} from 'react';
import {createRtcEngineAndWsServer} from './rtc';

export default class App extends Component {
    componentDidMount() {
        global.rtcEngineAndWsBinding = createRtcEngineAndWsServer();
    }

    componentWillUnmount() {
        if (global.rtcEngineAndWsBinding) {
            const {wsServer, rtcEngine, handlers} = global.rtcEngineAndWsBinding;
            wsServer.close(err => console.error(err));
            rtcEngine.release();
            global.rtcEngineAndWsBinding = null;
        }
    }

    render() {


        return (
            <div>
                Audio connector
            </div>
        )
    }
}
