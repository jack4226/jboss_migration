-- ALTER TABLE email.rule_element DROP CONSTRAINT rllmentRlLgicRowId;

-- ALTER TABLE email.rule_element ADD CONSTRAINT rllmentRlLgicRowId FOREIGN KEY (RuleLogicRowId) REFERENCES email.rule_logic (Row_Id) ON DELETE CASCADE;

-- ALTER TABLE email.message_attachment DROP CONSTRAINT mssgttchMssgnbxRwd;

-- ALTER TABLE email.message_attachment ADD CONSTRAINT mssgttchMssgnbxRwd FOREIGN KEY (MessageInboxRowId) REFERENCES email.message_inbox (Row_Id) ON DELETE CASCADE;

-- ALTER TABLE email.message_header DROP CONSTRAINT mssghdrMssgnbxRwId;

-- ALTER TABLE email.message_header ADD CONSTRAINT mssghdrMssgnbxRwId FOREIGN KEY (MessageInboxRowId) REFERENCES email.message_inbox (Row_Id) ON DELETE CASCADE;

ALTER TABLE email.message_stream DROP CONSTRAINT mssgstrmMssgnbxRwd;

ALTER TABLE email.message_stream ADD CONSTRAINT mssgstrmMssgnbxRwd FOREIGN KEY (MessageInboxRowId) REFERENCES email.message_inbox (Row_Id) ON DELETE CASCADE;

ALTER TABLE email.message_unsub_comment DROP CONSTRAINT mssgnsbcMssgnbxRwd;

ALTER TABLE email.message_unsub_comment ADD CONSTRAINT mssgnsbcMssgnbxRwd FOREIGN KEY (MessageInboxRowId) REFERENCES email.message_inbox (Row_Id) ON DELETE CASCADE;