
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

-- ----------------------------
-- Table structure for im_friendship_request
-- ----------------------------
DROP TABLE IF EXISTS `im_friendship_request`;
CREATE TABLE `im_friendship_request`  (
    `id` int(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
    `app_id` int(20) NULL DEFAULT NULL COMMENT 'app_id',
    `from_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'from_id',
    `to_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'to_id',
    `remark` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL  ,
    `read_status` int(10) NULL DEFAULT NULL COMMENT '1=The friend request has been read',
    `add_source` varchar(20) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'friend source',
    `add_message` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Friend verification information',
    `approve_status` int(10) NULL DEFAULT NULL COMMENT 'Approval status: 1=Agree 2=Reject',
    `create_time` bigint(20) NULL DEFAULT NULL,
    `update_time` bigint(20) NULL DEFAULT NULL,
    `sequence` bigint(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for im_friendship_group
-- ----------------------------
DROP TABLE IF EXISTS `im_friendship_group`;
CREATE TABLE `im_friendship_group`  (
    `app_id` int(20) NULL DEFAULT NULL COMMENT 'app_id',
    `from_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'from_id',
    `group_id` int(50) NOT NULL AUTO_INCREMENT,
    `group_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `sequence` bigint(20) NULL DEFAULT NULL,
    `create_time` bigint(20) NULL DEFAULT NULL,
    `update_time` bigint(20) NULL DEFAULT NULL,
    `del_flag` int(10) NULL DEFAULT NULL COMMENT '0=normal；1=delete',
    PRIMARY KEY (`group_id`) USING BTREE,
    UNIQUE INDEX `UNIQUE`(`app_id`, `from_id`, `group_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_friendship_group_member
-- ----------------------------
DROP TABLE IF EXISTS `im_friendship_group_member`;
CREATE TABLE `im_friendship_group_member`  (
    `group_id` bigint(20) NOT NULL,
    `to_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    PRIMARY KEY (`group_id`,`to_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for im_group
-- ----------------------------
DROP TABLE IF EXISTS `im_group`;
CREATE TABLE `im_group`  (
    `app_id` int(20) NOT NULL COMMENT 'app_id',
    `group_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'group_id',
    `owner_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'group owner',
    `group_type` int(10) NULL DEFAULT NULL COMMENT 'Group type: 1=Private group (WeChat); 2=Public group (QQ)',
    `group_name` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `mute` int(10) NULL DEFAULT NULL COMMENT 'Whether to mute all members. 0=unmuted; 1=muted.',
    `apply_join_type` int(10) NULL DEFAULT NULL COMMENT 'The options for applying to join the group include the following:\r\n// 0=no one is allowed to apply to join the group chat\r\n// 1=the group owner or administrator is required to approve\r\n// 2=Everyone can join the group without approval.',
    `photo` varchar(300) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `max_member_count` int(20) NULL DEFAULT NULL,
    `introduction` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Group introduction',
    `notification` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Group notice',
    `status` int(5) NULL DEFAULT NULL COMMENT 'Group status: 0=normal; 1=disbanded',
    `sequence` bigint(20) NULL DEFAULT NULL,
    `create_time` bigint(20) NULL DEFAULT NULL,
    `extra` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'extra info',
    `update_time` bigint(20) NULL DEFAULT NULL,
    PRIMARY KEY (`app_id`, `group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for im_group_member
-- ----------------------------
DROP TABLE IF EXISTS `im_group_member`;
CREATE TABLE `im_group_member`  (
    `group_member_id` bigint(20) NOT NULL AUTO_INCREMENT,
    `group_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'group_id',
    `app_id` int(10) NULL DEFAULT NULL,
    `member_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'member id',
    `role` int(10) NULL DEFAULT NULL COMMENT 'Group member type: 0=ordinary members; 1=administrator; 2=group owner; 3=banned members; 4=removed members',
    `speak_date` bigint(100) NULL DEFAULT NULL,
    `mute` int(10) NULL DEFAULT NULL COMMENT 'Whether to mute all members: 0=unmuted; 1=muted',
    `alias` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Group nickname',
    `join_time` bigint(20) NULL DEFAULT NULL,
    `leave_time` bigint(20) NULL DEFAULT NULL,
    `join_type` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `extra` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    PRIMARY KEY (`group_member_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for im_message_body
-- ----------------------------
DROP TABLE IF EXISTS `im_message_body`;
CREATE TABLE `im_message_body`  (
    `app_id` int(10) NOT NULL,
    `message_key` bigint(50) NOT NULL COMMENT 'Message body id',
    `message_body` varchar(5000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `security_key` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Used to encrypt the message body',
    `message_time` bigint(20) NULL DEFAULT NULL COMMENT 'The time the client sent the message',
    `create_time` bigint(20) NULL DEFAULT NULL COMMENT 'The time the server inserted the message data',
    `extra` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
    `del_flag` int(10) NULL DEFAULT NULL,
    PRIMARY KEY (`message_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;


-- ----------------------------
-- Table structure for im_message_history
-- ----------------------------
DROP TABLE IF EXISTS `im_message_history`;
CREATE TABLE `im_message_history`  (
    `app_id` int(20) NOT NULL,
    `from_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `to_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `owner_id` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
    `message_key` bigint(50) NOT NULL COMMENT 'Message body id',
    `message_time` bigint(20) NULL DEFAULT NULL COMMENT 'The time the client sent the message',
    `create_time` bigint(20) NULL DEFAULT NULL COMMENT 'The time the server inserted the message data',
    `sequence` bigint(20) NULL DEFAULT NULL,
    `message_random` int(20) NULL DEFAULT NULL,
    PRIMARY KEY (`app_id`, `owner_id`, `message_key`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;



SET FOREIGN_KEY_CHECKS = 1;
