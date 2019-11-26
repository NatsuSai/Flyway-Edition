--
-- Copyright 2010-2018 Boxfuse GmbH
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE TABLE `undo_log` (
`id`  bigint(20) NOT NULL AUTO_INCREMENT ,
`branch_id`  bigint(20) NOT NULL ,
`xid`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL ,
`context`  varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
`rollback_info`  longblob NOT NULL ,
`log_status`  int(11) NOT NULL ,
`log_created`  datetime NOT NULL ,
`log_modified`  datetime NOT NULL ,
`ext`  varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL ,
PRIMARY KEY (`id`),
UNIQUE INDEX `ux_undo_log` (`xid`, `branch_id`) USING BTREE
)
ENGINE=InnoDB
DEFAULT CHARACTER SET=utf8 COLLATE=utf8_general_ci
AUTO_INCREMENT=60
ROW_FORMAT=DYNAMIC
;

