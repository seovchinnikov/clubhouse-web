1. Install wine+winetricks in 32bits mode (+vcrun2017 in winetricks): agora-bridge/electron/prebuilds/ubuntu_install_wine.sh
2. Run agora-rtc-ws-connector.exe in 32-bits mode: agora-bridge/electron/prebuilds/linux_run_wine.sh:
```WINEPREFIX=~/_prefix32_wine WINEARCH=win32 WINEDLLOVERRIDES=libglesv2.dll=d  wine ubuntu/agora-rtc-ws-connector.exe```