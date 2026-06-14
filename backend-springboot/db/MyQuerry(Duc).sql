USE vexedb
SELECT a.username, r.roleName FROM account a JOIN account_role ar ON a.accountId = ar.accountId JOIN role r on ar.roleId = r.roleId
