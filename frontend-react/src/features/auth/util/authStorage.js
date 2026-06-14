const KEYS = {
  USER: 'user',
  TOKEN: 'accessToken',
  REFRESH_TOKEN: 'refreshToken',
}

function store(key, value, remember) {
  const serialized = JSON.stringify(value)
  if (remember) {
    localStorage.setItem(key, serialized)
    sessionStorage.removeItem(key)
  } else {
    sessionStorage.setItem(key, serialized)
    localStorage.removeItem(key)
  }
}

function retrieve(key) {
  const raw = localStorage.getItem(key) || sessionStorage.getItem(key)
  try { return raw ? JSON.parse(raw) : null } catch { return null }
}

function remove(key) {
  localStorage.removeItem(key)
  sessionStorage.removeItem(key)
}

export const authStorage = {
  getUser: () => retrieve(KEYS.USER),
  setUser: (user, remember) => store(KEYS.USER, user, remember),
  removeUser: () => remove(KEYS.USER),

  getToken: () => localStorage.getItem(KEYS.TOKEN) || sessionStorage.getItem(KEYS.TOKEN),
  setToken: (token, remember) => store(KEYS.TOKEN, token, remember),
  removeToken: () => remove(KEYS.TOKEN),

  getRefreshToken: () => retrieve(KEYS.REFRESH_TOKEN),
  setRefreshToken: (token, remember) => store(KEYS.REFRESH_TOKEN, token, remember),

  clearAll: () => { remove(KEYS.USER); remove(KEYS.TOKEN); remove(KEYS.REFRESH_TOKEN); },
}