
ALTER TABLE rule_element DROP FOREIGN KEY FK_rule_element_RuleLogicRowId;

ALTER TABLE rule_element ADD CONSTRAINT FK_rule_element_RuleLogicRowId FOREIGN KEY (RuleLogicRowId) REFERENCES rule_logic (Row_Id) ON DELETE CASCADE;

ALTER TABLE message_attachment DROP FOREIGN KEY FK_message_attachment_MessageInboxRowId;

ALTER TABLE message_attachment ADD CONSTRAINT FK_message_attachment_MessageInboxRowId FOREIGN KEY (MessageInboxRowId) REFERENCES message_inbox (Row_Id) ON DELETE CASCADE;

ALTER TABLE message_header DROP FOREIGN KEY FK_message_header_MessageInboxRowId;

ALTER TABLE message_header ADD CONSTRAINT FK_message_header_MessageInboxRowId FOREIGN KEY (MessageInboxRowId) REFERENCES message_inbox (Row_Id) ON DELETE CASCADE;

ALTER TABLE message_stream DROP FOREIGN KEY FK_message_stream_MessageInboxRowId;

ALTER TABLE message_stream ADD CONSTRAINT FK_message_stream_MessageInboxRowId FOREIGN KEY (MessageInboxRowId) REFERENCES message_inbox (Row_Id) ON DELETE CASCADE;

-- SQL statements for hibernate
ALTER TABLE message_unsub_comment DROP FOREIGN KEY FK_MsgUnsubCmt_MessageInboxRowId; --hibernate
ALTER TABLE message_unsub_comment ADD CONSTRAINT FK_MsgUnsubCmt_MessageInboxRowId FOREIGN KEY (MessageInboxRowId) REFERENCES message_inbox (Row_Id) ON DELETE CASCADE; --hibernate

-- SQL statements for eclipselink
ALTER TABLE message_unsub_comment DROP FOREIGN KEY FK_message_unsub_comment_MessageInboxRowId; --eclipselink
ALTER TABLE message_unsub_comment ADD CONSTRAINT FK_message_unsub_comment_MessageInboxRowId FOREIGN KEY (MessageInboxRowId) REFERENCES message_inbox (Row_Id) ON DELETE CASCADE; --eclipselink
