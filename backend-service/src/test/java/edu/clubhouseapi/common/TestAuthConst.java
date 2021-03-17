package edu.clubhouseapi.common;

import org.mockserver.model.Header;

import java.util.ArrayList;
import java.util.List;

public class TestAuthConst {

    public static final String COOKIE = "d7949e3801f62c89d21196ed4d076990958db24bfa6";

    public static final String USER_ID = "999";

    public static final String TOKEN_PREFIX = "Token ";

    public static final String TOKEN = "token";

    public static final String PHONE_NUMBER = "+89606666666";

    public static final String VERIFICATION_CODE = "1234";

    public static List<Header> getBasicHeaders() {
        List<Header> headers = new ArrayList<>();
        headers.add(new Header("CH-Languages", "en-JP,ja-JP"));
        headers.add(new Header("CH-Locale", "en_JP"));
        headers.add(new Header("Accept", "application/json"));
        headers.add(new Header("Accept-Language", "en-JP;q=1, ja-JP;q=0.9"));
        headers.add(new Header("Accept-Encoding", "gzip, deflate"));
        headers.add(new Header("CH-AppBuild", "304"));
        headers.add(new Header("CH-AppVersion", "0.1.28"));
        headers.add(new Header("User-Agent", "clubhouse/304 (iPhone; iOS 14.4; Scale/2.00)"));
        headers.add(new Header("Connection", "close"));
        headers.add(new Header("Content-Type", "application/json; charset=utf-8"));

        return headers;
    }
}
