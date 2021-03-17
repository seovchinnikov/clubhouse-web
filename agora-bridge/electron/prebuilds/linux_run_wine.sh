#!/bin/bash

if [ ! -d ubuntu ]; then
    unzip windows-agora-rtc-ws-connector-1.0.0.zip -d ubuntu
fi

WINEPREFIX=~/_prefix32_wine WINEARCH=win32 WINEDLLOVERRIDES=libglesv2.dll=d  wine ubuntu/agora-rtc-ws-connector.exe