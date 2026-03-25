CREATE TABLE IF NOT EXISTS `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `chat_type` int NOT NULL COMMENT '会话类型，见ChatType',
  `chat_id` varchar(128) NOT NULL COMMENT '会话ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，可空（V1兼容）',
  `current_summary` text COMMENT '当前摘要',
  `summary_version` int DEFAULT 0 COMMENT '摘要版本号',
  `raw_message_count` int DEFAULT 0 COMMENT '累计原始消息条数',
  `last_compacted_seq` int DEFAULT 0 COMMENT '上次压缩进度',
  `create_time` datetime DEFAULT NULL,
  `edit_time` datetime DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_chat_session_type_chat` (`chat_type`,`chat_id`),
  KEY `idx_ai_chat_session_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话状态表';

CREATE TABLE IF NOT EXISTS `ai_chat_summary_chunk` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `chat_type` int NOT NULL COMMENT '会话类型，见ChatType',
  `chat_id` varchar(128) NOT NULL COMMENT '会话ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID，可空（V1兼容）',
  `chunk_seq` int NOT NULL COMMENT '分段序号，从1递增',
  `from_seq` int NOT NULL COMMENT '本段覆盖起始消息序号（含）',
  `to_seq` int NOT NULL COMMENT '本段覆盖结束消息序号（含）',
  `chunk_summary` text COMMENT '该分段摘要',
  `create_time` datetime DEFAULT NULL,
  `edit_time` datetime DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_summary_chunk_seq` (`chat_type`,`chat_id`,`chunk_seq`),
  KEY `idx_ai_summary_chunk_user` (`user_id`),
  KEY `idx_ai_summary_chunk_range` (`chat_type`,`chat_id`,`from_seq`,`to_seq`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI会话分段摘要表';

CREATE TABLE IF NOT EXISTS `ai_user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `attr_key` varchar(64) NOT NULL COMMENT '画像键',
  `attr_value` varchar(512) DEFAULT NULL COMMENT '画像值',
  `confidence` decimal(5,4) DEFAULT 0.7000 COMMENT '置信度',
  `source` varchar(64) DEFAULT NULL COMMENT '来源',
  `create_time` datetime DEFAULT NULL,
  `edit_time` datetime DEFAULT NULL,
  `status` tinyint(1) DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ai_user_profile_user_key` (`user_id`,`attr_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI用户画像表（低风险骨架）';
