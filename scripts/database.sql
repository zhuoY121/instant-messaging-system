
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for im_user_data
-- ----------------------------
DROP TABLE IF EXISTS `im_user_data`;
CREATE TABLE `im_user_data`  (
     `app_id` int(11) NOT NULL,
     `user_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
     `nick_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'nickname',
     `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
     `photo` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
     `user_sex` int(10) NULL DEFAULT NULL,
     `birth_day` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'birthday',
     `location` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'location',
     `self_signature` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Signature',
     `friend_allow_type` int(10) NOT NULL DEFAULT 1 COMMENT 'Friend verification type (Friend_AllowType) 1=No verification required; 2=Verification required',
     `forbidden_flag` int(10) NOT NULL DEFAULT 0 COMMENT 'Disabled logo, 1=disabled',
     `disable_add_friend` int(10) NOT NULL DEFAULT 0 COMMENT 'Administrator prohibits users from adding friends: 0=not disabled; 1=disabled',
     `silent_flag` int(10) NOT NULL DEFAULT 0 COMMENT 'Ban mark, 1=ban',
     `user_type` int(10) NOT NULL DEFAULT 1 COMMENT 'User type, 1=Ordinary user; 2=Customer service; 3=Robot',
     `del_flag` int(20) NOT NULL DEFAULT 0,
     `extra` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
     PRIMARY KEY (`app_id`, `user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for app_user
-- ----------------------------
DROP TABLE IF EXISTS `app_user`;
CREATE TABLE `app_user`  (
    `user_id` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `user_name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `password` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `mobile` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `create_time` bigint(20) NULL DEFAULT NULL,
    `update_time` bigint(20) NULL DEFAULT NULL,
    PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_friendship
-- ----------------------------
DROP TABLE IF EXISTS `im_friendship`;
CREATE TABLE `im_friendship`  (
    `app_id` int(20) NOT NULL COMMENT 'app_id',
    `from_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'from_id',
    `to_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'to_id',
    `remark` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'remark',
    `status` int(10) NULL DEFAULT NULL COMMENT 'Status: 1=Normal; 2=Delete',
    `black` int(10) NULL DEFAULT NULL COMMENT '1=Normal; 2=Blocked',
    `create_time` bigint(20) NULL DEFAULT NULL,
    `friend_sequence` bigint(20) NULL DEFAULT NULL,
    `black_sequence` bigint(20) NULL DEFAULT NULL,
    `add_source` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'source',
    `extra` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'extra info',
    PRIMARY KEY (`app_id`, `from_id`, `to_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


SET FOREIGN_KEY_CHECKS = 1;
