package com.example.marketplace.helpers;

import com.nimbusds.jose.JWSAlgorithm;

import java.util.Arrays;
import java.util.List;

import static com.nimbusds.jose.JWSAlgorithm.HS384;
import static com.nimbusds.jose.JWSAlgorithm.HS512;

public class Constants {

        public static final JWSAlgorithm ACCESS_TOKEN_SIGNATURE_ALGORITHM = HS512;

        public static final JWSAlgorithm REFRESH_TOKEN_SIGNATURE_ALGORITHM = HS384;

        public static final String KAFKA_TOPIC_SEND_MAIL = "SEND_MAIL";

        public static final String KAFKA_TOPIC_HANDLE_FILE = "HANDLE_FILE";

        public static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

        public static final List<String> ALLOWED_MEDIA_TYPES = Arrays.asList(
                        "image/jpeg",
                        "image/png",
                        "image/gif",
                        "image/jpg",
                        "video/mp4");

        public static final String TEMP_DIR = "temp/";

        public static final long MAX_CHUNK_SIZE = 1024 * 1024 * 10; // 10MB

        public static final int MAX_FILE_SIZE = 1024 * 1024 * 1000; // 1GB

        public static String DEFAULT_AVATAR_URL;

        public static final String PRODUCT_FAKE_IMAGES_FOLDER = "product";

        public static final String USER_FAKE_AVATARS_FOLDER = "user-avatar";
}
