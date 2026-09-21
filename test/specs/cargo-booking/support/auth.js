export async function authenticateAsTicketStaff(page) {
  await authenticateAsRole(page, 'TICKET_STAFF', 'ticket.qa');
}

export async function authenticateAsRole(page, role, username = 'qa.user') {
  await page.addInitScript(({ selectedRole, selectedUsername }) => {
    localStorage.setItem('accessToken', JSON.stringify('e2e-access-token'));
    localStorage.setItem('refreshToken', JSON.stringify('e2e-refresh-token'));
    localStorage.setItem('user', JSON.stringify({
      username: selectedUsername,
      roles: [selectedRole],
    }));
  }, { selectedRole: role, selectedUsername: username });
}
